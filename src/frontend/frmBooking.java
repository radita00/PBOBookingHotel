package frontend;

import backend.Booking;
import backend.Customer;
import backend.Kamar;
import backend.Users; 
import util.DatePicker;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class frmBooking extends JFrame {
    private JComboBox<Customer> cmbCustomer;
    private DatePicker dateCheckIn, dateCheckOut;
    private JButton btnTampilkanKamar, btnBooking;
    
    // Tiga Tabel
    private JTable tblKamarTersedia, tblKamarTerisi, tblKamarPerawatan;
    private DefaultTableModel modelTersedia, modelTerisi, modelPerawatan;
    
    private JTextField txtNomorKamar;
    private ArrayList<Kamar> daftarKamarTersedia;
    private Users userLogin; 
    
    private static final int MAX_TABLE_HEIGHT = 150; 

    public frmBooking(Users userLogin) {
        this.userLogin = userLogin; 
        
        setTitle("Form Booking / Check-In (ID Pegawai: " + userLogin.getId_user() + " - " + userLogin.getUsername() + ")");
        setSize(1000, 800); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // --- Panel Input (NORTH) ---
        JPanel panelInput = new JPanel(new GridLayout(6, 2, 5, 5));
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
        
        panelInput.add(new JLabel(""));
        btnTampilkanKamar = new JButton("Tampilkan Kamar Tersedia");
        panelInput.add(btnTampilkanKamar);
        
        panelInput.add(new JLabel("Nomor Kamar Dipilih:"));
        txtNomorKamar = new JTextField();
        txtNomorKamar.setEditable(false);
        txtNomorKamar.setBackground(Color.LIGHT_GRAY);
        panelInput.add(txtNomorKamar);
        
        JPanel panelButtonBooking = new JPanel();
        btnBooking = new JButton("Booking");
        panelButtonBooking.add(btnBooking);
        panelInput.add(new JLabel(""));
        panelInput.add(panelButtonBooking);
        
        add(panelInput, BorderLayout.NORTH);
        
        // --- Panel Tabel (CENTER) ---
        JPanel panelTabelUtama = new JPanel();
        panelTabelUtama.setLayout(new BoxLayout(panelTabelUtama, BoxLayout.Y_AXIS));
        panelTabelUtama.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Inisialisasi Tiga Tabel dan Model secara eksplisit:
        
        // 1. Kamar Tersedia (Selectable)
        Object[] kolom = {"Pilih", "No. Kamar", "Tipe Kamar", "Harga/Malam", "Status"};
        modelTersedia = new DefaultTableModel(kolom, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKamarTersedia = new JTable(modelTersedia);
        initTableListeners(tblKamarTersedia, true); // Tabel dapat diklik
        panelTabelUtama.add(createTablePanel("KAMAR TERSEDIA", tblKamarTersedia, MAX_TABLE_HEIGHT, true));
        panelTabelUtama.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // 2. Kamar Terisi (Non-Selectable)
        Object[] kolomStatus = {"No. Kamar", "Tipe Kamar", "Harga/Malam", "Status"};
        modelTerisi = new DefaultTableModel(kolomStatus, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKamarTerisi = new JTable(modelTerisi);
        initTableListeners(tblKamarTerisi, false); // Tabel tidak dapat diklik
        panelTabelUtama.add(createTablePanel("KAMAR TERISI (Sedang Check-in)", tblKamarTerisi, MAX_TABLE_HEIGHT, false));
        panelTabelUtama.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // 3. Kamar Perawatan (Non-Selectable)
        modelPerawatan = new DefaultTableModel(kolomStatus, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKamarPerawatan = new JTable(modelPerawatan);
        initTableListeners(tblKamarPerawatan, false); // Tabel tidak dapat diklik
        panelTabelUtama.add(createTablePanel("KAMAR PERAWATAN", tblKamarPerawatan, MAX_TABLE_HEIGHT / 2, false));
        
        add(panelTabelUtama, BorderLayout.CENTER);
        
        // --- LOGIKA ---
        daftarKamarTersedia = new ArrayList<>();
        isiCustomer();
        isiSemuaTabelStatus(); // Isi tabel status saat start
        
        // Event listener untuk otomatis menampilkan kamar setelah memilih check-out
        dateCheckOut.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // Hanya tampilkan jika check-in dan check-out sudah diisi
                if (dateCheckIn.getDate() != null && dateCheckOut.getDate() != null) {
                    tampilkanKamarTersedia();
                }
            }
        });
        
        btnTampilkanKamar.addActionListener(e -> tampilkanKamarTersedia());
        btnBooking.addActionListener(e -> buatBooking());
        
        setLocationRelativeTo(null);
    }
    
    /**
     * Metode pembantu untuk mengatur properti dasar JTable dan menambahkan listeners.
     * Dipanggil di konstruktor.
     * @param table JTable yang akan diatur
     * @param isClickable apakah tabel dapat diklik/dipilih
     */
    private void initTableListeners(JTable table, boolean isClickable) {
        table.getTableHeader().setReorderingAllowed(false);
        
        if (isClickable) {
            // Hanya tabel tersedia yang dapat diklik
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // Event listener HANYA untuk tabel Kamar Tersedia
            if (table == tblKamarTersedia) {
                table.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        if (evt.getClickCount() == 1) {
                            pilihKamarDariTabel();
                        } else if (evt.getClickCount() == 2) {
                            buatBooking();
                        }
                    }
                });
            }
        } else {
            // Tabel terisi dan perawatan tidak dapat diklik
            table.setEnabled(false);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setBackground(new Color(240, 240, 240));
            table.setForeground(Color.DARK_GRAY);
        }
    }
    
    /**
     * Metode pembantu untuk membuat panel dengan JTable dan judul.
     */
    private JPanel createTablePanel(String title, JTable table, int maxHeight, boolean showInfo) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Label judul tabel dengan font tebal dan warna
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(new Color(0, 102, 204));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(lblTitle, BorderLayout.NORTH);
        
        // ScrollPane dengan tabel
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width, maxHeight));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scroll, BorderLayout.CENTER);
        
        // Info tambahan untuk tabel tersedia
        if (showInfo) {
            JLabel lblInfo = new JLabel("💡 Klik untuk memilih kamar | Double-click untuk Booking langsung");
            lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
            lblInfo.setForeground(new Color(100, 100, 100));
            lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            panel.add(lblInfo, BorderLayout.SOUTH);
        }
        
        return panel;
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
    
    // --- Metode Baru: Isi semua tabel status (Terisi & Perawatan) ---
    private void isiSemuaTabelStatus() {
        // Isi Kamar Terisi
        ArrayList<Kamar> listTerisi = new Kamar().getOccupied();
        modelTerisi.setRowCount(0);
        for (Kamar k : listTerisi) {
            modelTerisi.addRow(new Object[]{
                k.getNomor_kamar(), k.getTipe(), String.format("Rp %,.0f", k.getHarga()), k.getStatus()
            });
        }
        
        // Isi Kamar Perawatan
        ArrayList<Kamar> listPerawatan = new Kamar().getMaintenance();
        modelPerawatan.setRowCount(0);
        for (Kamar k : listPerawatan) {
            modelPerawatan.addRow(new Object[]{
                k.getNomor_kamar(), k.getTipe(), String.format("Rp %,.0f", k.getHarga()), k.getStatus()
            });
        }
    }

    private void tampilkanKamarTersedia() {
        try {
            if (dateCheckIn.getDate() == null || dateCheckOut.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Tanggal check-in dan check-out harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validasi tanggal
            if (!dateCheckOut.getDate().after(dateCheckIn.getDate())) {
                JOptionPane.showMessageDialog(this, "Tanggal check-out harus setelah tanggal check-in!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validasi tanggal tidak boleh masa lalu (kecuali hari ini)
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

            // Panggil query ketersediaan dari kelas Kamar
            daftarKamarTersedia = new Kamar().getKamarTersedia(tglIn, tglOut);
            
            modelTersedia.setRowCount(0);
            
            if (daftarKamarTersedia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada kamar tersedia pada tanggal tersebut.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (int i = 0; i < daftarKamarTersedia.size(); i++) {
                    Kamar k = daftarKamarTersedia.get(i);
                    Object[] row = {
                        "[ ]",  // Kolom pilih
                        k.getNomor_kamar(),
                        k.getTipe(),
                        String.format("Rp %,.0f", k.getHarga()),
                        k.getStatus()
                    };
                    modelTersedia.addRow(row);
                }
                JOptionPane.showMessageDialog(this, "Ditemukan " + daftarKamarTersedia.size() + " kamar tersedia.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Bersihkan teks kamar yang dipilih
            txtNomorKamar.setText("");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void pilihKamarDariTabel() {
        int selectedRow = tblKamarTersedia.getSelectedRow();
        if (selectedRow != -1) {
            // Update kolom pilih untuk menunjukkan kamar yang dipilih
            for (int i = 0; i < modelTersedia.getRowCount(); i++) {
                modelTersedia.setValueAt("[ ]", i, 0);
            }
            modelTersedia.setValueAt("[✓]", selectedRow, 0);
            
            String nomorKamar = modelTersedia.getValueAt(selectedRow, 1).toString();
            txtNomorKamar.setText(nomorKamar);
        }
    }

    private void buatBooking() {
        try {
            if (cmbCustomer.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Pilih customer terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int selectedRow = tblKamarTersedia.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih kamar dari tabel Kamar Tersedia terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (dateCheckIn.getDate() == null || dateCheckOut.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Tanggal check-in dan check-out harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Kamar selectedKamar = daftarKamarTersedia.get(selectedRow);
            
            Customer selectedCustomer = (Customer) cmbCustomer.getSelectedItem();
            Date tglIn = new Date(dateCheckIn.getDate().getTime());
            Date tglOut = new Date(dateCheckOut.getDate().getTime());
            double hargaAwal = selectedKamar.getHarga();

            // Panggil konstruktor Booking yang menyertakan userLogin
            Booking b = new Booking(selectedCustomer, selectedKamar, userLogin, tglIn, tglOut, hargaAwal);
            b.save(); 

            JOptionPane.showMessageDialog(this, 
                "Booking berhasil dibuat!\n" +
                "ID Booking: " + b.getId_booking() + "\n" +
                "Kamar: " + selectedKamar.getNomor_kamar() + " (" + selectedKamar.getTipe() + ")", 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            // Reset form dan update semua tabel
            dateCheckIn.setDate(null);
            dateCheckOut.setDate(null);
            txtNomorKamar.setText("");
            modelTersedia.setRowCount(0);
            daftarKamarTersedia.clear();
            isiSemuaTabelStatus(); // Update status Terisi setelah booking

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuat booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}