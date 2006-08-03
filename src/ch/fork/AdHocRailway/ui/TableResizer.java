
package ch.fork.AdHocRailway.ui;

//
// Utility class to adjust table row and column dimensions
//
// J.-P. Dubois / May 2006
//
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TableResizer {
    // Helper method to calculate the table column width
    public static void adjustColumnWidths(JTable table, int margin) {
        TableColumnModel colModel = table.getColumnModel();
        // Loop on all table columns
        for (int c = 0; c < table.getColumnCount(); c++) {
            int maxWidth = 0;
            // Get column header width
            TableColumn col = colModel.getColumn(c);
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null)
                renderer = table.getTableHeader().getDefaultRenderer();
            Component comp = renderer.getTableCellRendererComponent(table, col
                .getHeaderValue(), false, false, 0, c);
            maxWidth = comp.getPreferredSize().width;
            // For each column, loop on all table rows, to
            // get the widest cell
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, c);
                comp = renderer.getTableCellRendererComponent(table, table
                    .getValueAt(r, c), false, false, r, c);
                // Adapt max. width, if necessary
                maxWidth = Math.max(comp.getPreferredSize().width, maxWidth);
            }
            // Set column correct width
            col.setMaxWidth(maxWidth + margin);
        }
    }

    // Helper method to calculate the table cell height
    public static void adjustRowHeight(JTable table) {
        int maxHeight = 0;
        // Loop on all table columns
        for (int c = 0; c < table.getColumnCount(); c++) {
            // For each column, loop on all table rows to
            // get the highest cell
            for (int r = 0; r < table.getRowCount(); r++) {
                TableCellRenderer renderer = table.getCellRenderer(r, c);
                Component comp = renderer.getTableCellRendererComponent(table,
                    table.getValueAt(r, c), false, false, r, c);
                // Adapt max. height, if necessary
                maxHeight = Math.max(comp.getPreferredSize().height, maxHeight);
            }
        }
        // Set table correct row height
        table.setRowHeight(maxHeight);
    }
}
