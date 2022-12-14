package org.cytoscape.CytoCopasiApp.tasks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.COPASI.*;
import org.cytoscape.CytoCopasiApp.AttributeUtil;
import org.cytoscape.CytoCopasiApp.CopasiSaveDialog;

import org.cytoscape.CytoCopasiApp.CyActivator;
import org.cytoscape.CytoCopasiApp.GetPlot;
import org.cytoscape.CytoCopasiApp.GetTable;
import org.cytoscape.CytoCopasiApp.Report.ParsingReportGenerator;
import org.cytoscape.CytoCopasiApp.actions.CopasiFileReaderTask;
import org.cytoscape.CytoCopasiApp.actions.CopasiReaderTaskFactory;
import org.cytoscape.CytoCopasiApp.actions.ImportAction;
import org.jfree.chart.*;



public class SteadyStateTask extends AbstractCyAction {
	CySwingApplication cySwingApplication;
	FileUtil fileUtil;
	private String Duration;
	private String Intervals;
	private String IntervalSize;
	private String StartTime;
	private String menuName;
	private double[] data;
	private double[] simval;
	private String option;
	private Object[] options;
	private Object[] possibilities;
	private Object[] simspec;
	private String possibility;
	private String s;
	private File outFile;
	private CopasiSaveDialog saveDialog;
	private SteadyStateTask.SteadyTask parentTask;
	private Boolean newton = false;
	private Boolean integration = false;
	private Boolean backIntegration = false;
	private String iterationLimit;
	CyNetwork currentNetwork;
	CyNetwork previousNetwork;
	String[] csvColumns;
	String statMessage;
	Object[][] csvData;
	Object[][] csvFlux;
	double[] specNo;
	double[] reacNo;
	double[] speciesAttr;
	double[] fluxesAttr;
	double[] totals ;
	long networkSUID;
	long newNetworkSUID;
	Object[][] dataMCA;
    Object[][] transpMCA;
	JScrollPane f6;
	 LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	 @SuppressWarnings("rawtypes")
	SynchronousTaskManager synchronousTaskManager;

	public SteadyStateTask(CySwingApplication cySwingApplication, FileUtil fileUtil, LoadNetworkFileTaskFactory loadNetworkFileTaskFactory, SynchronousTaskManager synchronousTaskManager) {
		super(SteadyStateTask.class.getSimpleName());
		this.cySwingApplication = cySwingApplication;
		this.fileUtil = fileUtil;
		this.loadNetworkFileTaskFactory= loadNetworkFileTaskFactory;
		this.synchronousTaskManager = synchronousTaskManager;
		
		 
	
	}


	public void actionPerformed(ActionEvent e) {
		JFrame frame = new JFrame("Steady State Methods");
		
		JCheckBox aCheck = new JCheckBox();
		JCheckBox bCheck = new JCheckBox();
		JCheckBox cCheck = new JCheckBox();
		JTextField field = new JTextField(5);
		
		
		
		JPanel myPanel = new JPanel();
		
		myPanel.add(new JLabel("Use Newton"));
		myPanel.add(aCheck);
		myPanel.add(Box.createHorizontalStrut(15)); 
		myPanel.add(new JLabel("Use Integration"));
		myPanel.add(bCheck);
		myPanel.add(new JLabel("Use Back Integration"));
		myPanel.add(cCheck);
		myPanel.add(new JLabel("Iteration Limit"));
		myPanel.add(field);
		
		
		Object [] options = {"OK", "Cancel"};
		
		int result = JOptionPane.showOptionDialog(null, myPanel, "Enter Steady State Specifics", JOptionPane.PLAIN_MESSAGE, 1, null, options, options[0]);
		if (result == JOptionPane.OK_OPTION) {
		newton = aCheck.isSelected();
		integration = bCheck.isSelected();
		backIntegration = cCheck.isSelected();
		iterationLimit = field.getText();
		simspec = setData();
		}
		
		final SteadyTask parentTask = new SteadyTask(simspec, outFile);
		CyActivator.taskManager.execute(new TaskIterator(parentTask));
		
	}

	
	
	public Object[] setData() {
		if (iterationLimit.isBlank()) {
			
			JOptionPane.showMessageDialog(null, String.format("What is the iteration limit?"));
			 
		}
		int iteration = Integer.parseInt(iterationLimit);
		Object[] data = {newton, integration, backIntegration, iteration};
		
		return data;
	}
	
	public File getOutFile() {
		
		return outFile;
		
	}
	
