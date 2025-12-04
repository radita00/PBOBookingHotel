package util;

import javax.swing.*;
import javax.swing.JFormattedTextField;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePicker extends JPanel {
    private JFormattedTextField textField;
    private JButton btnPickDate;
    private SimpleDateFormat dateFormat;

    public DatePicker() {
        this("dd-MM-yyyy");
    }

    public DatePicker(String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
        initComponents();
    }

    private void initComponents() {
        setLayout(new java.awt.BorderLayout(5, 0));
        
        // Text field for displaying the selected date
        textField = new JFormattedTextField();
        textField.setColumns(10);
        textField.setEditable(false);
        
        // Button to show calendar popup
        btnPickDate = new JButton("...");
        btnPickDate.addActionListener(e -> showDatePicker());
        
        // Add components to panel
        add(textField, java.awt.BorderLayout.CENTER);
        add(btnPickDate, java.awt.BorderLayout.EAST);
    }
    
    private void showDatePicker() {
        // Get current date
        Date date = getDate();
        if (date == null) {
            date = new Date();
        }
        
        // Create a calendar and set it to the current date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        
        // Show JOptionPane with JSpinner for date selection
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, dateFormat.toPattern());
        dateSpinner.setEditor(dateEditor);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            dateSpinner,
            "Pilih Tanggal",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            setDate(((SpinnerDateModel) dateSpinner.getModel()).getDate());
        }
    }
    
    public Date getDate() {
        try {
            return textField.getText().isEmpty() ? null : dateFormat.parse(textField.getText());
        } catch (ParseException e) {
            return null;
        }
    }
    
    public JFormattedTextField getTextField() {
        return textField;
    }
    
    public void setDate(Date date) {
        if (date != null) {
            textField.setText(dateFormat.format(date));
        } else {
            textField.setText("");
        }
    }
    
    public String getDateAsString() {
        return textField.getText();
    }
    
    public void setDateAsString(String dateString) {
        try {
            if (dateString != null && !dateString.trim().isEmpty()) {
                Date date = dateFormat.parse(dateString);
                setDate(date);
            } else {
                textField.setText("");
            }
        } catch (ParseException e) {
            textField.setText("");
        }
    }
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
        btnPickDate.setEnabled(enabled);
    }
}
