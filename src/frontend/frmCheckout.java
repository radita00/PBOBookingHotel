package frontend;

import util.DatePicker;
import java.sql.Date;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import backend.Booking;
import backend.TransaksiCheckout;
import backend.Users; // 👈 IMPOR KELAS USER

public class frmCheckout extends javax.swing.JFrame {
    private JComboBox<Booking> cmbBookingAktif;
    private DatePicker dateCheckout;
    private JComboBox<String> cmbMetode;
    private JButton btnCheckout, btnRefresh;
    private JLabel lblTotalHarga, lblLamaInap, lblCustomer, lblKamar, lblCheckIn, lblCheckOut;
    private JLabel lblTotalHargaValue, lblLamaInapValue, lblCustomerValue, 
                             lblKamarValue, lblCheckInValue, lblCheckOutValue;

    // BARU: Input untuk Kode Transaksi
    private JTextField txtKodeTransaksi; 

    private int currentUserId; 
    private String currentUsername; 

    public frmCheckout(int id_user) {
        this.currentUserId = id_user;
        
        
        try {
            this.currentUsername = Users.getUsernameById(id_user); 
        } catch (Exception e) {
            this.currentUsername = "USERNAME_ERROR";
            System.err.println("Gagal memuat username untuk ID " + id_user + ": " + e.getMessage());
        }
        
        initComponents();
        isiBookingAktif();
        // Hanya untuk debugging:
        System.out.println("frmCheckout dibuka oleh User ID: " + this.currentUserId + " (" + this.currentUsername + ")");
    }

    private void initComponents() {
        // 🎯 MODIFIKASI: TAMPILKAN USERNAME DI TITLE
        setTitle("Form Check-Out (Dilayani oleh ID: " + this.currentUserId + " - " + this.currentUsername + ")"); 
        setSize(700, 550); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Data Check-Out"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Initialize components
        cmbBookingAktif = new JComboBox<>();
        dateCheckout = new DatePicker("yyyy-MM-dd");
        cmbMetode = new JComboBox<>(new String[]{"Tunai", "Transfer", "Kartu Kredit"}); 
        
        // BARU: Initialize Kode Transaksi
        txtKodeTransaksi = new JTextField(20);
        
        // **Default: Kosong dan Non-aktif**
        txtKodeTransaksi.setText(""); 
        txtKodeTransaksi.setEnabled(false); // Default: Non-editable

        // Initialize labels
        lblCustomer = new JLabel("Customer:");
        lblKamar = new JLabel("Kamar:");
        lblCheckIn = new JLabel("Check-in:");
        lblCheckOut = new JLabel("Check-out:");
        lblLamaInap = new JLabel("Lama Menginap:");
        lblTotalHarga = new JLabel("Total Harga:");
        
        lblCustomerValue = new JLabel("-");
        lblKamarValue = new JLabel("-");
        lblCheckInValue = new JLabel("-");
        lblCheckOutValue = new JLabel("-");
        lblLamaInapValue = new JLabel("0 hari");
        lblTotalHargaValue = new JLabel("Rp 0");

        // Add components to input panel
        int row = 0;
        
        // Booking selection
        addInputRow(inputPanel, gbc, "Booking Aktif:", cmbBookingAktif, row++);
        
        // Customer info
        addLabelRow(inputPanel, gbc, lblCustomer, lblCustomerValue, row++);
        
        // Kamar info
        addLabelRow(inputPanel, gbc, lblKamar, lblKamarValue, row++);
        
        // Check-in info
        addLabelRow(inputPanel, gbc, lblCheckIn, lblCheckInValue, row++);
        
        // Checkout date
        addInputRow(inputPanel, gbc, "Tanggal Checkout:", dateCheckout, row++);
        
        // Check-out info
        addLabelRow(inputPanel, gbc, lblCheckOut, lblCheckOutValue, row++);
        
        // Lama menginap
        addLabelRow(inputPanel, gbc, lblLamaInap, lblLamaInapValue, row++);
        
        // Total harga
        addLabelRow(inputPanel, gbc, lblTotalHarga, lblTotalHargaValue, row++);
        
        // Metode pembayaran
        addInputRow(inputPanel, gbc, "Metode Pembayaran:", cmbMetode, row++);

        // BARU: Kode Transaksi
        addInputRow(inputPanel, gbc, "Kode Transaksi:", txtKodeTransaksi, row++);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCheckout = new JButton("Proses Checkout");
        btnRefresh = new JButton("Refresh Data");
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCheckout);

