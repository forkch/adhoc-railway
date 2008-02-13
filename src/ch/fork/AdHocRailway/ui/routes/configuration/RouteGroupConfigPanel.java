/**
 * description
 * 
 * Version 0.1 12.02.2008
 * 
 * Copyright (C) Siemens Schweiz AG 2008, All Rights Reserved, Confidential
 */
package ch.fork.AdHocRailway.ui.routes.configuration;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import ch.fork.AdHocRailway.domain.routes.RouteGroup;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Benjamin Mueller <benjamin.b.mueller@siemens.com>
 * 
 */
public class RouteGroupConfigPanel extends JPanel {

	private PresentationModel<RouteGroup>	presentationModel;
	private JTextField						routeGroupName;
	private JSpinner						routeNumberOffset;
	private JSpinner						routeNumberAmount;

	public RouteGroupConfigPanel() {
		presentationModel = new PresentationModel<RouteGroup>(new ValueHolder(null, true));
		buildPanel();
	}
	
	public void setTurnoutGroup(RouteGroup group) {
		presentationModel.setBean(group);
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout("right:pref, 3dlu, pref:grow",
				"p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu");
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		layout.setRowGroups(new int[][] { { 1, 3, 5 } });

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addLabel("Name", cc.xy(1, 1));
		builder.add(routeGroupName, cc.xy(3, 1));
		builder.addLabel("Route Number Offset", cc.xy(1, 3));
		builder.add(routeNumberOffset, cc.xy(3, 3));
		builder.addLabel("Route Amount", cc.xy(1, 5));
		builder.add(routeNumberAmount, cc.xy(3, 5));
		

		add(builder.getPanel());
	}
	

	private void initComponents() {
		routeGroupName = BasicComponentFactory
				.createTextField(presentationModel
						.getModel(RouteGroup.PROPERTYNAME_NAME));
		routeGroupName.setColumns(5);

		routeNumberOffset = new JSpinner();
		routeNumberOffset
				.setModel(SpinnerAdapterFactory
						.createNumberAdapter(
								presentationModel
										.getModel(RouteGroup.PROPERTYNAME_ROUTE_NUMBER_OFFSET),
								0, // defaultValue
								0, // minValue
								1000, // maxValue
								10)); // step

		routeNumberAmount = new JSpinner();
		routeNumberAmount
				.setModel(SpinnerAdapterFactory
						.createNumberAdapter(
								presentationModel
										.getModel(RouteGroup.PROPERTYNAME_ROUTE_NUMBER_AMOUNT),
								0, // defaultValue
								0, // minValue
								1000, // maxValue
								10)); // step

	}
}
