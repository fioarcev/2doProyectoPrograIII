
package frontend.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import model.Usuario;
import repository.UsuarioRepository;
import shared.network.Respuesta;
import shared.network.Solicitud;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UsuarioProxyRepository implements UsuarioRepository {

    private final BackendProxy proxy;
    private final Gson gson = new Gson();

    public UsuarioProxyRepository(BackendProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public List<Usuario> listarTodos() {

        try {

            Solicitud solicitud = new Solicitud(
                    "USUARIO_LISTAR",
                    new JsonObject()
            );

            Respuesta respuesta =
                    proxy.enviarSolicitud(solicitud);

            verificarRespuesta(respuesta);

            JsonArray usuariosJson =
                    respuesta.getDatos().getAsJsonArray("usuarios");

            Type tipoLista =
                    new TypeToken<List<Usuario>>() {}.getType();

            return gson.fromJson(usuariosJson, tipoLista);

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo comunicar con el Backend.",
                    e
            );
        }
    }

    @Override
    public Usuario buscarPorId(String id) {

        try {

            JsonObject datos = new JsonObject();
            datos.addProperty("id", id);

            Solicitud solicitud = new Solicitud(
                    "USUARIO_BUSCAR",
                    datos
            );

            Respuesta respuesta =
                    proxy.enviarSolicitud(solicitud);

            verificarRespuesta(respuesta);

            if (!respuesta.getDatos().has("usuario")
                    || respuesta.getDatos().get("usuario").isJsonNull()) {
                return null;
            }

            return gson.fromJson(
                    respuesta.getDatos().get("usuario"),
                    Usuario.class
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo buscar el usuario en el Backend.",
                    e
            );
        }
    }

    @Override
    public void guardar(Usuario usuario) {
        try {
            JsonObject datos = new JsonObject();

            datos.addProperty("id", usuario.getId());
            datos.addProperty("clave", usuario.getClave());
            datos.addProperty("rol", usuario.getRol().name());

            Solicitud solicitud = new Solicitud(
                    "USUARIO_GUARDAR",
                    datos
            );

            Respuesta respuesta = proxy.enviarSolicitud(solicitud);

            verificarRespuesta(respuesta);

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo guardar el usuario en el Backend.",
                    e
            );
        }
    }

    @Override
    public void eliminar(String id) {

        try {

            JsonObject datos = new JsonObject();
            datos.addProperty("id", id);

            Solicitud solicitud = new Solicitud(
                    "USUARIO_ELIMINAR",
                    datos
            );

            Respuesta respuesta =
                    proxy.enviarSolicitud(solicitud);

            verificarRespuesta(respuesta);

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo eliminar el usuario en el Backend.",
                    e
            );
        }
    }

    private void verificarRespuesta(Respuesta respuesta) {

        if (respuesta == null || !respuesta.isExitoso()) {

            String mensaje = respuesta == null
                    ? "El Backend no devolvió una respuesta."
                    : respuesta.getMensaje();

            throw new RuntimeException(mensaje);
        }
    }
}
