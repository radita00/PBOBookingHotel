package frontend;

import backend.users;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class frmRegisterPegawai extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword, txtConfirmPassword;
    private JButton btnRegister, btnBack;
    
    public frmRegisterPegawai() {
        setTitle("Registrasi Pegawai Baru");
        setSize(500, 350); // Increased width to accommodate longer fields
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // --- Panel Utama ---
        JPanel panelMain = new JPanel(new GridBagLayout());
        panelMain.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Set default font
        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        
        // --- Username ---
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(boldFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelMain.add(lblUsername, gbc);
        
        txtUsername = new JTextField(25); // Increased column width
        txtUsername.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panelMain.add(txtUsername, gbc);
        
        // --- Password ---
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(boldFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelMain.add(lblPassword, gbc);
        
        txtPassword = new JPasswordField(25); // Increased column width
        txtPassword.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panelMain.add(txtPassword, gbc);
        
        // --- Confirm Password ---
        JLabel lblConfirmPassword = new JLabel("Konfirmasi Password:");
        lblConfirmPassword.setFont(boldFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panelMain.add(lblConfirmPassword, gbc);
        
        txtConfirmPassword = new JPasswordField(25); // Increased column width
        txtConfirmPassword.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panelMain.add(txtConfirmPassword, gbc);
        
        // --- Button Panel ---
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        btnBack = new JButton("Kembali");
        btnBack.setFont(boldFont);
        btnBack.addActionListener(e -> {
            new frmLogin().setVisible(true);
            dispose();
        });
        panelButton.add(btnBack);
        
        btnRegister = new JButton("Daftar");
        btnRegister.setFont(boldFont);
        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText();
            String password = new String(txtPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Password tidak cocok!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            users newUser = new users();
            newUser.setUsername(username);
            newUser.setPassword(password); // Note: Password should be hashed in production
            if (newUser.registerPegawai(username, password)) {
                JOptionPane.showMessageDialog(this, "Pendaftaran berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                new frmLogin().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mendaftarkan pengguna. Username mungkin sudah digunakan.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelButton.add(btnRegister);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelMain.add(panelButton, gbc);
        
        add(panelMain, BorderLayout.CENTER);
        
        // Set window to center of screen
        setLocationRelativeTo(null);
    }
}