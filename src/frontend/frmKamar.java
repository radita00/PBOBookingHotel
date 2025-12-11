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
    private JTextField txtCariKamar; // 🆕 Field baru
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
        panelNorth.add(panelSearch, BorderLayout.SOUTH); // Letakkan di bawah input
        
        add(panelNorth, BorderLayout.NORTH);
        
        // --- Tabel ---
        tblKamar = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblKamar);
        add(scrollPane, BorderLayout.CENTER);
        
        // --- LOGIKA ---
        tampilkanData(""); // Panggil dengan keyword kosong di awal
        
        // 🆕 Listener Pencarian
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
    
    // 🔄 Modifikasi method untuk menerima keyword
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
            if (idKamarSedangEdit > 0) {
                k = new Kamar().getById(idKamarSedangEdit);
                if (k.getId_kamar() == 0) { // Cek ID yang dikembalikan
                    JOptionPane.showMessageDialog(this, "Data kamar tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                k = new Kamar();
            }
            
            k.setNomor_kamar(txtNomor.getText());
            k.setTipe(cmbTipe.getSelectedItem().toString());
            k.setHarga(Double.parseDouble(txtHarga.getText()));
            k.setStatus(cmbStatus.getSelectedItem().toString());
            
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
        txtCariKamar.setText(""); // Bersihkan field search
        tampilkanData("");
    }

    private void hapusData() {
        int row = tblKamar.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int id = Integer.parseInt(tblKamar.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus kamar ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
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