package backend;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat; 

public class TransaksiCheckout {
    private int id_transaksi;
    private Booking booking; // Objek Booking (Foreign Key)
    private Date tanggal_checkout_aktual;
    private int lama_inap;
    private double total_bayar;
    private String metode;
    private String kode_transaksi;
    private int id_user; // 🆕 FIELD BARU: ID Pegawai yang melakukan checkout

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
    
    public String getKode_transaksi() { return kode_transaksi; }
    public void setKode_transaksi(String kode_transaksi) { this.kode_transaksi = kode_transaksi; }

    // 🆕 GETTER DAN SETTER UNTUK ID USER
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }

    /**
     * Metode untuk menghasilkan kode transaksi acak (untuk non-tunai)
     */
    public static String generateRandomCode() {
        // Format: TXN-YYYYMMDD-5digitRandom
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePart = sdf.format(new java.util.Date());
        
        // 5 digit angka acak
        int randomPart = (int) (Math.random() * 90000) + 10000; 
        
        return "TXN-" + datePart + "-" + randomPart;
    }
    
    /**
     * Metode untuk menghitung lama inap dalam hari
     */
    public static int hitungLamaInap(Date tglCheckin, Date tglCheckoutAktual) {
        LocalDate checkin = tglCheckin.toLocalDate();
        LocalDate checkout = tglCheckoutAktual.toLocalDate();
        long days = ChronoUnit.DAYS.between(checkin, checkout);
        // Pastikan minimal 1 hari (sesuai logika di frontend/calculatePayment)
        return (int) Math.max(1, days); 
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
        if (this.id_user <= 0) {
            throw new IllegalArgumentException("ID User (Pegawai) harus diisi."); // 🆕 VALIDASI ID USER
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
        // Hitung lama inap dan total bayar
        this.lama_inap = hitungLamaInap(this.booking.getTanggal_checkin(), this.tanggal_checkout_aktual);
        this.total_bayar = this.booking.getKamar().getHarga() * this.lama_inap; 
    }
    
    /**
     * Metode untuk menyimpan transaksi checkout dan menyelesaikan booking menggunakan TRANSAKSI DATABASE.
     */
    public void save() {
        Connection conn = null; // Deklarasi objek koneksi
        
        try {
            // 1. Validasi Data & Hitung Pembayaran
            validateCheckoutDate();
            calculatePayment();
            
            // 2. Tentukan Kode Transaksi berdasarkan Metode Pembayaran
            if (this.metode != null && !this.metode.equalsIgnoreCase("Tunai")) {
                this.kode_transaksi = generateRandomCode();
            } else {
                this.kode_transaksi = null; // Set null secara eksplisit untuk Tunai
            }

            // 3. Dapatkan koneksi dan mulai transaksi
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);
            
            // 4. Dapatkan tanggal dalam format yang benar untuk database
            java.sql.Date sqlCheckout = new java.sql.Date(this.tanggal_checkout_aktual.getTime());
            java.sql.Date sqlCheckin = new java.sql.Date(this.booking.getTanggal_checkin().getTime());

            try {
                // 5. Simpan transaksi checkout
                // 🛠️ PERUBAHAN SQL: Tambahkan kolom id_user
                String sql = "INSERT INTO transaksi_checkout (tanggal_checkout, id_customer, tanggal_checkin, "
                            + "tanggal_transaksi, lama_inap, total_bayar, metode, kode_transaksi, id_booking, id_user) "
                            + "VALUES (?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?)"; // Total 9 Placeholder + NOW()

                // Array Parameters
                Object[] params = new Object[]{
                    sqlCheckout,
                    this.booking.getCustomer().getId_customer(),
                    sqlCheckin,
                    this.lama_inap,
                    this.total_bayar,
                    this.metode,
                    this.kode_transaksi, 
                    this.booking.getId_booking(),
                    this.id_user // 🆕 TAMBAHKAN ID USER
                };

                this.id_transaksi = DBHelper.insertWithParamsGetId(
                    conn,
                    sql,
                    params // Gunakan array params yang sudah diperbarui
                );
                
                if (this.id_transaksi <= 0) {
                    throw new SQLException("Gagal menyimpan transaksi checkout: Tidak ada ID yang dihasilkan");
                }
                
                System.out.println("Transaksi berhasil disimpan dengan ID: " + this.id_transaksi);

                // 6. Update status booking menjadi 'selesai'
                String updateBooking = "UPDATE booking SET status = 'selesai' WHERE id_booking = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(updateBooking)) {
                    pstmt.setInt(1, this.booking.getId_booking());
                    int updated = pstmt.executeUpdate();
                    if (updated <= 0) {
                        throw new SQLException("Gagal mengupdate status booking: Tidak ada baris yang terpengaruh");
                    }
                    System.out.println("Status booking berhasil diupdate");
                }

                // 7. Update status kamar menjadi 'kosong'
                String updateKamar = "UPDATE kamar SET status = 'kosong' WHERE id_kamar = ?";
                try (java.sql.PreparedStatement pstmt = conn.prepareStatement(updateKamar)) {
                    pstmt.setInt(1, this.booking.getKamar().getId_kamar());
                    int updated = pstmt.executeUpdate();
                    if (updated <= 0) {
                        throw new SQLException("Gagal mengupdate status kamar: Tidak ada baris yang terpengaruh");
                    }
                    System.out.println("Status kamar berhasil diupdate");
                }

                // 8. Commit transaksi jika semua berhasil
                conn.commit();
                
                // Tampilkan pesan sukses
                JOptionPane.showMessageDialog(
                    null,
                    "Checkout berhasil!\nID Transaksi: " + this.id_transaksi + 
                    "\nMetode: " + this.metode + 
                    (this.kode_transaksi != null ? "\nKode Transaksi: " + this.kode_transaksi : "") +
                    "\nDilayani oleh ID User: " + this.id_user + // 🆕 TAMPILKAN ID USER
                    "\nKamar: " + this.booking.getKamar().getNomor_kamar() +
                    "\nLama Inap: " + this.lama_inap + " hari" + 
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