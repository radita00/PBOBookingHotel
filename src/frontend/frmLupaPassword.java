package frontend;

import backend.Users; 
import backend.DBHelper;
import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class frmLupaPassword extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtNewPassword, txtConfirmPassword;
    private JButton btnReset, btnBack;

    public frmLupaPassword() {
        setTitle("Lupa Password / Reset Password Pegawai");
        setSize(500, 350); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panelMain = new JPanel(new GridBagLayout());
        panelMain.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);

        //1. Username (Verifikasi)
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(boldFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridwidth = 1;
        panelMain.add(lblUsername, gbc);

        txtUsername = new JTextField(25);
        txtUsername.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        panelMain.add(txtUsername, gbc);
        
        //2. Password Baru
        JLabel lblNewPassword = new JLabel("Password Baru:");
        lblNewPassword.setFont(boldFont);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panelMain.add(lblNewPassword, gbc);

        txtNewPassword = new JPasswordField(25);
        txtNewPassword.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panelMain.add(txtNewPassword, gbc);

        //3. Konfirmasi Password Baru
        JLabel lblConfirmPassword = new JLabel("Konfirmasi Password:");
        lblConfirmPassword.setFont(boldFont);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panelMain.add(lblConfirmPassword, gbc);

        txtConfirmPassword = new JPasswordField(25);
        txtConfirmPassword.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panelMain.add(txtConfirmPassword, gbc);
        
       
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        
        btnBack = new JButton("Kembali ke Login");
        btnBack.setFont(boldFont);
        btnBack.addActionListener(e -> {
            new frmLogin().setVisible(true);
            dispose();
        });
        panelButton.add(btnBack);
        
        btnReset = new JButton("Reset Password");
        btnReset.setFont(boldFont);
        btnReset.addActionListener(e -> resetPasswordAction());
        panelButton.add(btnReset);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        panelMain.add(panelButton, gbc);
        
        add(panelMain, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    
    private void resetPasswordAction() {
        String username = txtUsername.getText();
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        // 1. Validasi Input
        if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 2. Validasi Konfirmasi Password
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password baru tidak cocok!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Users user = new Users();
        
        // 3. Cek apakah username ada
        String cekQuery = "SELECT id_user FROM users WHERE username = '" + username + "'";
        ResultSet rs = null;
        try {
            rs = DBHelper.selectQuery(cekQuery);
            if (!rs.next()) {
                // Username tidak ditemukan
                JOptionPane.showMessageDialog(this, "Username Tidak Ditemukan, Masukkan Username yang valid", "Error", JOptionPane.ERROR_MESSAGE);
                // Hapus semua input
                txtUsername.setText("");
                txtNewPassword.setText("");
                txtConfirmPassword.setText("");
                txtUsername.requestFocus(); // Fokuskan kembali ke field username
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memeriksa username", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            // Pastikan ResultSet ditutup
            if (rs != null) {
                try {
                    rs.getStatement().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // 4. Jika username valid, lakukan reset password
        if (user.resetPassword(username, newPassword)) {
            JOptionPane.showMessageDialog(this, "Reset Password berhasil! Silakan login dengan password baru.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            new frmLogin().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mereset password. Silakan coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }   
}