        // Add action listeners
        btnCheckout.addActionListener(e -> prosesCheckout());
        btnRefresh.addActionListener(e -> isiBookingAktif());
        cmbBookingAktif.addActionListener(e -> updateBookingDetails());
        
        // BARU: Listener untuk perubahan metode pembayaran
        cmbMetode.addActionListener(e -> handleMetodeChange()); 
        
        // Add document listener to the text field to detect date changes
        dateCheckout.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateLamaInap(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateLamaInap(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateLamaInap(); }
        });

        // Add components to main panel
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
        
        // Inisialisasi awal status kode transaksi 
        handleMetodeChange(); 
        
        // Center the frame
        setLocationRelativeTo(null);
    }
    
    // 🚨 MODIFIKASI INTI: Auto-fill dihilangkan
    private void handleMetodeChange() {
        String selectedMetode = cmbMetode.getSelectedItem().toString();
        boolean isTunai = selectedMetode.equalsIgnoreCase("Tunai");

        if (isTunai) {
            // Jika Tunai, kosongkan field dan non-aktifkan
            txtKodeTransaksi.setText("");
            txtKodeTransaksi.setEnabled(false);
            txtKodeTransaksi.setBackground(Color.LIGHT_GRAY);
            txtKodeTransaksi.setToolTipText("Tidak diperlukan untuk pembayaran Tunai.");
        } else {
            // Jika Non-Tunai, aktifkan. TETAP BIARKAN KOSONG
            txtKodeTransaksi.setEnabled(true);
            txtKodeTransaksi.setBackground(Color.WHITE);
            txtKodeTransaksi.setToolTipText("Kode transaksi WAJIB diisi manual.");
            // Logika auto-generate sudah dihapus
        }
    }

    private void addInputRow(JPanel panel, GridBagConstraints gbc, String label, JComponent component, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(component, gbc);
    }

    private void addLabelRow(JPanel panel, GridBagConstraints gbc, JLabel label, JLabel value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(value, gbc);
    }

    private void isiBookingAktif() {
        try {
            cmbBookingAktif.removeAllItems();
            // Asumsi method ini ada di class Booking
            ArrayList<Booking> bookings = new Booking().getBookingAktif(); 
            for (Booking b : bookings) {
                cmbBookingAktif.addItem(b);
            }
            updateBookingDetails();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data booking: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBookingDetails() {
        Booking selectedBooking = (Booking) cmbBookingAktif.getSelectedItem();
        if (selectedBooking != null) {
            lblCustomerValue.setText(selectedBooking.getCustomer().getNama());
            lblKamarValue.setText(selectedBooking.getKamar().getNomor_kamar() + " - " + 
                                 selectedBooking.getKamar().getTipe());
            lblCheckInValue.setText(selectedBooking.getTanggal_checkin().toString());
            lblCheckOutValue.setText(selectedBooking.getTanggal_checkout().toString());
            lblTotalHargaValue.setText("Rp " + String.format("%,.2f", selectedBooking.getTotal_harga()));
            
            // Set default checkout date to the booking's checkout date
            dateCheckout.setDate(selectedBooking.getTanggal_checkout());
            
            updateLamaInap();
            
            // Panggil handleMetodeChange untuk mengatur status txtKodeTransaksi
            handleMetodeChange(); 
            
        } else {
            // Reset fields if no booking is selected
            lblCustomerValue.setText("-");
            lblKamarValue.setText("-");
            lblCheckInValue.setText("-");
            lblCheckOutValue.setText("-");
            lblLamaInapValue.setText("0 hari");
            lblTotalHargaValue.setText("Rp 0");
            dateCheckout.setDate(null);
            // Bersihkan input kode transaksi saat booking di-reset
            txtKodeTransaksi.setText(""); 
        }
    }

    private void updateLamaInap() {
        Booking selectedBooking = (Booking) cmbBookingAktif.getSelectedItem();
        if (selectedBooking != null && dateCheckout.getDate() != null) {
            try {
                // Konversi java.util.Date dari DatePicker ke java.sql.Date
                Date tglCheckoutAktual = new Date(dateCheckout.getDate().getTime());
                
                // Hitung lama inap menggunakan method di TransaksiCheckout
                int days = TransaksiCheckout.hitungLamaInap(selectedBooking.getTanggal_checkin(), tglCheckoutAktual);
                
                lblLamaInapValue.setText(days + " hari");
                
                // Update total price
                double hargaPerMalam = selectedBooking.getKamar().getHarga();
                double totalHarga = hargaPerMalam * days;
                lblTotalHargaValue.setText("Rp " + String.format("%,.2f", totalHarga));
                
            } catch (Exception e) {
                e.printStackTrace();
                // Jika terjadi error, reset nilai
                lblLamaInapValue.setText("Error");
                lblTotalHargaValue.setText("Rp 0");
            }
        }
    }

    private void prosesCheckout() {
        try {
            if (cmbBookingAktif.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Pilih booking terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (dateCheckout.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Pilih tanggal checkout terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (this.currentUserId <= 0) { 
                JOptionPane.showMessageDialog(this, "ID Pegawai/User belum diatur. Proses checkout dibatalkan.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Booking selectedBooking = (Booking) cmbBookingAktif.getSelectedItem();
            Date tglCheckoutAktual = new Date(dateCheckout.getDate().getTime());
            String metode = cmbMetode.getSelectedItem().toString();
            String kodeTransaksi = null;

            // LOGIKA KODE TRANSAKSI: Diambil dari input user
            if (!metode.equalsIgnoreCase("Tunai")) {
                kodeTransaksi = txtKodeTransaksi.getText().trim();
                if (kodeTransaksi.isEmpty()) {
                    // Validasi ini memastikan pegawai harus input manual
                    JOptionPane.showMessageDialog(this, 
                        "Kode transaksi wajib diisi untuk metode Non-Tunai.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // Untuk Tunai, kodeTransaksi tetap null

            // Validasi tanggal checkout
            if (tglCheckoutAktual.before(selectedBooking.getTanggal_checkin())) {
                JOptionPane.showMessageDialog(this, "Tanggal checkout tidak boleh sebelum tanggal check-in", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Show confirmation
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Konfirmasi Checkout:\n" +
                "Kamar: " + selectedBooking.getKamar().getNomor_kamar() + "\n" +
                "Customer: " + selectedBooking.getCustomer().getNama() + "\n" +
                "Check-in: " + selectedBooking.getTanggal_checkin() + "\n" +
                "Checkout: " + tglCheckoutAktual + "\n" +
                "Lama Menginap: " + lblLamaInapValue.getText() + "\n" +
                "Total Bayar: " + lblTotalHargaValue.getText() + "\n" +
                "Metode Pembayaran: " + metode + 
                (kodeTransaksi != null ? "\nKode Transaksi: " + kodeTransaksi : "") + 
                // 🎯 MODIFIKASI: TAMPILKAN USERNAME DI KONFIRMASI
                "\nDilayani oleh: ID " + this.currentUserId + " - " + this.currentUsername,
                "Konfirmasi Checkout",
                JOptionPane.YES_NO_OPTION
            );
                
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            // Process checkout
            TransaksiCheckout tc = new TransaksiCheckout();
            tc.setBooking(selectedBooking);
            tc.setTanggal_checkout_aktual(tglCheckoutAktual);
            tc.setMetode(metode);
            tc.setId_user(this.currentUserId); 
            // PENTING: Set kode transaksi yang sudah divalidasi/diinput
            tc.setKode_transaksi(kodeTransaksi); 
            
            // Save transaction
            tc.save();

            // Reset form
            dateCheckout.setDate(null);
            cmbMetode.setSelectedIndex(0);
            txtKodeTransaksi.setText(""); // Bersihkan input manual
            handleMetodeChange(); // Atur ulang status field kode transaksi (menjadi non-aktif dan kosong)
            
            // Update booking list
            isiBookingAktif();
            
        } catch (Exception e) {
            // Pesan error sudah ditangani di TransaksiCheckout.save()
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Ganti angka 1 ini dengan ID user yang benar-benar login
            new frmCheckout(1).setVisible(true); 
        });
    }
}