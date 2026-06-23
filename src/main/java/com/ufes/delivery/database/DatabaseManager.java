package com.ufes.delivery.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private static String url = "jdbc:sqlite:delivery.db";

    private DatabaseManager() {
    }

    public static void setUrl(String newUrl) {
        url = newUrl;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite JDBC nao encontrado", e);
        }
        return DriverManager.getConnection(url);
    }

    public static void inicializarBanco() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nome TEXT NOT NULL,"
                + "usuario TEXT UNIQUE NOT NULL,"
                + "senha TEXT NOT NULL,"
                + "perfil TEXT NOT NULL,"
                + "situacao TEXT NOT NULL"
                + ");";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar o banco de dados", e);
        }
    }
}
