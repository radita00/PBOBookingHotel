package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import backend.Users;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class frmDataPegawai extends JFrame {
    
    private JTable tblPegawai;
    private JButton btnRefresh, btnTambah, btnEdit, btnHapus;
    private JTextField txtCariPegawai; // 🆕 Field baru

    public frmDataPegawai() {
        setTitle("Data Pegawai (Admin Access)");
        setSize(600, 450); // Tambah tinggi sedikit
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10));

        // --- Panel Atas (Title & Search) ---
        JPanel panelHeader = new JPanel(new BorderLayout(0, 10)); // Container untuk Title dan Search
        panelHeader.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Title
        JLabel lblTitle = new JLabel("Daftar Pegawai/Users Sistem", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        panelHeader.add(lblTitle, BorderLayout.NORTH);
        
        // 2. Search Field
        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtCariPegawai = new JTextField(20);
        JLabel lblCari = new JLabel("Cari Username:");
        panelSearch.add(lblCari);
        panelSearch.add(txtCariPegawai);
        panelHeader.add(panelSearch, BorderLayout.CENTER);
        
        add(panelHeader, BorderLayout.NORTH);
        
        // --- Tabel ---
        tblPegawai = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblPegawai);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Kontrol (SOUTH) ---
        JPanel panelKontrol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnTambah = new JButton("Tambah Pegawai");
        btnEdit = new JButton("Edit Pegawai");
        btnHapus = new JButton("Hapus Pegawai");
        btnRefresh = new JButton("Refresh Data");
        JButton btnTutup = new JButton("Tutup");
        
        Dimension btnSize = new Dimension(150, 30);
        btnTambah.setPreferredSize(btnSize);
        btnEdit.setPreferredSize(btnSize);
        btnHapus.setPreferredSize(btnSize);
        btnRefresh.setPreferredSize(btnSize);
        btnTutup.setPreferredSize(btnSize);
        
        panelKontrol.add(btnTambah);
        panelKontrol.add(btnEdit);
        panelKontrol.add(btnHapus);
        panelKontrol.add(btnRefresh);
        panelKontrol.add(btnTutup);
        add(panelKontrol, BorderLayout.SOUTH);

        // --- Logika ---
        tampilkanDataPegawai(""); // Panggil dengan keyword kosong di awal

        // 🆕 Listener Pencarian
        txtCariPegawai.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                tampilkanDataPegawai(txtCariPegawai.getText());
            }
        });

        btnTambah.addActionListener(e -> tambahPegawai());
        btnEdit.addActionListener(e -> editPegawai());
        btnHapus.addActionListener(e -> hapusPegawai());
        btnRefresh.addActionListener(e -> tampilkanDataPegawai("")); // Refresh membersihkan filter
        btnTutup.addActionListener(e -> dispose());
        
        setLocationRelativeTo(null);
    }

    // 🔄 Modifikasi method untuk menerima keyword
    private void tampilkanDataPegawai(String keyword) {
        String[] kolom = {"ID User", "Username", "Role"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        
        Users userHelper = new Users();
        ArrayList<Users> list;
        
        if (keyword.isEmpty()) {
            list = userHelper.getAllUsers();
        } else {
            list = userHelper.searchUsers(keyword); // Panggil method search baru
        }
        
        for (Users u : list) {
            Object[] row = {
                u.getId_user(),
                u.getUsername(),
                u.getRole()
            };
            model.addRow(row);
        }
        
        tblPegawai.setModel(model);
        
        if (tblPegawai.getColumnModel().getColumnCount() > 0) {
            tblPegawai.getColumnModel().getColumn(0).setPreferredWidth(50);
            tblPegawai.getColumnModel().getColumn(1).setPreferredWidth(150);
            tblPegawai.getColumnModel().getColumn(2).setPreferredWidth(100);
        }
    }
    
    private void tambahPegawai() {
        // ... (Logika Tambah Pegawai tetap sama) ...
        JDialog dialog = new JDialog(this, "Tambah Pegawai Baru", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);
        
        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"pegawai", "admin"});
        
        dialog.add(new JLabel("Username:"));
        dialog.add(txtUsername);
        dialog.add(new JLabel("Password:"));
        dialog.add(txtPassword);
        dialog.add(new JLabel("Role:"));
        dialog.add(cmbRole);
        
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        
        panelButton.add(btnSimpan);
        panelButton.add(btnBatal);
        
        dialog.add(new JLabel());
        dialog.add(panelButton);
        
        btnSimpan.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String role = (String) cmbRole.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username dan password harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Users newUser = new Users();
            // Panggil registerPegawai jika role = 'pegawai'
            if (role.equals("pegawai") && newUser.registerPegawai(username, password)) {
                JOptionPane.showMessageDialog(dialog, "Pegawai berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                tampilkanDataPegawai("");
            } 
            // Handle Admin role (karena registerPegawai defaultnya 'pegawai')
            else if (role.equals("admin")) {
                 try (java.sql.Connection conn = backend.DBHelper.getConnection();
                      java.sql.Statement stmt = conn.createStatement()) {
                     String query = "INSERT INTO users (username, password, role) VALUES ('" + 
                                  username + "', '" + password + "', 'admin')";
                     int result = stmt.executeUpdate(query);
                     if (result > 0) {
                         JOptionPane.showMessageDialog(dialog, "Admin berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                         dialog.dispose();
                         tampilkanDataPegawai("");
                     } else {
                         JOptionPane.showMessageDialog(dialog, "Gagal menambahkan admin (mungkin username sudah ada).", "Error", JOptionPane.ERROR_MESSAGE);
                     }
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     JOptionPane.showMessageDialog(dialog, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                 }
            } else {
                JOptionPane.showMessageDialog(dialog, "Gagal menambahkan pegawai. Username mungkin sudah digunakan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnBatal.addActionListener(e -> dialog.dispose());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void editPegawai() {
        int selectedRow = tblPegawai.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pegawai yang akan diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) tblPegawai.getValueAt(selectedRow, 0);
        String currentUsername = (String) tblPegawai.getValueAt(selectedRow, 1);
        String currentRole = (String) tblPegawai.getValueAt(selectedRow, 2);
        
        // ... (Logika Edit Pegawai tetap sama) ...
        JDialog dialog = new JDialog(this, "Edit Data Pegawai", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);
        
        JTextField txtUsername = new JTextField(currentUsername, 20);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"pegawai", "admin"});
        cmbRole.setSelectedItem(currentRole);
        
        dialog.add(new JLabel("Username:"));
        dialog.add(txtUsername);
        dialog.add(new JLabel("Role:"));
        dialog.add(cmbRole);
        
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = new JButton("Simpan Perubahan");
        JButton btnBatal = new JButton("Batal");
        
        panelButton.add(btnSimpan);
        panelButton.add(btnBatal);
        
        dialog.add(new JLabel());
        dialog.add(panelButton);
        
        btnSimpan.addActionListener(e -> {
            String newUsername = txtUsername.getText().trim();
            String newRole = (String) cmbRole.getSelectedItem();
            
            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Users user = new Users();
            if (user.updateUser(id, newUsername, newRole)) {
                JOptionPane.showMessageDialog(dialog, "Data pegawai berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                tampilkanDataPegawai("");
            } else {
                JOptionPane.showMessageDialog(dialog, "Gagal memperbarui data pegawai.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnBatal.addActionListener(e -> dialog.dispose());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void hapusPegawai() {
        int selectedRow = tblPegawai.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pegawai yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) tblPegawai.getValueAt(selectedRow, 0);
        String username = (String) tblPegawai.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Apakah Anda yakin ingin menghapus pegawai " + username + "?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            Users user = new Users();
            if (user.deleteUser(id)) {
                JOptionPane.showMessageDialog(this, "Pegawai berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                tampilkanDataPegawai("");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus pegawai.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}