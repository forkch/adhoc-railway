/*------------------------------------------------------------------------
 * 
 * <./ui/locomotives/configuration/LocomotiveConfig.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:57 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.TutorialUtils;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LocomotiveConfig extends JDialog {
	private boolean okPressed;

	private JTextField nameTextField;

	private JSpinner busTextField;

	private JSpinner addressTextField;

	private JTextField descTextField;

	private JTextField imageTextField;

	private JComboBox locomotiveTypeComboBox;

	private PresentationModel<Locomotive> presentationModel;
	
	private LocomotivePersistenceIface locomotivePersistence;

	private JButton okButton;

	public LocomotiveConfig(Frame owner, Locomotive myLocomotive) {
		super(owner, "Locomotive Config", true);
		this.presentationModel = new PresentationModel<Locomotive>(myLocomotive);
		initGUI();
	}

	public LocomotiveConfig(JDialog owner, Locomotive myLocomotive) {
		super(owner, "Locomotive Config", true);
		this.presentationModel = new PresentationModel<Locomotive>(myLocomotive);
		initGUI();
	}
	public LocomotiveConfig(JDialog owner, PresentationModel<Locomotive> presentationModel) {
		super(owner, "Locomotive Config", true);
		this.presentationModel = presentationModel;
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
	}

	private void initComponents() {


		nameTextField = BasicComponentFactory.createTextField(presentationModel
				.getModel(Locomotive.PROPERTYNAME_DESCRIPTION));
		nameTextField.setColumns(10);

		descTextField = BasicComponentFactory.createTextField(presentationModel
				.getModel(Locomotive.PROPERTYNAME_NAME));
		descTextField.setColumns(10);

		imageTextField = BasicComponentFactory.createTextField(presentationModel
				.getModel(Locomotive.PROPERTYNAME_IMAGE));
		imageTextField.setColumns(10);

		busTextField = new JSpinner();
		busTextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Locomotive.PROPERTYNAME_BUS), 1, // defaultValue
				0, // minValue
				100, // maxValue
				1)); // step

		addressTextField = new JSpinner();
		addressTextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Locomotive.PROPERTYNAME_ADDRESS), 1, // defaultValue
				0, // minValue
				324, // maxValue
				1)); // step

		
		locomotivePersistence = AdHocRailway.getInstance().getLocomotivePersistence();
		List<LocomotiveType> locomotiveTypes = new ArrayList<LocomotiveType>(locomotivePersistence.getAllLocomotiveTypes());

		ValueModel locomotiveTypeModel = presentationModel
				.getModel(Locomotive.PROPERTYNAME_LOCOMOTIVE_TYPE);
		locomotiveTypeComboBox = BasicComponentFactory
				.createComboBox(new SelectionInList<LocomotiveType>(locomotiveTypes,
						locomotiveTypeModel));
		
		okButton = new JButton(new ApplyChangesAction());
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, pref:grow, 30dlu, right:pref, 3dlu, pref:grow",
				"p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 10dlu,p:grow, 3dlu");
		layout.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		layout.setRowGroups(new int[][] {{3,5,7,9}});
		
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xyw(1, 1, 3));

		builder.addLabel("Name", cc.xy(1, 3));
		builder.add(nameTextField, cc.xy(3, 3));

		builder.addLabel("Description", cc.xy(1, 5));
		builder.add(descTextField, cc.xy(3, 5));

		builder.addLabel("Image", cc.xy(1, 7));
		builder.add(imageTextField, cc.xy(3, 7));

		builder.addLabel("Type", cc.xy(1, 9));
		builder.add(locomotiveTypeComboBox, cc.xy(3, 9));

		builder.addSeparator("Interface", cc.xyw(5, 1, 3));
		
		builder.addLabel("Bus", cc.xy(5, 3));
		builder.add(busTextField, cc.xy(7, 3));

		builder.addLabel("Address", cc.xy(5, 5));
		builder.add(addressTextField, cc.xy(7, 5));

		builder.add(buildButtonBar(), cc.xyw(1, 11, 7));

		add(builder.getPanel());
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {
		
		public ApplyChangesAction() {
			super("OK");
		}
		
		public void actionPerformed(ActionEvent e) {
			okPressed = true;
			LocomotiveConfig.this.setVisible(false);
		}
	}
}
