
public class ProcessNum {

	private int processNum;
	
	public ProcessNum() {
	}
	
	public synchronized void addProcess() {
		processNum++;
	}
	
	public synchronized void removeProcess() {
		processNum--;
	}
	
	public synchronized int countProcess() {
		return processNum;
	}
}