	String getMenuName() {
        return menuName;
    }
	
	CopasiSaveDialog getSaveDialog() {
		return saveDialog;
	}
	
	private File getSelectedFileFromSaveDialog() {
        
        saveDialog = new CopasiSaveDialog(".xlsx");
        
        
    int response = saveDialog.showSaveDialog(CyActivator.cytoscapeDesktopService.getJFrame());
    if (response == CopasiSaveDialog.CANCEL_OPTION)
        return null;
    
	  
    return saveDialog.getSelectedFile();
}
	public CyNetwork getCurrentNetwork() {
		return currentNetwork;
    	
    }
	
	private void writeOutFileDirectory() {
		
		if (outFile != null) {
            try {
            	
                PrintWriter recentDirWriter = new PrintWriter(saveDialog.getRecentDir());
                recentDirWriter.write(outFile.getParent());
                recentDirWriter.close();
            } catch (FileNotFoundException e1) {
                LoggerFactory.getLogger(SteadyStateTask.class).error(e1.getMessage());
            }
        }
    }
	public class SteadyTask extends AbstractTask {
		
	 TaskMonitor taskMonitor;
	 Object[] simspec;
	 File outFile;
	
	//public SteadyTask (Object[] data, File outFile) {

	
	
	//this.data = data;
	//this.outFile = outFile;
	//super.cancelled = false;
	
		
		public SteadyTask(Object[] simspec, File outFile) {
			this.simspec = simspec;
			this.outFile = outFile;
			super.cancelled = false;
		// TODO Auto-generated constructor stub
	}
		 
		 
		@SuppressWarnings("deprecation")
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		taskMonitor.setTitle("Steady State Analysis");
		taskMonitor.setStatusMessage("Simulation started");
		ParsingReportGenerator.getInstance().appendLine("newton: " + simspec[0].toString());
		ParsingReportGenerator.getInstance().appendLine("integration: " + simspec[1].toString());
		ParsingReportGenerator.getInstance().appendLine("back integration: " + simspec[2].toString());
//		ParsingReportGenerator.getInstance().appendLine("outFile: " + outFile.getAbsolutePath());
		taskMonitor.setProgress(0);
		String modelName = new Scanner(CyActivator.getReportFile(1)).useDelimiter("\\Z").next();
		CDataModel dm = CRootContainer.addDatamodel();
		String modelString = new Scanner(new File(modelName)).useDelimiter("\\Z").next();
		dm.loadFromString(modelString);
		CModel model = dm.getModel();	
		
			CSteadyStateTask task = (CSteadyStateTask)dm.getTask("Steady-State");
			task.setMethodType(CTaskEnum.Task_steadyState);
			task.getProblem().setModel(dm.getModel());
			task.setScheduled(true);
			CSteadyStateProblem prob = (CSteadyStateProblem)(task.getProblem());
			
			
			CSteadyStateMethod method = (CSteadyStateMethod)(task.getMethod());
			method.getParameter("Use Newton").setBoolValue((boolean) simspec[0]);
			method.getParameter("Use Integration").setBoolValue((boolean) simspec[1]);
			method.getParameter("Use Back Integration").setBoolValue((boolean) simspec[2]);
			method.getParameter("Iteration Limit").setIntValue((int) simspec[3]);
			method.getParameter("Resolution").setDblValue(1e-9);
			method.getParameter("Derivation Factor").setDblValue(0.001);
			task.processWithOutputFlags(true, (int)CCopasiTask.ONLY_TIME_SERIES);
			//CSteadyStateProblem prob = (CSteadyStateProblem)(task.getProblem());
			//prob.setJacobianRequested(false);
			prob.setModel(model);
		//	prob.setStabilityAnalysisRequested(true);
			FloatVectorCore state = task.getState();
			state.get(2);
			
			//CStateTemplate state = model.getStateTemplate();
			
			int stdStatus = task.getResult();
			ParsingReportGenerator.getInstance().appendLine("steady state: " + stdStatus);
			
