package com.inventario.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de conexión reutilizable basada en JDBC (mysql-connector-j).
 * Cada operación obtiene su propia conexión con try-with-resources.
 */
public class ConexionBD {

    private static String host = "localhost";
    private static String puerto = "3307";
    private static String baseDatos = "inventario_db";
    private static String usuario = "root";
    private static String clave = "";

    private ConexionBD() { }

    /** Define las credenciales que se usarán en las conexiones siguientes. */
    public static void configurar(String nuevoUsuario, String nuevaClave) {
        usuario = nuevoUsuario;
        clave = nuevaClave;
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Abre y cierra una conexión para comprobar que los datos son correctos. */
    public static void probarConexion() throws SQLException {
        try (Connection c = obtenerConexion()) {
            // Si llega aquí, la conexión fue exitosa.
        }
    }
}
