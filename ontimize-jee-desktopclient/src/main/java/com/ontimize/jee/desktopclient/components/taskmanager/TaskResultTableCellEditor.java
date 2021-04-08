package com.ontimize.jee.desktopclient.components.taskmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders a JProgressBar in a table cell.
 */
public class TaskResultTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    private static final Logger logger = LoggerFactory.getLogger(TaskResultTableCellEditor.class);

    //
    // Instance Variables
    //

    /** The Swing component being edited. */
    protected JButton editorComponent;

    /**
     * The delegate class which handles all methods sent from the <code>CellEditor</code>.
     */
    protected EditorDelegate delegate;

    /**
     * An integer specifying the number of clicks needed to start editing. Even if
     * <code>clickCountToStart</code> is defined as zero, it will not initiate until a click occurs.
     */
    protected int clickCountToStart = 1;

    //
    // Constructors
    //

    /**
     * Constructs a <code>DefaultCellEditor</code> object that uses a check box.
     * @param button a <code>JCheckBox</code> object
     */
    public TaskResultTableCellEditor() {
        this.editorComponent = new JButton();
        this.delegate = new EditorDelegate();
        this.editorComponent.addActionListener(this.delegate);
        this.editorComponent.setRequestFocusEnabled(false);
        this.addCellEditorListener(new CellEditorListener() {

            @Override
            public void editingStopped(ChangeEvent e) {
                ITask task = (ITask) ((TableCellEditor) e.getSource()).getCellEditorValue();
                task.onTaskClicked();
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                // Do nothing
            }
        });
    }

    /**
     * Returns a reference to the editor component.
     * @return the editor <code>Component</code>
     */
    public Component getComponent() {
        return this.editorComponent;
    }

    //
    // Modifying
    //

    /**
     * Specifies the number of clicks needed to start editing.
     * @param count an int specifying the number of clicks needed to start editing
     * @see #getClickCountToStart
     */
    public void setClickCountToStart(int count) {
        this.clickCountToStart = count;
    }

    /**
     * Returns the number of clicks needed to start editing.
     * @return the number of clicks needed to start editing
     */
    public int getClickCountToStart() {
        return this.clickCountToStart;
    }

    //
    // Override the implementations of the superclass, forwarding all methods
    // from the CellEditor interface to our delegate.
    //

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     *
     * @see EditorDelegate#getCellEditorValue
     */
    @Override
    public Object getCellEditorValue() {
        return this.delegate.getCellEditorValue();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     *
     * @see EditorDelegate#isCellEditable(EventObject)
     */
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return this.delegate.isCellEditable(anEvent);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     *
     * @see EditorDelegate#shouldSelectCell(EventObject)
     */
    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return this.delegate.shouldSelectCell(anEvent);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     *
     * @see EditorDelegate#stopCellEditing
     */
    @Override
    public boolean stopCellEditing() {
        return this.delegate.stopCellEditing();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the <code>delegate</code>.
     *
     * @see EditorDelegate#cancelCellEditing
     */
    @Override
    public void cancelCellEditing() {
        this.delegate.cancelCellEditing();
    }

    //
    // Implementing the CellEditor Interface
    //
    /** Implements the <code>TableCellEditor</code> interface. */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.delegate.setValue(value);
        // in order to avoid a "flashing" effect when clicking a checkbox
        // in a table, it is important for the editor to have as a border
        // the same border that the renderer has, and have as the background
        // the same color as the renderer has. This is primarily only
        // needed for JCheckBox since this editor doesn't fill all the
        // visual space of the table cell, unlike a text field.
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Component c = renderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
        if (c != null) {
            this.editorComponent.setOpaque(true);
            this.editorComponent.setBackground(c.getBackground());
            if (c instanceof JComponent) {
                this.editorComponent.setBorder(((JComponent) c).getBorder());
            }
        } else {
            this.editorComponent.setOpaque(false);
        }

        Icon icon = null;
        String tooltip = "";
        if (((ITask) value).isFinished() && ((ITask) value).hasResultDetails()) {
            tooltip = "task.seeDetails";
            icon = TaskResultTableCellRenderer.iconResult;
        } else {
            tooltip = "";
            icon = null;
        }
        this.editorComponent.setIcon(icon);
        this.editorComponent.setToolTipText(tooltip);
        return this.editorComponent;
    }

    //
    // Protected EditorDelegate class
    //

    /**
     * The protected <code>EditorDelegate</code> class.
     */
    protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

        /** The value of this cell. */
        protected Object value;

        /**
         * Returns the value of this cell.
         * @return the value of this cell
         */
        public Object getCellEditorValue() {
            return this.value;
        }

        /**
         * Sets the value of this cell.
         * @param value the new value of this cell
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Returns true if <code>anEvent</code> is <b>not</b> a <code>MouseEvent</code>. Otherwise, it
         * returns true if the necessary number of clicks have occurred, and returns false otherwise.
         * @param anEvent the event
         * @return true if cell is ready for editing, false otherwise
         * @see #setClickCountToStart
         * @see #shouldSelectCell
         */
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent) anEvent).getClickCount() >= TaskResultTableCellEditor.this.clickCountToStart;
            }
            return true;
        }

        /**
         * Returns true to indicate that the editing cell may be selected.
         * @param anEvent the event
         * @return true
         * @see #isCellEditable
         */
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        /**
         * Returns true to indicate that editing has begun.
         * @param anEvent the event
         */
        public boolean startCellEditing(EventObject anEvent) {
            return true;
        }

        /**
         * Stops editing and returns true to indicate that editing has stopped. This method calls
         * <code>fireEditingStopped</code>.
         * @return true
         */
        public boolean stopCellEditing() {
            TaskResultTableCellEditor.this.fireEditingStopped();
            return true;
        }

        /**
         * Cancels editing. This method calls <code>fireEditingCanceled</code>.
         */
        public void cancelCellEditing() {
            TaskResultTableCellEditor.this.fireEditingCanceled();
        }

        /**
         * When an action is performed, editing is ended.
         * @param e the action event
         * @see #stopCellEditing
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            TaskResultTableCellEditor.this.stopCellEditing();
        }

        /**
         * When an item's state changes, editing is ended.
         * @param e the action event
         * @see #stopCellEditing
         */
        @Override
        public void itemStateChanged(ItemEvent e) {
            TaskResultTableCellEditor.this.stopCellEditing();
        }

    }

}
