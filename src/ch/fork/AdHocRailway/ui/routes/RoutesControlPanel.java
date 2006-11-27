package ch.fork.AdHocRailway.ui.routes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.SortedSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.ui.TableResizer;
import ch.fork.AdHocRailway.ui.switches.canvas.Segment7;

public class RoutesControlPanel extends JPanel {

	private Segment7 seg1;

	private Segment7 seg2;

	private Segment7 seg3;

	private JFrame frame;

	private TableModel routesTableModel;

	private JTable routesTable;

	private JScrollPane scrollPane;

	public RoutesControlPanel(JFrame frame) {
		super();
		this.frame = frame;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout(5, 5));
		JPanel segmentPanelNorth = initSegmentPanel();
		JPanel routesPanel = initRoutesPanel();
		add(segmentPanelNorth, BorderLayout.NORTH);
		add(routesPanel, BorderLayout.CENTER);
	}

	private JPanel initSegmentPanel() {
		JPanel segmentPanelNorth = new JPanel(new FlowLayout(
				FlowLayout.TRAILING, 5, 0));
		seg1 = new Segment7();
		seg2 = new Segment7();
		seg3 = new Segment7();

		segmentPanelNorth.setBackground(new Color(0, 0, 0));
		segmentPanelNorth.add(seg3);
		segmentPanelNorth.add(seg2);
		segmentPanelNorth.add(seg1);
		JPanel p = new JPanel(new BorderLayout());
		p.add(segmentPanelNorth, BorderLayout.WEST);
		return p;
	}

	private JPanel initRoutesPanel() {
		JPanel routesPanel = new JPanel();
		routesTableModel = new RoutesControlTableModel(null);
		routesTable = new JTable(routesTableModel);
		routesTable.setRowHeight(35);
		TableColumn stateColumn = routesTable.getColumnModel().getColumn(1);
		stateColumn.setCellRenderer(
				new RouteStateRenderer());

		TableResizer.adjustColumnWidths(routesTable, 10);
		scrollPane = new JScrollPane(routesTable);
		scrollPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		routesPanel.add(scrollPane);
		return routesPanel;
	}

	public void update(SortedSet<Route> routes) {
		((RoutesControlTableModel) routesTableModel).setRoutes(routes);

		TableResizer.adjustColumnWidths(routesTable, 10);
		scrollPane.setPreferredSize(new Dimension(200, 500));

	}
}