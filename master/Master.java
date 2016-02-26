import java.util.*;
import java.io.*;  
import java.net.*;

import javax.net.ssl.*;

class Master {
	
	static int frameFlag = 0;
    
	public static void main(String[] args) throws Exception { 
		
		int jobId = 0;
		int workerId = 0;
		
		System.setProperty("javax.net.ssl.trustStore", "." + File.separator + "master.cer");
		System.setProperty("javax.net.ssl.trustStorePassword", "123456");
		SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		
		MasterFrame frame = null;
		
    	ArrayList<Worker> workerList = new ArrayList<Worker>();
    	ArrayList<Job> jobList = new ArrayList<Job>();
    	String hostPort;
    	ArrayList<String> ipPort = new ArrayList<String>();
    	BufferedReader ipAndPort = new BufferedReader
    			(new FileReader("." + File.separator + "IPandPort.txt"));
    	while ((hostPort = ipAndPort.readLine()) != null) {
 		   ipPort.add(hostPort);
 	    }
    	ipAndPort.close();
    	
    	for (String ip : ipPort) {
			String[] mix = new String[2];
			String m = ip;
			mix = m.split(" ");
			String host = mix[0];
			int port = Integer.parseInt(mix[1]);
			workerId++;
			Worker worker = new Worker(workerId, host, port);
	
			workerList.add(worker);
			try {
				SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket(host,port);
				Thread t = new Thread(new WorkerThread(jobList, worker, sslSocket));
				t.start();
			} catch (Exception e) {
				worker.setStatus(1);
                MasterFrame.workerModel.setValueAt("down", worker.getId() - 1, 2);
			}
    	}
    	
    	frame = new MasterFrame(workerId, workerList, jobList);
    	frameFlag = 1;
    	
    	while (true) {
    		while (jobList.size() > jobId) {
    			Job job = jobList.get(jobId);
    			jobId++;
    			System.out.println("For Job" + jobId + ": ");
    			int index = getIndex(workerList);
    			if (index >= 0 ) {
    				workerList.get(index).putJob(job);
    			}
    		}
    		while (workerList.size() > workerId) {
    			Worker worker = workerList.get(workerId);
    			workerId++;
    			SSLSocket sslSocket;
				try {
					sslSocket = (SSLSocket)sslSocketFactory.createSocket(worker.getHost(),worker.getPort());
					Thread t = new Thread(new WorkerThread(jobList, worker, sslSocket));
	    			t.start();
				} catch (Exception e) {
					worker.setStatus(1);
					MasterFrame.workerModel.setValueAt("down", worker.getId() - 1, 2);
				}
    		}
    		Thread.sleep(1000);
    	}
	}
	
	private static int getIndex(ArrayList<Worker> workerList) {
        int i = -1;
        int size = workerList.size();
        int min = Integer.MAX_VALUE;
    	for (int j = 0; j < size ; j++) {
    		if (workerList.get(j).getStatus().equals("running")) {
    			int num = workerList.get(j).getProcessNum();
    			System.out.println("Worker" + (j + 1) + ": " + num);
				if (num >= 0 && min > num) {
					min = num;
				    i = j;
				}	
    		}
    	}
    	if (i >= 0) {
    		System.out.println("Choose Worker" + (i + 1));
    	} else {
    		System.out.println("No Worker Available");
    	}
    	return i;
    }
}