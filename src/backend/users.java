package backend;

import backend.DBHelper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class users {
    private int id_user;
    private String username;
    private String password;
    private String role; // ini seperti 'admin', 'pegawai'

    public users() {}
    
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    
    public users login(String username, String password) {
        users user = null;
        String query = "SELECT * FROM users WHERE username = '" + username 
                        + "' AND password = '" + password + "'";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            if (rs.next()) {
                user = new users();
                user.setId_user(rs.getInt("id_user"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    
    public boolean registerPegawai(String username, String password) {
        String cekQuery = "SELECT * FROM users WHERE username = '" + username + "'";
        try (ResultSet rs = DBHelper.selectQuery(cekQuery)) {
            if (rs.next()) {
                return false;
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

    /**
     * Mengambil semua data users/pegawai dari database.
     * Digunakan oleh frmDataPegawai.
     */
    public ArrayList<users> getAllUsers() {
        ArrayList<users> listUsers = new ArrayList<>();
        // Hanya ambil ID, username, dan role
        String sql = "SELECT id_user, username, role FROM users";
        
        try (ResultSet rs = DBHelper.selectQuery(sql)) {
            
            while (rs.next()) {
                users user = new users();
                user.setId_user(rs.getInt("id_user"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                
                listUsers.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listUsers;
    }
    
    public boolean updateUser(int id, String username, String role) {
        String query = "UPDATE users SET username = '" + username + "', role = '" + role + "' WHERE id_user = " + id;
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            int result = stmt.executeUpdate(query);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id_user = " + id;
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            int result = stmt.executeUpdate(query);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
