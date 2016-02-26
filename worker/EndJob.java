
public class EndJob {

	private String folderPath;
	private int status;
	private String name;
	private String jarPath;
	private String inputPath;
	private String outputPath;
	private String stdOutputPath;
	private String stdErrPath;
	
	public EndJob(String name, String folderPath, String jarPath, String inputPath) {
		this.name = name;
		this.folderPath = folderPath;
		this.jarPath = jarPath;
		this.inputPath = inputPath;
	}
	
	public String getPath() {
		return folderPath;
	}
	
	public String getId() {
		return name.substring(3);
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getStatus() {
		switch(status) {
		case 1:
			return "finished";
		case 2:
			return "failed";
		default:
			return "running";
		}
	}
	
	public String getJarPath() {
		return jarPath;
	}
	
	public String getInputPath() {
		return inputPath;
	}
	
	public void setOutputPath(String outputPath) {
		 this.outputPath = outputPath;
	}
	
	public String getOutputPath() {
		return outputPath;
	}
	
	public void setStdOutputPath(String stdOutputPath) {
		 this.stdOutputPath = stdOutputPath;
	}
	
	public String getStdOutputPath() {
		return stdOutputPath;
	}
	
	public void setStdErrPath(String stdErrPath) {
		 this.stdErrPath = stdErrPath;
	}
	
	public String getStdErrPath() {
		return stdErrPath;
	}
}
