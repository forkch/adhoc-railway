/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchConfigurationDialog.java>  -  <>
 * 
 * begin     : Apr 10, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
 * language  : java
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.RailControl.ui.locomotives.configuration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.NoneLocomotive;

public class LocomotiveConfigurationDialog extends JDialog {

	private Preferences preferences;

	private Frame owner;

	private boolean okPressed = false;

	private boolean cancelPressed = false;

	private List<Locomotive> locomotives;
	private TableModel locomotiveTableModel;
	private JTable locomotiveTable;

	public LocomotiveConfigurationDialog(Frame owner, Preferences preferences,
			List<Locomotive> locomotives) {
		super(owner, "Switch Configuration", true);

		this.owner = owner;
		this.preferences = preferences;
		this.locomotives = locomotives;
		initGUI();
	}

	private void initGUI() {

		JPanel locomotivesPanel = createLocomotivesPanel();
		add(locomotivesPanel, BorderLayout.CENTER);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				LocomotiveConfigurationDialog.this.setVisible(false);
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelPressed = false;
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelPressed = true;
				LocomotiveConfigurationDialog.this.setVisible(false);
			}
		});
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setVisible(true);
		System.out.println(locomotives.get(1).getAddress());
	}

	private JPanel createLocomotivesPanel() {
		JPanel locomotivesPanel = new JPanel(new BorderLayout());
		
		locomotiveTableModel = new LocomotiveTableModel(locomotives);
		locomotiveTable = new JTable(locomotiveTableModel);

		//locomotiveType
		JComboBox locomotiveTypeComboBox = new JComboBox();
		locomotiveTypeComboBox.addItem("DigitalLocomotive");
		locomotiveTypeComboBox.addItem("DeltaLocomotive");
		locomotiveTypeComboBox.addItem("NoneLocomotive");

		TableColumn typeColumn = locomotiveTable.getColumnModel().getColumn(1);
		typeColumn.setCellEditor(new DefaultCellEditor(locomotiveTypeComboBox));
		typeColumn.setPreferredWidth(115);
		
		JScrollPane locomotiveTablePane = new JScrollPane(locomotiveTable);
		locomotiveTablePane.setPreferredSize(new Dimension(600, 400));
		locomotivesPanel.add(locomotiveTablePane, BorderLayout.CENTER);
		
		JButton addLocomotiveButton = new JButton("Add");
		addLocomotiveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Locomotive newLocomotive = new NoneLocomotive();
				locomotives.add(newLocomotive);
				locomotiveTable.repaint();
				locomotiveTable.revalidate();
				
			}
		});
		
		JButton removeLocomotiveButton = new JButton("Remove");
		removeLocomotiveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(addLocomotiveButton);
		buttonPanel.add(removeLocomotiveButton);
		
		locomotivesPanel.add(buttonPanel, BorderLayout.SOUTH);
		return locomotivesPanel;
	}
}