package backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = System.getenv().getOrDefault(
            "DB_URL",
            "jdbc:mysql://localhost:3306/reservas_recursos?serverTimezone=UTC");

    private static final String USUARIO = System.getenv("DB_USER");

    private static final String CLAVE = System.getenv("DB_PASSWORD");

    public static Connection obtenerConexion() throws SQLException {

        if (USUARIO == null || USUARIO.isBlank() || CLAVE == null || CLAVE.isBlank()) {
            throw new SQLException("Faltan las variables de entorno DB_USER y DB_PASSWORD.");
        }
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}