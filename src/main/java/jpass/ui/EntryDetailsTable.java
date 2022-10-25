/*
 * JPass
 *
 * Copyright (c) 2009-2022 Gabor Bata
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jpass.ui;

import jpass.ui.action.TableListener;
import jpass.util.Configuration;
import jpass.util.DateUtils;
import jpass.xml.bind.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Table to display entry details.
 */
public class EntryDetailsTable extends JTable {
    public static final String TITTLE = "Title";
    public static final String URL = "URL";
    public static final String USER = "User";
    public static final String PWD = "Password";
    public static final String CREATED = "Modified";
    public static final String MODIFIED = "Created";
    public static HashSet<String> isPasswordShowSet = new HashSet<>();

    private static final DateTimeFormatter FORMATTER
            = DateUtils.createFormatter(Configuration.getInstance().get("date.format", "yyyy-MM-dd"));

    private enum DetailType {
        TITLE(EntryDetailsTable.TITTLE, Entry::getTitle),
        URL(EntryDetailsTable.URL, Entry::getUrl),
        USER(EntryDetailsTable.USER, Entry::getUser),
        PASSWORD(EntryDetailsTable.PWD, Entry::getPassword),
        MODIFIED(EntryDetailsTable.MODIFIED, entry -> DateUtils.formatIsoDateTime(entry.getLastModification(), FORMATTER)),
        CREATED(EntryDetailsTable.CREATED, entry -> DateUtils.formatIsoDateTime(entry.getCreationDate(), FORMATTER));

        private final String description;
        private final Function<Entry, String> valueMapper;

        DetailType(String description, Function<Entry, String> valueMapper) {
            this.description = description;
            this.valueMapper = valueMapper;
        }

        public String getDescription() {
            return description;
        }

        public String getValue(Entry entry) {
            return entry != null ? valueMapper.apply(entry) : "";
        }
    }

    private static final Map<String, DetailType> DETAILS_BY_NAME = Arrays.stream(DetailType.values())
            .collect(Collectors.toMap(Enum::name, Function.identity()));

    private static final String[] DEFAULT_DETAILS = {
            DetailType.TITLE.name(),
            DetailType.URL.name(),
            DetailType.USER.name(),
            DetailType.PASSWORD.name(),
            DetailType.CREATED.name(),
            DetailType.MODIFIED.name()
    };

    private final List<DetailType> detailsToDisplay;
    private final DefaultTableModel tableModel;

    public EntryDetailsTable() {
        super();

        detailsToDisplay = Arrays.stream(Configuration.getInstance().getArray("entry.details", DEFAULT_DETAILS))
                .map(DETAILS_BY_NAME::get)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (detailsToDisplay.isEmpty()) {
            Arrays.stream(DEFAULT_DETAILS)
                    .map(DETAILS_BY_NAME::get)
                    .forEach(detailsToDisplay::add);
        }

        tableModel = new DefaultTableModel();
        detailsToDisplay.forEach(detail -> tableModel.addColumn(detail.getDescription()));
        setModel(tableModel);
        getTableHeader().setReorderingAllowed(true);
        addMouseListener(new TableListener());
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setIntercellSpacing(new Dimension(0, 5));
        fitTableColumns();
        addPasswordColumnButton();
    }

    private void addPasswordColumnButton() {
        this.getColumn(PWD).setCellEditor(new HideCellEditor(this));
        this.getColumn(PWD).setCellRenderer(new HideCellRender());
    }

    public void fitTableColumns() {
        JTableHeader header = getTableHeader();
        Enumeration columns = getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(
                            this, column.getIdentifier(), false, false, -1, col)
                    .getPreferredSize().getWidth();
            for (int row = 0; row < getRowCount(); row++) {
                int preferedwidth = (int) getCellRenderer(row, col).getTableCellRendererComponent(this,
                        getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedwidth);
            }
            header.setResizingColumn(column);
            column.setWidth(width + getIntercellSpacing().width + 100);

        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column != getColumn(PWD).getModelIndex()) {
            return super.getValueAt(row, column);
        }
        String rowTittle = getModel().getValueAt(row, 0).toString();
        if (isPasswordShowSet.contains(rowTittle)) {
            return super.getValueAt(row, column);
        }
        return "******";
    }

    @Override
    public boolean isCellEditable( int row, int column) {
        if ( getColumn(PWD).getModelIndex() == column) {
            return true;
        }
        return false;
    }

    public void clear () {
        tableModel.setRowCount(0);
    }

    public void addRow (Entry entry){
        tableModel.addRow(detailsToDisplay.stream()
                .map(detail -> detail.getValue(entry))
                .toArray(Object[]::new));
    }

    public int rowCount () {
        return tableModel.getRowCount();
    }
}
