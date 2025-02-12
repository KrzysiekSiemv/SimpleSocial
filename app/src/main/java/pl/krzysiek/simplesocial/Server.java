package pl.krzysiek.simplesocial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Server {
    String username = "";
    String password = "";
    String url = "jdbc:mysql://";
    Connection conn;

    public Connection connection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return conn;
    }

    public Statement stmt() {
        Statement stmt = null;
        try{ stmt = connection().createStatement(); } catch (SQLException ex) { ex.printStackTrace(); }
        return stmt;
    }

    public ResultSet query(String columns, String from, String type, String table, String joinOn, String where, String args) {
        ResultSet res = null;
        try {
            res = stmt().executeQuery("SELECT " + columns + " FROM " + from + " " + type + " JOIN " + table + " ON " + joinOn + " WHERE " + where + " " + args + ";");
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        close();
        return res;
    }

    public ResultSet query(String columns, String from, String type, String table, String joinOn, String where) {
        ResultSet res = null;
        try {
            res = stmt().executeQuery("SELECT " + columns + " FROM " + from + " " + type + " JOIN " + table + " ON " + joinOn + " WHERE " + where + ";");
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        close();
        return res;
    }

    public ResultSet query(String columns, String from, String where, String args) {
        ResultSet res = null;
        try {
            res = stmt().executeQuery("SELECT " + columns + " FROM " + from + " WHERE " + where + " " + args + ";");
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        close();
        return res;
    }

    public ResultSet query(String columns, String from, String where) {
        ResultSet res = null;
        try {
            res = stmt().executeQuery("SELECT " + columns + " FROM " + from + " WHERE " + where + ";");
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        close();
        return res;
    }

    public ResultSet query(String columns, String from) {
        ResultSet res = null;
        try {
            res = stmt().executeQuery("SELECT " + columns + " FROM " + from + ";");
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        close();
        return res;
    }

    public void update(String table, String changes, String where) {
        try {stmt().executeUpdate("UPDATE " + table + " SET " + changes + " WHERE " + where + ";"); } catch (SQLException ex) { ex.printStackTrace(); }
        close();
    }

    public void delete(String from, String where){
        try { stmt().executeUpdate("DELETE FROM " + from + " WHERE " + where + ";"); } catch (SQLException ex) { ex.printStackTrace(); }
        close();
    }

    public void insert(String table, String columns, String values){
        try {stmt().executeUpdate("INSERT INTO " + table + "(" + columns + ") VALUES(" + values + ");"); } catch (SQLException ex) { ex.printStackTrace(); }
        close();
    }

    public void insert(String table, String values){
        try {stmt().executeUpdate("INSERT INTO " + table + " VALUES(" + values + ");"); } catch (SQLException ex) { ex.printStackTrace(); }
        close();
    }

    public void close(){
        try{ connection().close(); } catch (SQLException ex) { ex.printStackTrace(); }
    }
}
