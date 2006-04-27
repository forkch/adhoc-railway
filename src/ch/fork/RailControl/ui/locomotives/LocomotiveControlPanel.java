package ch.fork.RailControl.ui.locomotives;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JPanel;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveControl;

public class LocomotiveControlPanel extends JPanel {
	public LocomotiveControlPanel() {
		super();
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		setLayout(controlPanelLayout);
		for(int i = 0; i < Preferences.getInstance().getLocomotiveControlNumber(); i++) {
			LocomotiveWidget w = new LocomotiveWidget();
			add(w);
		}
	}
	
	public void update(List<Locomotive> locomotives) {
		removeAll();
		for(int i = 0; i < Preferences.getInstance().getLocomotiveControlNumber(); i++) {
			LocomotiveWidget w = new LocomotiveWidget();
			w.registerLocomotives(locomotives);
			LocomotiveControl.getInstance().addLocomotiveChangeListener(w);
			add(w);
		}
		revalidate();
		repaint();
	}
}
