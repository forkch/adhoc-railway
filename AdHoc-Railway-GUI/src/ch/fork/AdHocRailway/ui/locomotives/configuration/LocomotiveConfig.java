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

package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ImagePreviewPanel;
import ch.fork.AdHocRailway.ui.ImageTools;
import ch.fork.AdHocRailway.ui.UIConstants;

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

public class LocomotiveConfig extends JDialog implements PropertyChangeListener {
	private boolean okPressed;

	private JTextField nameTextField;

	private JSpinner busTextField;

	private JSpinner addressTextField;

	private JTextField descTextField;

	private JTextField imageTextField;

	private JComboBox locomotiveTypeComboBox;

	private final PresentationModel<Locomotive> presentationModel;

	private JButton okButton;

	private JButton cancelButton;

	private boolean cancelPressed;

	private JLabel imageLabel;

	private JPanel imageChoserPanel;

	private JButton chooseImageButton;

	public LocomotiveConfig(Frame owner, Locomotive myLocomotive) {
		super(owner, "Locomotive Config", true);
		this.presentationModel = new PresentationModel<Locomotive>(myLocomotive);
		initGUI();
	}

	public LocomotiveConfig(JDialog owner, Locomotive myLocomotive) {
		super(owner, "Locomotive Config", true);
		this.presentationModel = new PresentationModel<Locomotive>(myLocomotive);
		initGUI();
	}

	public LocomotiveConfig(JDialog owner,
			PresentationModel<Locomotive> presentationModel) {
		super(owner, "Locomotive Config", true);
		this.presentationModel = presentationModel;
		initGUI();
	}

