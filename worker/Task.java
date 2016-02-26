import java.util.*;


public class Task {

	ArrayList<EndJob> taskList = new ArrayList<EndJob>();
	int index;
	
	public Task() {
	}
	
	public synchronized void putJob(EndJob job) {
		taskList.add(job);
	}
	
	public synchronized boolean existNewStatus() {
		return (taskList.size() > index);
	}
    
	public synchronized EndJob peekNewJob() {
		EndJob job = taskList.get(index);
		index++;
		return job;
	}
	
	public synchronized boolean existNewJob() {
		return (taskList.size() > 0);
	}
	
	public synchronized EndJob getNewJob() {
		EndJob job = taskList.get(0);
		taskList.remove(0);
		index--;
		return job;
	}
}
