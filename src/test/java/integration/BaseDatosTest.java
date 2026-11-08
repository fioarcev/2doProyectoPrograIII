package integration;

import backend.database.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDatosTest {

    public static boolean esBaseDePruebas() {
        String url = System.getenv("DB_URL");
        return url != null && url.contains("_test");
    }

    public static void limpiar() throws SQLException {
        if (!esBaseDePruebas()) {
            throw new IllegalStateException("DB_URL no apunta a una base _test. Se cancela para no borrar datos reales.");
        }
        try (Connection cn = ConexionBD.obtenerConexion();
             Statement st = cn.createStatement()) {
            st.executeUpdate("DELETE FROM detalle_reserva");
            st.executeUpdate("DELETE FROM reservas");
            st.executeUpdate("DELETE FROM recursos");
            st.executeUpdate("DELETE FROM categorias_recurso");
            st.executeUpdate("DELETE FROM funcionarios");
            st.executeUpdate("DELETE FROM usuarios");
        }
    }
}