/*------------------------------------------------------------------------
 * 
 * <./ui/switches/configuration/SwitchConfig.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:59:11 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id:TurnoutConfig.java 130 2008-02-01 20:23:34Z fork_ch $
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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import ch.fork.AdHocRailway.domain.turnouts.HibernateTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
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

public class TurnoutConfig extends JDialog {
	private boolean okPressed;
	private boolean cancelPressed;
	private boolean visible;
	private JSpinner numberTextField;
	private JTextField descTextField;
	private JSpinner bus1TextField;
	private JSpinner bus2TextField;
	private JSpinner address1TextField;
	private JSpinner address2TextField;
	private JCheckBox switched1Checkbox;
	private JCheckBox switched2Checkbox;
	private JComboBox turnoutTypeComboBox;
	private JComboBox turnoutDefaultStateComboBox;
	private JComboBox turnoutOrientationComboBox;

	private TurnoutPersistenceIface turnoutPersistence = HibernateTurnoutPersistence
			.getInstance();

	private PresentationModel<Turnout> presentationModel;
	private JButton okButton;
	private JButton cancelButton;

	public TurnoutConfig(Frame owner, Turnout myTurnout) {
		this(owner, myTurnout, true);
	}

	public TurnoutConfig(JDialog owner, Turnout myTurnout) {
		this(owner, new PresentationModel<Turnout>(myTurnout), true);
	}

	public TurnoutConfig(JDialog owner,
			PresentationModel<Turnout> presentationModel) {
		this(owner, presentationModel, true);
	}

	public TurnoutConfig(Frame owner, Turnout myTurnout, boolean visible) {
		super(owner, "Turnout Config", true);
		this.visible = visible;
		this.presentationModel = new PresentationModel<Turnout>(myTurnout);
		initGUI();
	}

	public TurnoutConfig(JDialog owner,
			PresentationModel<Turnout> presentationModel, boolean visible) {
		super(owner, "Turnout Config", true);
		this.visible = visible;
		this.presentationModel = presentationModel;
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(visible);
	}

	private void initComponents() {
		numberTextField = new JSpinner();
		numberTextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Turnout.PROPERTYNAME_NUMBER), 1, // defaultValue
				0, // minValue
				1000, // maxValue
				1)); // step

		descTextField = BasicComponentFactory.createTextField(presentationModel
				.getModel(Turnout.PROPERTYNAME_DESCRIPTION));
		descTextField.setColumns(5);

		bus1TextField = new JSpinner();
		bus1TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Turnout.PROPERTYNAME_BUS1), 1, // defaultValue
				0, // minValue
				100, // maxValue
				1)); // step

		address1TextField = new JSpinner();
		address1TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Turnout.PROPERTYNAME_ADDRESS1), 1, // defaultValue
				0, // minValue
				324, // maxValue
				1)); // step

		bus2TextField = new JSpinner();
		bus2TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Turnout.PROPERTYNAME_BUS2), 1, // defaultValue
				0, // minValue
				100, // maxValue
				1)); // step

		address2TextField = new JSpinner();
		address2TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Turnout.PROPERTYNAME_ADDRESS2), 1, // defaultValue
				0, // minValue
				324, // maxValue
				1)); // step
		switched1Checkbox = BasicComponentFactory.createCheckBox(
				presentationModel
						.getModel(Turnout.PROPERTYNAME_ADDRESS1_SWITCHED),
				"Inverted");

		switched2Checkbox = BasicComponentFactory.createCheckBox(
				presentationModel
						.getModel(Turnout.PROPERTYNAME_ADDRESS2_SWITCHED),
				"Inverted");

		List<TurnoutType> turnoutTypes = new ArrayList<TurnoutType>(
				turnoutPersistence.getAllTurnoutTypes());

		ValueModel turnoutTypeModel = presentationModel
				.getModel(Turnout.PROPERTYNAME_TURNOUT_TYPE);
		turnoutTypeComboBox = BasicComponentFactory
				.createComboBox(new SelectionInList<TurnoutType>(turnoutTypes,
						turnoutTypeModel));
		turnoutTypeComboBox.setRenderer(new TurnoutTypeComboBoxCellRenderer());

		ValueModel defaultStateModel = presentationModel
				.getModel(Turnout.PROPERTYNAME_DEFAULT_STATE);
		turnoutDefaultStateComboBox = BasicComponentFactory
				.createComboBox(new SelectionInList<TurnoutState>(
						new TurnoutState[] { TurnoutState.STRAIGHT,
								TurnoutState.LEFT }, defaultStateModel));
		turnoutDefaultStateComboBox
				.setRenderer(new TurnoutDefaultStateComboBoxCellRenderer());

		ValueModel orientationModel = presentationModel
				.getModel(Turnout.PROPERTYNAME_ORIENTATION);
		turnoutOrientationComboBox = BasicComponentFactory
				.createComboBox(new SelectionInList<TurnoutOrientation>(
						TurnoutOrientation.values(), orientationModel));

		okButton = new JButton(new ApplyChangesAction());
		cancelButton = new JButton(new CancelAction());
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, pref:grow, 30dlu, right:pref, 3dlu, pref:grow, 3dlu,pref:grow",
				"p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu, p:grow, 3dlu, p:grow, 10dlu,p:grow, 3dlu");
		layout.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		layout.setRowGroups(new int[][] { { 3, 5, 7, 9, 11 } });

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xyw(1, 1, 3));

		builder.addLabel("Number", cc.xy(1, 3));
		builder.add(numberTextField, cc.xy(3, 3));

		builder.addLabel("Description", cc.xy(1, 5));
		builder.add(descTextField, cc.xy(3, 5));

		builder.addLabel("Type", cc.xy(1, 7));
		builder.add(turnoutTypeComboBox, cc.xy(3, 7));

		builder.addLabel("Default State", cc.xy(1, 9));
		builder.add(turnoutDefaultStateComboBox, cc.xy(3, 9));

		builder.addLabel("Orientation", cc.xy(1, 11));
		builder.add(turnoutOrientationComboBox, cc.xy(3, 11));

		builder.addSeparator("Interface", cc.xyw(5, 1, 5));
		builder.addLabel("Bus 1", cc.xy(5, 3));
		builder.add(bus1TextField, cc.xy(7, 3));

		builder.addLabel("Address 1", cc.xy(5, 5));
		builder.add(address1TextField, cc.xy(7, 5));

		builder.addLabel("Bus 2", cc.xy(5, 7));
		builder.add(bus2TextField, cc.xy(7, 7));

		builder.addLabel("Address 2", cc.xy(5, 9));
		builder.add(address2TextField, cc.xy(7, 9));

		builder.add(switched1Checkbox, cc.xy(9, 5));

		builder.add(switched2Checkbox, cc.xy(9, 9));

		builder.add(buildButtonBar(), cc.xyw(1, 13, 9));

		add(builder.getPanel());
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {

		public ApplyChangesAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {

			try {
				if (presentationModel.getBean().getId() == 0) {
					HibernateTurnoutPersistence.getInstance().addTurnout(
							presentationModel.getBean());
				} else {
					HibernateTurnoutPersistence.getInstance().updateTurnout(
							presentationModel.getBean());
				}
				okPressed = true;
				TurnoutConfig.this.setVisible(false);
			} catch (TurnoutPersistenceException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}

		}
	}

	class CancelAction extends AbstractAction {

		public CancelAction() {
			super("Cancel");
		}

		public void actionPerformed(ActionEvent e) {

			okPressed = false;
			cancelPressed = true;
			TurnoutConfig.this.setVisible(false);
		}
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}
}
