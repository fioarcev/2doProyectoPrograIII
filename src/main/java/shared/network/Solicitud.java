
package shared.network;

import com.google.gson.JsonObject;

public class Solicitud {

    private String tipo;
    private String operacion;
    private JsonObject datos;

    public Solicitud(String operacion, JsonObject datos) {
        this.tipo = "SOLICITUD";
        this.operacion = operacion;
        this.datos = datos;
    }

    public String getTipo() {
        return tipo;
    }

    public String getOperacion() {
        return operacion;
    }

    public JsonObject getDatos() {
        return datos;
    }
}
