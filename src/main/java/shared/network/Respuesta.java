
package shared.network;

import com.google.gson.JsonObject;

public class Respuesta {

    private String tipo;
    private boolean exitoso;
    private String mensaje;
    private JsonObject datos;

    public Respuesta() {
    }

    public Respuesta(boolean exitoso, String mensaje, JsonObject datos) {
        this.tipo = "RESPUESTA";
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public Respuesta(String tipo, boolean exitoso,
                     String mensaje, JsonObject datos) {
        this.tipo = tipo;
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public static Respuesta crearNotificacion(
            String mensaje, JsonObject datos) {

        return new Respuesta(
                "NOTIFICACION",
                true,
                mensaje,
                datos
        );
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isNotificacion() {
        return "NOTIFICACION".equals(tipo);
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public JsonObject getDatos() {
        return datos;
    }
}