	private void initGUI() {
		buildPanel();
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	private void initComponents() {

		LocomotiveManager locomotivePersistence = AdHocRailway
				.getInstance().getLocomotivePersistence();
		nameTextField = BasicComponentFactory.createTextField(presentationModel
				.getModel(Locomotive.PROPERTYNAME_NAME));
		nameTextField.setColumns(30);

		descTextField = BasicComponentFactory.createTextField(presentationModel
				.getModel(Locomotive.PROPERTYNAME_DESCRIPTION));
		descTextField.setColumns(30);

		imageChoserPanel = new JPanel(new MigLayout("fill"));

		imageTextField = BasicComponentFactory
				.createTextField(presentationModel
						.getModel(Locomotive.PROPERTYNAME_IMAGE));
		imageTextField.setColumns(30);

		// imageTextField.addMouseListener(new MouseAdapter() {
		//
		// @Override
		// public void mouseClicked(MouseEvent e) {
		//
		// chooseLocoImage();
		//
		// }
		// });

		chooseImageButton = new JButton("Choose...");
		chooseImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				chooseLocoImage();
			}
		});

		imageChoserPanel.add(imageTextField, "grow");
		imageChoserPanel.add(chooseImageButton);

		imageLabel = new JLabel();
		imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		String image = presentationModel.getBean().getImage();

		if (image != null && !image.isEmpty()
				&& new File("locoimages/" + image).exists()) {
			imageLabel.setIcon(ImageTools
					.createImageIconFileSystem("locoimages/" + image));
		}

		busTextField = new JSpinner();
		busTextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Locomotive.PROPERTYNAME_BUS), 1, // defaultValue
				0, // minValue
				100, // maxValue
				1)); // step

		addressTextField = new JSpinner();
		addressTextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
				presentationModel.getModel(Locomotive.PROPERTYNAME_ADDRESS), 1, // defaultValue
				0, // minValue
				324, // maxValue
				1)); // step

		List<LocomotiveType> locomotiveTypes = new ArrayList<LocomotiveType>(
				locomotivePersistence.getAllLocomotiveTypes());

		ValueModel locomotiveTypeModel = presentationModel
				.getModel(Locomotive.PROPERTYNAME_LOCOMOTIVE_TYPE);
		locomotiveTypeComboBox = BasicComponentFactory
				.createComboBox(new SelectionInList<LocomotiveType>(
						locomotiveTypes, locomotiveTypeModel));
		// locomotiveTypeComboBox.setSelectedIndex(0);

		presentationModel.getBean().addPropertyChangeListener(this);
		validate(presentationModel.getBean(), null);
		okButton = new JButton(new ApplyChangesAction());
		cancelButton = new JButton(new CancelAction());
	}

	private void buildPanel() {
		initComponents();

		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, pref:grow, 30dlu, right:pref, 3dlu, pref:grow",
				"p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 10dlu,p:grow");
		layout.setColumnGroups(new int[][] { { 1, 5 }, { 3, 7 } });
		layout.setRowGroups(new int[][] { { 3, 5, 7, 9 } });

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		CellConstraints cc = new CellConstraints();

		builder.addSeparator("General", cc.xyw(1, 1, 3));

		builder.addLabel("Name", cc.xy(1, 3));
		builder.add(nameTextField, cc.xy(3, 3));

		builder.addLabel("Description", cc.xy(1, 5));
		builder.add(descTextField, cc.xy(3, 5));

		builder.addLabel("Type", cc.xy(1, 7));
		builder.add(locomotiveTypeComboBox, cc.xy(3, 7));

		builder.addLabel("Image", cc.xy(1, 9));
		builder.add(chooseImageButton, cc.xy(3, 9));

		builder.add(imageLabel, cc.xyw(1, 11, 3));

		builder.addSeparator("Interface", cc.xyw(5, 1, 3));

		builder.addLabel("Bus", cc.xy(5, 3));
		builder.add(busTextField, cc.xy(7, 3));

		builder.addLabel("Address", cc.xy(5, 5));
		builder.add(addressTextField, cc.xy(7, 5));

		builder.add(buildButtonBar(), cc.xyw(1, 11, 7));

		add(builder.getPanel());
	}

	private JComponent buildButtonBar() {
		return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	class ApplyChangesAction extends AbstractAction {

		public ApplyChangesAction() {
			super("OK", ImageTools.createImageIconFromIconSet("ok.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Locomotive locomotive = presentationModel.getBean();
			LocomotiveManager locomotivePersistence = AdHocRailway
					.getInstance().getLocomotivePersistence();
			if (locomotive.getId() == 0) {
				locomotivePersistence.addLocomotive(locomotive);
			} else {
				locomotivePersistence.updateLocomotive(locomotive);
			}

			okPressed = true;
			LocomotiveConfig.this.setVisible(false);

			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.AUTOSAVE))
				AdHocRailway.getInstance().saveActualFile();
		}
	}

	class CancelAction extends AbstractAction {

		public CancelAction() {
			super("Cancel", ImageTools.createImageIconFromIconSet("cancel.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Locomotive locomotive = presentationModel.getBean();
			locomotive.removePropertyChangeListener(LocomotiveConfig.this);
			okPressed = false;
			cancelPressed = true;
			LocomotiveConfig.this.setVisible(false);
		}
	}

	public boolean isCancelPressed() {
		return cancelPressed;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Locomotive locomotive = presentationModel.getBean();
		if (!validate(locomotive, event))
			return;
	}

	private boolean validate(Locomotive locomotive, PropertyChangeEvent event) {

		LocomotiveManager locomotivePersistence = AdHocRailway
				.getInstance().getLocomotivePersistence();
		boolean validate = true;
		if (event == null
				|| event.getPropertyName().equals(Locomotive.PROPERTYNAME_NAME)) {
			if (locomotive.getName() == null || locomotive.getName().equals("")) {
				validate = false;
				nameTextField.setBackground(UIConstants.ERROR_COLOR);
			} else {
				nameTextField
						.setBackground(UIConstants.DEFAULT_TEXTFIELD_COLOR);
			}
		}
		if (event == null
				|| event.getPropertyName().equals(Locomotive.PROPERTYNAME_BUS)
				|| event.getPropertyName().equals(
						Locomotive.PROPERTYNAME_ADDRESS)) {
			boolean busValid = true;

			if (locomotive.getBus() == 0) {
				setSpinnerColor(busTextField, UIConstants.ERROR_COLOR);
				validate = false;
				busValid = false;
			} else {
				setSpinnerColor(busTextField,
						UIConstants.DEFAULT_TEXTFIELD_COLOR);
			}

			boolean addressValid = true;

			if (locomotive.getAddress() == 0
					|| locomotive.getAddress() > MMTurnout.MAX_MM_TURNOUT_ADDRESS) {
				setSpinnerColor(addressTextField, UIConstants.ERROR_COLOR);
				validate = false;
				addressValid = false;
			} else {
				setSpinnerColor(addressTextField,
						UIConstants.DEFAULT_TEXTFIELD_COLOR);
			}

			if (busValid && addressValid) {

				int bus = ((Integer) busTextField.getValue()).intValue();
				int address = ((Integer) addressTextField.getValue())
						.intValue();
				boolean unique = true;
				for (Locomotive l : locomotivePersistence.getAllLocomotives()) {
					if (l.getBus() == bus && l.getAddress() == address
							&& !l.equals(locomotive))
						unique = false;
				}
				if (!unique) {
					setSpinnerColor(busTextField, UIConstants.WARN_COLOR);
					setSpinnerColor(addressTextField, UIConstants.WARN_COLOR);
				} else {
					setSpinnerColor(busTextField,
							UIConstants.DEFAULT_TEXTFIELD_COLOR);
					setSpinnerColor(addressTextField,
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

	/**
	 * 
	 */
	public void chooseLocoImage() {
		JFileChooser chooser = new JFileChooser("locoimages");

		ImagePreviewPanel preview = new ImagePreviewPanel();
		chooser.setAccessory(preview);
		chooser.addPropertyChangeListener(preview);

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return "*.png";
			}

			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				if (f.getName().matches(".*\\.png"))
					return true;
				return false;
			}
		});

		int ret = chooser.showOpenDialog(LocomotiveConfig.this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			presentationModel.getBean().setImage(
					chooser.getSelectedFile().getName());
			String image = presentationModel.getBean().getImage();

			if (image != null && !image.isEmpty()
					&& new File("locoimages/" + image).exists()) {
				imageLabel.setIcon(ImageTools
						.createImageIconFileSystem("locoimages/" + image));
				pack();
			} else {
				imageLabel.setIcon(null);
				pack();
			}
		}
	}
}
