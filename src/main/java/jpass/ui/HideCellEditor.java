package jpass.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HideCellEditor extends DefaultCellEditor {

    private EntryDetailsTable table;
    private JPanel panel;
    private JLabel label;
    private JButton button;

    public HideCellEditor(EntryDetailsTable table) {
        super(new JTextField());
        this.table = table;
        setClickCountToStart(1);
        panel = new JPanel(new BorderLayout());
        this.initButton();
        label = new JLabel();
        panel.add(label, BorderLayout.WEST);
        panel.add(button, BorderLayout.EAST);
    }

    private void initButton() {
        button = new JButton(MessageDialog.getIcon("keyring"));
        button.setBounds(0, 0, 16, 16);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingCanceled();
                String tittle = table.getModel().getValueAt(table.getSelectedRow(), 0).toString();
                if (EntryDetailsTable.isPasswordShowSet.contains(tittle)) {
                    EntryDetailsTable.isPasswordShowSet.remove(tittle);
                } else {
                    EntryDetailsTable.isPasswordShowSet.add(tittle);
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        label.setText(value.toString());
        return this.panel;
    }

    @Override
    public Object getCellEditorValue() {
        return label.getText();
    }
}
