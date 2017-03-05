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

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.LocomotiveImageHelper;
import ch.fork.AdHocRailway.ui.utils.GlobalKeyShortcutHelper;
import ch.fork.AdHocRailway.ui.utils.ImageTools;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.utils.UIConstants;
import ch.fork.AdHocRailway.ui.widgets.ErrorPanel;
import ch.fork.AdHocRailway.ui.widgets.ImagePreviewPanel;
import com.google.common.collect.Lists;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.ArrayListModel;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.Trigger;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import de.dermoba.srcp.model.turnouts.MMTurnout;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

public class LocomotiveConfig extends JDialog implements PropertyChangeListener {


    private final Trigger trigger = new Trigger();
    private LocomotiveContext ctx;
    private boolean createLocomotive;
    private PresentationModel<Locomotive> presentationModel;
    private JTextField nameTextField;
    private JSpinner busSpinner;
    private JSpinner address1Spinner;
    private JSpinner address2Spinner;
    private JTextField descTextField;
    private JTextField imageTextField;
    @SuppressWarnings("rawtypes")
    private JComboBox<LocomotiveType> locomotiveTypeComboBox;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel imageLabel;
    private JPanel imageChoserPanel;
    private JButton chooseImageButton;
    private LocomotiveGroup selectedLocomotiveGroup;
    private JTable functionsTable;
    private ArrayListModel<LocomotiveFunction> functions;
    private ErrorPanel errorPanel;
    private SelectionInList<LocomotiveFunction> functionsModel;
    private JSpinner mfxUuidSpinner;

    public LocomotiveConfig(final LocomotiveContext ctx, final Frame owner,
                            final Locomotive myLocomotive,
                            final LocomotiveGroup selectedLocomotiveGroup, boolean createLocomotive) {
        super(owner, "Locomotive Config", true);
        init(ctx, myLocomotive, selectedLocomotiveGroup, createLocomotive);
    }

    public LocomotiveConfig(final LocomotiveContext ctx, final JDialog owner,
                            final Locomotive myLocomotive,
                            final LocomotiveGroup selectedLocomotiveGroup, boolean createLocomotive) {
        super(owner, "Locomotive Config", true);
        init(ctx, myLocomotive, selectedLocomotiveGroup, createLocomotive);
    }

    private void init(LocomotiveContext ctx, Locomotive myLocomotive, LocomotiveGroup selectedLocomotiveGroup, boolean createLocomotive) {
        this.ctx = ctx;
        this.createLocomotive = createLocomotive;
        this.selectedLocomotiveGroup = selectedLocomotiveGroup;
        this.presentationModel = new PresentationModel<Locomotive>(
                myLocomotive, trigger);
        initGUI();
    }

    private void initGUI() {
        buildPanel();
        initShortcuts();
        nameTextField.requestFocus();
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void initShortcuts() {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_ENTER, 0, new ApplyChangesAction());
    }

