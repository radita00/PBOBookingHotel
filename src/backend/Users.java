package backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;

// Asumsi ada kelas DBHelper yang memiliki selectQuery dan executeQuery
// import util.DBHelper; // Pastikan Anda mengimpor DBHelper dengan benar

public class Users {
    private int id_user;
    private String username;
    private String password;
    private String role;

    public Users() {}
    
    // --- Getter dan Setter ---
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Helper method untuk memetakan ResultSet ke objek Users
    private Users mapResultSetToUsers(ResultSet rs) throws SQLException {
        Users user = new Users();
        user.setId_user(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        // Asumsi kolom password ada, jika tidak, hapus try-catch ini
        try {
            user.setPassword(rs.getString("password"));
        } catch (SQLException ignored) {
            // Kolom password mungkin tidak diambil, ini diabaikan
        }
        user.setRole(rs.getString("role"));
        return user;
    }

    // BARU: Method untuk mengambil Users berdasarkan ID
    public Users getById(int id) {
        String query = "SELECT id_user, username, role, password FROM users WHERE id_user = " + id;
        
        // NOTE: Asumsi DBHelper.selectQuery mengembalikan ResultSet yang harus ditutup.
        try (ResultSet rs = DBHelper.selectQuery(query)) { 
            if (rs.next()) {
                return mapResultSetToUsers(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Users(); // Mengembalikan objek Users kosong jika tidak ditemukan
    }
    
    // 🎯 FUNGSI BARU: Mengambil Username secara statis berdasarkan ID
    public static String getUsernameById(int id_user) {
        Users user = new Users().getById(id_user);
        
        // Cek jika user ditemukan (asumsi id_user > 0 menandakan user valid)
        if (user.getId_user() > 0) {
            return user.getUsername();
        } else {
            return "User Not Found (ID: " + id_user + ")";
        }
    }
    
    //1. Login
    public Users login(String username, String password) {
        Users user = null;
        String query = "SELECT id_user, username, role, password FROM users WHERE username = '" + username 
                         + "' AND password = '" + password + "'";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            if (rs.next()) {
                user = mapResultSetToUsers(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    
    //2. Registrasi Pegawai
    public boolean registerPegawai(String username, String password) {
        String cekQuery = "SELECT id_user FROM users WHERE username = '" + username + "'";
        try (ResultSet rs = DBHelper.selectQuery(cekQuery)) {
            if (rs.next()) {
                return false; // Username sudah ada
            }
            
            String query = "INSERT INTO users (username, password, role) VALUES ('"
                    + username + "', '" 
                    + password + "', 'pegawai')";
                    
            int result = DBHelper.insertQueryGetId(query);
            return result > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //3. Mengambil Semua Data Users (Untuk Admin)
    public ArrayList<Users> getAllUsers() {
        ArrayList<Users> listUsers = new ArrayList<>();
        // Asumsi query ini sudah mengambil semua kolom yang diperlukan oleh mapResultSetToUsers
        String sql = "SELECT id_user, username, role, password FROM users"; 
        
        try (ResultSet rs = DBHelper.selectQuery(sql)) {
            
            while (rs.next()) {
                listUsers.add(mapResultSetToUsers(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listUsers;
    }
    
    //Search berdasarkan Username
    public ArrayList<Users> searchUsers(String keyword) {
        ArrayList<Users> listUsers = new ArrayList<>();
        String sql = "SELECT id_user, username, role, password FROM users WHERE username LIKE '%" + keyword + "%'";

        try (ResultSet rs = DBHelper.selectQuery(sql)) {
            while (rs.next()) {
                listUsers.add(mapResultSetToUsers(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listUsers;
    }

    //4. Update Data User (Admin)
    public boolean updateUser(int id, String username, String role) {
        String query = "UPDATE users SET username = '" + username + "', role = '" + role + "' WHERE id_user = " + id;
        return DBHelper.executeQuery(query); 
    }
    
    //5. Hapus User (Admin)
    public boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id_user = " + id;
        return DBHelper.executeQuery(query);
    }
    
    //6. Reset Password (Untuk frmLupaPassword)
    public boolean resetPassword(String username, String newPassword) {
        String cekQuery = "SELECT id_user FROM users WHERE username = '" + username + "'";
        int id_user = -1;

        try (ResultSet rs = DBHelper.selectQuery(cekQuery)) {
            if (rs.next()) {
                id_user = rs.getInt("id_user");
            } else {
                return false; // User tidak ditemukan
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (id_user != -1) {
            String updateQuery = "UPDATE users SET password = '" + newPassword + "' WHERE id_user = " + id_user;
            return DBHelper.executeQuery(updateQuery); 
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "ID: " + id_user + " - " + username + " (" + role + ")";
    }
}