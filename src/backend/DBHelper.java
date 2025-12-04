package backend;

import java.sql.*;

public class DBHelper {
    // Kredensial Database MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/bookinghotel"; 
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Ganti dengan password Anda jika ada
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Membuat dan mengembalikan koneksi baru ke database.
     * PENTING: Koneksi yang dikembalikan HARUS ditutup oleh metode yang memanggilnya.
     * @return Objek Connection
     * @throws SQLException Jika terjadi kesalahan koneksi
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC tidak ditemukan: " + DRIVER);
            throw new SQLException("Driver JDBC tidak ditemukan.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // --- QUERY UNTUK TRANSAKSI (Menerima Connection) ---

    /**
     * Executes an INSERT query with parameters, using an existing Connection (for transactions), 
     * and returns the generated ID.
     * @param conn Connection objek yang sudah disiapkan untuk transaksi (AutoCommit=false)
     * @param query The SQL query with ? placeholders
     * @param params The parameter values
     * @return The generated ID, atau -1 jika gagal
     * @throws SQLException Jika terjadi kesalahan SQL
     */
    public static int insertWithParamsGetId(Connection conn, String query, Object[] params) throws SQLException {
        int result = -1;
        // Gunakan PreparedStatement dengan Connection yang sudah ada
        try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                // Perhatikan: index parameter dimulai dari 1
                pstmt.setObject(i + 1, params[i]); 
            }
            
            // Execute update
            pstmt.executeUpdate();
            
            // Get generated keys
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
            
        } 
        return result;
    }
    
    /**
     * Executes an UPDATE or DELETE query, using an existing Connection (for transactions).
     * @param conn Connection objek yang sudah disiapkan untuk transaksi (AutoCommit=false)
     * @param query Query UPDATE atau DELETE
     * @return true jika berhasil
     * @throws SQLException Jika terjadi kesalahan SQL
     */
    public static boolean executeQuery(Connection conn, String query) throws SQLException {
        // Gunakan Statement dengan Connection yang sudah ada
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            return true;
        }
    }

    // --- QUERY NON-TRANSAKSI (Dipertahankan untuk class lain yang mungkin memanggilnya) ---
    
    /**
     * Mengeksekusi query INSERT dan mengembalikan ID dari baris yang baru dibuat.
     */
    public static int insertQueryGetId(String query){
        int result = -1;
        try (Connection conn = getConnection(); 
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Mengeksekusi query UPDATE atau DELETE (Non-transactional, membuat koneksi sendiri).
     */
    public static boolean executeQuery(String query){
        boolean result = false;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Mengeksekusi query SELECT.
     */
    public static ResultSet selectQuery(String query){
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}