package frontend;

import javax.swing.*;
import java.awt.*;

// Penting: Pastikan semua form (frmKamar, frmCustomer, dll.) 
// yang dipanggil di sini ada di paket 'frontend'.

public class frmMainMenu extends JFrame {
    
    private String username;
    private String role;
    private int userId;
    
    public frmMainMenu(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        
        setTitle("Main Menu - Aplikasi Booking Hotel");
        setSize(500, 400); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 20));
        
        // --- 1. Panel Header ---
        JPanel panelHeader = new JPanel(new BorderLayout(10, 5));
        panelHeader.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Logout button on the left
        JButton btnLogout = new JButton("Logout");
        btnLogout.setPreferredSize(new Dimension(100, 30));
        panelHeader.add(btnLogout, BorderLayout.WEST);
        
        // User info on the right
        JLabel lblUser = new JLabel("User: " + username + " (Role: " + role.toUpperCase() + ")"); 
        lblUser.setFont(new Font("Arial", Font.BOLD, 12));
        JPanel panelUserInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelUserInfo.add(lblUser);
        panelHeader.add(panelUserInfo, BorderLayout.EAST);
        
        add(panelHeader, BorderLayout.NORTH);
        
        // --- 2. Panel Menu Utama ---
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.Y_AXIS));
        panelMenu.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton btnKamar = new JButton("1. Data Kamar");
        JButton btnCustomer = new JButton("2. Data Customer");
        JButton btnBooking = new JButton("3. Booking / Check-In");
        JButton btnCheckout = new JButton("4. Check-Out");
        JButton btnDataPegawai = new JButton("5. Data Pegawai (Admin Only)");

        // Set preferred size for all buttons
        Dimension btnSize = new Dimension(220, 40);
        btnKamar.setMaximumSize(btnSize);
        btnCustomer.setMaximumSize(btnSize);
        btnBooking.setMaximumSize(btnSize);
        btnCheckout.setMaximumSize(btnSize);
        btnDataPegawai.setMaximumSize(btnSize);
        
        // Center align buttons
        btnKamar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCustomer.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBooking.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCheckout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDataPegawai.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add some vertical space between buttons
        panelMenu.add(Box.createVerticalStrut(10));
        panelMenu.add(btnKamar);
        panelMenu.add(Box.createVerticalStrut(10));
        panelMenu.add(btnCustomer);
        panelMenu.add(Box.createVerticalStrut(10));
        panelMenu.add(btnBooking);
        panelMenu.add(Box.createVerticalStrut(10));
        panelMenu.add(btnCheckout);

        // Show admin button only for admin role
        if (this.role.equalsIgnoreCase("admin")) {
            panelMenu.add(Box.createVerticalStrut(10));
            panelMenu.add(btnDataPegawai);
            
            btnDataPegawai.addActionListener(e -> {
                // Panggil form data pegawai (frontend.frmDataPegawai)
                new frmDataPegawai().setVisible(true);
            });
        }

        JScrollPane scrollPane = new JScrollPane(panelMenu);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
        add(scrollPane, BorderLayout.CENTER);
        
        // --- Action Listeners ---
        btnKamar.addActionListener(e -> new frmKamar().setVisible(true)); 
        btnCustomer.addActionListener(e -> new frmCustomer().setVisible(true));
        btnBooking.addActionListener(e -> new frmBooking().setVisible(true));
        btnCheckout.addActionListener(e -> new frmCheckout(userId).setVisible(true));
        
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                new frmLogin().setVisible(true); 
                dispose();
            }
        });
        
        setLocationRelativeTo(null);
    }
}