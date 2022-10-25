package jpass.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HideCellRender extends DefaultTableCellRenderer {
    private JPanel panel;
    private JButton button;
    private JLabel label;

    public HideCellRender() {
        panel = new JPanel(new BorderLayout());
        button = new JButton(MessageDialog.getIcon("keyring"));
        button.setBounds(0, 0, 16, 16);
        label = new JLabel();
        panel.add(label, BorderLayout.WEST);
        panel.add(button, BorderLayout.EAST);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        label.setText(value.toString());
        return this.panel;
    }
}
