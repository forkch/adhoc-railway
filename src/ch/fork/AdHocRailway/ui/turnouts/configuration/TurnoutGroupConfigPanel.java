/**
 * description
 * 
 * Version 0.1 12.02.2008
 * 
 * Copyright (C) Siemens Schweiz AG 2008, All Rights Reserved, Confidential
 */
package ch.fork.AdHocRailway.ui.turnouts.configuration;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Benjamin Mueller <benjamin.b.mueller@siemens.com>
 * 
 */
public class TurnoutGroupConfigPanel
		extends JPanel {

	private PresentationModel<TurnoutGroup>	presentationModel;
	private JTextField						turnoutGroupName;
	private JSpinner						turnoutNumberOffset;
	private JSpinner						turnoutNumberAmount;
	private SelectionInList<TurnoutGroup>	turnoutGroupModel;

	public TurnoutGroupConfigPanel(SelectionInList<TurnoutGroup> turnoutGroupModel) {
		this.turnoutGroupModel = turnoutGroupModel;
		buildPanel();
	}

	private void buildPanel() {
		initComponents();
		
		FormLayout layout =
				new FormLayout(
						"right:pref, 3dlu, pref:grow",
						"p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu");
		layout.setColumnGroups(new int[][] { { 1, 3 }});
		layout.setRowGroups(new int[][] { { 1,3,5 } });

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Name", cc.xy(1, 1));
		builder.add(turnoutGroupName, cc.xy(3, 1));
		builder.addLabel("Turnout Number Offset", cc.xy(1, 3));
		builder.add(turnoutNumberOffset, cc.xy(3, 3));
		builder.addLabel("Turnout Amount", cc.xy(1, 5));
		builder.add(turnoutNumberAmount, cc.xy(3, 5));
		
		add(builder.getPanel());
	}

	private void initComponents() {
		turnoutGroupName =
				BasicComponentFactory.createTextField(presentationModel
						.getModel(TurnoutGroup.PROPERTYNAME_NAME));
		turnoutGroupName.setColumns(5);

		turnoutNumberOffset = new JSpinner();
		turnoutNumberOffset
				.setModel(SpinnerAdapterFactory
						.createNumberAdapter(
								presentationModel
										.getModel(TurnoutGroup.PROPERTYNAME_TURNOUT_NUMBER_OFFSET),
								0, // defaultValue
								0, // minValue
								1000, // maxValue
								10)); // step

		turnoutNumberAmount = new JSpinner();
		turnoutNumberAmount
				.setModel(SpinnerAdapterFactory
						.createNumberAdapter(
								presentationModel
										.getModel(TurnoutGroup.PROPERTYNAME_TURNOUT_NUMBER_AMOUNT),
								0, // defaultValue
								0, // minValue
								1000, // maxValue
								10)); // step
	}
}
