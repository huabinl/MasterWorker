import java.util.ArrayList;

public class Worker {

	protected int id;
	protected ArrayList<Job> jobList;
	protected int status;
	protected int processNum;
	protected int index;
	protected String host;
	protected int port;
	
	public Worker(int id, String host, int port) {
		index = 0;
		processNum = -1;
		this.id = id;
		jobList = new ArrayList<Job>();
		this.host = host;
		this.port = port;
	}
	
	public int getId() {
		return id;
	}
	
	public synchronized void putJob(Job job) {
	    jobList.add(job);
	}
	
	public synchronized boolean existNewJob() {
		if (jobList.size() > index) {
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized Job getNewJob() {
		Job job = jobList.get(index);
		index++;
		return job;
	}
	
	public synchronized Job getJob(int index) {
		return jobList.get(index);
	}
	
	public synchronized int getListLength() {
		return jobList.size();
	}
	
	public synchronized void setStatus(int status) {
		this.status = status;
	}
	
	public synchronized String getStatus() {
		switch(status) {
		case 0: 
			return "running";
		case 1:
			return "down";
		default:
			return "connecting";
		}
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setProcessNum(int processNum) {
		this.processNum = processNum;
	}
	
	public int getProcessNum() {
		return processNum;
	}	
}
