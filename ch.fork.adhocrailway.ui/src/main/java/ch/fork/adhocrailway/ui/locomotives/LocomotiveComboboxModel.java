package ch.fork.adhocrailway.ui.locomotives;

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import com.google.common.collect.Lists;

import javax.swing.*;
import java.util.List;
import java.util.SortedSet;

public class LocomotiveComboboxModel extends AbstractListModel<Locomotive> implements ComboBoxModel<Locomotive> {
    private List<Locomotive> locomotives = Lists.newArrayList();
    private Locomotive selectedLocomotive;

    public void clearAndAddAll(SortedSet<Locomotive> locomotives) {
        this.locomotives.clear();
        this.locomotives.addAll(locomotives);

        fireContentsChanged(this, 0, Math.max(this.locomotives.size() - 1, 0));
    }


    @Override
    public int getSize() {
        return locomotives.size();
    }

    @Override
    public Locomotive getElementAt(int index) {
        return locomotives.get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedLocomotive = (Locomotive) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selectedLocomotive;
    }
}
