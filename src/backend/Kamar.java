package backend;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// Asumsi class DBHelper sudah tersedia
public class Kamar {
    private int id_kamar;
    private String nomor_kamar;
    private String tipe;
    private double harga;
    private String status;

    public Kamar() {
    }

    public Kamar(String nomor_kamar, String tipe, double harga) {
        this.nomor_kamar = nomor_kamar;
        this.tipe = tipe;
        this.harga = harga;
        this.status = "kosong";
    }

    // --- GETTERS & SETTERS ---
    public int getId_kamar() { return id_kamar; }
    public void setId_kamar(int id_kamar) { this.id_kamar = id_kamar; }
    public String getNomor_kamar() { return nomor_kamar; }
    public void setNomor_kamar(String nomor_kamar) { this.nomor_kamar = nomor_kamar; }
    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // Metode bantuan untuk memetakan ResultSet ke objek Kamar
    private Kamar mapResultSetToKamar(ResultSet rs) throws SQLException {
        Kamar k = new Kamar();
        k.setId_kamar(rs.getInt("id_kamar"));
        k.setNomor_kamar(rs.getString("nomor_kamar"));
        k.setTipe(rs.getString("tipe"));
        k.setHarga(rs.getDouble("harga"));
        k.setStatus(rs.getString("status"));
        return k;
    }

    /**
     * Metode statis untuk mengecek apakah kamar sedang di-booking aktif ('booked').
     * Digunakan untuk validasi update status di frmKamar.
     */
    public static boolean isKamarCurrentlyBooked(int idKamar) {
        String query = "SELECT COUNT(*) FROM booking WHERE id_kamar = " + idKamar + " AND status = 'booked'";
        try (ResultSet rs = DBHelper.selectQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking booking status for Kamar ID " + idKamar + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void save() {
        if (this.id_kamar == 0) {
            String sql = "INSERT INTO kamar (nomor_kamar, tipe, harga, status) VALUES ("
                    + "'" + this.nomor_kamar + "',"
                    + "'" + this.tipe + "',"
                    + "" + this.harga + ","
                    + "'" + this.status + "'"
                    + ")";

            // Asumsi DBHelper.insertQueryGetId mengembalikan ID yang dihasilkan
            this.id_kamar = DBHelper.insertQueryGetId(sql);
        } else {
            String sql = "UPDATE kamar SET "
                    + "nomor_kamar = '" + this.nomor_kamar + "',"
                    + "tipe = '" + this.tipe + "',"
                    + "harga = " + this.harga + ","
                    + "status = '" + this.status + "'"
                    + " WHERE id_kamar = " + this.id_kamar;

            DBHelper.executeQuery(sql);
        }
    }

    public ArrayList<Kamar> getAll() {
        ArrayList<Kamar> listKamar = new ArrayList<>();
        String query = "SELECT * FROM kamar";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                listKamar.add(mapResultSetToKamar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }
    
    public ArrayList<Kamar> searchKamar(String keyword) {
        ArrayList<Kamar> listKamar = new ArrayList<>();
        String query = "SELECT * FROM kamar WHERE nomor_kamar LIKE '%" + keyword + "%' "
                     + "OR tipe LIKE '%" + keyword + "%'"; 

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                listKamar.add(mapResultSetToKamar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }

    public Kamar getById(int id) {
        Kamar k = new Kamar();
        String query = "SELECT * FROM kamar WHERE id_kamar = " + id;

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            if (rs != null && rs.next()) {
                k = mapResultSetToKamar(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return k;
    }

    public ArrayList<Kamar> getAvailable() {
        ArrayList<Kamar> listKamar = new ArrayList<>();
        String query = "SELECT * FROM kamar WHERE status = 'kosong'";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                listKamar.add(mapResultSetToKamar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }
    
    // 🆕 METHOD BARU: Mendapatkan Kamar yang sedang 'terisi' (Occupied)
    public ArrayList<Kamar> getOccupied() {
        ArrayList<Kamar> listKamar = new ArrayList<>();
        String query = "SELECT * FROM kamar WHERE status = 'terisi'";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                listKamar.add(mapResultSetToKamar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }
    
    // 🆕 METHOD BARU: Mendapatkan Kamar yang sedang 'perawatan' (Maintenance)
    public ArrayList<Kamar> getMaintenance() {
        ArrayList<Kamar> listKamar = new ArrayList<>();
        String query = "SELECT * FROM kamar WHERE status = 'perawatan'";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                listKamar.add(mapResultSetToKamar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }

    /**
     * Mendapatkan kamar yang tersedia untuk dibooking antara tanggal checkIn dan checkOut.
     * Kamar dianggap tersedia jika statusnya 'kosong' DAN tidak ada konflik dengan booking aktif ('booked').
     */
    public ArrayList<Kamar> getKamarTersedia(Date checkIn, Date checkOut) {
        ArrayList<Kamar> listKamar = new ArrayList<>();

        // Query: Pilih Kamar yang statusnya 'kosong' (di tabel kamar) 
        // DAN ID Kamar tersebut TIDAK ada dalam daftar booking yang berkonflik.
        String query = "SELECT k.* FROM kamar k "
                + "WHERE k.status = 'kosong' " 
                + "AND k.id_kamar NOT IN ( "
                + "      SELECT id_kamar FROM booking "
                + "      WHERE status = 'booked' " // Hanya periksa booking yang masih aktif
                + "      AND ( "
                // Logika Konflik: (Awal Booking <= Akhir Pencarian) AND (Akhir Booking >= Awal Pencarian)
                + "          (tanggal_checkin <= '" + checkOut.toString() + "' AND tanggal_checkout >= '" + checkIn.toString() + "')"
                + "      ) "
                + ")";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                listKamar.add(mapResultSetToKamar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }

    @Override
    public String toString() {
        return "Kamar " + nomor_kamar + " (" + tipe + ") - Rp " + harga;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM kamar WHERE id_kamar = " + id;
        return DBHelper.executeQuery(sql);
    }
}