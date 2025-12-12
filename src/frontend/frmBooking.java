package frontend;

import backend.Booking;
import backend.Customer;
import backend.Kamar;
import backend.Users; // Import Users
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
    private JTable tblKamar;
    private DefaultTableModel modelTabel;
    private JTextField txtNomorKamar;
    private JScrollPane scrollPane;
    private ArrayList<Kamar> daftarKamarTersedia;
    
    private Users userLogin; // BARU: Field untuk menyimpan user yang sedang login

    // MODIFIKASI KONSTRUKTOR untuk menerima Users
    public frmBooking(Users userLogin) {
        this.userLogin = userLogin; // Simpan user yang sedang login
        
        setTitle("Form Booking / Check-In (Operator: " + userLogin.getUsername() + ")"); // Opsional: Tampilkan nama user
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // --- Panel Input ---
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
        txtNomorKamar.setToolTipText("Nomor kamar yang dipilih dari tabel");
        panelInput.add(txtNomorKamar);
        
        JPanel panelButtonBooking = new JPanel();
        btnBooking = new JButton("Booking");
        panelButtonBooking.add(btnBooking);
        panelInput.add(new JLabel(""));
        panelInput.add(panelButtonBooking);
        
        add(panelInput, BorderLayout.NORTH);
        
        // --- Panel Tabel ---
        JPanel panelTabel = new JPanel(new BorderLayout());
        panelTabel.setBorder(BorderFactory.createTitledBorder("Kamar Tersedia"));
        
        String[] kolom = {"No. Kamar", "Tipe Kamar", "Harga/Malam", "Status"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblKamar = new JTable(modelTabel);
        tblKamar.getTableHeader().setReorderingAllowed(false);
        tblKamar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Event saat single click untuk memilih, double click untuk langsung booking
        tblKamar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    pilihKamarDariTabel();
                } else if (evt.getClickCount() == 2) {
                    buatBooking();
                }
            }
        });
        
        scrollPane = new JScrollPane(tblKamar);
        panelTabel.add(scrollPane, BorderLayout.CENTER);
        
        JLabel lblInfo = new JLabel("Klik untuk memilih kamar | Double-click untuk langsung booking");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        panelTabel.add(lblInfo, BorderLayout.SOUTH);
        
        add(panelTabel, BorderLayout.CENTER);
        
        // --- LOGIKA ---
        daftarKamarTersedia = new ArrayList<>();
        isiCustomer();
        
        // Event listener untuk otomatis menampilkan kamar setelah memilih check-out
        dateCheckOut.addPropertyChangeListener("date", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (dateCheckIn.getDate() != null && dateCheckOut.getDate() != null) {
                    tampilkanKamarTersedia();
                }
            }
        });
        
        btnTampilkanKamar.addActionListener(e -> tampilkanKamarTersedia());
        btnBooking.addActionListener(e -> buatBooking());
        
        setLocationRelativeTo(null);
    }

    // KONSTRUKTOR DEFAULT (Dihapus atau dipertahankan jika dibutuhkan, tetapi tidak disarankan)
    // public frmBooking() {
    //    this(new Users().getById(1)); // Contoh: Jika Anda ingin menggunakan ID 1 sebagai default jika tidak ada user login
    // }

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

    private void tampilkanKamarTersedia() {
        // ... (Logika tampilkanKamarTersedia tetap sama)
        try {
            if (dateCheckIn.getDate() == null || dateCheckOut.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Tanggal check-in dan check-out harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!dateCheckOut.getDate().after(dateCheckIn.getDate())) {
                JOptionPane.showMessageDialog(this, "Tanggal check-out harus setelah tanggal check-in!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
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

            daftarKamarTersedia = new Kamar().getKamarTersedia(tglIn, tglOut);
            
            modelTabel.setRowCount(0);
            
            if (daftarKamarTersedia.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada kamar tersedia pada tanggal tersebut.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                for (Kamar k : daftarKamarTersedia) {
                    Object[] row = {
                        k.getNomor_kamar(),
                        k.getTipe(),
                        String.format("Rp %,.0f", k.getHarga()),
                        k.getStatus()
                    };
                    modelTabel.addRow(row);
                }
                JOptionPane.showMessageDialog(this, "Ditemukan " + daftarKamarTersedia.size() + " kamar tersedia.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void pilihKamarDariTabel() {
        int selectedRow = tblKamar.getSelectedRow();
        if (selectedRow != -1) {
            String nomorKamar = modelTabel.getValueAt(selectedRow, 0).toString();
            txtNomorKamar.setText(nomorKamar);
        }
    }

    private void buatBooking() {
        try {
            if (cmbCustomer.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Pilih customer terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int selectedRow = tblKamar.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Pilih kamar dari tabel terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
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

            // MODIFIKASI: Panggil konstruktor Booking yang baru (termasuk userLogin)
            Booking b = new Booking(selectedCustomer, selectedKamar, userLogin, tglIn, tglOut, hargaAwal);
b.save();

            JOptionPane.showMessageDialog(this, 
                "Booking berhasil dibuat!\n" +
                "Operator: " + userLogin.getUsername() + "\n" + // Tampilkan operator yang membuat booking
                "ID Booking: " + b.getId_booking() + "\n" +
                "Kamar: " + selectedKamar.getNomor_kamar() + "\n" +
                "Tipe: " + selectedKamar.getTipe() + "\n" +
                "Harga: Rp " + String.format("%,.0f", hargaAwal), 
                "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            // Reset form
            dateCheckIn.setDate(null);
            dateCheckOut.setDate(null);
            txtNomorKamar.setText("");
            modelTabel.setRowCount(0);
            daftarKamarTersedia.clear();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuat booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}