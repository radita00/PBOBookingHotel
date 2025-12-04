package frontend;
import frontend.Customer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class frmCustomer extends JFrame {
    private JTextField txtNama, txtIdentitas, txtHP, txtAlamat;
    private JTable tblCustomer;
    private JButton btnSimpan, btnHapus, btnClear;
    private int selectedCustomerId = 0; // Field untuk ID yang dipilih/diedit

    public frmCustomer() {
        setTitle("Form Master Customer");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new java.awt.BorderLayout(10, 10));
        
        // --- Panel Input ---
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
        
        add(panelInput, java.awt.BorderLayout.NORTH);
        
        // --- Tabel ---
        tblCustomer = new JTable();
        JScrollPane scrollPane = new JScrollPane(tblCustomer);
        add(scrollPane, java.awt.BorderLayout.CENTER);
        
        // --- LOGIKA ---
        tampilkanData();
        
        btnSimpan.addActionListener(e -> simpanData());
        btnClear.addActionListener(e -> clearForm());
        btnHapus.addActionListener(e -> hapusData());
        
        tblCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblCustomer.getSelectedRow();
                if (row >= 0) {
                    // Simpan ID customer yang dipilih (untuk Update/Delete)
                    selectedCustomerId = Integer.parseInt(tblCustomer.getValueAt(row, 0).toString());
                    
                    txtNama.setText(tblCustomer.getValueAt(row, 1).toString());
                    txtIdentitas.setText(tblCustomer.getValueAt(row, 2).toString());
                    txtHP.setText(tblCustomer.getValueAt(row, 3).toString());
                    txtAlamat.setText(tblCustomer.getValueAt(row, 4).toString());
                }
            }
        });
        
        setLocationRelativeTo(null);
    }

    private void tampilkanData() {
        String[] kolom = {"ID", "Nama", "No. Identitas", "HP", "Alamat"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        
        ArrayList<Customer> list = new Customer().getAll();
        
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
            
            // Set ID customer yang sedang diedit (0 jika baru, ID jika update)
            c.setId_customer(selectedCustomerId); 
            
            c.setNama(txtNama.getText());
            c.setNo_identitas(txtIdentitas.getText());
            c.setNo_hp(txtHP.getText());
            c.setAlamat(txtAlamat.getText());
            
            c.save(); // Logika save() di model akan menentukan INSERT atau UPDATE
            JOptionPane.showMessageDialog(this, "Data Customer berhasil disimpan.");
            tampilkanData();
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
        selectedCustomerId = 0; // Reset ID saat form dibersihkan
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
            c.setId_customer(id); // Set ID sebelum menghapus
            
            // Panggil method delete() tanpa argumen
            if (c.delete()) {
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
                tampilkanData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data. (Mungkin terikat dengan booking lain)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}