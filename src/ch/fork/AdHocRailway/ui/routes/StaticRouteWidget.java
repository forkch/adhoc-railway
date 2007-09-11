package ch.fork.AdHocRailway.ui.routes;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.routes.RouteOld;

public class StaticRouteWidget extends JPanel {

	private RouteOld route;

	private JLabel nameLabel;

	private JLabel iconLabel;

	private Icon routeStopIcon;

	private Icon routeStartIcon;

	private JLabel numberLabel;

	public StaticRouteWidget(RouteOld route) {
		this.route = route;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createLineBorder(Color.GRAY));

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.LINE_AXIS));
		routeStopIcon = new ImageIcon(ClassLoader
				.getSystemResource("routes/route_stop.png"));
		routeStartIcon = new ImageIcon(ClassLoader
				.getSystemResource("routes/route_start.png"));
		numberLabel = new JLabel("" + route.getNumber());
		// numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
		nameLabel = new JLabel(route.getName());
		switch (route.getRouteState()) {
		case ENABLED:
			iconLabel = new JLabel(routeStartIcon);
			break;
		case DISABLED:
			iconLabel = new JLabel(routeStopIcon);
			break;
		}

		northPanel.add(Box.createHorizontalStrut(5));
		northPanel.add(numberLabel);
		northPanel.add(Box.createHorizontalStrut(5));
		northPanel.add(nameLabel);
		northPanel.add(Box.createGlue());
		northPanel.add(Box.createHorizontalStrut(5));
		northPanel.add(iconLabel);
		northPanel.add(Box.createHorizontalStrut(5));

		add(northPanel);
	}

	public RouteOld getRoute() {
		return route;
	}
}