			switch (stdStatus) {
			case CSteadyStateMethod.found: 
				taskMonitor.setStatusMessage("Steady State was found");
				break;
			
			case CSteadyStateMethod.notFound: 
				taskMonitor.setStatusMessage("Steady State was not found");
				statMessage = "Steady State was not found";
				//JOptionPane.showMessageDialog(null, String.format("Steady State was not found"));
				
				
				
				break;
			
			case CSteadyStateMethod.foundEquilibrium: 
				taskMonitor.setStatusMessage("Equilibrium");
				break;
			
			case CSteadyStateMethod.foundNegative: 
				taskMonitor.setStatusMessage("Could not find a steady state with non-negative concentrations");
				statMessage = "Could not find a steady state with non-negative concentrations";
			//	JOptionPane.showMessageDialog(null, String.format("Could not find a steady state with non-negative concentrations"));
				
			//CRootContainer.destroy();
			return;
			
			}
			
			if (stdStatus == CSteadyStateMethod.found) {
				
			
			long numspec = model.getNumMetabs();
			long numreac = model.getNumReactions();
			
			JFrame f;
			
			Object[][] dataConc = new Object[(int) numspec][4]; 
			Object[] currentNetworks = CyActivator.netMgr.getNetworkSet().toArray();
			networkSUID = CyActivator.listener.getSUID();
			System.out.println("suid:" + networkSUID);
			if (currentNetworks.length==1) {
				currentNetwork = CyActivator.netMgr.getNetwork(networkSUID);
			}
			
			if (currentNetworks.length>1) {
				newNetworkSUID = CyActivator.listener.getSUID();
				currentNetwork =  CyActivator.netMgr.getNetwork(newNetworkSUID);
				if (currentNetwork.getSUID()==((CyNetwork)currentNetworks[0]).getSUID()) {
					previousNetwork = (CyNetwork) currentNetworks[1];
				} else {
					previousNetwork = (CyNetwork) currentNetworks[0];
				}
			}
			CyNetworkView networkView = CyActivator.networkViewManager.getNetworkViews(currentNetwork).iterator().next();
			csvColumns = new String[4];
			csvColumns[0]="Name";
			csvColumns[1] = "Concentration";
			csvColumns[2] = "Rate";
			csvColumns[3] = "Transition Time";
			
			csvData = new Object[(int) numspec][4];
			specNo = new double[(int) numspec];
			speciesAttr = new double[(int) numspec];
			int nodenumber = currentNetwork.getNodeCount();
			java.util.List<CyNode> nodes = currentNetwork.getNodeList();
			for (int a = 0; a< numspec; a++) {
				specNo[a]=a+1;
				ParsingReportGenerator.getInstance().appendLine("std st conc is: " + model.getMetabolite(a).getObjectDisplayName() + model.getMetabolite(a).getConcentration());
				dataConc[a][0]= model.getMetabolite(a).getObjectDisplayName();
				csvData[a][0]= model.getMetabolite(a).getObjectDisplayName();
				dataConc[a][1] = model.getMetabolite(a).getConcentration();
				csvData[a][1] = model.getMetabolite(a).getConcentration();
				for (int i= 0; i<nodenumber; i++) {
					
					
					if (AttributeUtil.get(currentNetwork, nodes.get(i), "name", String.class).equals(dataConc[a][0])==true) {
						AttributeUtil.set(currentNetwork,  nodes.get(i), "std-st concentration", dataConc[a][1], Double.class);
						System.out.println("Conc:"+AttributeUtil.get(currentNetwork,  nodes.get(i), "std-st concentration", Double.class));
						speciesAttr[a]= AttributeUtil.get(currentNetwork,  nodes.get(i), "std-st concentration", Double.class);
						
					}
					
				}
				
				dataConc[a][2] = model.getMetabolite(a).getRate();
				csvData[a][2] = model.getMetabolite(a).getRate();
				dataConc[a][3] = model.getMetabolite(a).getTransitionTime();
				csvData[a][3] = model.getMetabolite(a).getTransitionTime();
				
			}
			CreateCSV writeToCsv = new CreateCSV();
			File csvFile = writeToCsv.writeDataAtOnce("Concentration",modelName, csvData, csvColumns, specNo);

			String[] column = new String[4];
			column[0] = "Species";
			column[1] = "Std St Concentration [mmol/l]";
			column[2] = "Rate [mmol/(min*l)";
			column[3] = "Transition Time [min]";
			
			GetTable getTb = new GetTable();
			JScrollPane f1 = getTb.getTable("Concentrations", dataConc, column);
			
			Object[][] dataFlux = new Object[(int) numreac][3];
			csvFlux = new Object[(int) numreac][3];
			reacNo = new double[(int) numreac];
			fluxesAttr = new double[(int) numreac];
			for (int b =0 ; b< numreac; b++) {
				reacNo[b] = b+1;
				ParsingReportGenerator.getInstance().appendLine("std st flux: " + model.getReaction(b).getObjectDisplayName()+model.getReaction(b).getFlux());
				ParsingReportGenerator.getInstance().appendLine("number of parameters: " + model.getReaction(b).getObjectDisplayName()+model.getReaction(b).getFunctionParameters().size());
				dataFlux[b][0] = model.getReaction(b).getObjectDisplayName();
				dataFlux[b][1] = model.getReaction(b).getFlux();
				dataFlux[b][2] = model.getReaction(b).getReactionScheme();
				
				for (int i= 0; i<nodenumber; i++) {
					CyNode node = currentNetwork.getNodeList().get(i);
					
					if (AttributeUtil.get(currentNetwork, node, "display name", String.class).equals(dataFlux[b][0])==true) {
						AttributeUtil.set(currentNetwork, node, "std-st flux", dataFlux[b][1], Double.class);
						fluxesAttr[b] = AttributeUtil.get(currentNetwork, node, "std-st flux", Double.class);

						CyActivator.netMgr.addNetwork(currentNetwork);
					}
					

				}
				
				
			}
			  CyEventHelper eventHelper = CyActivator.cyEventHelper;
              eventHelper.flushPayloadEvents();
			networkView.updateView();
			CyActivator.netMgr.addNetwork(currentNetwork);
			
			String[] column2 = new String[3];
			
			column2[0] = "Reaction";
			column2[1] = "Flux";
			column2[2] = "Formula";
			
			File csvFluxFile = writeToCsv.writeDataAtOnce("Flux", modelName, dataFlux, column2, reacNo);
			
			GetTable getTb2 = new GetTable();
			JScrollPane f2 = getTb2.getTable("Fluxes", dataFlux, column2);
			JButton changeButton = new JButton("Show Before&After");
			JPanel f3 = new JPanel();
			f3.add(changeButton);
			JPanel f4 = new JPanel();
			JPanel f5 = new JPanel();
			JButton mcaButton = new JButton("MCA");
	        JButton compareButton = new JButton("Compare");
	        f4.add(compareButton);
	        f5.add(mcaButton);
			final JFrame frame = new JFrame("Steady State Was Found!");
			 
	        // Display the window.
			
	        frame.setSize(600, 400);
	        frame.setVisible(true);
	 
	        // set grid layout for the frame
	        frame.getContentPane().setLayout(new GridLayout(1, 1));
	        
	        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	        tabbedPane.add("Concentration", f1);
	        tabbedPane.add("Flux", f2);
	        tabbedPane.add("Compare Growth",f3);
	        tabbedPane.add("Compare to a variation", f4);
	        tabbedPane.add("MCA", f5);
	        frame.getContentPane().add(tabbedPane);
	      
	        
	        compareButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					JButton concCompare = new JButton("Concentration");
					JButton fluxCompare = new JButton("Flux");
					JButton resetButton = new JButton("Reset");
					JButton mcaCompare = new JButton("MCA variation");
					
					f4.remove(compareButton);
					f4.add(concCompare);
					f4.add(fluxCompare);
					if (f6!=null ) {
						f4.add(mcaCompare);
					}
					f4.add(resetButton);
					
			    	ComparativeStdSt compareStdSt = new ComparativeStdSt(cySwingApplication, fileUtil, loadNetworkFileTaskFactory, synchronousTaskManager);
			    	
					concCompare.addActionListener(new ActionListener() {
						
						
						@Override
						public void actionPerformed(ActionEvent e) {
							
								CompareDifferentNetworks mergeNetworks = new CompareDifferentNetworks(cySwingApplication, fileUtil, loadNetworkFileTaskFactory, synchronousTaskManager);
								mergeNetworks.compareDifferentNetworks(currentNetwork, previousNetwork, "concentration");
						}
						
					});
					
					fluxCompare.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							CompareDifferentNetworks mergeNetworks = new CompareDifferentNetworks(cySwingApplication, fileUtil, loadNetworkFileTaskFactory, synchronousTaskManager);
							mergeNetworks.compareDifferentNetworks(currentNetwork, previousNetwork, "flux");
						}
						
					});
					
					mcaCompare.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							Collection<FileChooserFilter> filters = new HashSet<>();
					    	String[] extensions = {"", "csv"};
					    	filters.add(new FileChooserFilter("EXCEL files (*, *.csv)", extensions));

					    	File[] files = fileUtil.getFiles(cySwingApplication.getJFrame(), "Open Std St Report", FileDialog.LOAD, filters);
					    	compareStdSt.compare(files[0], numreac, dataFlux, currentNetwork, nodes,totals, networkView);
					    	
						}
						
					});
					
					resetButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							// TODO Auto-generated method stub
							CyTable nodeTable = currentNetwork.getDefaultNodeTable();
							nodeTable.deleteColumn("percentage change");
							nodeTable.deleteColumn("variation");

						}
						
					});
					
				}
	        	
	        });
	        
	        mcaButton.addActionListener(new ActionListener() {
	        	
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					String[] column3 = new String[(int) numreac];
					double[] mcaRow = new double[(int)numreac+1];
					for (int i=0;i<numreac; i++) {
						column3[i] = dm.getModel().getReaction(i).getObjectDisplayName();
						mcaRow[i] = i+1;
					}
					mcaRow[(int)numreac]= numreac+1;
					
					CMCATask pTask = (CMCATask) (dm.getTask("Metabolic Control Analysis"));
					CMCAProblem pMCAProblem = (CMCAProblem)(pTask.getProblem());
					pMCAProblem.setSteadyStateRequested(true);
					CMCAMethod pMCAMethod = (CMCAMethod)(pTask.getMethod());
					pMCAMethod.getParameter("Modulation Factor").setDblValue(1e-9);
					
					
					CDataArray pCCC = pMCAMethod.getScaledFluxCCAnn();
					   
				          // since this matrix is probably very large, we will only output the flux control coefficient for
				          // the last reaction in the model
					  pTask.processWithOutputFlags(true, (int)CCopasiTask.ONLY_TIME_SERIES);
				          long numReactions = dm.getModel().getReactions().size();
				          CReaction pReaction = dm.getModel().getReaction(numReactions-1);
				          SizeTStdVector index = new SizeTStdVector(2);
				          ReportItemVector annotations = pCCC.getAnnotationsCN(1);				          
				         dataMCA = new Object[(int) numreac+1][(int) numreac];
				           transpMCA = new Object[(int) numreac][(int) numreac+1];
				           totals = new double[(int)numreac];
				          
				          for (int i=0; i<numReactions; i++) {

				          index.set(0,i);
				          for (int j=0; j<numReactions; j++) {
				          index.set(1,j);
				          
				          dataMCA[i][j]=pCCC.getArray().get(index);
				          transpMCA[j][i]=dataMCA[i][j];
				         
				          }
				          
				          }
				          
				          for (int i=0; i<numReactions; i++) {
				        	  totals[i] = 0;
				        	  for (int j=0; j<numReactions; j++) {
				        		  if (i==j) {
				        		  totals[i] = Double.parseDouble(transpMCA[i][j].toString());
				        		  } 
				        	  }
				        	  dataMCA[(int) numreac][i]=totals[i];

				          }
				          
				          for (int b =0 ; b< numreac; b++) {
								
								
								for (int i= 0; i<nodenumber; i++) {
									CyNode node = currentNetwork.getNodeList().get(i);
									
									if (AttributeUtil.get(currentNetwork, node, "display name", String.class).equals(dataFlux[b][0])==true) {
										AttributeUtil.set(currentNetwork, node, "FCC total", totals[b], Double.class);
										

										CyActivator.netMgr.addNetwork(currentNetwork);
									}
									

								}
								
								
							}
							try {
								GetTable getTbMCA = new GetTable();
								 f6 = getTbMCA.getTable("MCA", dataMCA, column3);
								String column4[] = new String[(int) (numreac+1)];
								for (int i=0; i<numreac; i++){
									column4[i]=column3[i];
									
								}
								column4[(int) numreac]="total";
								JList mcaColumnList = new JList(column4);
								mcaColumnList.setFixedCellHeight(15);
								f6.setRowHeaderView(mcaColumnList);
								f6.validate();
								f6.repaint();
								tabbedPane.remove(f5);
								tabbedPane.add("MCA",f6);
								
								File csvMCAFile = writeToCsv.writeDataAtOnce("MCA", modelName, dataMCA, column3, mcaRow);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

				          }
	        	
	        });
	        
			} else if (stdStatus == CSteadyStateMethod.notFound){
				JLabel stdStWarn = new JLabel("Steady State Not found. Run time course simulation?");
				JTextField timeCourse = new JTextField(4);
				JFrame stdStWarnFrame = new JFrame();
				stdStWarnFrame.add(stdStWarn);
				stdStWarnFrame.add(timeCourse);
				//int stdStWarnDialog = JOptionPane.showConfirmDialog(stdStWarnFrame, stdStWarn, null, JOptionPane.DEFAULT_OPTION, 0);
			//	if (stdStWarnDialog==0) {
				taskMonitor.setTitle("Steady State Not Found");
				
				taskMonitor.setStatusMessage("Steady State Not found, so running a long time course simulation");
				CTrajectoryTask trajectoryTask = (CTrajectoryTask)dm.getTask("Time-Course");
	
				trajectoryTask.setMethodType(CTaskEnum.Task_timeCourse);
				trajectoryTask.getProblem().setModel(dm.getModel());
				
				trajectoryTask.setScheduled(true);
				//double duration =Double.parseDouble(timeCourse.getText());
				CTrajectoryProblem problem = (CTrajectoryProblem)trajectoryTask.getProblem();
				problem.setDuration(15000);
				problem.setStepNumber((long) 15000);
				model.setInitialTime(0.0);
				problem.setTimeSeriesRequested(true);
				CTrajectoryMethod timeMethod = (CTrajectoryMethod)trajectoryTask.getMethod();
				CCopasiParameter parameter = timeMethod.getParameter("Absolute Tolerance");
				parameter.setDblValue(1.0e-12);
				trajectoryTask.processWithOutputFlags(true, (int)CCopasiTask.OUTPUT_UI);
				CTimeSeries timeSeries = trajectoryTask.getTimeSeries();
				int iMax = (int)timeSeries.getNumVariables();
				int lastIndex = (int)timeSeries.getRecordedSteps() - 1;
				csvData = new Object[(int) model.getNumMetabs()][2];
				speciesAttr = new double[(int) model.getNumMetabs()];
				Object[] currentNetworks = CyActivator.netMgr.getNetworkSet().toArray();
				networkSUID = CyActivator.listener.getSUID();
				System.out.println("suid:" + networkSUID);
				if (currentNetworks.length==1) {
					
					currentNetwork = CyActivator.netMgr.getNetwork(networkSUID);
				}
				
				if (currentNetworks.length>1) {
					newNetworkSUID = CyActivator.listener.getSUID();
					currentNetwork =  CyActivator.netMgr.getNetwork(newNetworkSUID);
					if (currentNetwork.getSUID()==((CyNetwork)currentNetworks[0]).getSUID()) {
						previousNetwork = (CyNetwork) currentNetworks[1];
					} else {
						previousNetwork = (CyNetwork) currentNetworks[0];
					}
				}
				int nodenumber = currentNetwork.getNodeCount();
				java.util.List<CyNode> nodes = currentNetwork.getNodeList();
				CyNetworkView networkView = CyActivator.networkViewManager.getNetworkViews(currentNetwork).iterator().next();
		
				for (int i=0; i<iMax; i++) {
					csvData[i][0]= timeSeries.getTitle(i);
					csvData[i][1]= (new Double(timeSeries.getConcentrationData(lastIndex, i)));
					for (int b= 0; b<nodenumber; b++) {

						if (AttributeUtil.get(currentNetwork, nodes.get(b), "name", String.class).equals(csvData[i][0])==true) {
							AttributeUtil.set(currentNetwork,  nodes.get(b), "std-st concentration", csvData[i][1], Double.class);
							System.out.println("Conc:"+AttributeUtil.get(currentNetwork,  nodes.get(b), "std-st concentration", Double.class));
							speciesAttr[i]= AttributeUtil.get(currentNetwork,  nodes.get(b), "std-st concentration", Double.class);
							
						}
						
					}
					
				}
				if (currentNetworks.length>1) {
				JButton timeCompareButton = new JButton("Compare to the previous network");	
				CyActivator.myCopasiPanel.add(timeCompareButton);
				
				
				timeCompareButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						CompareDifferentNetworks mergeNetworks = new CompareDifferentNetworks(cySwingApplication, fileUtil, loadNetworkFileTaskFactory, synchronousTaskManager);
						mergeNetworks.compareDifferentNetworks(currentNetwork, previousNetwork, "concentration");
						
					}
					
				});
				}
				//}else {
				//	cancel();

			//	}
			} else {
				cancel();
		
			}
		}
		@Override
	    public void cancel() {
			super.cancel();
			super.cancelled=true;
			parentTask.cancel();
			taskMonitor.setProgress(1.0);
	    }
	}
}
		
		
			
	






	






	
	
		


