package ch.fork.RailControl.ui.locomotives;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveControl;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;

public class LocomotiveControlPanel extends JPanel {

	private int[][] keyBindings = new int[][] {
			{ KeyEvent.VK_A, KeyEvent.VK_Z, KeyEvent.VK_Q },
			{ KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_W },
			{ KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_E },
			{ KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_R },
			{ KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_T }, };

	public LocomotiveControlPanel() {
		super();
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		setLayout(controlPanelLayout);
		for (int i = 0; i < Preferences.getInstance()
				.getLocomotiveControlNumber(); i++) {
			LocomotiveWidget w = new LocomotiveWidget(keyBindings[i][0],
					keyBindings[i][1], keyBindings[i][2]);
			add(w);
		}
	}

	public void update(List<Locomotive> locomotives) {
		removeAll();
		for (int i = 0; i < Preferences.getInstance()
				.getLocomotiveControlNumber(); i++) {
			LocomotiveWidget w = new LocomotiveWidget(keyBindings[i][0],
					keyBindings[i][1], keyBindings[i][2]);
			w.registerLocomotives(locomotives);
			LocomotiveControl.getInstance().addLocomotiveChangeListener(w);
			add(w);
		}
		revalidate();
		repaint();
	}

}
