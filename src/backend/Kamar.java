package backend;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

    public int getId_kamar() {
        return id_kamar;
    }

    public void setId_kamar(int id_kamar) {
        this.id_kamar = id_kamar;
    }

    public String getNomor_kamar() {
        return nomor_kamar;
    }

    public void setNomor_kamar(String nomor_kamar) {
        this.nomor_kamar = nomor_kamar;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void save() {
        if (this.id_kamar == 0) {
            String sql = "INSERT INTO kamar (nomor_kamar, tipe, harga, status) VALUES ("
                    + "'" + this.nomor_kamar + "',"
                    + "'" + this.tipe + "',"
                    + "" + this.harga + ","
                    + "'" + this.status + "'"
                    + ")";

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
                Kamar k = new Kamar();
                k.setId_kamar(rs.getInt("id_kamar"));
                k.setNomor_kamar(rs.getString("nomor_kamar"));
                k.setTipe(rs.getString("tipe"));
                k.setHarga(rs.getDouble("harga"));
                k.setStatus(rs.getString("status"));
                listKamar.add(k);
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
                k.setId_kamar(rs.getInt("id_kamar"));
                k.setNomor_kamar(rs.getString("nomor_kamar"));
                k.setTipe(rs.getString("tipe"));
                k.setHarga(rs.getDouble("harga"));
                k.setStatus(rs.getString("status"));
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
                Kamar k = new Kamar();
                k.setId_kamar(rs.getInt("id_kamar"));
                k.setNomor_kamar(rs.getString("nomor_kamar"));
                k.setTipe(rs.getString("tipe"));
                k.setHarga(rs.getDouble("harga"));
                k.setStatus(rs.getString("status"));
                listKamar.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listKamar;
    }

    public ArrayList<Kamar> getKamarTersedia(Date checkIn, Date checkOut) {
        ArrayList<Kamar> listKamar = new ArrayList<>();

        String query = "SELECT k.* FROM kamar k "
                + "WHERE k.status = 'kosong' "
                + "AND k.id_kamar NOT IN ( "
                + "    SELECT id_kamar FROM booking "
                + "    WHERE status != 'selesai' "
                + "    AND ( "
                + "        (tanggal_checkin <= '" + checkOut.toString() + "' AND tanggal_checkout >= '" + checkIn.toString() + "')"
                + "    ) "
                + ")";

        try (ResultSet rs = DBHelper.selectQuery(query)) {
            while (rs != null && rs.next()) {
                Kamar kamar = new Kamar();
                kamar.setId_kamar(rs.getInt("id_kamar"));
                kamar.setNomor_kamar(rs.getString("nomor_kamar"));
                kamar.setTipe(rs.getString("tipe"));
                kamar.setHarga(rs.getDouble("harga"));
                kamar.setStatus(rs.getString("status"));
                listKamar.add(kamar);
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