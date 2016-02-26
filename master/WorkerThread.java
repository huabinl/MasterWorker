import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class WorkerThread implements Runnable {

	protected int flag = 0;
    protected Socket sock;
    protected Worker worker;
    protected ArrayList<Job> jobList;
    
    public WorkerThread(ArrayList<Job> jobList, Worker worker, Socket sock) {
    	this.jobList = jobList;
    	this.sock = sock;
    	this.worker = worker;
    }	
    
    public void run() {
        try {
        	OutputStream sockOut = sock.getOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(sockOut);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            
			InputStream sockin = sock.getInputStream();   
	        InputStreamReader inputstreamreader=new InputStreamReader(sockin);
		    BufferedReader bufferedReader=new BufferedReader(inputstreamreader);
		    
		    while (true) { 
		    	// receive worker's process number (and status) before sending new 
        		// job(s) to worker 
	            while (true) {
					if (worker.existNewJob()) {
						JSONObject obj = new JSONObject(); 
						obj.put("command","have job");
	                    bufferedWriter.write(obj.toJSONString() + "\n");
					    bufferedWriter.flush();
					    
					    getStatus(bufferedReader);
					    getProcessNumber(bufferedReader);
						sendJob(bufferedWriter, bufferedReader);
					} else {
						JSONObject obj = new JSONObject(); 
						obj.put("command","no job");
						bufferedWriter.write(obj.toJSONString() + "\n");
			            bufferedWriter.flush();
			            getProcessNumber(bufferedReader);
			            break;	
				    }
			    }
				// receive worker's process number (and status) before receiving new 
        		// job(s) to master	
	        	while (true) { 
			        String checkOutput = bufferedReader.readLine();
			        JSONObject obj = new JSONObject(); 
			        obj = (JSONObject)JSONValue.parse(checkOutput);
			   
			        if (obj.get("command").equals("have output")) { 
						getStatus(bufferedReader);
						getProcessNumber(bufferedReader);
						receiveJob(bufferedReader, bufferedWriter);	  
					} else {
						getProcessNumber(bufferedReader);
						break;
					}
				}

	        	Thread.sleep(1000);        	   
	        }
	            
        }catch (InterruptedException e) {
		    e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();		
        }
        finally {
        	//set worker to status "down"
        	worker.setStatus(1);
        	if(Master.frameFlag == 1){
        		MasterFrame.workerModel.setValueAt("down", worker.getId() - 1, 2);
        	}
        	//set unfinished job to status "disconnected"
        	int length = worker.getListLength();  
        	for (int i = 0; i < length; i++) {
        		if (!worker.getJob(i).isArrived()) {
        			worker.getJob(i).setStatus(4); 
        			int jobId = worker.getJob(i).getId();
        			MasterFrame.jobModel.setValueAt("disconnected", jobId - 1, 2);
        		}
        	}
        }
    }

	private void getProcessNumber(BufferedReader bufferedReader) throws IOException {
		JSONObject obj = new JSONObject(); 
		obj = (JSONObject) JSONValue.parse(bufferedReader.readLine());
		long proNum = (Long) obj.get("processnum");
		int processNumber = (int) proNum;
		if (flag == 0) {
			worker.setStatus(0);
			if (Master.frameFlag == 1) {
				MasterFrame.workerModel.setValueAt("running", worker.getId() - 1, 2);
			}
			
			flag++;
		}
		worker.setProcessNum(processNumber);
	}

	private void sendJob(BufferedWriter bufferedWriter,
			BufferedReader bufferedReader) throws IOException {
		Job job = worker.getNewJob();
		
		JSONObject obj = new JSONObject(); 
		obj.put("jobname", job.getName());
		obj.put("time", String.valueOf(job.getLimitTime()));
		bufferedWriter.write(obj.toJSONString() + "\n");
	    bufferedWriter.flush();
	    
	    try {
	    	File jarFile = new File(job.getJarPath());
			sendFile(jarFile, bufferedReader, bufferedWriter);
			File inputFile = new File(job.getInputPath());
			sendFile(inputFile, bufferedReader, bufferedWriter);
		} catch (Exception e) {
				e.printStackTrace();
		}   
		    
		String status = bufferedReader.readLine();
		JSONObject obj1 = new JSONObject(); 
		obj1 = (JSONObject)JSONValue.parse(status);
		if (obj1.get("status").equals("start")){
			job.setStatus(1);
			MasterFrame.jobModel.setValueAt("running", job.getId()-1, 2);
		}
	}
	
	private void sendFile(File file, BufferedReader bufferedReader,
    		BufferedWriter bufferedWriter) throws Exception {   
		BufferedReader bufferedReader = null;  
		
        String fileName = file.getName();   
        int size = 0;
		FileInputStream fileInputStream = new FileInputStream(file.getAbsoluteFile());
		OutputStream sockOut = sock.getOutputStream();
	    size = fileInputStream.available();
        JSONObject obj1 = new JSONObject();
        obj1.put("filename", fileName);       
        bufferedWriter.write(obj1.toJSONString() + "\n");  
        bufferedWriter.flush();
        
        JSONObject objSize = new JSONObject();
        objSize.put("size", String.valueOf(size));
        bufferedWriter.write(objSize.toJSONString() + "\n");  
        bufferedWriter.flush();
        
        JSONObject obj2 = new JSONObject();
        String serverInfo = bufferedReader.readLine();  
        obj2 = (JSONObject) JSONValue.parse(serverInfo);
        String info = (String) obj2.get("command");
        if (info.equals("ready")) {     
            byte[] buffer = new byte[1024];   
            int len = 0;
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            while (true) {
            	len = fileInputStream.read(buffer);
            	if (len != -1) {
            		byte[] buffer2 = new byte[len];
            		for(int i = 0; i < len; i++) {
            			buffer2[i] = buffer[i];
            		}
            		String sendOut = Base64.getEncoder().encodeToString(buffer2);
            		JSONObject dataJason = new JSONObject();
            		dataJason.put("filedata", sendOut);       
                    out.writeUTF(dataJason.toJSONString());  
                    out.flush();
            	} else {
            		break;
            	}
            } 
            bufferedReader.readLine();  
        }
    }  
    
    private void receiveJob(BufferedReader bufferedReader,
    		BufferedWriter bufferedWriter) throws IOException {
    	JSONObject obj = new JSONObject();
    	String jobNameAndFileNum = bufferedReader.readLine();
    	obj = (JSONObject) JSONValue.parse(jobNameAndFileNum);
        String jobId0 = (String) obj.get("jobId");
        int jobId = Integer.parseInt(jobId0);
        String fileNum = (String) obj.get("filenum");
        int num = Integer.parseInt(fileNum);
        
        Job job = jobList.get(jobId - 1);
        while(num > 0) {
        	receiveFile(job, bufferedReader, bufferedWriter);
        	num--;
        }
        job.setArrived();	
    }

    private void receiveFile(Job job, BufferedReader bufferedReader, 
			BufferedWriter bufferedWriter) throws IOException {
		JSONObject obj = new JSONObject();
		String fileName0 = bufferedReader.readLine(); 
		obj = (JSONObject) JSONValue.parse(fileName0);
		String fileName = (String) obj.get("fileName");
		String size0 = (String) obj.get("size");
		int size = Integer.parseInt(size0);
		String outputPath = job.getOutputFolderPath() + File.separator + fileName;
		File outputFile = new File(outputPath); 
		if (outputFile.exists()) {
			outputFile.delete();
		}
		if (outputFile.createNewFile()) {   
			JSONObject obj1 = new JSONObject();
			obj1.put("command", "ready");
		    bufferedWriter.write(obj1.toJSONString() + "\n");
		    bufferedWriter.flush();  
		}  
	
		FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
	    int sum = 0;
	    DataInputStream in = new DataInputStream(sock.getInputStream());
	    if(size != 0) {
	    	while (true) {
			    String dataJason = in.readUTF();
			    JSONObject obj2 = new JSONObject();
				obj2 = (JSONObject) JSONValue.parse(dataJason);
				String fileData = (String) obj2.get("filedata");
				byte[] asBytes = Base64.getDecoder().decode(fileData);
			    sum = sum + asBytes.length;
			    fileOutputStream.write(asBytes);
			    if(sum==size)
			    	break;
			}
	    }
	    fileOutputStream.close();
	    JSONObject obj1 = new JSONObject();
		obj1.put("command", "finish");
	    bufferedWriter.write(obj1.toJSONString() + "\n");
	    bufferedWriter.flush();    
	}
    
    private void getStatus(BufferedReader bufferedReader) throws IOException {
    	while (true) {
    		JSONObject obj = new JSONObject();
    		String updateStatus0 = bufferedReader.readLine();
    		obj = (JSONObject) JSONValue.parse(updateStatus0);
    		String updateStatus = (String) obj.get("command");
    		if (updateStatus.equals("has status")) {
    			JSONObject obj2 = new JSONObject();
    			String jobNameAndStatus = bufferedReader.readLine();
    			obj = (JSONObject) JSONValue.parse(jobNameAndStatus);
    			String jobId0 = (String) obj.get("jobId");
    			int jobId = Integer.parseInt(jobId0);
    			String status = (String) obj.get("status");
    			Job job = jobList.get(jobId - 1);
    			if (status.equals("finished")) {
    				job.setStatus(2);
    				MasterFrame.jobModel.setValueAt("finished", jobId - 1, 2);
    			} else {
    				job.setStatus(3);
    				MasterFrame.jobModel.setValueAt("failed", jobId - 1, 2);
    			}
    		} else {
    			break;
    		}
    	}
    }
}	

