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

package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.*;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.turnouts.TurnoutWidget;
import ch.fork.AdHocRailway.ui.utils.GlobalKeyShortcutHelper;
import ch.fork.AdHocRailway.ui.utils.ImageTools;
import ch.fork.AdHocRailway.ui.utils.SwingUtils;
import ch.fork.AdHocRailway.ui.utils.UIConstants;
import ch.fork.AdHocRailway.ui.widgets.ErrorPanel;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.BufferedValueModel;
import com.jgoodies.binding.value.Trigger;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

public class TurnoutConfig extends JDialog {
    private final Trigger trigger = new Trigger();
    private TurnoutGroup selectedTurnoutGroup;
    private PresentationModel<Turnout> presentationModel;
    private Turnout testTurnout;
    private TurnoutManager turnoutManager;
    private TurnoutContext ctx;
    private boolean createTurnout;
    private JSpinner numberTextField;
    private JTextField descTextField;
    private JSpinner bus1TextField;
    private JSpinner bus2TextField;
    private JSpinner address1TextField;
    private JSpinner address2TextField;
    private JCheckBox switched1Checkbox;
    private JCheckBox switched2Checkbox;
    private JComboBox<?> turnoutTypeComboBox;
    private JComboBox<?> turnoutDefaultStateComboBox;
    private JComboBox<?> turnoutOrientationComboBox;
    private JButton okButton;
    private JButton cancelButton;
    private TurnoutWidget testTurnoutWidget;
    private PanelBuilder builder;
    private List<Turnout> allTurnouts;
    private ErrorPanel errorPanel;
    private BufferedValueModel numberModel;
    private BufferedValueModel descriptionModel;
    private BufferedValueModel bus1Model;
    private BufferedValueModel address1Model;
    private BufferedValueModel bus2Model;
    private BufferedValueModel address2Model;
    private BufferedValueModel swiched1Model;
    private BufferedValueModel switched2Model;
    private BufferedValueModel turnoutTypeModel;
    private BufferedValueModel defaultStateModel;
    private BufferedValueModel orientationModel;

    public TurnoutConfig(final JDialog owner, final TurnoutContext ctx,
                         final Turnout myTurnout, final TurnoutGroup selectedTurnoutGroup, boolean createTurnout) {
        super(owner, "Turnout Config", true);
        init(ctx, myTurnout, selectedTurnoutGroup, createTurnout);
    }

    public TurnoutConfig(final Frame owner, final TurnoutContext ctx,
                         final Turnout myTurnout, final TurnoutGroup selectedTurnoutGroup, boolean createTurnout) {
        super(owner, "Turnout Config", true);

        init(ctx, myTurnout, selectedTurnoutGroup, createTurnout);
    }

    private void init(final TurnoutContext ctx, final Turnout myTurnout, final TurnoutGroup selectedTurnoutGroup, boolean createTurnout) {
        this.ctx = ctx;
        this.createTurnout = createTurnout;

        turnoutManager = ctx.getTurnoutManager();
        testTurnout = TurnoutHelper.copyTurnout(myTurnout);
        this.presentationModel = new PresentationModel<Turnout>(myTurnout,
                trigger);
        this.selectedTurnoutGroup = selectedTurnoutGroup;
        initGUI();
    }

    private void initGUI() {

        allTurnouts = turnoutManager.getAllTurnouts();
        initComponents();
        buildPanel();
        initEventHandling();
        initShortcuts();
        address1TextField.requestFocusInWindow();
        pack();
        SwingUtils.addEscapeListener(this);
        setLocationRelativeTo(getParent());
        setVisible(true);
    }

