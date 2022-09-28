package org.cytoscape.CytoCopasiApp.tasks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.COPASI.CCommonName;
import org.COPASI.CCopasiMessage;
import org.COPASI.CCopasiParameter;
import org.COPASI.CCopasiParameterGroup;
import org.COPASI.CCopasiReportSeparator;
import org.COPASI.CCopasiTask;
import org.COPASI.CDataHandler;
import org.COPASI.CDataModel;
import org.COPASI.CDataObject;
import org.COPASI.CDataString;
import org.COPASI.CMetab;
import org.COPASI.CModel;
import org.COPASI.CModelEntity;
import org.COPASI.CModelValue;
import org.COPASI.COptItem;
import org.COPASI.COptMethod;
import org.COPASI.COptProblem;
import org.COPASI.COptTask;
import org.COPASI.COutputInterface;
import org.COPASI.CReaction;
import org.COPASI.CRegisteredCommonName;
import org.COPASI.CReportDefinition;
import org.COPASI.CReportDefinitionVector;
import org.COPASI.CRootContainer;
import org.COPASI.CScanItem;
import org.COPASI.CScanProblem;
import org.COPASI.CScanTask;
import org.COPASI.CSteadyStateMethod;
import org.COPASI.CSteadyStateProblem;
import org.COPASI.CSteadyStateTask;
import org.COPASI.CTaskEnum;
import org.COPASI.CTrajectoryProblem;
import org.COPASI.CTrajectoryTask;
import org.COPASI.ContainerList;
import org.COPASI.DataModelVector;
import org.COPASI.FloatMatrix;
import org.COPASI.FloatStdVector;
import org.COPASI.ObjectStdVector;
import org.COPASI.ReportItemVector;
import org.COPASI.SWIGTYPE_p_std__vectorT_std__vectorT_double_t_t;
import org.cytoscape.CytoCopasiApp.AttributeUtil;
import org.cytoscape.CytoCopasiApp.CyActivator;
import org.cytoscape.CytoCopasiApp.GetTable;
import org.cytoscape.CytoCopasiApp.Report.ParsingReportGenerator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ParameterScan extends AbstractCyAction{
	
	CySwingApplication cySwingApplication;
	FileUtil fileUtil;
	private JTree tree;
	Object selectedParam;
	JLabel selectedParamLabel;
	JTextField paramField;
	JTextField plotField;
	JTextField intervalField;
	JTextField minField;
	JTextField maxField;
	JComboBox scanItem;
	JComboBox taskItem;
	String[] myParameter;
	String myVariable;
	String myTask;
	String myMethod;
	String myInterval;
	String[] myMin;
	String[] myMax;
	Object[] scanData;
	CDataHandler dh;
	//CDataObject obj;
	public boolean valid;
	public String typeName;
	public int type;
	public String displayName;
	public Integer numSteps;
	public Double minValue;
	public Double maxValue;
	public String cn;
	Object[] displayNames ;
	CScanProblem scanProblem;
	CDataModel dataModel ;
	CDataObject scanObj;
	CyNetwork currentNetwork;
	double[] initialValues;
	double[] finalValues;
	double[] percentageChanges;
	double[] logChanges;
	DefaultListModel<String> parameters;
	JList<String> paramlist;
	JList<String> lowerBlist;
	JList<String> upperBlist;
	DefaultListModel<String> lowerBounds;
	DefaultListModel<String> upperBounds;
	private int count = 0;
	JLabel newModelPanelLabel;
	private ParameterScan.ScanTask parentTask;
//	private ParamaterScan.ScanTask parentTask;
	String[] finalScanItems ;
	String[] finalInit ;
	String[] finalFinal;
	PassthroughMapping pMapping ;
	PassthroughMapping pMapping_tooltip;
	DiscreteMapping pMapping_color;
	public ParameterScan(CySwingApplication cySwingApplication, FileUtil fileUtil) {
		super(ParameterScan.class.getSimpleName());
		this.cySwingApplication = cySwingApplication;
		this.fileUtil = fileUtil;
	}

	
	public void actionPerformed(ActionEvent e) {
		JFrame frame = new JFrame("Parameter Scan");
		JPanel myPanel = new JPanel();
		frame.setPreferredSize(new Dimension(600,600));
		myPanel.setPreferredSize(new Dimension(600,600));
		myPanel.setLayout(new GridLayout(15, 15));
		
		JLabel selectedParamLabel = new JLabel("Object");
		JLabel intervalLabel = new JLabel("Intervals");
		JLabel minLabel = new JLabel("min");
		JLabel maxLabel = new JLabel("max");
		
		Box paramValBox = Box.createHorizontalBox();
		parameters = new DefaultListModel<>();
		lowerBounds = new DefaultListModel<>();
		upperBounds = new DefaultListModel<>();
		
		paramlist = new JList<>(parameters);
		lowerBlist = new JList<>(lowerBounds);
		upperBlist = new JList<>(upperBounds);
		
		paramField = new JTextField(20);
		intervalField = new JTextField(5);
		minField = new JTextField(5);
		maxField = new JTextField(5);
		
		JLabel plotLabel = new JLabel("");
		JTextField plotField = new JTextField(20);
		
		JLabel methodLabel = new JLabel ("New Scan Item");
		String[] methods = {"Scan", "Repeat", "Random Distribution"};
		scanItem = new JComboBox(methods);
		Box methodBox = Box.createVerticalBox();
		methodBox.add(methodLabel);
		methodBox.add(scanItem);
		
		JLabel taskLabel = new JLabel("Task");
		String[] tasks = {"Time Course", "Steady State"};
		taskItem = new JComboBox(tasks);
		Box taskBox = Box.createVerticalBox();
		taskBox.add(taskLabel);
		taskBox.add(taskItem);
		
		JButton create = new JButton("Add New Scan Item");
		JButton plotObj = new JButton("Monitor Perturbations");
		Box topBox = Box.createHorizontalBox();
		topBox.add(methodBox);
		topBox.add(taskBox);
		topBox.add(create);
		
		create.addActionListener((ActionListener) new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				Box newParamBox = Box.createHorizontalBox();
				Box overallParam = Box.createVerticalBox();
				
				//JButton btnParam = new JButton("->");
				JLabel newParamLabel = new JLabel("Parameter_" + (count+1));
				JLabel newLowLabel = new JLabel("From");
				JLabel newUpLabel = new JLabel("To");
				JButton newBtnParam = new JButton("->");
				JTextField newParam = new JTextField(30);
				newParam.setEditable(false);
				JTextField newLow = new JTextField(5);
				JTextField newUp = new JTextField(5);
				newParamBox.add(newParamLabel);
				newParamBox.add(newBtnParam);
				newParamBox.add(newParam);
				newParamBox.add(newLowLabel);
				newParamBox.add(newLow);
				newParamBox.add(newUpLabel);
				newParamBox.add(newUp);
				
				count++;
				overallParam.add(newParamBox);
				myPanel.add(overallParam);
				myPanel.validate();
				myPanel.repaint();
				newBtnParam.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						JPanel panel = new JPanel();
						JTextField lowBBox = new JTextField(5);
						JTextField upBBox = new JTextField(5);
						JLabel lowLabel = new JLabel("Initial Value");
						JLabel upLabel = new JLabel("Final Value");
						panel.setPreferredSize(new Dimension(500,500));
						DefaultMutableTreeNode param = new DefaultMutableTreeNode("Select Items");
						String[] paramCat = {"Reactions","Species"};
						try {
							createNodes(param, paramCat);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
						tree = new JTree(param);
						
						tree.addTreeSelectionListener(new TreeSelectionListener() {

							@SuppressWarnings("null")
							public void valueChanged(TreeSelectionEvent e) {
								// TODO Auto-generated method stub
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
								if (node==null) 
									return;
								
									
									
									Object selectedParam = e.getNewLeadSelectionPath().getLastPathComponent();
									newParam.setText(selectedParam.toString());	
									parameters.addElement(newParam.getText());
							}
							
							}			
								);
						
							
						JScrollPane treeView = new JScrollPane(tree);
						treeView.setPreferredSize(new Dimension(420,420));
						panel.add(treeView);
						panel.add(lowLabel);
						panel.add(lowBBox);
						panel.add(upLabel);
						panel.add(upBBox);
						Object[] paroptions= {"OK","Cancel"};
						int parameterSelection = JOptionPane.showOptionDialog(null, panel, "Select Parameter",JOptionPane.PLAIN_MESSAGE, 1, null, paroptions, paroptions[0]);
						
						if (parameterSelection == JOptionPane.OK_OPTION) {
							newLow.setText(lowBBox.getText());
							newUp.setText(upBBox.getText());
							lowerBounds.addElement(lowBBox.getText());
							upperBounds.addElement(upBBox.getText());
						}
					
					}
					
					
					}	
						);
					}
					
				});
				
		
		plotObj.addActionListener((ActionListener) new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JPanel panel2 = new JPanel();
				panel2.setPreferredSize(new Dimension(100,100));

				JRadioButton concPert = new JRadioButton("Concentration");
				JRadioButton fluxPert = new JRadioButton("Flux");
				panel2.add(concPert);
				panel2.add(fluxPert);
				Object[] plotoptions= {"OK","Cancel"};
				int parameterSelection = JOptionPane.showOptionDialog(null, panel2, "Select Item",JOptionPane.PLAIN_MESSAGE, 1, null, plotoptions, plotoptions[0]);
				
				if (parameterSelection == JOptionPane.OK_OPTION) {
					if (concPert.isSelected()==true) {
						plotLabel.setText("Concentrations");
					} else {
						plotLabel.setText("Fluxes");
					}
					myPanel.add(plotLabel);
					//myPanel.add(plotField);
					myPanel.validate();
					myPanel.repaint();
				}
				
			}
			
			
			
		});
		
		
		//myPanel.add(methodBox);
		//myPanel.add(taskBox);
		//myPanel.add(create);
		myPanel.add(topBox);
        Object [] options = {plotObj, "OK", "Cancel"};
		
		
		int result = JOptionPane.showOptionDialog(frame, myPanel, 
	               "Parameter Scan", JOptionPane.PLAIN_MESSAGE, 1, null, options, options[0]);
	    
		if (result == JOptionPane.OK_OPTION); {
			myParameter = new String[count];
			myMin = new String[count];
			myMax = new String[count];
			
			for (int a=0; a<count ; a++) {
				myParameter[a] = parameters.get(a);
				myMin[a] = lowerBounds.get(a);
				myMax[a] = upperBounds.get(a);
				//startValue[a] = Double.parseDouble(startV[a]);
			}
			
			
			myVariable = plotLabel.getText();
			myInterval = intervalField.getText();
			myTask = taskItem.getSelectedItem().toString();
			myMethod = scanItem.getSelectedItem().toString();
			
			scanData = setScanData();
			
		}
		final ScanTask task = new ScanTask();
		CyActivator.taskManager.execute(new TaskIterator(task));
	}
	
	
	public Object[] setScanData() {
		
		Object[] scanData = {myParameter, myVariable, myInterval, myMin, myMax, myTask, myMethod};
		ParsingReportGenerator.getInstance().appendLine("Task: " + myTask.toString());
		return scanData;
	}
	
	public class ScanTask extends AbstractTask {
		private TaskMonitor taskMonitor;
		
		public ScanTask() {
			super.cancelled = false;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			this.taskMonitor = taskMonitor;
			taskMonitor.setTitle("Parameter Scan");
			taskMonitor.setStatusMessage("Parameter scan started");
			
			taskMonitor.setProgress(0);
			
			String modelName = new Scanner(CyActivator.getReportFile(1)).next();
			String modelString = new Scanner(new File(modelName)).useDelimiter("\\Z").next();
			//ParsingReportGenerator.getInstance().appendLine("Model String: " + modelString);
			 dataModel = CRootContainer.addDatamodel();
			dataModel.loadFromString(modelString);
			
			scanData = setScanData();
			
			CModel model = dataModel.getModel();
		
			
			
			CScanTask scanTask = (CScanTask) dataModel.getTask("Scan");
			
			 scanProblem = (CScanProblem) scanTask.getProblem();
	
				scanProblem.setSubtask(CTaskEnum.Task_steadyState);
		    scanProblem.setOutputInSubtask(false);
			
			if (scanData[1].toString().equals("Concentrations")==true) {
				displayNames = new Object[(int) model.getNumMetabs()];
				initialValues = new double[(int) model.getNumMetabs()];
				finalValues = new double[(int) model.getNumMetabs()];
				percentageChanges = new double[(int) model.getNumMetabs()];
				logChanges = new double[(int) model.getNumMetabs()];

				for (int i=0; i<model.getNumMetabs(); i++) {
					displayNames[i]= model.getMetabolite(i).getObjectDisplayName();
				}
			} else if (scanData[1].toString().equals("Fluxes")==true){
				displayNames = new Object[(int) model.getNumReactions()];
				initialValues = new double[(int) model.getNumReactions()];
				finalValues = new double[(int) model.getNumReactions()];
				percentageChanges = new double[(int) model.getNumReactions()];
				logChanges = new double[(int) model.getNumReactions()];

				for (int i=0; i<model.getNumReactions(); i++) {
					displayNames[i]= model.getReaction(i).getObjectDisplayName();
					System.out.println(displayNames[i]);
				}
			}
		
			
			
				addScanItem(scanProblem, scanData);
				 dh = new CDataHandler();
				 CDataObject obj;
				for (int i = 0; i<displayNames.length; i++) {
					if (scanData[1].toString().equals("Concentrations")==true) {
						obj = model.getMetabolite(i);
					} else {
						obj = model.getReaction(i);
					}
			//		 obj = dataModel.findObjectByDisplayName(displayNames[i].toString());
				 
				if (obj == null) {
					valid = false;
					System.err.println("couldn't resolve displayName: " + displayNames[i]);
					
				}

			       if (obj instanceof CMetab) {
			           obj = ((CMetab) obj).getConcentrationReference();
			       }else if (obj instanceof CReaction) {
			           obj = ((CReaction) obj).getFluxReference();

			       }
				
			         dh.addDuringName(new CRegisteredCommonName( obj.getCN().getString()));
			         dh.addAfterName(new CRegisteredCommonName( obj.getCN().getString()));

						ParsingReportGenerator.getInstance().appendLine("what's added to the dh: " + obj.getCN().getString());

			       }

			       
			   
			       // initialize passing along the output handler
			       if (!scanTask.initializeRawWithOutputHandler((int)CCopasiTask.OUTPUT_UI, dh))
			       { 
			         System.err.println("Couldn't initialize the steady state task");
			         System.err.println(CCopasiMessage.getAllMessageText());
			       }
			       //run
			       if (!scanTask.processRaw(true));
			       {
			         System.err.println("Couldn't run the steady state task");
			         System.err.println(CCopasiMessage.getAllMessageText());
			       }
			       scanTask.restore();
			       Object[] currentNetworks = CyActivator.netMgr.getNetworkSet().toArray();
					
					currentNetwork = (CyNetwork) currentNetworks[currentNetworks.length-1];
					CyNetworkView networkView = CyActivator.networkViewManager.getNetworkViews(currentNetwork).iterator().next();
					int nodenumber = currentNetwork.getNodeCount();
					java.util.List<CyNode> nodes = currentNetwork.getNodeList();
					
			       int numRows = dh.getNumRowsDuring();
				   	ParsingReportGenerator.getInstance().appendLine("NumRows: " + numRows);
				   	for (int i= 0; i<nodenumber; i++) {   	
				         FloatStdVector data = dh.getNthRow(0);
				         for (int j = 0; j < data.size(); j++)
				         {
						if (AttributeUtil.get(currentNetwork, nodes.get(i), "display name", String.class).equals(displayNames[j])==true) {
							AttributeUtil.set(currentNetwork,  nodes.get(i),scanData[1].toString()+ ":initial", data.get(j), Double.class);
							initialValues[j]=data.get(j);
				           System.out.print(data.get(j));
				           if (j + 1 < data.size())
				             System.out.print("\t");
				         }
				         }
				         System.out.println();
				       
				System.out.println();
			      
			       FloatStdVector data2 = dh.getAfterData();
			       
			       for (int j = 0; j < data2.size(); j++)
			       {
			    	   if (AttributeUtil.get(currentNetwork, nodes.get(i), "display name", String.class).equals(displayNames[j])==true) {
							AttributeUtil.set(currentNetwork,  nodes.get(i), scanData[1].toString()+":final", data2.get(j), Double.class);

			         System.out.print(data2.get(j));
						finalValues[j]=data2.get(j);
						double difference = finalValues[j]-initialValues[j];
						percentageChanges[j] = 100*Math.abs(difference)/Math.abs(initialValues[j]);
						AttributeUtil.set(currentNetwork, nodes.get(i), "perturbation",String.valueOf(Math.round(initialValues[j]*100.0)/100.0)+"->"+ String.valueOf(Math.round(finalValues[j]*100.0)/100.0) , String.class);

						if (finalValues[j]==0 && initialValues[j]==0) {
							AttributeUtil.set(currentNetwork, nodes.get(i), "change", 0.0, Double.class);

						}
						else if (percentageChanges[j]>100) {
							AttributeUtil.set(currentNetwork, nodes.get(i), "change", 100.0, Double.class);

						} else {
						AttributeUtil.set(currentNetwork, nodes.get(i), "change", percentageChanges[j], Double.class);
						}
						if (difference>0) {
							AttributeUtil.set(currentNetwork, nodes.get(i), "variation", "Increase", String.class);
							
						} else {
							AttributeUtil.set(currentNetwork, nodes.get(i), "variation", "Decrease", String.class);
						}
						 pMapping_color = (DiscreteMapping) CyActivator.vmfFactoryD.createVisualMappingFunction("variation", String.class, BasicVisualLexicon.NODE_FILL_COLOR);
						 pMapping_color.putMapValue("Increase", Color.RED);
						 pMapping_color.putMapValue("Decrease", Color.CYAN);
						
			         if (j + 1 < data2.size())
			           System.out.print("\t");
			       }
				   	}
			 
				   	}
				   	VisualStyle visStyle = CyActivator.visualMappingManager.getVisualStyle(networkView);
				   String ctrAttrName1 = "change";
				   String ctrAttrName2 = "perturbation";

					 pMapping = (PassthroughMapping) CyActivator.vmfFactoryP.createVisualMappingFunction(ctrAttrName1, Double.class, BasicVisualLexicon.NODE_SIZE);
					 pMapping_tooltip = (PassthroughMapping) CyActivator.vmfFactoryP.createVisualMappingFunction(ctrAttrName2, String.class, BasicVisualLexicon.NODE_TOOLTIP);
					
					visStyle.addVisualMappingFunction(pMapping);
					visStyle.addVisualMappingFunction(pMapping_tooltip);
					visStyle.addVisualMappingFunction(pMapping_color);
					 CyActivator.visualMappingManager.setCurrentVisualStyle(visStyle);
				        visStyle.apply(networkView);
				        networkView.updateView();
				        if (newModelPanelLabel!=null) {
					        CyActivator.myCopasiPanel.remove(newModelPanelLabel);

				        }
				        newModelPanelLabel = new JLabel("Model Perturbation");
					    Font newModelFont = new Font("Calibri", Font.BOLD, 16);
					    newModelPanelLabel.setFont(newModelFont);
					    newModelPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
				        CyActivator.myCopasiPanel.add(newModelPanelLabel);
				        JLabel[] modLabels = new JLabel[finalScanItems.length];
				        for (int i = 0; i<modLabels.length;i++) {
				        	modLabels[i]= new JLabel("Changed "+finalScanItems[i]+" from "+finalInit[i].toString()+" to "+finalFinal[i].toString());
				        	 CyActivator.myCopasiPanel.add(modLabels[i]);
				        }
				        JButton resetButton = new JButton("Reset modifications");
				        CyActivator.myCopasiPanel.add(resetButton);
				        resetButton.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								CyTable nodeTable = currentNetwork.getDefaultNodeTable();
								nodeTable.deleteColumn("change");
								nodeTable.deleteColumn("variation");
								nodeTable.deleteColumn("perturbation");
							}
				        	
				        });
				        CyActivator.myCopasiPanel.validate();
				        CyActivator.myCopasiPanel.repaint();
			    }

			  
	

		
	
	
	 boolean addScanItem(CScanProblem scanProblem, Object[] scanData) {
		
		if (scanData[6] == "Scan") {
			type = CScanProblem.SCAN_LINEAR;
			
		} else if (scanData[6] == "Repeat") {
			type = CScanProblem.SCAN_REPEAT;
			
		} else if (scanData[6] == "Random Distribution") {
			type = CScanProblem.SCAN_RANDOM;
			
		}
		
		ParsingReportGenerator.getInstance().appendLine("Scan Type " + type);
		
		numSteps = 2;
		ParsingReportGenerator.getInstance().appendLine("Number of Steps: " + numSteps);
		
		finalScanItems = (String[]) scanData[0];
		finalInit = (String[]) scanData[3];
		finalFinal = (String[]) scanData[4];
		
		for (int x=0; x<finalScanItems.length; x++) {
			
		CCopasiParameterGroup cItem = scanProblem.addScanItem(type, numSteps);
		
		double finalMin = Double.parseDouble(finalInit[x].toString());
		ParsingReportGenerator.getInstance().appendLine("minimum: " + finalMin);
		double finalMax = Double.parseDouble(finalFinal[x].toString());
		ParsingReportGenerator.getInstance().appendLine("maximum: " + finalMax);
	
		 scanObj = dataModel.findObjectByDisplayName(finalScanItems[x].toString());
		
		if (scanObj == null) {
			valid = false;
			System.err.println("couldn't resolve displayName: " + finalScanItems[x].toString());
			return false;
		}

		if (scanObj instanceof CModelEntity) {
			// resolve model elements to their initial value reference
			scanObj = ((CModelEntity) scanObj).getInitialValueReference();
		} else if (scanObj instanceof CCopasiParameter) {
			// resolve local parameters to its value reference
			scanObj = ((CCopasiParameter) scanObj).getValueReference();
		}

		cn = scanObj.getCN().getString();
		
		ParsingReportGenerator.getInstance().appendLine("scanning the parameter: " + cn);
		cItem.getParameter("Maximum").setDblValue(finalMax);
		cItem.getParameter("Minimum").setDblValue(finalMin);
		cItem.getParameter("Object").setCNValue(cn);
		}
		return true;
	}
	 
	

		
}
	


	private void createNodes(DefaultMutableTreeNode item, String[] categoryNames) throws Exception {
		DefaultMutableTreeNode paramItem = null;
		DefaultMutableTreeNode paramItem2 = null;
		DefaultMutableTreeNode paramItem3 = null;
		DefaultMutableTreeNode paramItem4 = null;
		String reactCat = "Reaction Parameters";
		String specCat = "Initial Concentration";
		for (int a=0; a<categoryNames.length; a++) {
			paramItem = new DefaultMutableTreeNode(categoryNames[a]);
			item.add(paramItem);
			try {
				String modelName = new Scanner(CyActivator.getReportFile(1)).next();
				CDataModel dm = CRootContainer.addDatamodel();
				String modelString = new Scanner(new File(modelName)).useDelimiter("\\Z").next();
				dm.loadFromString(modelString);
				CModel model = dm.getModel();
				if(categoryNames[a] == "Reactions") {
					paramItem2 = new DefaultMutableTreeNode(reactCat);
					paramItem.add(paramItem2);
					int numreac = (int) model.getNumReactions();
					
					for (int b=0; b<numreac; b++) {
						paramItem3 = new DefaultMutableTreeNode(model.getReaction(b).getObjectDisplayName());
						paramItem2.add(paramItem3);
						int numParam = (int) model.getReaction(b).getParameters().size();
						for (int c=0; c<numParam; c++) {
							paramItem4 = new DefaultMutableTreeNode(model.getReaction(b).getParameters().getParameter(c).getObjectDisplayName());
							paramItem3.add(paramItem4);
						}
						
					}
				}else if (categoryNames[a] == "Species") {
					paramItem2 = new DefaultMutableTreeNode(specCat);
					paramItem.add(paramItem2);
					int numSpec = (int) model.getNumMetabs();
					for (int b=0; b< numSpec; b++) {
						paramItem3 = new DefaultMutableTreeNode(model.getMetabolite(b).getInitialConcentrationReference().getObjectDisplayName());
						paramItem2.add(paramItem3);
					}
				}
			
		} catch (IOException e){
			throw new Exception("problem with the objective function");
		}
		
	}
	}
	
}