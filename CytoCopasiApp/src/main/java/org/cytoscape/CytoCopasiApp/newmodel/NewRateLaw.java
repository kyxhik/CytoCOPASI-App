package org.cytoscape.CytoCopasiApp.newmodel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.COPASI.CEvaluationTree;
import org.COPASI.CFunction;
import org.COPASI.CFunctionDB;
import org.COPASI.CFunctionParameter;
import org.COPASI.CFunctionParameters;

public class NewRateLaw {
	String newFormula;
	CFunctionParameters variables;
	DefaultTableModel newRateLawModel;
	JTable rateLawTable;
	JComboBox typeCombo; 
	JScrollPane sp;
	JScrollPane sp3 ;
	
	public void addRateLaw(CFunctionDB functionDB, JComboBox rateLawCombo) {
		// TODO Auto-generated method stub
		JFrame newRateLawFrame = new JFrame("Add a new rate law");
		JPanel newRateLawPanel = new JPanel();
		newRateLawPanel.setPreferredSize(new Dimension(400,300));
		newRateLawPanel.setLayout(new GridLayout(4,2));
		Box functionNameBox = Box.createHorizontalBox();
		JLabel functionNameLabel = new JLabel("Function: ");
		JTextField functionName = new JTextField(3);
		functionNameBox.add(functionNameLabel);
		functionNameBox.add(functionName);
		
		Box formulaBox = Box.createHorizontalBox();
		JLabel formulaLabel = new JLabel("Formula: ");
		JTextArea formula = new JTextArea(5,1);
		JButton commitButton = new JButton("commit");
		
		
		formulaBox.add(formulaLabel);
		formulaBox.add(formula);
		formulaBox.add(commitButton);
		
		newRateLawPanel.add(functionNameBox);
		newRateLawPanel.add(formulaBox);
		newRateLawPanel.validate();
		newRateLawPanel.repaint();
		
		
		commitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//CFunction newFunction = new CFunction(functionName.getText());
				//functionDB.add(newFunction, true);
				if (functionDB.findFunction(functionName.getText())!=null) {
					functionDB.removeFunction(functionName.getText());
				}
				CEvaluationTree newFunction = functionDB.createFunction(functionName.getText(), CEvaluationTree.UserDefined);
				newFormula = formula.getText();
				newFunction.setInfix(newFormula);
				//newFunction.setReversible(COPASI.TriUnspecified);
				
				variables = ((CFunction) newFunction).getVariables();
				System.out.println("number of parameters: " + variables.size());
				System.out.println("rate law: " + newFunction.getInfix());
				
				//set function parameters and values here. When you click on add, the values will be added to changed objects and become a part of your model
				String description[] = {"Name", "Type", "Units"};
				String type[] = {"Variable", "Substrate", "Product", "Modifier", "Parameter"};
				newRateLawModel = new DefaultTableModel();
				rateLawTable = new JTable();
				rateLawTable.setModel(newRateLawModel);
				
				newRateLawModel.addColumn(description[0]);
				newRateLawModel.addColumn(description[1]);
				newRateLawModel.addColumn(description[2]);
				typeCombo = new JComboBox(type);
				typeCombo.setSelectedItem(type[0]);
				
				for (int i =0; i< variables.size() ; i++) {
					
					newRateLawModel.addRow(new Object[] {variables.getParameter(i).getObjectName(), typeCombo.getSelectedItem(), variables.getParameter(i).getUnits()});
					
				}
				
				if (sp3!=null) {
					newRateLawPanel.remove(sp3);
					newRateLawPanel.validate();
					newRateLawPanel.repaint();
					
					sp3 = null;
				}
				
				
				
				rateLawTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCombo));
				sp3 = new JScrollPane(rateLawTable);
				newRateLawPanel.add(sp3);
				newRateLawPanel.validate();
				newRateLawPanel.repaint();
			}
			
		});
		
		
		newRateLawFrame.add(newRateLawPanel);
		Object[] rateLawAddOptions = {"Add", "Cancel"};
		
		int rateLawAddDialog = JOptionPane.showOptionDialog(newRateLawFrame, newRateLawPanel, "Add a new rate law", JOptionPane.PLAIN_MESSAGE, 1, null, rateLawAddOptions, rateLawAddOptions[0]);
		
		if (rateLawAddDialog == 0) {
			rateLawCombo.addItem(functionName.getText());
			rateLawCombo.setSelectedItem(functionName.getText());
			
			for (int i=0;i< variables.size(); i++) {
				String paramType = (String) newRateLawModel.getValueAt(i,1);
				if (paramType == "Substrate") {
					variables.getParameter(i).setUsage(CFunctionParameter.Role_SUBSTRATE);
					System.out.println("substrate when set:"+variables.getParameter(i).getObjectName());
				}else if (paramType == "Product") {
					variables.getParameter(i).setUsage(CFunctionParameter.Role_PRODUCT);
					System.out.println("product when set:"+variables.getParameter(i).getObjectName());
				}else if (paramType == "Modifier") {
					variables.getParameter(i).setUsage(CFunctionParameter.Role_MODIFIER);
					System.out.println("modifier when set:"+variables.getParameter(i).getObjectName());
				}else if (paramType == "Parameter") {
					variables.getParameter(i).setUsage(CFunctionParameter.Role_PARAMETER);										
				}
				
			}
		}
	}
	


}
