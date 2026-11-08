
package frontend.proxy;

import shared.network.Solicitud;
import shared.network.Respuesta;

import com.google.gson.JsonObject;

import java.io.IOException;

public class PruebaProxy {

    public static void main(String[] args) {

        BackendProxy proxy = null;

        try {

            proxy = new BackendProxy(
                    "localhost",
                    5000
            );

            System.out.println(
                    "Conectado al Backend."
            );

            JsonObject datos = new JsonObject();

            datos.addProperty("id", "admin");
            datos.addProperty("clave", "admin");

            Solicitud solicitud =
                    new Solicitud("LOGIN", datos);

            Respuesta respuesta =
                    proxy.enviarSolicitud(solicitud);

            System.out.println("\nRespuesta del Backend:");

            System.out.println(
                    "Exitoso: "
                            + respuesta.isExitoso()
            );

            System.out.println(
                    "Mensaje: "
                            + respuesta.getMensaje()
            );

            if (respuesta.isExitoso()) {

                System.out.println(
                        "ID: "
                                + respuesta.getDatos()
                                .get("id").getAsString()
                );

                System.out.println(
                        "Rol: "
                                + respuesta.getDatos()
                                .get("rol").getAsString()
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Error de comunicación: "
                            + e.getMessage()
            );

        } finally {

            if (proxy != null) {
                proxy.cerrarConexion();
            }
        }
    }
}
