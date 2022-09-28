package org.cytoscape.CytoCopasiApp.Query;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.action.LinkAction;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.HyperlinkProvider;

public class Brenda {
	
	DefaultTableModel ecNoModel;
	 String[] resultColumns;
		Object [][] results;
		JScrollPane sp;
		JScrollPane sp3;
	public void brendaConnect(JPanel reactionPanel, JLabel[] paramLabels, JTextField[] paramVals) {
		JFrame loginFrame = new JFrame("Login");
		Object[] loginOptions = {"OK"};
		loginFrame.setPreferredSize(new Dimension(350,250));
	
		Box loginBox = Box.createVerticalBox();
		loginBox.setPreferredSize(new Dimension(280,100));
		JLabel emailLabel = new JLabel("email");
		JTextField emailField = new JTextField(7);
		JLabel passwordLabel = new JLabel("password");
		JPasswordField passwordField = new JPasswordField(7);
		
		loginBox.add(emailLabel);
		loginBox.add(emailField);
		loginBox.add(passwordLabel);
		loginBox.add(passwordField);
		
		//loginPanel.add(loginBox);
		loginFrame.add(loginBox);
		
		JOptionPane loginPane = new JOptionPane(loginBox, JOptionPane.QUESTION_MESSAGE, 0, null, loginOptions, loginOptions[0]);
		JDialog logDialog = new JDialog(loginFrame, "Please Enter Your Email and Password", true);
		
		logDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		logDialog.setContentPane(loginPane);
		
		loginPane.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				if (JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())) {
					
					loginFrame.add(new JLabel("Logging in...this may take a while..."));
					SoapClient query = new SoapClient();
					String myEmail = emailField.getText();
					String myPassword = String.valueOf(passwordField.getPassword());

					if (loginPane.getValue().equals(loginOptions[0])) {
						loginPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
						String myOrganisms = query.getOrganismNames(myEmail, myPassword);
						System.out.println("myOrganisms:" + myOrganisms);
						if (myOrganisms==null) {
							
							JOptionPane.showMessageDialog(loginFrame, "Username or password is wrong, or account was not activated.\n"
									+ "");
							loginPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
							loginPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						}else {
							loginPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
							logDialog.dispose();
							String[] organismList = myOrganisms.split("!");
							JComboBox organismCombo = new JComboBox();
							for (int i = 0; i<organismList.length; i++) {
								organismCombo.addItem(organismList[i]);
							}
							
							JFrame queryFrame = new JFrame("Brenda Query");
							JPanel queryPanel = new JPanel();
							queryFrame.setPreferredSize(new Dimension(550,250));
							queryPanel.setPreferredSize(new Dimension(550,250));
							queryPanel.setLayout(new GridLayout(5,2));
							
							
							JLabel enzymeNameLabel = new JLabel("Enzyme");
							JTextField enzymeField = new JTextField(7);
							JLabel organismLabel = new JLabel("Select Organism");
							JLabel queryTypeLabel = new JLabel("Value");
							String[] queryTypeOptions = {"Km", "KmKcat", "Ki"};
							JComboBox queryTypeCombo = new JComboBox(queryTypeOptions);
							
							
							Box queryBox = Box.createVerticalBox();
							
							queryPanel.add(enzymeNameLabel);
							queryPanel.add(enzymeField);
							queryPanel.add(organismLabel);
							queryPanel.add(organismCombo);
							queryPanel.add(queryTypeLabel);
							queryPanel.add(queryTypeCombo);
							queryPanel.validate();
							queryPanel.repaint();
							queryFrame.add(queryPanel);
							Object[] queryOptions = {"Find"};
							
							JOptionPane queryPane = new JOptionPane(queryPanel, JOptionPane.QUESTION_MESSAGE, 0, null, queryOptions, queryOptions[0]);
							JDialog queryDial = new JDialog(queryFrame, "Welcome to Brenda", true);
							
							queryDial.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
							queryDial.setContentPane(queryPane);
							
							queryPane.addPropertyChangeListener(new PropertyChangeListener() {
							
								@Override
								public void propertyChange(PropertyChangeEvent evt2) {
									
									// TODO Auto-generated method stub
									if (JOptionPane.VALUE_PROPERTY.equals(evt2.getPropertyName())) {
										queryPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));

										String enzymeName = enzymeField.getText();
										ecNoModel = new DefaultTableModel();
										String ecTableColumns[] = {"EC Number", "Recommended Name", "Synonyms"};
										ecNoModel.addColumn(ecTableColumns[0]);
										ecNoModel.addColumn(ecTableColumns[1]);
										ecNoModel.addColumn(ecTableColumns[2]);
										JXTable ecTable = new JXTable(ecNoModel);
										ECFinder ecFinder = new ECFinder();	
							            String[][] ecData = ecFinder.getECFromHTML(enzymeName);
							            
							            if (queryPane.getValue().equals(queryOptions[0])) {
							            if (ecData==null) {
											queryPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));

							            	JOptionPane.showMessageDialog(queryFrame, "Enzyme not found, try again.");
							            	queryPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							            } else {
											queryPane.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));

							            	queryDial.dispose();
							            	
							            	@SuppressWarnings({ "serial", "rawtypes" })
							            	
											LinkAction ecAction = new LinkAction<Object>(null) {
												public void actionPerformed(ActionEvent e) {
											       
													JFrame queryResultsFrame = new JFrame("Brenda SOAP Query");
													QueryResultSplitter split = new QueryResultSplitter();
													
													String eventText = e.getSource().toString();
													System.out.println(eventText);
													String myEC = StringUtils.substringBetween(eventText, "text=", ",defaultCapable");
													System.out.println(myEC);
														for (int i = 0; i<ecData[0].length; i++) {
															if (myEC.equals(ecData[0][i])==true) {
															
															System.out.println(ecData[0][i]);
																try {
																	String resultString = query.getValue(myEmail, myPassword, ecData[0][i], organismCombo.getSelectedItem().toString(), queryTypeCombo.getSelectedItem().toString());
																	//System.out.println("query items: "+ myEmail+ myPassword+ ecData[0][i]+ organismCombo.getSelectedItem().toString()+ queryTypeCombo.getSelectedItem().toString());
																	System.out.println("result string:" + resultString);
																	
																	if (resultString.equals("")){
																		JOptionPane.showMessageDialog(queryFrame, "No enzyme data found for the organism " + organismCombo.getSelectedItem().toString());
																	}
																	String[] rows = split.splitResults(resultString);
																	resultColumns = split.splitColumnNames(rows[0]);
																	results = new Object[rows.length][resultColumns.length];
																	//String[] resultsTemp = new String[rows.length];
																	for (int j=0; j<rows.length; j++) {
																		String[] resultsTemp = split.splitData(rows[j]);
																		
																		for(int k=0; k< resultsTemp.length; k++) {
																			System.out.println("result temp:" + resultsTemp[k]);
																			results[j][k] = resultsTemp[k];
																		}
																	}
																	
																	JXTable jt = new JXTable(results, resultColumns);
																	
																	JScrollPane sp2 = new JScrollPane(jt);
																	sp2.setPreferredSize(new Dimension(1500,450));
																	JPanel brendaResultsPanel = new JPanel();
																	brendaResultsPanel.setPreferredSize(new Dimension(1600,600));
																	brendaResultsPanel.add(sp2);
																	Object[] brResOptions = {"Select", "Cancel"};
																	
																	LinkAction ec2Action = new LinkAction<Object>(null) {

																		
																		public void actionPerformed(ActionEvent e) {
																			String referenceNo =  (String) jt.getValueAt(jt.getSelectedRow(), 7);
																			System.out.println("referenceNo:" + referenceNo);

																			String referenceSt = query.getPubmedLink(myEmail, myPassword, referenceNo);
																			String pubmedNo = StringUtils.substringBetween(referenceSt, "pubmedId*", "#");
																			System.out.println("pubmedNo:" + pubmedNo);
																			String pubmedURL = "https://pubmed.ncbi.nlm.nih.gov/"+pubmedNo+"/";
																			URI pubmedURI = URI.create(pubmedURL);
																			try {
																				Desktop.getDesktop().browse(pubmedURI);
																			} catch (IOException e1) {
																				// TODO Auto-generated catch block
																				e1.printStackTrace();
																			}
																		}
																		
																	};
																	TableCellRenderer ecRenderer2 = new DefaultTableRenderer(
																			
																			new HyperlinkProvider(ec2Action));
																	jt.getColumnExt(7).setEditable(false);
																	jt.getColumnExt(7).setCellRenderer(ecRenderer2);
																	int queryDialog = JOptionPane.showOptionDialog(null, brendaResultsPanel, "Brenda Results: "+myEC, JOptionPane.PLAIN_MESSAGE, 1, null, brResOptions, brResOptions[0]);

																	if (queryDialog == 0) {
																		String selectedBrenda = (String) jt.getValueAt(jt.getSelectedRow(), 1);
																		System.out.println(selectedBrenda);
																		for (int h = 0 ; h< paramLabels.length; h++) {
																			
																			System.out.println("Query Combo:"+queryTypeCombo.getSelectedItem().toString());
																			System.out.println(paramLabels[h].getText());
																			if (queryTypeCombo.getSelectedItem().toString().equals(paramLabels[h].getText())) {
																				paramVals[h].setText(selectedBrenda);
																			} 
																		}
																	}
																}catch (NullPointerException e1) {
																	throw new RuntimeException ("This parameter does not exist in the reaction");
																} catch (NoSuchAlgorithmException e1) {
																	// TODO Auto-generated catch block
																	e1.printStackTrace();
																} catch (MalformedURLException e1) {
																	// TODO Auto-generated catch block
																	e1.printStackTrace();
																} catch (RemoteException e1) {
																	// TODO Auto-generated catch block
																	e1.printStackTrace();
																}
																
																
																
														}
														
														}
													
													
												}

											};
											
											TableCellRenderer ecRenderer = new DefaultTableRenderer(
													
													new HyperlinkProvider(ecAction));
											
											
											//ecNoTable = new JTable();
											ecTable.setModel(ecNoModel);
											
											
											
											ecTable.getColumnExt(0).setEditable(false);
											ecTable.getColumnExt(0).setCellRenderer(ecRenderer);
											//ecNoTable.getColumnModel().getColumn(1).setWidth(6);
											if (sp!=null) {
									              reactionPanel.remove(sp);
									              reactionPanel.validate();
												  reactionPanel.repaint();
											}
								              for(int i = 0; i<ecData[0].length; i++) {
								            	  ecNoModel.addRow(new Object[] {ecData[0][i],ecData[1][i],ecData[2][i]});
								            	  
								            	  System.out.println(ecData[0][i]);
								            	  System.out.println(ecData[1][i]);
								            	  System.out.println(ecData[2][i]);
								            	  reactionPanel.validate();
												  reactionPanel.repaint();
								       		   
								              }
								              
								              
								              sp = new JScrollPane(ecTable);
								              
											  reactionPanel.add(sp);
											  reactionPanel.validate();
											  reactionPanel.repaint();
											  
										
											 
							            }
									}
								}
								}
							});
							
						queryDial.pack();
						queryDial.setLocationRelativeTo(queryFrame);
						queryDial.setVisible(true);
							
						}
						}
						
					}
					
							
					}
			
				});
		
	    logDialog.pack();
	    logDialog.setLocationRelativeTo(loginFrame);
	    logDialog.setVisible(true);
			}
	}


