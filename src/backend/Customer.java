package bookinghotel;

import backend.DBHelper;
import java.sql.*;
import java.util.ArrayList;

public class Customer {
    private int id_customer;
    private String nama;
    private String no_identitas;
    private String no_hp;
    private String alamat;

    // --- Konstruktor ---
    public Customer() {
    }

    public Customer(String nama, String no_identitas, String no_hp, String alamat) {
        this.nama = nama;
        this.no_identitas = no_identitas;
        this.no_hp = no_hp;
        this.alamat = alamat;
    }

    // --- Getter & Setter (Dipertahankan) ---
    public int getId_customer() { return id_customer; }
    public void setId_customer(int id_customer) { this.id_customer = id_customer; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNo_identitas() { return no_identitas; }
    public void setNo_identitas(String no_identitas) { this.no_identitas = no_identitas; }
    public String getNo_hp() { return no_hp; }
    public void setNo_hp(String no_hp) { this.no_hp = no_hp; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    
    // Metode bantuan untuk memetakan ResultSet ke objek Customer
    private static Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId_customer(rs.getInt("id_customer"));
        c.setNama(rs.getString("nama"));
        c.setNo_identitas(rs.getString("no_identitas"));
        c.setNo_hp(rs.getString("no_hp"));
        c.setAlamat(rs.getString("alamat"));
        return c;
    }
    
    // 🆕 Metode untuk mencari Customer berdasarkan Nomor Identitas
    public Customer getByNoIdentitas(String noIdentitas) {
        String query = "SELECT * FROM customer WHERE no_identitas = ?";
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, noIdentitas);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Mengembalikan null jika tidak ditemukan
    }

    
    // 🔄 PERBAIKAN LOGIKA SAVE: Menentukan apakah INSERT atau UPDATE berdasarkan ID dan No. Identitas
    public void save() {
        // --- Langkah 1: Pengecekan Eksistensi ---
        if (this.id_customer == 0) {
            // Jika ID 0 (objek baru), cek apakah No. Identitas sudah ada di DB
            Customer existingCustomer = getByNoIdentitas(this.no_identitas);
            
            if (existingCustomer != null) {
                // Jika customer DITEMUKAN: Ganti mode ke UPDATE dengan mengambil ID yang sudah ada
                this.id_customer = existingCustomer.getId_customer();
            }
        }
        
        // --- Langkah 2: Eksekusi Query ---
        
        if (this.id_customer == 0) {
            // Mode: INSERT (Setelah yakin tidak ada di DB)
            String sql = "INSERT INTO customer (nama, no_identitas, no_hp, alamat) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, this.nama);
                stmt.setString(2, this.no_identitas);
                stmt.setString(3, this.no_hp);
                stmt.setString(4, this.alamat);
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        this.id_customer = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                // RuntimeException akan dilempar ke UI untuk ditampilkan
                throw new RuntimeException("Gagal menyimpan customer baru: " + e.getMessage(), e);
            }
            
        } else {
            // Mode: UPDATE
            String sql = "UPDATE customer SET nama = ?, no_identitas = ?, no_hp = ?, alamat = ? WHERE id_customer = ?";
            
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, this.nama);
                stmt.setString(2, this.no_identitas);
                stmt.setString(3, this.no_hp);
                stmt.setString(4, this.alamat);
                stmt.setInt(5, this.id_customer);
                
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Gagal memperbarui customer: " + e.getMessage(), e);
            }
        }
    }

    // --- Metode lainnya (tetap) ---

    public ArrayList<Customer> getAll() {
        ArrayList<Customer> listCustomer = new ArrayList<>();
        String query = "SELECT * FROM customer";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                listCustomer.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listCustomer;
    }
    
    public Customer getById(int id) {
        String query = "SELECT * FROM customer WHERE id_customer = ?";
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Customer(); 
    }

    @Override
    public String toString() {
        return "ID: " + id_customer + " - " + nama + " (" + no_identitas + ")";
    }

    public boolean delete() {
        if (this.id_customer == 0) return false;

        String sql = "DELETE FROM customer WHERE id_customer = ?";
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, this.id_customer);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus customer: " + e.getMessage(), e);
        }
    }
}