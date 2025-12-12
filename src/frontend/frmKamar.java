package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import backend.Kamar;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class frmKamar extends JFrame {
    private JTextField txtNomor, txtHarga;
    private JComboBox<String> cmbTipe, cmbStatus;
    private JTable tblKamar;
    private JButton btnSimpan, btnHapus, btnClear;
    private JTextField txtCariKamar;
    private int idKamarSedangEdit = 0;

    public frmKamar() {
        setTitle("Form Master Kamar");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new java.awt.BorderLayout(10, 10));
        
        // --- Panel Input dan Search (NORTH) ---
        JPanel panelNorth = new JPanel(new BorderLayout());
        
        // 1. Panel Input (Kiri/Atas)
        JPanel panelInput = new JPanel(new java.awt.GridLayout(5, 2, 5, 10));
        panelInput.setBorder(BorderFactory.createTitledBorder("Input Data Kamar"));
        
        panelInput.add(new JLabel("Nomor Kamar:"));
        txtNomor = new JTextField(15);
        panelInput.add(txtNomor);
        
        panelInput.add(new JLabel("Tipe Kamar:"));
        cmbTipe = new JComboBox<>(new String[]{"Standard", "Advanced"});
        panelInput.add(cmbTipe);
        
        panelInput.add(new JLabel("Harga:"));
        JPanel panelHarga = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        txtHarga = new JTextField(10);
        panelHarga.add(txtHarga);
        panelHarga.add(new JLabel("/ Malam"));
        panelInput.add(panelHarga);
        
        panelInput.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"kosong", "terisi", "perawatan"});
        panelInput.add(cmbStatus);
        
        JPanel panelButton = new JPanel();
        btnSimpan = new JButton("Simpan");
        btnHapus = new JButton("Hapus");
        btnClear = new JButton("Bersihkan");
        panelButton.add(btnSimpan);
        panelButton.add(btnHapus);
        panelButton.add(btnClear);
        panelInput.add(panelButton);
        
        // 2. Panel Search (Bawah Panel Input)
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtCariKamar = new JTextField(20);
        JLabel lblCari = new JLabel("Cari No. Kamar/Tipe:");
        panelSearch.add(lblCari);
        panelSearch.add(txtCariKamar);
        
        panelNorth.add(panelInput, BorderLayout.CENTER);
        panelNorth.add(panelSearch, BorderLayout.SOUTH);
        
        add(panelNorth, BorderLayout.NORTH);
        
        // --- Tabel ---
        tblKamar = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblKamar);
        add(scrollPane, BorderLayout.CENTER);
        
        // --- LOGIKA ---
        tampilkanData("");
        
        // Listener Pencarian
        txtCariKamar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                tampilkanData(txtCariKamar.getText());
            }
        });
        
        btnSimpan.addActionListener(e -> simpanData());
        btnClear.addActionListener(e -> clearForm());
        btnHapus.addActionListener(e -> hapusData());
        btnHapus.setEnabled(false);
        
        tblKamar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblKamar.getSelectedRow();
                if (row >= 0) {
                    idKamarSedangEdit = Integer.parseInt(tblKamar.getValueAt(row, 0).toString());
                    txtNomor.setText(tblKamar.getValueAt(row, 1).toString());
                    cmbTipe.setSelectedItem(tblKamar.getValueAt(row, 2).toString());
                    txtHarga.setText(tblKamar.getValueAt(row, 3).toString());
                    cmbStatus.setSelectedItem(tblKamar.getValueAt(row, 4).toString());
                    
                    btnSimpan.setText("Update");
                    btnHapus.setEnabled(true);
                }
            }
        });
        
        setLocationRelativeTo(null);
    }
    
    // Modifikasi method untuk menerima keyword
    private void tampilkanData(String keyword) {
        String[] kolom = {"ID", "Nomor", "Tipe", "Harga", "Status"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        
        Kamar kamarHelper = new Kamar();
        ArrayList<Kamar> list;
        
        if (keyword.isEmpty()) {
            list = kamarHelper.getAll();
        } else {
            list = kamarHelper.searchKamar(keyword);
        }
        
        for (Kamar k : list) {
            Object[] row = {
                k.getId_kamar(),
                k.getNomor_kamar(),
                k.getTipe(),
                k.getHarga(),
                k.getStatus()
            };
            model.addRow(row);
        }
        tblKamar.setModel(model);
    }
    
    private void simpanData() {
        try {
            if (txtNomor.getText().isEmpty() || txtHarga.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Kamar k;
            String statusLama = "";
            String statusBaru = cmbStatus.getSelectedItem().toString();
            
            if (idKamarSedangEdit > 0) {
                k = new Kamar().getById(idKamarSedangEdit);
                if (k.getId_kamar() == 0) {
                    JOptionPane.showMessageDialog(this, "Data kamar tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                statusLama = k.getStatus();
                
                // 1. Validasi Kamar sedang di-booking (status 'terisi')
                if (statusLama.equals("terisi")) {
                    // Cek apakah ada upaya untuk mengubah status dari 'terisi'
                    if (!statusBaru.equals(statusLama)) {
                        JOptionPane.showMessageDialog(this, 
                            "Kamar " + k.getNomor_kamar() + " (ID: " + idKamarSedangEdit + ") sedang terisi (booked) dan tidak dapat diubah statusnya.", 
                            "Peringatan Status", JOptionPane.WARNING_MESSAGE);
                        // Kembalikan ComboBox ke status lama dan hentikan proses
                        cmbStatus.setSelectedItem(statusLama);
                        return;
                    }
                } 
                
                // 2. Validasi Kamar tidak boleh diubah ke 'terisi' secara manual (harus melalui booking)
                if (statusBaru.equals("terisi") && !statusLama.equals("terisi")) {
                    JOptionPane.showMessageDialog(this, 
                        "Kamar hanya dapat diubah statusnya menjadi 'terisi' melalui proses Booking/Check-in.", 
                        "Peringatan Status", JOptionPane.WARNING_MESSAGE);
                    // Kembalikan ComboBox ke status lama dan hentikan proses
                    cmbStatus.setSelectedItem(statusLama);
                    return;
                }
                
                // 3. Validasi Kamar 'perawatan' tidak bisa dibooking/diisi (sama dengan poin 2)
                // Jika statusLama='perawatan' dan statusBaru='terisi', maka sudah terhalang oleh poin 2.
                // Jika kita ingin memastikan kamar perawatan tetap perawatan/kosong/lain, kita bisa tambahkan logika di sini.
                // Untuk saat ini, kita hanya memastikan 'terisi' tidak bisa diset manual.

            } else {
                // Proses Insert, Kamar baru selalu diset 'kosong'
                k = new Kamar();
                if (statusBaru.equals("terisi")) {
                    JOptionPane.showMessageDialog(this, "Kamar baru tidak bisa diset status 'terisi'.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            // Lanjutkan penyimpanan jika validasi lolos
            k.setNomor_kamar(txtNomor.getText());
            k.setTipe(cmbTipe.getSelectedItem().toString());
            k.setHarga(Double.parseDouble(txtHarga.getText()));
            k.setStatus(statusBaru); // Gunakan statusBaru yang sudah divalidasi
            
            k.save();
            
            JOptionPane.showMessageDialog(this, 
                idKamarSedangEdit > 0 ? "Data berhasil diupdate!" : "Data berhasil disimpan!");
                
            tampilkanData("");
            clearForm();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        idKamarSedangEdit = 0;
        txtNomor.setText("");
        cmbTipe.setSelectedIndex(0);
        txtHarga.setText("");
        cmbStatus.setSelectedIndex(0);
        btnSimpan.setText("Simpan");
        btnHapus.setEnabled(false);
        txtCariKamar.setText("");
        tampilkanData("");
    }

    private void hapusData() {
        int row = tblKamar.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int id = Integer.parseInt(tblKamar.getValueAt(row, 0).toString());
        String statusKamar = tblKamar.getValueAt(row, 4).toString();
        String nomorKamar = tblKamar.getValueAt(row, 1).toString();
        
        if (statusKamar.equals("terisi")) {
            JOptionPane.showMessageDialog(this, "Kamar " + nomorKamar + " (ID: " + id + ") sedang terisi dan tidak dapat dihapus.", "Peringatan Hapus", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus kamar " + nomorKamar + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (new Kamar().delete(id)) {
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
                tampilkanData("");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}