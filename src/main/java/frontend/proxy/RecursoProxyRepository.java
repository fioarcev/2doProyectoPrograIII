package frontend.proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.CategoriaRecurso;
import model.Recurso;
import repository.RecursoRepository;
import shared.network.Respuesta;
import shared.network.Solicitud;
import shared.network.MensajeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecursoProxyRepository implements RecursoRepository {

    private final BackendProxy proxy;

    public RecursoProxyRepository(BackendProxy proxy) {
        this.proxy = proxy;
    }

    private Respuesta enviar(String operacion, JsonObject datos) {
        try {
            return proxy.enviarSolicitud(new Solicitud(operacion, datos));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo comunicar con el Backend: " + e.getMessage(), e);
        }
    }

    @Override
    public void guardar(Recurso recurso) {
        JsonObject datos = new JsonObject();
        datos.addProperty("id", recurso.getId());
        datos.addProperty("categoriaId", recurso.getCategoria().getId());
        datos.addProperty("descripcion", recurso.getDescripcion());

        comprobar(enviar("RECURSO_GUARDAR", datos));
    }

    @Override
    public List<Recurso> listar() {
        Respuesta respuesta = enviar("RECURSO_LISTAR", new JsonObject());
        comprobar(respuesta);

        JsonArray arreglo = respuesta.getDatos().getAsJsonArray("recursos");
        List<Recurso> recursos = new ArrayList<>();

        for (int i = 0; i < arreglo.size(); i++) {
            recursos.add(MensajeUtil.convertirDesdeJson(
                    arreglo.get(i).toString(), Recurso.class));
        }

        return recursos;
    }

    @Override
    public List<Recurso> listarTodos() {
        return listar();
    }

    @Override
    public Recurso buscarPorId(String id) {
        JsonObject datos = new JsonObject();
        datos.addProperty("id", id);

        Respuesta respuesta = enviar("RECURSO_BUSCAR", datos);
        comprobar(respuesta);

        if (respuesta.getDatos() == null ||
                respuesta.getDatos().get("recurso") == null ||
                respuesta.getDatos().get("recurso").isJsonNull()) {
            return null;
        }

        return MensajeUtil.convertirDesdeJson(
                respuesta.getDatos().get("recurso").toString(), Recurso.class);
    }

    @Override
    public void actualizar(Recurso recurso) {
        JsonObject datos = new JsonObject();
        datos.addProperty("id", recurso.getId());
        datos.addProperty("categoriaId", recurso.getCategoria().getId());
        datos.addProperty("descripcion", recurso.getDescripcion());

        comprobar(enviar("RECURSO_ACTUALIZAR", datos));
    }

    @Override
    public void eliminar(Recurso recurso) {
        JsonObject datos = new JsonObject();
        datos.addProperty("id", recurso.getId());

        comprobar(enviar("RECURSO_ELIMINAR", datos));
    }

    @Override
    public List<Recurso> buscarPorCategoria(String categoriaId) {
        JsonObject datos = new JsonObject();
        datos.addProperty("categoriaId", categoriaId);

        Respuesta respuesta = enviar("RECURSO_BUSCAR_CATEGORIA", datos);
        comprobar(respuesta);

        JsonArray arreglo = respuesta.getDatos().getAsJsonArray("recursos");
        List<Recurso> recursos = new ArrayList<>();

        for (int i = 0; i < arreglo.size(); i++) {
            recursos.add(MensajeUtil.convertirDesdeJson(
                    arreglo.get(i).toString(), Recurso.class));
        }

        return recursos;
    }

    private void comprobar(Respuesta respuesta) {
        if (respuesta == null || !respuesta.isExitoso()) {
            String mensaje = respuesta != null
                    ? respuesta.getMensaje()
                    : "No se recibió respuesta del Backend.";

            throw new RuntimeException(mensaje);
        }
    }
}