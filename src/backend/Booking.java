package backend;

import backend.DBHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class Booking {
    private int id_booking;
    private Customer customer;
    private Kamar kamar;
    private Date tanggal_checkin;
    private Date tanggal_checkout;
    private double total_harga;
    private String status;

    public Booking() {
    }

    public Booking(Customer customer, Kamar kamar, Date tgl_checkin, Date tgl_checkout, double total_harga) {
        setCustomer(customer);
        setKamar(kamar);
        setTanggal_checkin(tgl_checkin);
        setTanggal_checkout(tgl_checkout);
        setTotal_harga(total_harga);
        this.status = "booked";
    }

    // --- GETTERS & SETTERS (Dipertahankan) ---
    // (Kode getter dan setter Anda yang sudah ada di sini...)
    public int getId_booking() { return id_booking; }
    public void setId_booking(int id_booking) { 
        if (id_booking < 0) { throw new IllegalArgumentException("ID booking tidak valid"); }
        this.id_booking = id_booking; 
    }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = Objects.requireNonNull(customer, "Customer tidak boleh null"); }
    public Kamar getKamar() { return kamar; }
    public void setKamar(Kamar kamar) { this.kamar = Objects.requireNonNull(kamar, "Kamar tidak boleh null"); }
    public Date getTanggal_checkin() { return tanggal_checkin != null ? new Date(tanggal_checkin.getTime()) : null; }
    public void setTanggal_checkin(Date tanggal_checkin) { this.tanggal_checkin = Objects.requireNonNull(tanggal_checkin, "Tanggal check-in tidak boleh null"); }
    public Date getTanggal_checkout() { return tanggal_checkout != null ? new Date(tanggal_checkout.getTime()) : null; }
    public void setTanggal_checkout(Date tanggal_checkout) { 
        this.tanggal_checkout = Objects.requireNonNull(tanggal_checkout, "Tanggal check-out tidak boleh null"); 
        validateDates(); 
    }
    public double getTotal_harga() { return total_harga; }
    public void setTotal_harga(double total_harga) { 
        if (total_harga < 0) { throw new IllegalArgumentException("Total harga tidak boleh negatif"); }
        this.total_harga = total_harga; 
    }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = Objects.requireNonNull(status, "Status tidak boleh null"); }

    private void validateDates() {
        if (tanggal_checkin != null && tanggal_checkout != null && 
            tanggal_checkout.before(tanggal_checkin)) {
            throw new IllegalArgumentException("Tanggal check-out tidak boleh sebelum tanggal check-in");
        }
    }

    private void validateBooking() {
        validateDates();
        if (customer == null || kamar == null) {
            throw new IllegalStateException("Customer dan Kamar harus diisi");
        }
    }

    // --- PERBAIKAN METHOD SAVE DENGAN TRANSAKSI ---
    public void save() {
        validateBooking();
        
        if (this.id_booking == 0) {
            // Proses Insert (Memerlukan Transaksi: Insert Booking + Update Kamar Status)
            Connection conn = null;
            
            try {
                // 1. Dapatkan koneksi & mulai transaksi
                conn = DBHelper.getConnection();
                conn.setAutoCommit(false); // Matikan autocommit
                
                // 2. INSERT data booking
                String sqlInsert = "INSERT INTO booking (id_customer, id_kamar, tanggal_checkin, tanggal_checkout, total_harga, status) " +
                                 "VALUES (?, ?, ?, ?, ?, ?)";
                
                int generatedId = -1;
                
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    
                    stmt.setInt(1, this.customer.getId_customer());
                    stmt.setInt(2, this.kamar.getId_kamar());
                    stmt.setDate(3, new java.sql.Date(this.tanggal_checkin.getTime()));
                    stmt.setDate(4, new java.sql.Date(this.tanggal_checkout.getTime()));
                    stmt.setDouble(5, this.total_harga);
                    stmt.setString(6, this.status);
                    
                    int affectedRows = stmt.executeUpdate();
                    
                    if (affectedRows == 0) {
                        throw new SQLException("Gagal menyimpan booking, tidak ada baris yang terpengaruh.");
                    }
                    
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            generatedId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Gagal mendapatkan ID booking.");
                        }
                    }
                }
                
                this.id_booking = generatedId;
                
                // 3. UPDATE Status Kamar menjadi 'terisi' (Menggunakan koneksi transaksi)
                // Catatan: Asumsi Kamar.save() tidak cocok karena akan membuat koneksi baru.
                // Kita akan menggunakan DBHelper.executeQuery(conn, ...)
                
                String sqlUpdateKamar = "UPDATE kamar SET status = 'terisi' WHERE id_kamar = " + this.kamar.getId_kamar();
                boolean kamarUpdated = DBHelper.executeQuery(conn, sqlUpdateKamar); 
                
                if (!kamarUpdated) {
                    throw new SQLException("Gagal mengupdate status kamar.");
                }

                // 4. COMMIT TRANSAKSI
                conn.commit();
                
                // Set status objek lokal
                this.kamar.setStatus("terisi"); 
                
            } catch (SQLException e) {
                // ROLLBACK jika ada kesalahan pada langkah 2 atau 3
                if (conn != null) {
                    try {
                        conn.rollback();
                        System.err.println("Transaksi booking dibatalkan (Rollback).");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                throw new RuntimeException("Gagal menyimpan booking (Transaksi): " + e.getMessage(), e);
            } finally {
                // 5. Tutup koneksi dan kembalikan autocommit
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } else {
            // Update existing booking (Tidak memerlukan Transaksi jika hanya update status)
            String sql = "UPDATE booking SET status = ?, tanggal_checkin = ?, tanggal_checkout = ?, total_harga = ? WHERE id_booking = ?";
            
            try (Connection conn = DBHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, this.status);
                stmt.setDate(2, new java.sql.Date(this.tanggal_checkin.getTime()));
                stmt.setDate(3, new java.sql.Date(this.tanggal_checkout.getTime()));
                stmt.setDouble(4, this.total_harga);
                stmt.setInt(5, this.id_booking);

                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Gagal memperbarui booking: " + e.getMessage(), e);
            }
        }
    }

    // --- QUERY METHODS (Dipertahankan) ---
    // (Kode getById, getBookingAktif, getAll, dan helper methods Anda di sini...)

    public static Booking getById(int id) {
        String sql = "SELECT * FROM booking WHERE id_booking = ?";
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data booking: " + e.getMessage(), e);
        }
        return null;
    }

    public static ArrayList<Booking> getBookingAktif() {
        return getBookingsByStatus("booked");
    }

    public static ArrayList<Booking> getAllBelumSelesai() {
        return getBookingsByStatus("booked");
    }

    public static ArrayList<Booking> getAll() {
        String sql = "SELECT * FROM booking";
        return getBookingsByQuery(sql);
    }

    private static ArrayList<Booking> getBookingsByStatus(String status) {
        String sql = "SELECT * FROM booking WHERE status = ?";
        return getBookingsByQuery(sql, status);
    }

    private static ArrayList<Booking> getBookingsByQuery(String sql, Object... params) {
        ArrayList<Booking> bookings = new ArrayList<>();
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil daftar booking: " + e.getMessage(), e);
        }
        return bookings;
    }

    private static Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId_booking(rs.getInt("id_booking"));
        
        // Load related customer and room
        // PENTING: Asumsikan Kamar dan Customer memiliki constructor default dan method getById(int id)
        Customer customer = new Customer().getById(rs.getInt("id_customer"));
        Kamar kamar = new Kamar().getById(rs.getInt("id_kamar"));
        
        booking.setCustomer(customer);
        booking.setKamar(kamar);
        booking.setTanggal_checkin(rs.getDate("tanggal_checkin"));
        booking.setTanggal_checkout(rs.getDate("tanggal_checkout"));
        booking.setTotal_harga(rs.getDouble("total_harga"));
        booking.setStatus(rs.getString("status"));
        
        return booking;
    }

    public boolean delete() {
        if (this.id_booking <= 0) {
            throw new IllegalStateException("Tidak dapat menghapus booking yang belum disimpan");
        }
        
        String sql = "DELETE FROM booking WHERE id_booking = ?";
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, this.id_booking);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus booking: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        String customerName = (customer != null) ? customer.getNama() : "Unknown";
        String kamarNum = (kamar != null) ? kamar.getNomor_kamar() : "Unknown";
        return String.format("ID: %d - Kamar %s (%s) - %s - Rp %,.2f", 
            id_booking, kamarNum, customerName, status, total_harga);
    }
}