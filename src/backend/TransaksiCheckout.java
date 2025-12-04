package backend;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import javax.swing.JOptionPane;

public class TransaksiCheckout {
    private int id_transaksi;
    private Booking booking; // Objek Booking (Foreign Key)
    private Date tanggal_checkout_aktual;
    private int lama_inap;
    private double total_bayar;
    private String metode;

    // --- Konstruktor, Getter, dan Setter ---
    
    public TransaksiCheckout() {
        // Constructor kosong, set booking nanti melalui setter
    }
    
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Date getTanggal_checkout_aktual() { return tanggal_checkout_aktual; }
    public void setTanggal_checkout_aktual(Date tanggal_checkout_aktual) { this.tanggal_checkout_aktual = tanggal_checkout_aktual; }
    public int getId_transaksi() { return id_transaksi; }
    public void setId_transaksi(int id_transaksi) { this.id_transaksi = id_transaksi; }
    public String getMetode() { return metode; }
    public void setMetode(String metode) { this.metode = metode; }
    public int getLama_inap() { return lama_inap; }
    public double getTotal_bayar() { return total_bayar; }
    // ... (Getter dan Setter lainnya)

    /**
     * Metode untuk menghitung lama inap dalam hari
     */
    public static int hitungLamaInap(Date tglCheckin, Date tglCheckoutAktual) {
        LocalDate checkin = tglCheckin.toLocalDate();
        LocalDate checkout = tglCheckoutAktual.toLocalDate();
        return (int) ChronoUnit.DAYS.between(checkin, checkout);
    }
    
    /**
     * Validasi tanggal checkout terhadap tanggal booking
     */
    private void validateCheckoutDate() {
        if (this.booking == null) {
            throw new IllegalArgumentException("Booking tidak boleh null");
        }
        if (this.tanggal_checkout_aktual == null) {
            throw new IllegalArgumentException("Tanggal checkout harus diisi");
        }
        // Validasi tanggal checkout tidak boleh sebelum tanggal check-in
        if (this.tanggal_checkout_aktual.before(this.booking.getTanggal_checkin())) {
            throw new IllegalArgumentException("Tanggal checkout tidak boleh sebelum tanggal check-in (" 
                + this.booking.getTanggal_checkin() + ")");
        }
        // Validasi tanggal checkout tidak boleh lebih dari 1 tahun dari sekarang
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 1);
        if (this.tanggal_checkout_aktual.after(new Date(maxDate.getTimeInMillis()))) {
            throw new IllegalArgumentException("Tanggal checkout tidak boleh lebih dari 1 tahun ke depan");
        }
    }
    
    /**
     * Menghitung total bayar berdasarkan lama inap
     */
    private void calculatePayment() {
        this.lama_inap = hitungLamaInap(this.booking.getTanggal_checkin(), this.tanggal_checkout_aktual);
        this.total_bayar = this.booking.getKamar().getHarga() * this.lama_inap; 
    }
    
    /**
     * Metode untuk menyimpan transaksi checkout dan menyelesaikan booking menggunakan TRANSAKSI DATABASE.
     */
    public void save() {
        Connection conn = null; // Deklarasi objek koneksi
        
        try {
            // Validate data & Calculate payment 
            validateCheckoutDate();
            calculatePayment();

            // 1. Dapatkan koneksi dan mulai transaksi
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);
            
            // 2. Dapatkan tanggal dalam format yang benar untuk database
            java.sql.Date sqlCheckout = new java.sql.Date(this.tanggal_checkout_aktual.getTime());
            java.sql.Date sqlCheckin = new java.sql.Date(this.booking.getTanggal_checkin().getTime());

            try {
                // 3. Simpan transaksi checkout
                // REVISI PADA TransaksiCheckout.java -> method save()

// 1. Ganti Query SQL (Pastikan id_booking ada)
// REVISI PADA TransaksiCheckout.java -> method save()

// 1. Ganti Query SQL:
// PASTIKAN kolom 'id_booking' dimasukkan dalam daftar kolom
String sql = "INSERT INTO transaksi_checkout (tanggal_checkout, id_customer, tanggal_checkin, " +
             "tanggal_transaksi, lama_inap, total_bayar, metode, id_booking) " + // <-- TAMBAH id_booking
             "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?)"; // <-- PASTIKAN ADA 7 PLACEHOLDER + 1 (?)

// 2. Ganti Array Parameters:
// PASTIKAN this.booking.getId_booking() dimasukkan dalam daftar nilai
this.id_transaksi = DBHelper.insertWithParamsGetId(
    conn,
    sql,
    new Object[]{
        sqlCheckout,
        this.booking.getCustomer().getId_customer(),
        sqlCheckin,
        this.lama_inap,
        this.total_bayar,
        this.metode,
        this.booking.getId_booking() // <-- TAMBAH NILAI ID BOOKING
    }
);
                
                if (this.id_transaksi <= 0) {
                    throw new SQLException("Gagal menyimpan transaksi checkout: Tidak ada ID yang dihasilkan");
                }
                
                System.out.println("Transaksi berhasil disimpan dengan ID: " + this.id_transaksi);

                // 4. Update status booking menjadi 'selesai'
                String updateBooking = "UPDATE booking SET status = 'selesai' WHERE id_booking = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(updateBooking)) {
                    pstmt.setInt(1, this.booking.getId_booking());
                    int updated = pstmt.executeUpdate();
                    if (updated <= 0) {
                        throw new SQLException("Gagal mengupdate status booking: Tidak ada baris yang terpengaruh");
                    }
                    System.out.println("Status booking berhasil diupdate");
                }

                // 5. Update status kamar menjadi 'kosong'
                String updateKamar = "UPDATE kamar SET status = 'kosong' WHERE id_kamar = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(updateKamar)) {
                    pstmt.setInt(1, this.booking.getKamar().getId_kamar());
                    int updated = pstmt.executeUpdate();
                    if (updated <= 0) {
                        throw new SQLException("Gagal mengupdate status kamar: Tidak ada baris yang terpengaruh");
                    }
                    System.out.println("Status kamar berhasil diupdate");
                }

                // 6. Commit transaksi jika semua berhasil
                conn.commit();
                
                // Tampilkan pesan sukses
                JOptionPane.showMessageDialog(
                    null,
                    "Checkout berhasil!\nID Transaksi: " + this.id_transaksi + 
                    "\nKamar: " + this.booking.getKamar().getNomor_kamar() +
                    "\nTotal Bayar: Rp " + String.format("%,.2f", this.total_bayar),
                    "Checkout Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
            } catch (SQLException e) {
                // Rollback jika terjadi error
                if (conn != null) {
                    try {
                        conn.rollback();
                        System.err.println("Transaksi dibatalkan (Rollback) karena error: " + e.getMessage());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                throw new SQLException("Gagal menyimpan transaksi: " + e.getMessage(), e);
            } finally {
                // Reset auto-commit dan tutup koneksi
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        } catch (Exception e) {
            // Tampilkan pesan error ke pengguna
            JOptionPane.showMessageDialog(
                null,
                "Error saat melakukan checkout: " + e.getMessage(),
                "Error Checkout",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}