/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutOrientation;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.TutorialUtils;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.dermoba.srcp.model.turnouts.MMTurnout;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;

public class TurnoutConfig extends JDialog implements PropertyChangeListener {
	private boolean						okPressed;
	private boolean						cancelPressed;
	private JSpinner					numberTextField;
	private JTextField					descTextField;
	private JSpinner					bus1TextField;
	private JSpinner					bus2TextField;
	private JSpinner					address1TextField;
	private JSpinner					address2TextField;
	private JCheckBox					switched1Checkbox;
	private JCheckBox					switched2Checkbox;
	private JComboBox					turnoutTypeComboBox;
	private JComboBox					turnoutDefaultStateComboBox;
	private JComboBox					turnoutOrientationComboBox;
	private Set<Integer>				usedTurnoutNumbers;

	private TurnoutPersistenceIface		turnoutPersistence	= AdHocRailway
																	.getInstance()
																	.getTurnoutPersistence();

	private PresentationModel<Turnout>	presentationModel;
	private JButton						okButton;
	private JButton						cancelButton;
	private TurnoutWidget				testTurnoutWidget;
	private PanelBuilder				builder;

	public TurnoutConfig(JDialog owner, Turnout myTurnout) {
		this(owner, new PresentationModel<Turnout>(myTurnout));
	}

	public TurnoutConfig(Frame owner, Turnout myTurnout) {
		super(owner, "Turnout Config", true);
		this.presentationModel = new PresentationModel<Turnout>(myTurnout);
		initGUI();
	}

	public TurnoutConfig(JDialog owner,
			PresentationModel<Turnout> presentationModel) {
		super(owner, "Turnout Config", true);
		this.presentationModel = presentationModel;
		initGUI();
	}

	public TurnoutConfig(Frame owner,
			PresentationModel<Turnout> presentationModel) {
		super(owner, "Turnout Config", true);
		this.presentationModel = presentationModel;
		initGUI();
	}

	private void initGUI() {
		usedTurnoutNumbers = turnoutPersistence.getUsedTurnoutNumbers();
		usedTurnoutNumbers.remove(presentationModel.getBean().getNumber());
		buildPanel();
		pack();
		TutorialUtils.locateOnOpticalScreenCenter(this);
		setVisible(true);
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
				.createComboBox(new SelectionInList<SRCPTurnoutState>(
						new SRCPTurnoutState[] { SRCPTurnoutState.STRAIGHT,
								SRCPTurnoutState.LEFT }, defaultStateModel));
		turnoutDefaultStateComboBox
				.setRenderer(new TurnoutDefaultStateComboBoxCellRenderer());

		ValueModel orientationModel = presentationModel
				.getModel(Turnout.PROPERTYNAME_ORIENTATION);
		turnoutOrientationComboBox = BasicComponentFactory
				.createComboBox(new SelectionInList<TurnoutOrientation>(
						TurnoutOrientation.values(), orientationModel));

		testTurnoutWidget = new TurnoutWidget(presentationModel.getBean(), true);
		if (!isTurnoutReadyToTest(presentationModel.getBean()))
			testTurnoutWidget.setEnabled(false);

		okButton = new JButton(new ApplyChangesAction());
		cancelButton = new JButton(new CancelAction());
		presentationModel.getBean().addPropertyChangeListener(this);
		validate(presentationModel.getBean());
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, pref:grow, 30dlu, right:pref, 3dlu, pref:grow, 3dlu,pref:grow, 30dlu, pref",
				"p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu, p:grow, 3dlu, p:grow, 10dlu,p:grow");
		layout.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		layout.setRowGroups(new int[][] { { 3, 5, 7, 9, 11 } });

		builder = new PanelBuilder(layout);
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

		builder.addSeparator("Test", cc.xy(11, 1));
		builder.add(testTurnoutWidget, cc.xywh(11, 3, 1, 9));

		builder.add(buildButtonBar(), cc.xyw(1, 13, 11));

