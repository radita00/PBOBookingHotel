package backend;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

public class Users {
    private int id_user;
    private String username;
    private String password;
    private String role;

    public Users() {}
    
    //Getter dan Setter
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    //1. Login
    public Users login(String username, String password) {
        Users user = null;
        String query = "SELECT * FROM users WHERE username = '" + username 
                        + "' AND password = '" + password + "'";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            if (rs.next()) {
                user = new Users();
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
    
    //2. Registrasi Pegawai
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

    //3. Mengambil Semua Data Users (Untuk Admin)
    public ArrayList<Users> getAllUsers() {
        ArrayList<Users> listUsers = new ArrayList<>();
        String sql = "SELECT id_user, username, role FROM users";
        
        try (ResultSet rs = DBHelper.selectQuery(sql)) {
            
            while (rs.next()) {
                Users user = new Users();
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
    
    //Search berdasarkan Username
    public ArrayList<Users> searchUsers(String keyword) {
        ArrayList<Users> listUsers = new ArrayList<>();
        // Pencarian berdasarkan username
        String sql = "SELECT id_user, username, role FROM users WHERE username LIKE '%" + keyword + "%'";

        try (ResultSet rs = DBHelper.selectQuery(sql)) {
            while (rs.next()) {
                Users user = new Users();
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
                return false;
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
}