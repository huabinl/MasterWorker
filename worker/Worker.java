import java.net.*;  
import java.io.*; 

import javax.net.ssl.*;

class Worker {  
	
    public static void main(String[] args) throws Exception {  
        
    	int masterId = 0;
    	System.out.println("Worker starting......"); 
    	ProcessNum process = new ProcessNum();
    	System.setProperty("javax.net.ssl.keyStore", "." + File.separator + "master.cer");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		
		SSLServerSocketFactory sslServerSocketFactory=(SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket sslServerSocket=(SSLServerSocket)sslServerSocketFactory.createServerSocket(8000);
	    
		String mainPath = workerFolder();
        while (true) {  

        	SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();
        	masterId++;
        	
            Socket sock = sslSocket;           
            new Thread(new MasterThread(process, mainPath, sock, masterId)).start();  
        } 
    } 
    
    private static String workerFolder() {
    	String mainFolderName = "Worker";
    	File file = new File(mainFolderName);           
    	if (file.isFile()) {                    
		    file.delete();                       
		} else if (file.isDirectory()) {             
			deleteDir(file);
		} 
		file.mkdir();
		String mainPath = file.getAbsolutePath();
		return mainPath;
	} 
    
    private static void deleteDir(File dir) {
	    if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                deleteDir(new File(dir, children[i]));
            }
        }
	    dir.delete();
    }
}  