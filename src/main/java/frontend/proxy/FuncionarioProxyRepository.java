
package frontend.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.Funcionario;
import repository.FuncionarioRepository;
import shared.network.Respuesta;
import shared.network.Solicitud;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class FuncionarioProxyRepository implements FuncionarioRepository {

    private final BackendProxy proxy;
    private final Gson gson = new Gson();

    public FuncionarioProxyRepository(BackendProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public List<Funcionario> listarTodos() {
        try {
            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("FUNCIONARIO_LISTAR", new JsonObject())
            );

            verificar(r);

            Type tipo = new TypeToken<List<Funcionario>>() {}.getType();

            return gson.fromJson(
                    r.getDatos().get("funcionarios"),
                    tipo
            );

        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend.", e);
        }
    }

    @Override
    public Funcionario buscarPorId(String id) {
        try {
            JsonObject datos = new JsonObject();
            datos.addProperty("id", id);

            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("FUNCIONARIO_BUSCAR", datos)
            );

            verificar(r);

            if (r.getDatos() == null
                    || !r.getDatos().has("funcionario")
                    || r.getDatos().get("funcionario") == null
                    || r.getDatos().get("funcionario").isJsonNull()) {
                return null;
            }

            return gson.fromJson(
                    r.getDatos().get("funcionario"),
                    Funcionario.class
            );

        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend.", e);
        }
    }

    @Override
    public void guardar(Funcionario funcionario) {
        try {
            JsonObject datos = new JsonObject();
            datos.add("funcionario", gson.toJsonTree(funcionario));

            Respuesta r = proxy.enviarSolicitud(
                    new Solicitud("FUNCIONARIO_GUARDAR", datos)
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
                    new Solicitud("FUNCIONARIO_ELIMINAR", datos)
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
