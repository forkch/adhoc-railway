package ch.fork.AdHocRailway.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ErrorPanel extends JPanel {

	private JTextArea errorTextArea;
	private JLabel iconLabel;
	private Color defaultColor;

	public ErrorPanel() {
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout(5, 5));

		errorTextArea = new JTextArea(2,30);
		errorTextArea.setEditable(false);
		errorTextArea.setForeground(Color.BLACK);
		defaultColor = errorTextArea.getBackground();
		errorTextArea.setFont(new Font("Dialog", Font.BOLD, 12));
		
		iconLabel = new JLabel("");
		addMouseListener(new ErrorConfirmAction());
		errorTextArea.addMouseListener(new ErrorConfirmAction());

		add(iconLabel, BorderLayout.WEST);
		add(errorTextArea, BorderLayout.CENTER);
	}

	public void setErrorTextIcon(String text, Icon icon) {
		iconLabel.setIcon(icon);
		iconLabel.setBackground(Color.RED);
		setErrorText(text);
		
	}
	public void setErrorText(String text) {
		errorTextArea.setText(text);
		errorTextArea.setBackground(Color.RED);
		revalidate();
		repaint();
	}

	private class ErrorConfirmAction extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {

			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				errorTextArea.setBackground(defaultColor);
				errorTextArea.setText("");
				iconLabel.setIcon(null);
			}
		}
	}
}
