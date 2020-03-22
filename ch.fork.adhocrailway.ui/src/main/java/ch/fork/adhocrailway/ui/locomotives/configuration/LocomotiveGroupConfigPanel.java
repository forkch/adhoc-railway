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
package ch.fork.adhocrailway.ui.locomotives.configuration;

import ch.fork.adhocrailway.model.locomotives.LocomotiveGroup;
import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueHolder;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LocomotiveGroupConfigPanel extends JPanel {

    private final PresentationModel<LocomotiveGroup> presentationModel;
    private JTextField locomotiveGroupName;

    public LocomotiveGroupConfigPanel() {
        presentationModel = new PresentationModel<LocomotiveGroup>(
                new ValueHolder(null, true));
        initComponents();
        buildPanel();
        setLocomotiveGroup(null);
    }

    public void setLocomotiveGroup(final LocomotiveGroup group) {
        locomotiveGroupName.setEnabled(group != null);
        presentationModel.setBean(group);
    }

    private void buildPanel() {
        setLayout(new MigLayout());
        add(new JLabel("Name"), "");
        add(locomotiveGroupName, "");

    }

    private void initComponents() {
        locomotiveGroupName = BasicComponentFactory
                .createTextField(presentationModel
                        .getModel(TurnoutGroup.PROPERTYNAME_NAME));
        locomotiveGroupName.setColumns(5);

    }
}
