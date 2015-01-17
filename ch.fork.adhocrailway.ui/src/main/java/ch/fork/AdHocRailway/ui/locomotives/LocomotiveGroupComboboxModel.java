package ch.fork.AdHocRailway.ui.locomotives;

import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.List;

public class LocomotiveGroupComboboxModel extends AbstractListModel<LocomotiveGroup> implements ComboBoxModel<LocomotiveGroup> {

    private List<LocomotiveGroup> locomotiveGroupList = Lists.newArrayList();
    private LocomotiveGroup selectedLocomotiveGroup;

    public void clearAndAddAll(List<LocomotiveGroup> locomotiveGroups) {
        locomotiveGroupList.clear();
        locomotiveGroupList.addAll(locomotiveGroups);
        fireContentsChanged(this, 0, locomotiveGroupList.size() - 1);
    }

    @Override
    public int getSize() {
        return locomotiveGroupList.size();
    }

    @Override
    public LocomotiveGroup getElementAt(int index) {
        return locomotiveGroupList.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {

        selectedLocomotiveGroup = (LocomotiveGroup) anItem;

    }

    @Override
    public Object getSelectedItem() {
        return selectedLocomotiveGroup;
    }
}
