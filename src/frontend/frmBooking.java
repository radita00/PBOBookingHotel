package frontend;

import backend.Booking;
import backend.Customer;
import backend.Kamar;
import util.DatePicker;
import javax.swing.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;

public class frmBooking extends JFrame {
    private JComboBox<Customer> cmbCustomer;
    private JComboBox<Kamar> cmbKamar;
    private DatePicker dateCheckIn, dateCheckOut;
    private JButton btnCariKamar, btnBooking;

    public frmBooking() {
        setTitle("Form Booking / Check-In");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new java.awt.BorderLayout(10, 10));
        
        // --- Panel Input ---
        JPanel panelInput = new JPanel(new java.awt.GridLayout(5, 2, 5, 5));
        panelInput.setBorder(BorderFactory.createTitledBorder("Input Data Booking"));
        
        panelInput.add(new JLabel("Customer:"));
        cmbCustomer = new JComboBox<>();
        panelInput.add(cmbCustomer);
        
        panelInput.add(new JLabel("Tanggal Check-In:"));
        dateCheckIn = new DatePicker("yyyy-MM-dd");
        panelInput.add(dateCheckIn);
        
        panelInput.add(new JLabel("Tanggal Check-Out:"));
        dateCheckOut = new DatePicker("yyyy-MM-dd");
        panelInput.add(dateCheckOut);
        
        panelInput.add(new JLabel("Kamar Tersedia:"));
        cmbKamar = new JComboBox<>();
        panelInput.add(cmbKamar);
        
        JPanel panelButton = new JPanel();
        btnCariKamar = new JButton("Cari Kamar");
        btnBooking = new JButton("Booking");
        panelButton.add(btnCariKamar);
        panelButton.add(btnBooking);
        panelInput.add(panelButton);
        
        add(panelInput, java.awt.BorderLayout.CENTER);
        
        // --- LOGIKA ---
        isiCustomer();
        
        btnCariKamar.addActionListener(e -> cariKamar());
        btnBooking.addActionListener(e -> buatBooking());
        
        setLocationRelativeTo(null);
    }

    private void isiCustomer() {
        ArrayList<Customer> list = new Customer().getAll();
        cmbCustomer.removeAllItems();
        for (Customer c : list) {
            cmbCustomer.addItem(c);
        }
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada customer. Tambahkan customer terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cariKamar() {
        try {
            if (dateCheckIn.getDate() == null || dateCheckOut.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Tanggal check-in dan check-out harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validasi tanggal check-out harus setelah check-in
            if (!dateCheckOut.getDate().after(dateCheckIn.getDate())) {
                JOptionPane.showMessageDialog(this, "Tanggal check-out harus setelah tanggal check-in!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validasi tanggal check-in tidak boleh sebelum hari ini
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            if (dateCheckIn.getDate().before(today.getTime())) {
                JOptionPane.showMessageDialog(this, "Tanggal check-in tidak boleh sebelum hari ini!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Date tglIn = new Date(dateCheckIn.getDate().getTime());
            Date tglOut = new Date(dateCheckOut.getDate().getTime());

            ArrayList<Kamar> list = new Kamar().getKamarTersedia(tglIn, tglOut);
            
            cmbKamar.removeAllItems();
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada kamar tersedia pada tanggal tersebut.");
            } else {
                for (Kamar k : list) {
                    cmbKamar.addItem(k);
                }
                JOptionPane.showMessageDialog(this, "Ditemukan " + list.size() + " kamar tersedia.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buatBooking() {
        try {
            if (cmbCustomer.getSelectedItem() == null || cmbKamar.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Pilih customer dan kamar terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Customer selectedCustomer = (Customer) cmbCustomer.getSelectedItem();
            Kamar selectedKamar = (Kamar) cmbKamar.getSelectedItem();
            Date tglIn = new Date(dateCheckIn.getDate().getTime());
            Date tglOut = new Date(dateCheckOut.getDate().getTime());

            double hargaAwal = selectedKamar.getHarga();

            Booking b = new Booking(selectedCustomer, selectedKamar, tglIn, tglOut, hargaAwal);
            b.save();

            JOptionPane.showMessageDialog(this, "Booking berhasil dibuat! ID Booking: " + b.getId_booking(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            dateCheckIn.setDate(null);
            dateCheckOut.setDate(null);
            cmbKamar.removeAllItems();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuat booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}