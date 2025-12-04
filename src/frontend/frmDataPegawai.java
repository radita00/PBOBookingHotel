package frontend;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import backend.Users;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class frmDataPegawai extends JFrame {
    
    private JTable tblPegawai;
    private JButton btnRefresh, btnTambah, btnEdit, btnHapus;

    public frmDataPegawai() {
        setTitle("Data Pegawai (Admin Access)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10));

        // --- Panel Judul ---
        JLabel lblTitle = new JLabel("Daftar Pegawai/Users Sistem", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);
        
        // --- Tabel ---
        tblPegawai = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblPegawai);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Kontrol ---
        JPanel panelKontrol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnTambah = new JButton("Tambah Pegawai");
        btnEdit = new JButton("Edit Pegawai");
        btnHapus = new JButton("Hapus Pegawai");
        btnRefresh = new JButton("Refresh Data");
        JButton btnTutup = new JButton("Tutup");
        
        // Set preferred size for buttons
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
        tampilkanDataPegawai();

        btnTambah.addActionListener(e -> tambahPegawai());
        btnEdit.addActionListener(e -> editPegawai());
        btnHapus.addActionListener(e -> hapusPegawai());
        btnRefresh.addActionListener(e -> tampilkanDataPegawai());
        btnTutup.addActionListener(e -> dispose());
        
        setLocationRelativeTo(null);
    }

    private void tampilkanDataPegawai() {
        String[] kolom = {"ID User", "Username", "Role"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        
        ArrayList<Users> list = new Users().getAllUsers();
        
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
        // Create form dialog
        JDialog dialog = new JDialog(this, "Tambah Pegawai Baru", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);
        
        // Form fields
        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"pegawai", "admin"});
        
        // Add components to dialog
        dialog.add(new JLabel("Username:"));
        dialog.add(txtUsername);
        dialog.add(new JLabel("Password:"));
        dialog.add(txtPassword);
        dialog.add(new JLabel("Role:"));
        dialog.add(cmbRole);
        
        // Buttons
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        
        panelButton.add(btnSimpan);
        panelButton.add(btnBatal);
        
        dialog.add(new JLabel()); // Empty cell for layout
        dialog.add(panelButton);
        
        // Button actions
        btnSimpan.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String role = (String) cmbRole.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username dan password harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Users newUser = new Users();
            if (role.equals("pegawai")) {
                if (newUser.registerPegawai(username, password)) {
                    JOptionPane.showMessageDialog(dialog, "Pegawai berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    tampilkanDataPegawai();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Gagal menambahkan pegawai. Username mungkin sudah digunakan.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // For admin, we need to handle it differently since registerPegawai only creates 'pegawai' role
                try (java.sql.Connection conn = backend.DBHelper.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    String query = "INSERT INTO users (username, password, role) VALUES ('" + 
                                 username + "', '" + password + "', 'admin')";
                    int result = stmt.executeUpdate(query);
                    if (result > 0) {
                        JOptionPane.showMessageDialog(dialog, "Admin berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        tampilkanDataPegawai();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Gagal menambahkan admin.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Terjadi kesalahan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnBatal.addActionListener(e -> dialog.dispose());
        
        // Show dialog
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void editPegawai() {
        int selectedRow = tblPegawai.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih pegawai yang akan diedit!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected user data
        int id = (int) tblPegawai.getValueAt(selectedRow, 0);
        String currentUsername = (String) tblPegawai.getValueAt(selectedRow, 1);
        String currentRole = (String) tblPegawai.getValueAt(selectedRow, 2);
        
        // Create edit dialog
        JDialog dialog = new JDialog(this, "Edit Data Pegawai", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);
        
        // Form fields
        JTextField txtUsername = new JTextField(currentUsername, 20);
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"pegawai", "admin"});
        cmbRole.setSelectedItem(currentRole);
        
        // Add components to dialog
        dialog.add(new JLabel("Username:"));
        dialog.add(txtUsername);
        dialog.add(new JLabel("Role:"));
        dialog.add(cmbRole);
        
        // Buttons
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSimpan = new JButton("Simpan Perubahan");
        JButton btnBatal = new JButton("Batal");
        
        panelButton.add(btnSimpan);
        panelButton.add(btnBatal);
        
        dialog.add(new JLabel()); // Empty cell for layout
        dialog.add(panelButton);
        
        // Button actions
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
                tampilkanDataPegawai();
            } else {
                JOptionPane.showMessageDialog(dialog, "Gagal memperbarui data pegawai.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnBatal.addActionListener(e -> dialog.dispose());
        
        // Show dialog
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
                tampilkanDataPegawai();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus pegawai.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}