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

package ch.fork.AdHocRailway.ui.routes.configuration;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.domain.routes.RouteManagerListener;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.ThreeDigitDisplay;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RoutesConfigurationDialog extends JDialog implements
		RouteManagerListener {

	private JList routeGroupList;

	private JButton addRouteGroupButton;

	private JButton removeRouteGroupButton;

	private JList routesList;

	private JButton addRouteButton;

	private JButton removeRouteButton;

	private SelectionInList<RouteGroup> routeGroupModel;
	private JButton okButton;
	protected boolean okPressed;
	private SelectionInList<Route> routesModel;
	public StringBuffer enteredNumberKeys;
	private PanelBuilder builder;
	private RouteGroupConfigPanel routeGroupConfig;
	public ThreeDigitDisplay digitDisplay;

	private ArrayListModel<RouteGroup> routeGroups;
	private ArrayListModel<Route> routes;

	private final RouteManager routePersistence = AdHocRailway.getInstance()
			.getRoutePersistence();

	private JButton editGroupButton;

	public RoutesConfigurationDialog(JFrame parent) {
		super(parent, "Edit Routes", true);
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		routePersistence.addRouteManagerListener(this);
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	private void buildPanel() {
		initComponents();
		initEventHandling();

		FormLayout layout = new FormLayout(
				"pref, 5dlu, pref, 5dlu",
				"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref:grow, 3dlu, pref:grow, 3dlu, pref, 3dlu, pref");
		builder = new PanelBuilder(layout);
		layout.setColumnGroups(new int[][] { { 1, 3 } });
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("Route Groups", cc.xyw(1, 1, 1));

		builder.add(new JScrollPane(routeGroupList), cc.xy(1, 3));
		builder.add(routeGroupConfig, cc.xy(1, 5));
		builder.add(buildRouteGroupButtonBar(), cc.xy(1, 7));

		builder.addSeparator("Routes", cc.xyw(3, 1, 1));

		builder.add(new JScrollPane(routesList), cc.xywh(3, 3, 1, 3));
		builder.add(buildRouteButtonBar(), cc.xy(3, 7));

		builder.add(buildMainButtonBar(), cc.xyw(1, 9, 4));

		add(builder.getPanel());
	}

	private Component buildRouteGroupButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteGroupButton,
				editGroupButton, removeRouteGroupButton);
	}

	private Component buildRouteButtonBar() {
		return ButtonBarFactory.buildCenteredBar(addRouteButton,
				removeRouteButton);
	}

	private Component buildMainButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton);
	}

	private void initComponents() {
		routeGroups = new ArrayListModel<RouteGroup>(
				routePersistence.getAllRouteGroups());
		routeGroupModel = new SelectionInList<RouteGroup>(
				(ListModel) routeGroups);

		routeGroupList = BasicComponentFactory.createList(routeGroupModel);
		routeGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		routeGroupList.setCellRenderer(new RouteGroupListCellRenderer());

		routeGroupConfig = new RouteGroupConfigPanel();

		addRouteGroupButton = new JButton(new AddRouteGroupAction());
		editGroupButton = new JButton(new EditRouteGroupAction());
		removeRouteGroupButton = new JButton(new RemoveRouteGroupAction());

		routes = new ArrayListModel<Route>();
		SelectionInList<Route> routesModel = new SelectionInList<Route>();
		routesModel.setList(routes);
		routesList = new JList();
		routesList.setModel(routesModel);
		routesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		routesList.setCellRenderer(new RouteListCellRenderer());

		addRouteButton = new JButton(new AddRouteAction());
		removeRouteButton = new JButton(new RemoveRouteAction());

		okButton = new JButton("OK",
				ImageTools.createImageIconFromIconSet("ok.png"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okPressed = true;
				setVisible(false);
				routePersistence
						.removeRouteManagerListenerInNextEvent(RoutesConfigurationDialog.this);
			}

		});
	}

	private void initEventHandling() {
		routeGroupList
				.addListSelectionListener(new RouteGroupSelectionHandler());

		routesList.addListSelectionListener(new RouteSelectionHandler());
		routesList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					new EditRouteAction().actionPerformed(null);
				}
			}

		});
	}

	/**
	 * Sets the selected RouteGroup as bean in the details model.
	 */
	private final class RouteGroupSelectionHandler implements
			ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (routeGroupList.getSelectedIndex() == -1) {
				routeGroupList.setSelectedIndex(0);
			}
			RouteGroup selectedGroup = (RouteGroup) routeGroupList
					.getSelectedValue();
			if (selectedGroup == null) {
				return;
			}
			routes.clear();
			routes.addAll(selectedGroup.getRoutes());
			routeGroupConfig.setRouteGroup(selectedGroup);
		}
	}

	/**
	 * Sets the selected Route as bean in the details model.
	 */
	private final class RouteSelectionHandler implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}
			if (routesList.getSelectedIndex() == -1) {
				routesList.setSelectedIndex(0);
			}
			Route selectedRoute = (Route) routesList.getSelectedValue();
			if (selectedRoute == null) {
				return;
			}
		}
	}

	/**
	 * Used to renders RouteGroups in JLists and JComboBoxes. If the combo box
	 * selection is null, an empty text <code>""</code> is rendered.
	 */
	private static final class RouteGroupListCellRenderer extends
			DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);

			RouteGroup group = (RouteGroup) value;
			setText(group == null ? "" : (" " + group.getName()));
			return component;
		}
	}

	/**
	 * Used to renders Route in JLists and JComboBoxes. If the combo box
	 * selection is null, an empty text <code>""</code> is rendered.
	 */
	private static final class RouteListCellRenderer extends
			DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);

			Route route = (Route) value;
			setText(route == null ? "" : (" " + route.getName()));
			return component;
		}
	}

	private class EditRouteGroupAction extends AbstractAction {
		public EditRouteGroupAction() {
			super("Edit Group");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			RouteGroup groupToEdit = routeGroupModel.getSelection();

			routePersistence.updateRouteGroup(groupToEdit);

		}
	}

	private class AddRouteGroupAction extends AbstractAction {

		public AddRouteGroupAction() {
			super("Add Group", ImageTools.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String newRouteGroupName = JOptionPane.showInputDialog(
					RoutesConfigurationDialog.this,
					"Enter the name of the new route group", "Add route group",
					JOptionPane.QUESTION_MESSAGE);

			if (newRouteGroupName == null || newRouteGroupName.equals("")) {
				return;
			}

			RouteGroup newRouteGroup = new RouteGroup();
			newRouteGroup.setName(newRouteGroupName);
			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
				String newAmount = JOptionPane.showInputDialog(
						RoutesConfigurationDialog.this,
						"How many Routes should be in this group?", "10");
				int newOffset = 1;
				for (RouteGroup group : routePersistence.getAllRouteGroups()) {
					newOffset += group.getRouteNumberAmount();
				}
				newRouteGroup.setRouteNumberOffset(newOffset);
				newRouteGroup.setRouteNumberAmount(Integer.parseInt(newAmount));
			}

			routePersistence.addRouteGroup(newRouteGroup);

		}

	}

	private class RemoveRouteGroupAction extends AbstractAction {

		public RemoveRouteGroupAction() {
			super("Remove Group", ImageTools
					.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RouteGroup routeGroupToDelete = routeGroupModel.getSelection();
			if (routeGroupToDelete == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a Route-Group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response = JOptionPane.showConfirmDialog(
					RoutesConfigurationDialog.this,
					"Really remove Route-Group '"
							+ routeGroupToDelete.getName() + "' ?",
					"Remove Route-Group", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				try {
					routePersistence.deleteRouteGroup(routeGroupToDelete);
					routeGroupConfig.setRouteGroup(null);
					routeGroups.remove(routeGroupToDelete);
				} catch (RouteManagerException e1) {
					ExceptionProcessor.getInstance().processException(e1);
				}
			}
		}
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	private class AddRouteAction extends AbstractAction {

		public AddRouteAction() {
			super("Add Route", ImageTools.createImageIconFromIconSet("add.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			RouteGroup selectedRouteGroup = routeGroupModel.getSelection();
			if (selectedRouteGroup == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int nextNumber = 0;
			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
				nextNumber = routePersistence
						.getNextFreeRouteNumberOfGroup(selectedRouteGroup);
				if (nextNumber == -1) {
					JOptionPane.showMessageDialog(
							RoutesConfigurationDialog.this,
							"No more free numbers in this group", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				nextNumber = routePersistence.getNextFreeRouteNumber();
			}

			Route newRoute = createDefaultRoute(selectedRouteGroup, nextNumber);

			RouteConfig routeConfig = new RouteConfig(
					RoutesConfigurationDialog.this, newRoute);
		}

		private Route createDefaultRoute(RouteGroup selectedRouteGroup,
				int nextNumber) {
			Route newRoute = new Route();
			newRoute.setNumber(nextNumber);
			newRoute.setRouteGroup(selectedRouteGroup);
			return newRoute;
		}
	}

	private class EditRouteAction extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {

			// PresentationModel<Turnout> model = new
			// PresentationModel<Turnout>(
			// turnoutModel);
			Route route = (Route) routesList.getSelectedValue();
			PresentationModel<Route> model = new PresentationModel<Route>(route);
			new RouteConfig(RoutesConfigurationDialog.this, model);
		}
	}

	private class RemoveRouteAction extends AbstractAction {

		public RemoveRouteAction() {
			super("Remove Route", ImageTools
					.createImageIconFromIconSet("remove.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Route routeToDelete = routesModel.getSelection();
			if (routeToDelete == null) {
				JOptionPane.showMessageDialog(RoutesConfigurationDialog.this,
						"Please select a route", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			int response = JOptionPane.showConfirmDialog(
					RoutesConfigurationDialog.this, "Really remove Route '"
							+ routeToDelete.getName() + "' ?", "Remove Route",
					JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				routePersistence.removeRoute(routeToDelete);
			}
		}
	}

	@Override
	public void routesUpdated(List<RouteGroup> allRouteGroups) {
	}

	@Override
	public void routeRemoved(Route route) {
		if (route.getRouteGroup().equals(routeGroupModel.getSelection())) {
			routes.remove(route);
		}
	}

	@Override
	public void routeAdded(Route route) {
		if (route.getRouteGroup().equals(routeGroupModel.getSelection())) {
			routes.add(route);
		}
	}

	@Override
	public void routeUpdated(Route route) {
		if (route.getRouteGroup().equals(routeGroupModel.getSelection())) {
			routes.remove(route);
			routes.add(route);
		}
	}

	@Override
	public void routeGroupAdded(RouteGroup routeGroup) {
		routeGroups.add(routeGroup);
	}

	@Override
	public void routeGroupRemoved(RouteGroup routeGroup) {
		if (routeGroupModel.getSelection().equals(routeGroup)) {
			routes.clear();
		}
		routeGroups.remove(routeGroup);

	}

	@Override
	public void routeGroupUpdated(RouteGroup routeGroup) {
		routeGroups.remove(routeGroup);
		routeGroups.add(routeGroup);
	}

	@Override
	public void failure(RouteManagerException routeManagerException) {

	}
}
