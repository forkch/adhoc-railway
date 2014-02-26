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
package ch.fork.AdHocRailway.ui.routes.configuration;

import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
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
public class RouteGroupConfigPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 6564150077064098335L;
    private final PresentationModel<RouteGroup> presentationModel;
    private JTextField routeGroupName;

    public RouteGroupConfigPanel() {
        presentationModel = new PresentationModel<RouteGroup>(new ValueHolder(
                null, true));
        buildPanel();
    }

    public void setRouteGroup(final RouteGroup group) {
        presentationModel.setBean(group);
    }

    private void buildPanel() {
        initComponents();

        final FormLayout layout = new FormLayout("right:pref, 3dlu, pref:grow",
                "p:grow, 3dlu,p:grow, 3dlu,p:grow, 3dlu");

        final PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        final CellConstraints cc = new CellConstraints();

        builder.addLabel("Name", cc.xy(1, 1));
        builder.add(routeGroupName, cc.xy(3, 1));

        add(builder.getPanel());
    }

    private void initComponents() {
        routeGroupName = BasicComponentFactory
                .createTextField(presentationModel
                        .getModel(RouteGroup.PROPERTYNAME_NAME));
        routeGroupName.setColumns(5);

    }
}
