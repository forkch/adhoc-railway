package ch.fork.RailControl.ui.locomotives;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.LocomotiveControl;
import ch.fork.RailControl.domain.locomotives.exception.LocomotiveException;
import ch.fork.RailControl.ui.ExceptionProcessor;

public class LocomotiveControlPanel extends JPanel {

	private int[][] keyBindings = new int[][] {
			{ KeyEvent.VK_A, KeyEvent.VK_Z, KeyEvent.VK_Q },
			{ KeyEvent.VK_S, KeyEvent.VK_X, KeyEvent.VK_W },
			{ KeyEvent.VK_D, KeyEvent.VK_C, KeyEvent.VK_E },
			{ KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_R },
			{ KeyEvent.VK_G, KeyEvent.VK_B, KeyEvent.VK_T }, 
			{ KeyEvent.VK_H, KeyEvent.VK_N, KeyEvent.VK_Y },
			{ KeyEvent.VK_J, KeyEvent.VK_M, KeyEvent.VK_U },
			{ KeyEvent.VK_K, KeyEvent.VK_COMMA, KeyEvent.VK_I },
			{ KeyEvent.VK_L, KeyEvent.VK_DECIMAL, KeyEvent.VK_O },	
			{ KeyEvent.VK_COLON, KeyEvent.VK_MINUS, KeyEvent.VK_P }
	};

	private List<LocomotiveWidget> locomotiveWidgets;
	
	public LocomotiveControlPanel() {
		super();
		locomotiveWidgets = new ArrayList<LocomotiveWidget>();
		FlowLayout controlPanelLayout = new FlowLayout(FlowLayout.LEFT, 10, 0);
		setLayout(controlPanelLayout);
		for (int i = 0; i < Preferences.getInstance()
				.getLocomotiveControlNumber(); i++) {
			LocomotiveWidget w = new LocomotiveWidget(keyBindings[i][0],
					keyBindings[i][1], keyBindings[i][2]);
			add(w);
		}
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "stop_all");
		this.getActionMap().put("stop_all", new LocomotiveStopAction());
		
	}

	public void update(List<Locomotive> locomotives) {
		removeAll();
		locomotiveWidgets.clear();
		for (int i = 0; i < Preferences.getInstance()
				.getLocomotiveControlNumber(); i++) {
			LocomotiveWidget w = new LocomotiveWidget(keyBindings[i][0],
					keyBindings[i][1], keyBindings[i][2]);
			w.registerLocomotives(locomotives);
			LocomotiveControl.getInstance().addLocomotiveChangeListener(w);
			add(w);
			locomotiveWidgets.add(w);
		}
		revalidate();
		repaint();
	}

	private class LocomotiveStopAction extends AbstractAction {
		
		public void actionPerformed(ActionEvent e) {
			try {
				for(LocomotiveWidget widget : locomotiveWidgets) {
					Locomotive myLocomotive = widget.getMyLocomotive();
					if (myLocomotive.isInitialized() != true) {
						myLocomotive.init();
					}
					LocomotiveControl.getInstance().setSpeed(myLocomotive, 0);
				}
				
			} catch (LocomotiveException e3) {
				ExceptionProcessor.getInstance().processException(e3);
			}
		}
	}

}