		add(builder.getPanel());
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Turnout turnout = presentationModel.getBean();
		if (!validate(turnout))
			return;
		if (!isTurnoutReadyToTest(turnout)) {
			testTurnoutWidget.setEnabled(false);
			return;
		}
		testTurnoutWidget.setEnabled(true);
		repaint();
	}

	private boolean validate(Turnout turnout) {
		boolean validate = true;
		if (turnout.getNumber() == 0
				|| usedTurnoutNumbers.contains(turnout.getNumber())) {
			setSpinnerColor(numberTextField, UIConstants.ERROR_COLOR);
			validate = false;
			okButton.setEnabled(false);
		} else {
			setSpinnerColor(numberTextField,
					UIConstants.DEFAULT_TEXTFIELD_COLOR);
			okButton.setEnabled(true);
		}
		boolean bus1Valid = true;
		if (turnout.getBus1() == 0) {
			setSpinnerColor(bus1TextField, UIConstants.ERROR_COLOR);
			validate = false;
			bus1Valid = false;
		} else {
			setSpinnerColor(bus1TextField, UIConstants.DEFAULT_TEXTFIELD_COLOR);
		}

		boolean address1Valid = true;
		if (turnout.getAddress1() == 0
				|| turnout.getAddress1() > MMTurnout.MAX_MM_TURNOUT_ADDRESS) {
			setSpinnerColor(address1TextField, UIConstants.ERROR_COLOR);
			validate = false;
			address1Valid = false;
		} else {
			setSpinnerColor(address1TextField,
					UIConstants.DEFAULT_TEXTFIELD_COLOR);
		}

		if (bus1Valid && address1Valid) {

			int bus1 = ((Integer) bus1TextField.getValue()).intValue();
			int address1 = ((Integer) address1TextField.getValue()).intValue();
			boolean unique1 = true;
			for (Turnout t : turnoutPersistence.getAllTurnouts()) {
				if (t.getBus1() == bus1 && t.getAddress1() == address1
						&& !t.equals(turnout))
					unique1 = false;
			}
			if (!unique1) {
				setSpinnerColor(bus1TextField, UIConstants.WARN_COLOR);
				setSpinnerColor(address1TextField, UIConstants.WARN_COLOR);
			} else {
				setSpinnerColor(bus1TextField,
						UIConstants.DEFAULT_TEXTFIELD_COLOR);
				setSpinnerColor(address1TextField,
						UIConstants.DEFAULT_TEXTFIELD_COLOR);
			}
		}

		if (turnout.isThreeWay()) {
			boolean bus2Valid = true;
			if (turnout.getBus2() == 0) {
				setSpinnerColor(bus2TextField, UIConstants.ERROR_COLOR);
				validate = false;
				bus2Valid = false;
			} else {
				setSpinnerColor(bus2TextField,
						UIConstants.DEFAULT_TEXTFIELD_COLOR);
			}
			boolean address2Valid = true;
			if (turnout.getAddress2() == 0
					|| turnout.getAddress2() > MMTurnout.MAX_MM_TURNOUT_ADDRESS) {
				setSpinnerColor(address2TextField, UIConstants.ERROR_COLOR);
				validate = false;
				address2Valid = false;
			} else {
				setSpinnerColor(address2TextField,
						UIConstants.DEFAULT_TEXTFIELD_COLOR);
			}
			if (bus2Valid && address2Valid) {
				int bus2 = ((Integer) bus2TextField.getValue()).intValue();
				int address2 = ((Integer) address2TextField.getValue())
						.intValue();
				boolean unique2 = true;
				for (Turnout t : turnoutPersistence.getAllTurnouts()) {
					if (t.getBus2() == bus2 && t.getAddress2() == address2
							&& !t.equals(turnout))
						unique2 = false;
				}
				if (!unique2) {
					setSpinnerColor(bus2TextField, UIConstants.WARN_COLOR);
					setSpinnerColor(address2TextField, UIConstants.WARN_COLOR);
				} else {
					setSpinnerColor(bus2TextField,
							UIConstants.DEFAULT_TEXTFIELD_COLOR);
					setSpinnerColor(address2TextField,
							UIConstants.DEFAULT_TEXTFIELD_COLOR);
				}
			}
		}
		return validate;
	}

	private void setSpinnerColor(JSpinner spinner, Color color) {
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
				.getEditor();
		editor.getTextField().setBackground(color);
	}

	private boolean isTurnoutReadyToTest(Turnout turnout) {
		if (turnout.getAddress1() == 0 || turnout.getBus1() == 0) {
			return false;
		}
		if (turnout.isThreeWay()) {
			if (turnout.getAddress2() == 0 || turnout.getBus2() == 0) {
				return false;
			}
		}

		return true;
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {

		public ApplyChangesAction() {
			super("OK");
		}

		public void actionPerformed(ActionEvent e) {

			Turnout turnout = presentationModel.getBean();
			if (turnout.getId() == 0) {
				turnoutPersistence.addTurnout(turnout);
			} else {
				turnoutPersistence.updateTurnout(turnout);
			}
			okPressed = true;
			turnout.removePropertyChangeListener(TurnoutConfig.this);
			TurnoutConfig.this.setVisible(false);

		}
	}

	class CancelAction extends AbstractAction {

		public CancelAction() {
			super("Cancel");
		}

		public void actionPerformed(ActionEvent e) {
			Turnout turnout = presentationModel.getBean();
			turnout.removePropertyChangeListener(TurnoutConfig.this);
			okPressed = false;
			cancelPressed = true;
			TurnoutConfig.this.setVisible(false);
		}
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

}
