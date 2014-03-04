/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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

/**
 * description
 *
 * Version 0.1 12.02.2008
 *
 * Copyright (C) Siemens Schweiz AG 2008, All Rights Reserved, Confidential
 */
package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;

/**
 * @author Benjamin Mueller <benjamin.b.mueller@siemens.com>
 */
public class TurnoutGroupConfigPanel extends JPanel {

    private final PresentationModel<TurnoutGroup> presentationModel;
    private JTextField turnoutGroupName;

    public TurnoutGroupConfigPanel() {
        presentationModel = new PresentationModel<TurnoutGroup>(
                new ValueHolder(null, true));
        buildPanel();
        setTurnoutGroup(null);
    }

    public void setTurnoutGroup(final TurnoutGroup group) {
        turnoutGroupName.setEnabled(group != null);
        if (Preferences.getInstance().getBooleanValue(
                PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
        }
        presentationModel.setBean(group);
    }

    private void buildPanel() {
        initComponents();

        final FormLayout layout = new FormLayout("right:pref, 3dlu, pref:grow",
                "p:grow, 3dlu");

        final PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        final CellConstraints cc = new CellConstraints();

        builder.addLabel("Name", cc.xy(1, 1));
        builder.add(turnoutGroupName, cc.xy(3, 1));
        add(builder.getPanel());
    }

    private void initComponents() {
        turnoutGroupName = BasicComponentFactory
                .createTextField(presentationModel
                        .getModel(TurnoutGroup.PROPERTYNAME_NAME));
        turnoutGroupName.setColumns(5);

    }
}