    private void initComponents() {

        numberModel = getBufferedModel(Turnout.PROPERTYNAME_NUMBER);
        descriptionModel = getBufferedModel(Turnout.PROPERTYNAME_DESCRIPTION);
        bus1Model = getBufferedModel(Turnout.PROPERTYNAME_BUS1);
        address1Model = getBufferedModel(Turnout.PROPERTYNAME_ADDRESS1);
        bus2Model = getBufferedModel(Turnout.PROPERTYNAME_BUS2);
        address2Model = getBufferedModel(Turnout.PROPERTYNAME_ADDRESS2);
        swiched1Model = getBufferedModel(Turnout.PROPERTYNAME_ADDRESS1_SWITCHED);
        switched2Model = getBufferedModel(Turnout.PROPERTYNAME_ADDRESS2_SWITCHED);
        turnoutTypeModel = getBufferedModel(Turnout.PROPERTYNAME_TURNOUT_TYPE);
        defaultStateModel = getBufferedModel(Turnout.PROPERTYNAME_DEFAULT_STATE);
        orientationModel = getBufferedModel(Turnout.PROPERTYNAME_ORIENTATION);

        numberTextField = new JSpinner();
        numberTextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
                numberModel, 1, // defaultValue
                0, // minValue
                1000, // maxValue
                1)); // step

        descTextField = BasicComponentFactory.createTextField(descriptionModel);
        descTextField.setColumns(5);

        bus1TextField = new JSpinner();
        bus1TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
                bus1Model, 1, // defaultValue
                0, // minValue
                100, // maxValue
                1)); // step

        address1TextField = new JSpinner();
        address1TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
                address1Model, 1, // defaultValue
                0, // minValue
                324, // maxValue
                1)); // step

        bus2TextField = new JSpinner();
        bus2TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
                bus2Model, 0, // defaultValue
                0, // minValue
                100, // maxValue
                1)); // step

        address2TextField = new JSpinner();
        address2TextField.setModel(SpinnerAdapterFactory.createNumberAdapter(
                address2Model, 0, // defaultValue
                0, // minValue
                324, // maxValue
                1)); // step

        switched1Checkbox = BasicComponentFactory.createCheckBox(swiched1Model,
                "Inverted");

        switched2Checkbox = BasicComponentFactory.createCheckBox(
                switched2Model, "Inverted");

        final List<TurnoutType> turnoutTypes = Arrays.asList(TurnoutType
                .values());

        turnoutTypeComboBox = BasicComponentFactory
                .createComboBox(new SelectionInList<TurnoutType>(turnoutTypes,
                        turnoutTypeModel));
        turnoutTypeComboBox.setRenderer(new TurnoutTypeComboBoxCellRenderer());
        turnoutTypeComboBox
                .addActionListener(new TurnoutTypeSelectionListener());

        switch (presentationModel.getBean().getType()) {
            case DEFAULT_LEFT:
            case DOUBLECROSS:
            case CUTTER:
                bus2TextField.setValue(0);
                bus2TextField.setEnabled(false);
                address2TextField.setValue(0);
                address2TextField.setEnabled(false);
                break;
            case THREEWAY:
                bus2TextField.setEnabled(true);
                address2TextField.setEnabled(true);
                break;
            default:
                break;
        }

        turnoutDefaultStateComboBox = BasicComponentFactory
                .createComboBox(new SelectionInList<TurnoutState>(
                        new TurnoutState[]{TurnoutState.STRAIGHT,
                                TurnoutState.LEFT}, defaultStateModel
                ));
        turnoutDefaultStateComboBox
                .setRenderer(new TurnoutDefaultStateComboBoxCellRenderer());

        turnoutOrientationComboBox = BasicComponentFactory
                .createComboBox(new SelectionInList<TurnoutOrientation>(
                        TurnoutOrientation.values(), orientationModel));

        testTurnoutWidget = new TurnoutWidget(ctx, testTurnout, false, true);
        if (!TurnoutHelper.isTurnoutReadyToTest(presentationModel.getBean())) {
            testTurnoutWidget.setEnabled(false);
        }

        testTurnoutWidget.connectedToRailwayDevice(new ConnectedToRailwayEvent(ctx
                .getRailwayDeviceManager().isConnected()));

        errorPanel = new ErrorPanel();

        okButton = new JButton(new ApplyChangesAction());
        cancelButton = new JButton(new CancelAction());
        validate(presentationModel.getBean());
    }

    private void buildPanel() {
        initComponents();

        final FormLayout layout = new FormLayout(
                "right:pref, 3dlu, pref:grow, 30dlu, right:pref, 3dlu, pref:grow, 3dlu,pref:grow, 30dlu, pref",
                "p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu, p:grow, 3dlu, p:grow, 10dlu,p:grow");
        layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});
        layout.setRowGroups(new int[][]{{3, 5, 7, 9, 11}});

        builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        final CellConstraints cc = new CellConstraints();

        builder.addSeparator("General", cc.xyw(1, 1, 3));

        builder.addLabel("Number", cc.xy(1, 3));
        builder.add(numberTextField, cc.xy(3, 3));

        builder.addLabel("Description", cc.xy(1, 5));
        builder.add(descTextField, cc.xy(3, 5));

        builder.addLabel("Type", cc.xy(1, 7));
        builder.add(turnoutTypeComboBox, cc.xy(3, 7));

        builder.addLabel("Default State", cc.xy(1, 9));
        builder.add(turnoutDefaultStateComboBox, cc.xy(3, 9));

        builder.addLabel("Orientation", cc.xy(1, 11));
        builder.add(turnoutOrientationComboBox, cc.xy(3, 11));

        builder.addSeparator("Interface", cc.xyw(5, 1, 5));
        builder.addLabel("Bus 1", cc.xy(5, 3));
        builder.add(bus1TextField, cc.xy(7, 3));

        builder.addLabel("Address 1", cc.xy(5, 5));
        builder.add(address1TextField, cc.xy(7, 5));

        builder.addLabel("Bus 2", cc.xy(5, 7));
        builder.add(bus2TextField, cc.xy(7, 7));

        builder.addLabel("Address 2", cc.xy(5, 9));
        builder.add(address2TextField, cc.xy(7, 9));

        builder.add(switched1Checkbox, cc.xy(9, 5));

        builder.add(switched2Checkbox, cc.xy(9, 9));

        builder.addSeparator("Test", cc.xy(11, 1));
        builder.add(testTurnoutWidget, cc.xywh(11, 3, 1, 9));

        builder.add(errorPanel, cc.xyw(1, 13, 7));
        builder.add(buildButtonBar(), cc.xyw(7, 13, 5));

        add(builder.getPanel());
    }

    private void initEventHandling() {
        numberModel.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_NUMBER));
        descriptionModel.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_DESCRIPTION));
        bus1Model.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_BUS1));
        address1Model.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_ADDRESS1));
        bus2Model.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_BUS2));
        address2Model.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_ADDRESS2));
        swiched1Model.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_ADDRESS1_SWITCHED));
        switched2Model.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_ADDRESS2_SWITCHED));
        turnoutTypeModel.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_TURNOUT_TYPE));
        defaultStateModel.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_DEFAULT_STATE));
        orientationModel.addValueChangeListener(new TurnoutChangeListener(
                Turnout.PROPERTYNAME_ORIENTATION));
    }

    private void initShortcuts() {
        GlobalKeyShortcutHelper.registerKey(getRootPane(), KeyEvent.VK_ENTER, 0, new ApplyChangesAction());
    }

    private BufferedValueModel getBufferedModel(
            final String propertynameDescription) {
        final BufferedValueModel numberModel = presentationModel
                .getBufferedModel(propertynameDescription);

        return numberModel;
    }

    private JComponent buildButtonBar() {
        return ButtonBarFactory.buildRightAlignedBar(okButton, cancelButton);
    }

    private boolean validate(final Turnout turnoutToValidate) {
        boolean valid = true;
        if (TurnoutHelper.isNumberValid(turnoutToValidate,
                presentationModel.getBean(), turnoutManager)) {
            setSpinnerColor(numberTextField,
                    UIConstants.DEFAULT_TEXTFIELD_COLOR);
            okButton.setEnabled(true);
        } else {
            setSpinnerColor(numberTextField, UIConstants.ERROR_COLOR);
            valid = false;
            okButton.setEnabled(false);
        }

        if (TurnoutHelper.isBusValid(turnoutToValidate.getBus1())) {
            setSpinnerColor(bus1TextField, UIConstants.DEFAULT_TEXTFIELD_COLOR);
        } else {
            setSpinnerColor(bus1TextField, UIConstants.ERROR_COLOR);
            valid = false;
        }

        if (TurnoutHelper.isAddressValid(turnoutToValidate.getAddress1())) {
            setSpinnerColor(address1TextField,
                    UIConstants.DEFAULT_TEXTFIELD_COLOR);
        } else {
            setSpinnerColor(address1TextField, UIConstants.ERROR_COLOR);
            valid = false;
        }

        if (TurnoutHelper
                .isBusAddressUnique(turnoutToValidate.getBus1(),
                        turnoutToValidate.getAddress1(), turnoutToValidate,
                        allTurnouts)) {
            setSpinnerColor(bus1TextField, UIConstants.DEFAULT_TEXTFIELD_COLOR);
            setSpinnerColor(address1TextField,
                    UIConstants.DEFAULT_TEXTFIELD_COLOR);
        } else {
            setSpinnerColor(bus1TextField, UIConstants.WARN_COLOR);
            setSpinnerColor(address1TextField, UIConstants.WARN_COLOR);
        }

        if (turnoutToValidate.isThreeWay()) {
            if (TurnoutHelper.isBusValid(turnoutToValidate.getBus2())) {
                setSpinnerColor(bus2TextField,
                        UIConstants.DEFAULT_TEXTFIELD_COLOR);
            } else {
                setSpinnerColor(bus2TextField, UIConstants.ERROR_COLOR);
                valid = false;
            }

            if (TurnoutHelper.isAddressValid(turnoutToValidate.getAddress2())) {
                setSpinnerColor(address2TextField,
                        UIConstants.DEFAULT_TEXTFIELD_COLOR);
            } else {
                setSpinnerColor(address2TextField, UIConstants.ERROR_COLOR);
                valid = false;
            }

            if (TurnoutHelper.isBusAddressUnique(turnoutToValidate.getBus2(),
                    turnoutToValidate.getAddress2(), turnoutToValidate,
                    allTurnouts)) {
                setSpinnerColor(bus2TextField,
                        UIConstants.DEFAULT_TEXTFIELD_COLOR);
                setSpinnerColor(address2TextField,
                        UIConstants.DEFAULT_TEXTFIELD_COLOR);
            } else {
                setSpinnerColor(bus2TextField, UIConstants.WARN_COLOR);
                setSpinnerColor(address2TextField, UIConstants.WARN_COLOR);
            }
        }
        return valid;
    }

    private void setSpinnerColor(final JSpinner spinner, final Color color) {
        final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner
                .getEditor();
        editor.getTextField().setBackground(color);
    }

    class TurnoutTypeSelectionListener implements ActionListener {

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TurnoutType selectedTurnoutType = (TurnoutType) turnoutTypeComboBox
                    .getSelectedItem();
            switch (selectedTurnoutType) {
                case DEFAULT_LEFT:
                case DOUBLECROSS:
                    bus2TextField.setValue(0);
                    bus2TextField.setEnabled(false);
                    address2TextField.setValue(0);
                    address2TextField.setEnabled(false);
                    break;
                case THREEWAY:
                    bus2TextField.setValue(Constants.DEFAULT_BUS);
                    bus2TextField.setEnabled(true);
                    address2TextField.setEnabled(true);
                    break;
                case CUTTER:
                    break;
                default:
                    break;
            }
        }

    }

    class TurnoutChangeListener implements PropertyChangeListener {
        private final String property;

        public TurnoutChangeListener(final String property) {
            this.property = property;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {

            TurnoutHelper.update(testTurnout, property, evt.getNewValue());

            testTurnoutWidget.setTurnout(testTurnout);

            if (!validate(testTurnout)) {
                return;
            }

            if (!TurnoutHelper.isTurnoutReadyToTest(testTurnout)) {
                testTurnoutWidget.setEnabled(false);
                return;
            }
            testTurnoutWidget.setEnabled(true);
            repaint();
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
            final Turnout turnout = presentationModel.getBean();

            turnoutManager.addTurnoutManagerListener(new TurnoutAddListener() {

                @Override
                public void turnoutAdded(final Turnout turnout) {
                    success(turnout);
                }

                @Override
                public void turnoutUpdated(final Turnout turnout) {
                    success(turnout);
                }

                private void success(final Turnout turnout) {
                    turnoutManager
                            .removeTurnoutManagerListenerInNextEvent(this);

                    TurnoutConfig.this.setVisible(false);

                }

                @Override
                public void failure(final ManagerException arg0) {
                    errorPanel.setErrorText(arg0.getMessage());
                }

            });

            if (createTurnout) {
                turnoutManager.addTurnoutToGroup(turnout, selectedTurnoutGroup);
            } else {
                turnoutManager.updateTurnout(turnout);
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
            TurnoutConfig.this.setVisible(false);
        }
    }

}
