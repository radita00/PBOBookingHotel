package frontend;

import backend.users;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class frmLogin extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public frmLogin() {
        setTitle("Sistem Booking Hotel - Login");
        setSize(400, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); 

        // Set default font to bold for all components
        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        UIManager.put("Button.font", boldFont);
        UIManager.put("Label.font", boldFont);
        UIManager.put("TextField.font", boldFont);
        UIManager.put("PasswordField.font", boldFont);
        UIManager.put("OptionPane.messageFont", boldFont);
        UIManager.put("OptionPane.buttonFont", boldFont);

        // --- Inisialisasi Komponen ---
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setBounds(50, 40, 80, 25);
        lblUsername.setFont(boldFont);
        add(lblUsername);

        txtUsername = new JTextField(20);
        txtUsername.setBounds(150, 40, 180, 25);
        txtUsername.setFont(boldFont);
        add(txtUsername);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setBounds(50, 80, 80, 25);
        lblPassword.setFont(boldFont);
        add(lblPassword);

        txtPassword = new JPasswordField(20);
        txtPassword.setBounds(150, 80, 180, 25);
        txtPassword.setFont(boldFont);
        add(txtPassword);

        btnLogin = new JButton("Login");
        btnLogin.setFont(boldFont);
        btnLogin.setBounds(150, 120, 180, 30);
        add(btnLogin);

        // Add KeyListener to both text fields for Enter key
        ActionListener loginAction = e -> performLogin();
        
        txtUsername.addActionListener(loginAction);
        txtPassword.addActionListener(loginAction);
        btnLogin.addActionListener(loginAction);

        // Tombol Registrasi Pegawai
        JButton btnRegister = new JButton("Daftar Pegawai Baru");
        btnRegister.setBounds(150, 160, 180, 25);
        btnRegister.setFont(boldFont);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setBorderPainted(false);
        btnRegister.setForeground(Color.BLUE);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> {
            new frmRegisterPegawai().setVisible(true);
            dispose();
        });
        add(btnRegister);

        // Add window listener to set focus to username field
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtUsername.requestFocusInWindow();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void performLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        
        users user = new users().login(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(null, "Login Berhasil! Selamat datang, " + user.getUsername(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            String roleUser = user.getRole();
            new frmMainMenu(user.getUsername(), roleUser).setVisible(true);
            dispose(); 
        } else {
            JOptionPane.showMessageDialog(null, "Username atau Password salah.", "Gagal", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocusInWindow();
        }
    }

    public static void main(String[] args) {
        try {
            // Set look and feel to system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run the login form in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new frmLogin().setVisible(true);
        });
    }
}