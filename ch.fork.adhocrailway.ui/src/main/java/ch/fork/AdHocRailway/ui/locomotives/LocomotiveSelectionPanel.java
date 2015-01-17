package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToPersistenceEvent;
import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveGroupListCellRenderer;
import ch.fork.AdHocRailway.utils.LocomotiveHelper;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.*;
import java.util.SortedSet;

/**
 * Created by fork on 17.01.15.
 */
public class LocomotiveSelectionPanel extends JPanel {

    public static final int LOCOMOTIVE_IMAGE_HEIGHT = 40;

    private OnLocomotiveSelectionListener selectionListener;
    private final LocomotiveContext ctx;
    private LocomotiveSelectAction locomotiveSelectAction;
    private LocomotiveGroupSelectAction groupSelectAction;

    private JComboBox<Locomotive> locomotiveComboBox;
    private JComboBox<LocomotiveGroup> locomotiveGroupComboBox;
    private DefaultComboBoxModel<LocomotiveGroup> locomotiveGroupComboBoxModel;
    private DefaultComboBoxModel<Locomotive> locomotiveComboBoxModel;
    private boolean ignoreGroupAndLocomotiveSelectionEvents;
    private final LocomotiveGroup allLocomotivesGroup;
    private LocomotiveComboBoxRenderer locomotiveComboboxRendererWithLocoImage;
    private JLabel locomotiveImage;

    public LocomotiveSelectionPanel(OnLocomotiveSelectionListener selectionListener, LocomotiveContext locomotiveContext) {
        this.selectionListener = selectionListener;
        this.ctx = locomotiveContext;
        ctx.getMainBus().register(this);
        allLocomotivesGroup = new LocomotiveGroup("", "All");
        initGUI();
        updateLocomotiveGroups();
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        updateLocomotiveGroups();
    }

    @Subscribe
    public void onConnectedToPersistenceEvent(ConnectedToPersistenceEvent e) {
        updateLocomotiveGroups();
    }

    public void updateLocomotiveGroups() {
        if (ctx.getLocomotiveManager() == null) {
            return;
        }
        SortedSet<LocomotiveGroup> locomotiveGroups = ctx.getLocomotiveManager().getAllLocomotiveGroups();

        ignoreGroupAndLocomotiveSelectionEvents = true;
        locomotiveGroupComboBoxModel.removeAllElements();
        locomotiveComboBoxModel.removeAllElements();
        allLocomotivesGroup.getLocomotives().clear();

        locomotiveGroupComboBoxModel.addElement(allLocomotivesGroup);

        for (final LocomotiveGroup lg : locomotiveGroups) {
            for (final Locomotive l : lg.getLocomotives()) {
                allLocomotivesGroup.addLocomotive(l);
                locomotiveComboBoxModel.addElement(l);
            }
            locomotiveGroupComboBoxModel.addElement(lg);
        }

        locomotiveComboBox.setSelectedIndex(-1);
        ignoreGroupAndLocomotiveSelectionEvents = false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        locomotiveGroupComboBox.setEnabled(enabled);
        locomotiveComboBox.setEnabled(enabled);
    }

    private void initGUI() {

        setLayout(new MigLayout(" insets 0, gap 5, fill"));

        initSelectionPanel();

    }

    private void initSelectionPanel() {
        locomotiveGroupComboBox = new JComboBox<LocomotiveGroup>();
        locomotiveGroupComboBoxModel = new DefaultComboBoxModel<LocomotiveGroup>();
        locomotiveGroupComboBox.setModel(locomotiveGroupComboBoxModel);
        locomotiveGroupComboBox.setFocusable(false);
        locomotiveGroupComboBox.setFont(locomotiveGroupComboBox.getFont()
                .deriveFont(14));
        locomotiveGroupComboBox.setMaximumRowCount(10);
        locomotiveGroupComboBox.setSelectedIndex(-1);
        locomotiveGroupComboBox.setRenderer(new LocomotiveGroupListCellRenderer());

        groupSelectAction = new LocomotiveGroupSelectAction();
        locomotiveGroupComboBox.addItemListener(groupSelectAction);

        locomotiveComboBox = new JComboBox<Locomotive>();
        locomotiveComboBoxModel = new DefaultComboBoxModel<Locomotive>();
        locomotiveComboBox.setModel(locomotiveComboBoxModel);
        locomotiveComboBox.setFocusable(false);
        locomotiveSelectAction = new LocomotiveSelectAction();
        locomotiveComboBox.addItemListener(locomotiveSelectAction);
        locomotiveComboboxRendererWithLocoImage = new LocomotiveComboBoxRenderer();
        locomotiveComboBox.setRenderer(locomotiveComboboxRendererWithLocoImage);

        locomotiveImage = new JLabel();
        locomotiveImage.setIcon(LocomotiveImageHelper.getEmptyLocoIconScaledToHeight(LOCOMOTIVE_IMAGE_HEIGHT));
        locomotiveImage.setHorizontalAlignment(SwingConstants.CENTER);
        locomotiveImage.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(locomotiveGroupComboBox, "grow, width 80");
        add(locomotiveComboBox, "grow, width 120, wrap");
        add(locomotiveImage, "span 2, grow");

    }

    private class LocomotiveGroupSelectAction implements ItemListener {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            if (ignoreGroupAndLocomotiveSelectionEvents) {
                return;
            }

            final LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) locomotiveGroupComboBoxModel
                    .getSelectedItem();
            final int idx = locomotiveGroupComboBox.getSelectedIndex();

            if (selectedLocomotiveGroup == null) {
                return;
            }
            locomotiveComboBox.setEnabled(false);
            locomotiveComboBoxModel.removeAllElements();
            for (final Locomotive l : selectedLocomotiveGroup.getLocomotives()) {
                locomotiveComboBoxModel.addElement(l);
            }
            locomotiveComboBox.setEnabled(true);

            locomotiveComboBox.setSelectedIndex(-1);

            locomotiveImage.setIcon(LocomotiveImageHelper.getEmptyLocoIconScaledToHeight(LOCOMOTIVE_IMAGE_HEIGHT));
            selectionListener.onLocomotiveGroupSelected(selectedLocomotiveGroup);

        }
    }

    private class LocomotiveSelectAction implements ItemListener {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            if (ignoreGroupAndLocomotiveSelectionEvents) {
                return;
            }
            if (locomotiveComboBox.getItemCount() == 0
                    || locomotiveComboBox.getSelectedIndex() == -1) {
                return;
            }

            Locomotive selectedLocomotive = (Locomotive) locomotiveComboBox
                    .getSelectedItem();

            final String locomotiveDescriptionToolTip = LocomotiveHelper
                    .getLocomotiveDescription(selectedLocomotive);

            locomotiveComboBox.setToolTipText(locomotiveDescriptionToolTip);

            locomotiveImage.setIcon(LocomotiveImageHelper.getLocomotiveIconScaledToHeight(selectedLocomotive, LOCOMOTIVE_IMAGE_HEIGHT));
            selectionListener.onLocomotiveSelected(selectedLocomotive);

        }
    }

    interface OnLocomotiveSelectionListener {

        void onLocomotiveGroupSelected(LocomotiveGroup selectedLocomotiveGroup);

        void onLocomotiveSelected(Locomotive selectedLocomotive);

    }

}
