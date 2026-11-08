
package backend.database;

import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {

        try (Connection conexion = ConexionBD.obtenerConexion()) {

            System.out.println("¡Conexión con MySQL exitosa!");

        } catch (SQLException e) {

            System.out.println("Error al conectar con MySQL:");
            e.printStackTrace();
        }
    }
}