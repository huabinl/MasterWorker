import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

// This class of thread executes a job by creating a local subprocess.
public class ProcessThread implements Runnable {
    
    private EndJob job;
    private Task task;
    private String jobPath;
    private String jarPath;
    private String inputPath;
    
    private String inputFormat;
    private long timeLimit;
    
    public ProcessThread (EndJob job, Task task,long timeLimit) 
    		throws IOException, InterruptedException {
        this.job = job;
        this.task = task;
        this.timeLimit = timeLimit;
        jobPath = job.getPath();
        jarPath = job.getJarPath();
        inputPath = job.getInputPath();
        inputFormat = inputPath.substring(inputPath.lastIndexOf("."), inputPath.length());
    }
    
    public void run() {
        
        // Create two new files to store output and errorInfo.
        File jarFile = new File(jarPath);
        File inputFile = new File(inputPath);
        String jarName = jarFile.getName().substring(0, jarFile.getName().lastIndexOf("."));
        String inputName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
        String commonPath = jobPath + File.separator + jarName + "_" + inputName; 
        
        String stdOutputPath = commonPath + "_stdout.txt";
        String stdErrPath = commonPath + "_stderr.txt";
        String outputPath = commonPath + "_output" + inputFormat;
        File stdOut = new File(stdOutputPath);
        File stdErr = new File(stdErrPath);
        File output = new File(outputPath);
        try {
			stdOut.createNewFile();
            stdErr.createNewFile();
            output.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // execute the jar command.
        String javaExePath = Paths.get(System.getProperty("java.home"),
        		"bin", "java").toAbsolutePath().toString();
        System.out.println("start to run the jar file.");
        
        ProcessBuilder workerProcessBuilder = new ProcessBuilder(javaExePath, 
        		"-jar", jarPath, inputPath, output.getAbsolutePath());
        
        // redirect them to the file created before.
        workerProcessBuilder.redirectInput();
        workerProcessBuilder.redirectErrorStream(false);
        workerProcessBuilder.redirectError(stdErr);
        workerProcessBuilder.redirectOutput(stdOut);
        
        Process workerProcess;
        try {
			workerProcess = workerProcessBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

        System.out.println("The status of process is: Running");
        
        //set timeLimit to specific value if user specify them.
        if (timeLimit > 0) {
            try {
            	if (workerProcess.waitFor(timeLimit, TimeUnit.SECONDS)) {  
    			    if (workerProcess.exitValue() == 0) {
    			        success(stdOut, stdErr, outputPath);
    			    } else {
    			        failed(output, stdOutputPath, stdErrPath);
    			    }
    			} else {
    			    workerProcess.destroy();
    			    FileOutputStream fos = new FileOutputStream(stdErr);
    			    PrintStream ps = new PrintStream(fos);
    			    PrintStream original = System.out;
    			    System.setOut(ps);
                    System.setOut(original);
    			    System.out.println("Error: Job stops executing because it has exceeded the "
    			    		+ "limited time. Which is " + timeLimit + " seconds.");

    		        failed(output, stdOutputPath, stdErrPath);
    			}
		    } catch (FileNotFoundException e) {
			    e.printStackTrace();
		    } catch (InterruptedException e) {
			    e.printStackTrace();
		    }   
        } else {  //Run until it finishes if user doesn't specify the limitTime.
        	try {
				workerProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
        	if (workerProcess.exitValue() == 0) {
			    success(stdOut, stdErr, outputPath);
			} else {
			    failed(output, stdOutputPath, stdErrPath);
	        }	
        }  
    }

    // When the process is successful, delete related standard files (output file
    // and error file), and put the output file into a queue to send back
    private void success(File stdOut, File stdErr, String outputPath) {
        System.out.println("The status of process is: Finished");
        if (stdOut.exists()) {
            stdOut.delete();
        }
        if (stdErr.exists()) {
            stdErr.delete();
        }
        job.setStatus(1);
        job.setOutputPath(outputPath);
        task.putJob(job);
    }

    // When the process is failed, delete the output file, and put related startdard
    // files together into the sending queue 
    private void failed(File output, String stdOutputPath, String stdErrPath) {
        System.out.println("The status of process is: Failed");
        if (output.exists()) {
            output.delete();
        }
        job.setStatus(2);
        job.setStdOutputPath(stdOutputPath);
        job.setStdErrPath(stdErrPath);
        task.putJob(job);
    }   
}
