package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.ui.context.LocomotiveContext;
import ch.fork.AdHocRailway.ui.locomotives.configuration.LocomotiveGroupListCellRenderer;
import ch.fork.AdHocRailway.utils.LocomotiveHelper;
import com.google.common.collect.Lists;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.SortedSet;

public class LocomotiveSelectionPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(LocomotiveSelectionPanel.class.getSimpleName());
    public static final int LOCOMOTIVE_IMAGE_HEIGHT = 40;

    private OnLocomotiveSelectionListener selectionListener;
    private final LocomotiveContext ctx;
    private LocomotiveSelectAction locomotiveSelectAction;
    private LocomotiveGroupSelectAction groupSelectAction;

    private JComboBox<Locomotive> locomotiveComboBox;
    private JComboBox<LocomotiveGroup> locomotiveGroupComboBox;
    private LocomotiveGroupComboboxModel locomotiveGroupComboBoxModel;
    private LocomotiveComboboxModel locomotiveComboBoxModel;
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

    public void updateLocomotiveGroups() {
        if (ctx.getLocomotiveManager() == null) {
            return;
        }
        SortedSet<LocomotiveGroup> locomotiveGroups = ctx.getLocomotiveManager().getAllLocomotiveGroups();

        allLocomotivesGroup.getLocomotives().clear();

        for (final LocomotiveGroup lg : locomotiveGroups) {
            for (final Locomotive l : lg.getLocomotives()) {
                allLocomotivesGroup.addLocomotive(l);
            }
        }

        List<LocomotiveGroup> listForModel = Lists.newArrayList();
        listForModel.add(allLocomotivesGroup);
        listForModel.addAll(locomotiveGroups);

        locomotiveGroupComboBoxModel.clearAndAddAll(listForModel);
        locomotiveComboBoxModel.clearAndAddAll(allLocomotivesGroup.getLocomotives());
        locomotiveGroupComboBox.setSelectedIndex(0);
        locomotiveComboBox.setSelectedIndex(-1);

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
        locomotiveGroupComboBoxModel = new LocomotiveGroupComboboxModel();
        locomotiveGroupComboBox.setModel(locomotiveGroupComboBoxModel);
        locomotiveGroupComboBox.setFocusable(false);
        locomotiveGroupComboBox.setFont(locomotiveGroupComboBox.getFont());
        locomotiveGroupComboBox.setMaximumRowCount(10);
        locomotiveGroupComboBox.setSelectedIndex(-1);
        locomotiveGroupComboBox.setRenderer(new LocomotiveGroupListCellRenderer());

        groupSelectAction = new LocomotiveGroupSelectAction();
        locomotiveGroupComboBox.addItemListener(groupSelectAction);

        locomotiveComboBox = new JComboBox<Locomotive>();
        locomotiveComboBoxModel = new LocomotiveComboboxModel();
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

        add(locomotiveGroupComboBox, "growy, width 120");
        add(locomotiveComboBox, "growy" +
                ", width 120, wrap");
        add(locomotiveImage, "span 2, height 60, grow");

    }

    private class LocomotiveGroupSelectAction implements ItemListener {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            final LocomotiveGroup selectedLocomotiveGroup = (LocomotiveGroup) locomotiveGroupComboBoxModel
                    .getSelectedItem();

            if (selectedLocomotiveGroup == null) {
                return;
            }
            locomotiveComboBoxModel.clearAndAddAll(selectedLocomotiveGroup.getLocomotives());

            locomotiveImage.setIcon(LocomotiveImageHelper.getEmptyLocoIconScaledToHeight(LOCOMOTIVE_IMAGE_HEIGHT));
            selectionListener.onLocomotiveGroupSelected(selectedLocomotiveGroup);
            locomotiveComboBox.setSelectedIndex(-1);

        }
    }

    private class LocomotiveSelectAction implements ItemListener {

        @Override
        public void itemStateChanged(final ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
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

            setToolTipText(locomotiveDescriptionToolTip);

            locomotiveImage.setIcon(LocomotiveImageHelper.getLocomotiveIconScaledToHeight(selectedLocomotive, LOCOMOTIVE_IMAGE_HEIGHT));
            selectionListener.onLocomotiveSelected(selectedLocomotive);

        }
    }

    interface OnLocomotiveSelectionListener {

        void onLocomotiveGroupSelected(LocomotiveGroup selectedLocomotiveGroup);

        void onLocomotiveSelected(Locomotive selectedLocomotive);

    }

}