    private void initComponents() {

        nameTextField = BasicComponentFactory.createTextField(presentationModel
                .getBufferedModel(Locomotive.PROPERTYNAME_NAME));
        nameTextField.setColumns(30);

        descTextField = BasicComponentFactory.createTextField(presentationModel
                .getBufferedModel(Locomotive.PROPERTYNAME_DESCRIPTION));
        descTextField.setColumns(30);

        imageChoserPanel = new JPanel(new MigLayout("fill"));

        imageTextField = BasicComponentFactory
                .createTextField(presentationModel
                        .getBufferedModel(Locomotive.PROPERTYNAME_IMAGE));
        imageTextField.setColumns(30);

        chooseImageButton = new JButton("Choose...");
        chooseImageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                chooseLocoImage();
            }
        });

        imageChoserPanel.add(imageTextField, "grow");
        imageChoserPanel.add(chooseImageButton);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        imageLabel.setIcon(LocomotiveImageHelper.getLocomotiveIconOriginalSize(presentationModel
                .getBean()));

        busSpinner = new JSpinner();
        busSpinner
                .setModel(SpinnerAdapterFactory.createNumberAdapter(
                        presentationModel
                                .getBufferedModel(Locomotive.PROPERTYNAME_BUS),
                        1, // defaultValue
                        0, // minValue
                        100, // maxValue
                        1
                )); // step

        address1Spinner = new JSpinner();
        address1Spinner.setModel(SpinnerAdapterFactory.createNumberAdapter(
                presentationModel
                        .getBufferedModel(Locomotive.PROPERTYNAME_ADDRESS1), 1, // defaultValue
                0, // minValue
                324, // maxValue
                1
        )); // step

        address2Spinner = new JSpinner();
        address2Spinner.setModel(SpinnerAdapterFactory.createNumberAdapter(
                presentationModel
                        .getBufferedModel(Locomotive.PROPERTYNAME_ADDRESS2), 1, // defaultValue
                0, // minValue
                324, // maxValue
                1
        )); // step


        mfxUuidSpinner = new JSpinner();
        mfxUuidSpinner.setModel(SpinnerAdapterFactory.createNumberAdapter(
                presentationModel
                        .getBufferedModel(Locomotive.PROPERTYNAME_MFX_UUID), 1, // defaultValue
                0, // minValue
                Integer.MAX_VALUE, // maxValue
                1
        )); // step

        final List<LocomotiveType> locomotiveTypes = Arrays
                .asList(LocomotiveType.values());

        final ValueModel locomotiveTypeModel = presentationModel
                .getBufferedModel(Locomotive.PROPERTYNAME_LOCOMOTIVE_TYPE);

        locomotiveTypeComboBox = BasicComponentFactory
                .createComboBox(new SelectionInList<LocomotiveType>(
                        locomotiveTypes, locomotiveTypeModel));
        updateUIBasedOnLocomotiveType(presentationModel.getBean().getType());


        locomotiveTypeComboBox
                .addActionListener(new LocomotiveTypeSelectionListener());
        functions = new ArrayListModel<LocomotiveFunction>(presentationModel
                .getBean().getFunctions());
        functionsTable = new JTable();
        functionsModel = new SelectionInList<LocomotiveFunction>();
        functionsModel.setList(functions);

        functionsTable
                .setModel(new LocomotiveFunctionTableModel(functionsModel));
        functionsTable.getColumnModel().getColumn(0)
                .setCellRenderer(new CenterRenderer());

        errorPanel = new ErrorPanel();

        validate(presentationModel.getBean(), null);
        presentationModel.getBean().addPropertyChangeListener(this);
        okButton = new JButton(new ApplyChangesAction());
        cancelButton = new JButton(new CancelAction());
    }

    private void updateUIBasedOnLocomotiveType(LocomotiveType type) {
        switch (type) {
            case DELTA:
                SwingUtils.enableDisableSpinners(false, mfxUuidSpinner);
                break;
            case DIGITAL:
                SwingUtils.enableDisableSpinners(false, mfxUuidSpinner);
                break;
            case SIMULATED_MFX:
                SwingUtils.enableDisableSpinners(false, mfxUuidSpinner);
                break;
            case MFX:
                SwingUtils.enableDisableSpinners(true, mfxUuidSpinner);
                break;
            case DCC:
                SwingUtils.enableDisableSpinners(false, mfxUuidSpinner);
                break;

        }
    }

    private void buildPanel() {
        initComponents();

        MigLayout manager = new MigLayout();
        setLayout(manager);

        add(new JLabel("Name"));
        add(nameTextField, "w 300!");

        add(new JLabel("Bus"), "gap unrelated");
        add(busSpinner, "w 150!, wrap");

        add(new JLabel("Description"));
        add(descTextField, "w 300!");

        add(new JLabel("Address 1"), "gap unrelated");
        add(address1Spinner, "w 150!, wrap");

        add(new JLabel("Type"));
        add(locomotiveTypeComboBox, "w 150!");

        add(new JLabel("Address 2"), "gap unrelated");
        add(address2Spinner, "w 150!, wrap");

        add(new JLabel("Image"));
        add(chooseImageButton, "w 150!");

        add(new JLabel("mfx Uuid"));
        add(mfxUuidSpinner, "w 150!, wrap");

        add(imageLabel, "align center, span 2");

        add(new JLabel("Functions"), "gap unrelated");
        add(new JScrollPane(functionsTable), "h 200!, w 300!, wrap");

        add(buildButtonBar(), "span 4, align right");

    }

    private JComponent buildButtonBar() {
        return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent event) {
        final Locomotive locomotive = presentationModel.getBean();

        if (event.getPropertyName().equals(
                Locomotive.PROPERTYNAME_LOCOMOTIVE_TYPE)) {
            initializeFunctions(locomotive);

        }

        if (!validate(locomotive, event)) {
            return;
        }
    }

    private void initializeFunctions(final Locomotive locomotive) {

        switch (locomotive.getType()) {
            case DELTA:
                locomotive.setFunctions(LocomotiveFunction.getDeltaFunctions());
                break;
            case DIGITAL:
                locomotive.setFunctions(LocomotiveFunction.getDigitalFunctions());
                break;
            case SIMULATED_MFX:
                locomotive.setFunctions(LocomotiveFunction
                        .getSimulatedMfxFunctions());
                break;
            case MFX:
                locomotive.setFunctions(LocomotiveFunction.getMfxFunctions());
                break;
            case DCC:
                locomotive.setFunctions(LocomotiveFunction.getDccFunctions());
                break;
            default:
                break;

        }

    }

    private boolean validate(final Locomotive locomotive,
                             final PropertyChangeEvent event) {

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
                Locomotive.PROPERTYNAME_ADDRESS1)) {
            boolean busValid = true;

            if (locomotive.getBus() == 0) {
                setSpinnerColor(busSpinner, UIConstants.ERROR_COLOR);
                validate = false;
                busValid = false;
            } else {
                setSpinnerColor(busSpinner, UIConstants.DEFAULT_TEXTFIELD_COLOR);
            }

            boolean addressValid = true;

            if (locomotive.getAddress1() == 0
                    || locomotive.getAddress1() > MMTurnout.MAX_MM_TURNOUT_ADDRESS) {
                setSpinnerColor(address1Spinner, UIConstants.ERROR_COLOR);
                validate = false;
                addressValid = false;
            } else {
                setSpinnerColor(address1Spinner,
                        UIConstants.DEFAULT_TEXTFIELD_COLOR);
            }

            if (busValid && addressValid) {

                final int bus = ((Integer) busSpinner.getValue()).intValue();
                final int address = ((Integer) address1Spinner.getValue())
                        .intValue();
                boolean unique = true;
                for (final Locomotive l : ctx.getLocomotiveManager().getAllLocomotives()) {
                    if (l.getBus() == bus && l.getAddress1() == address
                            && !l.equals(locomotive)) {
                        unique = false;
                    }
                }
                if (!unique) {
                    setSpinnerColor(busSpinner, UIConstants.WARN_COLOR);
                    setSpinnerColor(address1Spinner, UIConstants.WARN_COLOR);
                } else {
                    setSpinnerColor(busSpinner,
                            UIConstants.DEFAULT_TEXTFIELD_COLOR);
                    setSpinnerColor(address1Spinner,
                            UIConstants.DEFAULT_TEXTFIELD_COLOR);
                }
            }
        }

        return validate;
    }

    private void setSpinnerColor(final JSpinner spinner, final Color color) {
        final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
                .getEditor();
        editor.getTextField().setBackground(color);
    }

    public void chooseLocoImage() {
        File previousLocoDir = ctx.getPreviousLocoDir();
        if (previousLocoDir == null) {
            previousLocoDir = new File("locoimages");
        }
        final JFileChooser chooser = new JFileChooser(previousLocoDir);

        final ImagePreviewPanel preview = new ImagePreviewPanel();
        chooser.setAccessory(preview);
        chooser.addPropertyChangeListener(preview);

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "Image Files";
            }

            @Override
            public boolean accept(final File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (StringUtils.endsWithAny(f.getName().toLowerCase(), ".png",
                        ".gif", ".bmp", ".jpg")) {
                    return true;
                }
                return false;
            }
        });

        final int ret = chooser.showOpenDialog(LocomotiveConfig.this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            ctx.setPreviousLocoDir(selectedFile.getParentFile());
            presentationModel.getBean().setImage(
                    selectedFile.getName());
            final String image = presentationModel.getBean().getImage();
            presentationModel.getBean().setImageBase64(LocomotiveImageHelper.getImageBase64(selectedFile));
            if (image != null && !image.isEmpty()) {
                imageLabel.setIcon(LocomotiveImageHelper.getLocomotiveIconOriginalSize(presentationModel.getBean()));
                pack();
            } else {
                imageLabel.setIcon(null);
                pack();
            }
        }
    }

    class CenterRenderer extends DefaultTableCellRenderer {

        protected CenterRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }
    }

    class ApplyChangesAction extends AbstractAction {

        public ApplyChangesAction() {
            super("OK", ImageTools
                    .createImageIconFromIconSet("dialog-ok-apply.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            trigger.triggerCommit();
            final Locomotive locomotive = presentationModel.getBean();
            ctx.getLocomotiveManager()
                    .addLocomotiveManagerListener(new LocomotiveAddListener() {

                        @Override
                        public void locomotiveAdded(final Locomotive locomotive) {
                            success(ctx.getLocomotiveManager(), locomotive);
                        }

                        @Override
                        public void locomotiveUpdated(
                                final Locomotive locomotive) {
                            success(ctx.getLocomotiveManager(), locomotive);
                        }

                        private void success(
                                final LocomotiveManager locomotivePersistence,
                                final Locomotive locomotive) {
                            locomotivePersistence
                                    .removeLocomotiveManagerListenerInNextEvent(this);

                            LocomotiveConfig.this.setVisible(false);
                        }

                        @Override
                        public void failure(
                                final AdHocServiceException serviceException) {
                            errorPanel.setErrorText(serviceException.getMessage());
                        }

                    });
            if (createLocomotive) {
                ctx.getLocomotiveManager().addLocomotiveToGroup(locomotive,
                        selectedLocomotiveGroup);
            } else {
                ctx.getLocomotiveManager().updateLocomotive(locomotive);
            }

        }
    }

    class CancelAction extends AbstractAction {

        public CancelAction() {
            super("Cancel", ImageTools
                    .createImageIconFromIconSet("dialog-cancel.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            trigger.triggerFlush();
            LocomotiveConfig.this.setVisible(false);
        }
    }


    private class LocomotiveTypeSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            final LocomotiveType selectedLocomotiveType = (LocomotiveType) locomotiveTypeComboBox
                    .getSelectedItem();
            updateUIBasedOnLocomotiveType(selectedLocomotiveType);
        }
    }
}
