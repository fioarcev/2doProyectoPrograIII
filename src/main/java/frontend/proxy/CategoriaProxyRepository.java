
package frontend.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.CategoriaRecurso;
import repository.CategoriaRepository;
import shared.network.Respuesta;
import shared.network.Solicitud;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CategoriaProxyRepository implements CategoriaRepository {

    private final BackendProxy proxy;
    private final Gson gson = new Gson();

    public CategoriaProxyRepository(BackendProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public List<CategoriaRecurso> listarTodas() {
        return listarTodos();
    }

    @Override
    public List<CategoriaRecurso> listarTodos() {
        try {
            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("CATEGORIA_LISTAR", new JsonObject())
            );

            verificar(r);

            Type tipo = new TypeToken<List<CategoriaRecurso>>() {}.getType();

            return gson.fromJson(
                    r.getDatos().get("categorias"),
                    tipo
            );

        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend.", e);
        }
    }

    @Override
    public CategoriaRecurso buscarPorId(String id) {
        try {
            JsonObject datos = new JsonObject();
            datos.addProperty("id", id);

            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("CATEGORIA_BUSCAR", datos)
            );

            verificar(r);

            if (r.getDatos().get("categoria").isJsonNull()) {
                return null;
            }

            return gson.fromJson(
                    r.getDatos().get("categoria"),
                    CategoriaRecurso.class
            );

        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend.", e);
        }
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {
        try {
            JsonObject datos = new JsonObject();
            datos.add("categoria", gson.toJsonTree(categoria));

            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("CATEGORIA_GUARDAR", datos)
            );

            verificar(r);

        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend.", e);
        }
    }

    @Override
    public void eliminar(String id) {
        try {
            JsonObject datos = new JsonObject();
            datos.addProperty("id", id);

            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("CATEGORIA_ELIMINAR", datos)
            );

            verificar(r);

        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend.", e);
        }
    }

    private void verificar(Respuesta r) {
        if (r == null || !r.isExitoso()) {
            throw new RuntimeException(
                    r == null ? "Sin respuesta del Backend." : r.getMensaje()
            );
        }
    }
}
