import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MasterFrame extends JFrame {
	int jobId;
	int workerId;
	String ip;
	String port;
	String jarPath;
	String inputPath;
	String outputPath;
	String time;
	ArrayList<Worker> workerList;
	ArrayList<Job> jobList;
	JTextField ipField = new JTextField(ip,20);
	JTextField portField = new JTextField(port,20);
	JTextField jarField = new JTextField(jarPath,20);
	JTextField inputField = new JTextField(inputPath,20);
	JTextField outputField = new JTextField(outputPath,20);
	JTextField timeField = new JTextField(time,20);
	
	JButton addWorkerButton = new JButton("AddWorker");
	JButton jarButton = new JButton("Browse");
	JButton inputButton = new JButton("Browse");
	JButton outputButton = new JButton("Browse");
	JButton addJobButton = new JButton("AddJob");
	
	Object[] workerColNames = {"IP","Port","Status"};
	Object[] jobColNames = {"Jar","Input","Status","Output"};
	
	static DefaultTableModel workerModel = new DefaultTableModel();
	static DefaultTableModel jobModel = new DefaultTableModel();
	
	JTable workerTable = null;
	JTable jobTable = null;
	
	JPanel panel = new JPanel();
	JPanel workerPanel = new JPanel();
	JPanel jobPanel = new JPanel();
	
	
	public MasterFrame(int workerId, 
			ArrayList<Worker> workerList, ArrayList<Job> jobList) {
		this.workerId = workerId;
		this.workerList = workerList;
		this.jobList = jobList;
		
		initView();
		
		setSize(1000,560);
		setResizable(false);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("exit");
				System.exit(0);
			}
		});
		
		setVisible(true);
		
		for(int i = 0; i < workerList.size(); i++) {
			Worker worker = workerList.get(i);
			Object[] row = {worker.getHost(),worker.getPort(),worker.getStatus()};
			workerModel.addRow(row);
		}
	}
	
	public void initView() {
		
		workerPanel.setLayout(new GridBagLayout());
		workerModel.setColumnIdentifiers(workerColNames);
		workerTable = new JTable(workerModel);
		workerTable.setEnabled(false);
		workerTable.setPreferredScrollableViewportSize(new Dimension(350,200));
		JScrollPane workerPane = new JScrollPane(workerTable);
		
		addWorkerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ip = ipField.getText();
				port = portField.getText();
				if(!ip.equals("") && !port.equals("")) {
					Object[] row = {ip,port,"connecting"};
					workerModel.addRow(row);
					int port0 = Integer.parseInt(port);
					workerId++;
					Worker worker = new Worker(workerId, ip, port0);
					workerList.add(worker);
					ipField.setText("");
					portField.setText("");
				}
			}
		});
		
		addItem(workerPanel,new JLabel("Add New Worker"),0,0,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,new JLabel("IP Address:"),0,1,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,ipField,1,1,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,new JLabel("Port Number:"),0,2,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,portField,1,2,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,addWorkerButton,1,3,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,new JLabel("Worker List"),0,4,1,1,GridBagConstraints.WEST);
		addItem(workerPanel,workerPane,0,5,4,4,GridBagConstraints.WEST);
		
		jobPanel.setLayout(new GridBagLayout());
		jobModel.setColumnIdentifiers(jobColNames);
		jobTable = new JTable(jobModel);
		jobTable.setEnabled(false);
		jobTable.setPreferredScrollableViewportSize(new Dimension(550,200));
		JScrollPane jobPane = new JScrollPane(jobTable);
		
		jarField.setEditable(false);
		inputField.setEditable(false);
		outputField.setEditable(false);
		
		jarButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				try {
					JFileChooser jarChooser = new JFileChooser();
					jarChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jarChooser.setMultiSelectionEnabled(false);
					jarChooser.showOpenDialog(jarButton);
					jarPath = jarChooser.getSelectedFile().getAbsolutePath();
					jarField.setText(jarPath);
				} catch (Exception e1) {
					
				}
			}
		});
		
		inputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser inputChooser = new JFileChooser();
					inputChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					inputChooser.setMultiSelectionEnabled(false);
					inputChooser.showOpenDialog(inputButton);
					inputPath = inputChooser.getSelectedFile().getAbsolutePath();
					inputField.setText(inputPath);
				} catch (Exception e1) {
					
				}
			}
		});
		
		outputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser outputChooser = new JFileChooser();
					outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					outputChooser.showSaveDialog(outputButton);
					outputPath = outputChooser.getSelectedFile().getAbsolutePath();
					outputField.setText(outputPath);
				} catch (Exception e1) {
					
				}
			}
		});
		
		addJobButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jarPath = jarField.getText();
				inputPath = inputField.getText();
				outputPath = outputField.getText();
				if(!jarPath.equals("") && !inputPath.equals("") && !outputPath.equals("")){
					time = timeField.getText();
					Object[] row = {jarPath,inputPath,"transferring",outputPath};
					jobModel.addRow(row);
					int time0;
					if (time.equals("")) {
						time0 = 0;
					} else {
						time0 = Integer.parseInt(time);
					}
					try {
						jobId++;
						Job job = new Job(jarPath, inputPath, outputPath, jobId, time0);
						jobList.add(job);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					jarField.setText("");
					inputField.setText("");
					outputField.setText("");
					timeField.setText("");
				}
			}	
		});
		
		addItem(jobPanel,new JLabel("Add New Job"),0,0,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,new JLabel("Jar File:"),0,1,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,jarField,1,1,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,jarButton,2,1,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,new JLabel("Input File:"),0,2,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,inputField,1,2,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,inputButton,2,2,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,new JLabel("Output Path:"),0,3,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,outputField,1,3,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,outputButton,2,3,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,new JLabel("Time Limit (second):"),0,4,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,timeField,1,4,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,addJobButton,2,5,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,new JLabel("Job List"),0,6,1,1,GridBagConstraints.WEST);
		addItem(jobPanel,jobPane,0,7,6,4,GridBagConstraints.WEST);
		
		panel.setLayout(new GridBagLayout());
		addItem(panel,workerPanel,0,0,6,5,GridBagConstraints.NORTHWEST);
		addItem(panel,jobPanel,1,0,6,5,GridBagConstraints.NORTHEAST);
		
		getContentPane().add(panel);
	}
	
	private void addItem(JPanel panel,JComponent component,int x,int y,int width,int height,int align) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.weightx = 100.0;
		constraints.weighty = 100.0;
		constraints.insets = new Insets(10,10,5,5);
		constraints.anchor = align;
		
		panel.add(component, constraints);
	}
}

