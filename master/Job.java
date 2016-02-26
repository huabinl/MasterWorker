import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Job {
	
	protected int id;
	protected String jarPath;
	protected String inputPath;
	protected String outputFolderPath;
	protected long limitTime;
	protected int status;
	protected boolean arrived;
    
	public Job(String jarPath, String inputPath, String outputFolderPath, int id, int limitTime) throws IOException {
		this.id = id;
		this.jarPath = jarPath;
		this.inputPath = inputPath;
		this.outputFolderPath = outputFolderPath;
		this.limitTime = limitTime;
		arrived = false;
	}
	
	public int getId() {
		return id;
	}

	public String getJarPath() {
		return jarPath;
	}
	
	public String getInputPath() {
		return inputPath;
	}
	
	public String getOutputFolderPath() {
		return outputFolderPath;
	}
	
	public long getLimitTime() {
		return limitTime;
	}

	public synchronized void setArrived() {
		arrived = true;
	}

	public synchronized boolean isArrived() {
		return arrived;
	}
	
	public synchronized void setStatus(int status) {
		this.status = status;
	}
	
	public synchronized String getStatus() {
		switch(status) {
		case 1:
			return "running";
		case 2:
			return "finished";
		case 3:
			return "failed";
		case 4:
			return "disconnected";
		default:
			return "transferring";
		}
	}
	
	public String getName() {
		return "Job" + id;
	}
}
