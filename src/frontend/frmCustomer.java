package frontend;
import backend.Customer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class frmCustomer extends JFrame {
    private JTextField txtNama, txtIdentitas, txtHP, txtAlamat;
    private JTable tblCustomer;
    private JButton btnSimpan, btnHapus, btnClear;
    private JTextField txtCariCustomer; // 🆕 Field baru
    private int selectedCustomerId = 0;

    public frmCustomer() {
        setTitle("Form Master Customer");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new java.awt.BorderLayout(10, 10));
        
        // --- Panel Input dan Search (NORTH) ---
        JPanel panelNorth = new JPanel(new BorderLayout(0, 10)); // Container utama NORTH
        
        // 1. Panel Input Data
        JPanel panelInput = new JPanel(new java.awt.GridLayout(5, 2, 5, 5));
        panelInput.setBorder(BorderFactory.createTitledBorder("Input Data Customer"));
        
        panelInput.add(new JLabel("Nama:"));
        txtNama = new JTextField(20);
        panelInput.add(txtNama);
        
        panelInput.add(new JLabel("No. Identitas:"));
        txtIdentitas = new JTextField(20);
        panelInput.add(txtIdentitas);
        
        panelInput.add(new JLabel("No. HP:"));
        txtHP = new JTextField(20);
        panelInput.add(txtHP);
        
        panelInput.add(new JLabel("Alamat:"));
        txtAlamat = new JTextField(20);
        panelInput.add(txtAlamat);
        
        JPanel panelButton = new JPanel();
        btnSimpan = new JButton("Simpan");
        btnHapus = new JButton("Hapus");
        btnClear = new JButton("Bersihkan");
        panelButton.add(btnSimpan);
        panelButton.add(btnHapus);
        panelButton.add(btnClear);
        panelInput.add(panelButton);
        
        panelNorth.add(panelInput, BorderLayout.NORTH);
        
        // 2. Panel Search
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtCariCustomer = new JTextField(20);
        JLabel lblCari = new JLabel("Cari Nama Customer:");
        panelSearch.add(lblCari);
        panelSearch.add(txtCariCustomer);
        
        panelNorth.add(panelSearch, BorderLayout.SOUTH);
        
        add(panelNorth, java.awt.BorderLayout.NORTH);
        
        // --- Tabel ---
        tblCustomer = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblCustomer);
        add(scrollPane, java.awt.BorderLayout.CENTER);
        
        // --- LOGIKA ---
        tampilkanData(""); // Panggil dengan keyword kosong di awal
        
        // 🆕 Listener Pencarian
        txtCariCustomer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                tampilkanData(txtCariCustomer.getText());
            }
        });

        btnSimpan.addActionListener(e -> simpanData());
        btnClear.addActionListener(e -> clearForm());
        btnHapus.addActionListener(e -> hapusData());
        
        tblCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblCustomer.getSelectedRow();
                if (row >= 0) {
                    selectedCustomerId = Integer.parseInt(tblCustomer.getValueAt(row, 0).toString());
                    btnSimpan.setText("Update"); // Tambahan: Agar jelas mode edit
                    
                    txtNama.setText(tblCustomer.getValueAt(row, 1).toString());
                    txtIdentitas.setText(tblCustomer.getValueAt(row, 2).toString());
                    txtHP.setText(tblCustomer.getValueAt(row, 3).toString());
                    txtAlamat.setText(tblCustomer.getValueAt(row, 4).toString());
                }
            }
        });
        
        setLocationRelativeTo(null);
    }

    // 🔄 Modifikasi method untuk menerima keyword
    private void tampilkanData(String keyword) {
        String[] kolom = {"ID", "Nama", "No. Identitas", "HP", "Alamat"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        
        Customer customerHelper = new Customer();
        ArrayList<Customer> list;
        
        if (keyword.isEmpty()) {
            list = customerHelper.getAll();
        } else {
            list = customerHelper.searchCustomer(keyword);
        }
        
        for (Customer c : list) {
            Object[] row = {
                c.getId_customer(),
                c.getNama(),
                c.getNo_identitas(),
                c.getNo_hp(),
                c.getAlamat()
            };
            model.addRow(row);
        }
        tblCustomer.setModel(model);
    }

    private void simpanData() {
        try {
            if (txtNama.getText().isEmpty() || txtIdentitas.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nama dan No. Identitas harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Customer c = new Customer();
            c.setId_customer(selectedCustomerId); 
            
            c.setNama(txtNama.getText());
            c.setNo_identitas(txtIdentitas.getText());
            c.setNo_hp(txtHP.getText());
            c.setAlamat(txtAlamat.getText());
            
            c.save();
            JOptionPane.showMessageDialog(this, "Data Customer berhasil disimpan.");
            tampilkanData("");
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        txtNama.setText("");
        txtIdentitas.setText("");
        txtHP.setText("");
        txtAlamat.setText("");
        selectedCustomerId = 0; 
        btnSimpan.setText("Simpan"); // Reset button text
        txtCariCustomer.setText(""); // Bersihkan field search
        tampilkanData("");
    }

    private void hapusData() {
        int row = tblCustomer.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int id = Integer.parseInt(tblCustomer.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Anda yakin ingin menghapus customer ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Customer c = new Customer();
            c.setId_customer(id);
            
            if (c.delete()) {
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
                tampilkanData("");
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data. (Mungkin terikat dengan booking lain)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}