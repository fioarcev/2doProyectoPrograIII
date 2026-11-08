
package shared.network;

import com.google.gson.Gson;

public class MensajeUtil {

    private static final Gson gson = new Gson();

    public static String convertirAJson(Object objeto) {
        return gson.toJson(objeto);
    }

    public static <T> T convertirDesdeJson(String json, Class<T> clase) {
        return gson.fromJson(json, clase);
    }
}
