package frontend.proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.CategoriaRecurso;
import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import repository.ReservaRepository;
import shared.network.Respuesta;
import shared.network.Solicitud;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaProxyRepository implements ReservaRepository {

    private final BackendProxy proxy;

    public ReservaProxyRepository(BackendProxy proxy) {
        this.proxy = proxy;
    }

    private Respuesta enviar(String operacion, JsonObject datos) {
        try {
            return proxy.enviarSolicitud(new Solicitud(operacion, datos));
        } catch (IOException e) {
            throw new RuntimeException("Error de comunicación con el Backend: "
                    + e.getMessage(), e);
        }
    }

    private void comprobar(Respuesta respuesta) {
        if (respuesta == null || !respuesta.isExitoso()) {
            String mensaje = respuesta == null
                    ? "No se recibió respuesta del Backend."
                    : respuesta.getMensaje();

            throw new RuntimeException(mensaje);
        }
    }

    @Override
    public void guardar(Reserva reserva) {
        comprobar(enviar("RESERVA_GUARDAR", convertirReserva(reserva)));
    }

    @Override
    public List<Reserva> listar() {
        Respuesta respuesta = enviar("RESERVA_LISTAR", new JsonObject());
        comprobar(respuesta);

        JsonArray arreglo = respuesta.getDatos().getAsJsonArray("reservas");
        List<Reserva> reservas = new ArrayList<>();

        for (JsonElement elemento : arreglo) {
            reservas.add(convertirReserva(elemento.getAsJsonObject()));
        }

        return reservas;
    }

    @Override
    public Reserva buscarPorId(String id) {
        JsonObject datos = new JsonObject();
        datos.addProperty("id", id);

        Respuesta respuesta = enviar("RESERVA_BUSCAR", datos);
        comprobar(respuesta);

        JsonElement elemento = respuesta.getDatos().get("reserva");

        if (elemento == null || elemento.isJsonNull()) {
            return null;
        }

        return convertirReserva(elemento.getAsJsonObject());
    }

    @Override
    public void actualizar(Reserva reserva) {
        comprobar(enviar("RESERVA_ACTUALIZAR", convertirReserva(reserva)));
    }

    @Override
    public void eliminar(Reserva reserva) {
        JsonObject datos = new JsonObject();
        datos.addProperty("id", reserva.getId());

        comprobar(enviar("RESERVA_ELIMINAR", datos));
    }

    private JsonObject convertirReserva(Reserva reserva) {
        JsonObject datos = new JsonObject();

        datos.addProperty("id", reserva.getId());
        datos.addProperty("actividad", reserva.getActividad());
        datos.addProperty("fecha", reserva.getFecha().toString());
        datos.addProperty("horaInicio", reserva.getHoraInicio().toString());
        datos.addProperty("horaFin", reserva.getHoraFin().toString());
        datos.addProperty("funcionarioId", reserva.getFuncionarioId());
        datos.addProperty("estado", reserva.getEstado().name());

        JsonArray detalles = new JsonArray();

        if (reserva.getDetalles() != null) {
            for (DetalleReserva detalle : reserva.getDetalles()) {
                JsonObject elemento = new JsonObject();

                elemento.addProperty("categoriaId", detalle.getCategoria().getId());
                elemento.addProperty("recursoId", detalle.getRecurso().getId());

                detalles.add(elemento);
            }
        }

        datos.add("detalles", detalles);

        return datos;
    }

    private Reserva convertirReserva(JsonObject datos) {
        Reserva reserva = new Reserva(
                datos.get("id").getAsString(),
                datos.get("actividad").getAsString(),
                LocalDate.parse(datos.get("fecha").getAsString()),
                LocalTime.parse(datos.get("horaInicio").getAsString()),
                LocalTime.parse(datos.get("horaFin").getAsString()),
                datos.get("funcionarioId").getAsString(),
                EstadoReserva.valueOf(datos.get("estado").getAsString())
        );

        JsonArray detalles = datos.getAsJsonArray("detalles");

        if (detalles != null) {
            for (JsonElement elemento : detalles) {
                JsonObject detalleJson = elemento.getAsJsonObject();

                String categoriaId =
                        detalleJson.get("categoriaId").getAsString();

                String recursoId =
                        detalleJson.get("recursoId").getAsString();

                String categoriaDescripcion =
                        detalleJson.has("categoriaDescripcion")
                                && !detalleJson.get("categoriaDescripcion").isJsonNull()
                                ? detalleJson.get("categoriaDescripcion").getAsString()
                                : categoriaId;

                String recursoDescripcion =
                        detalleJson.has("recursoDescripcion")
                                && !detalleJson.get("recursoDescripcion").isJsonNull()
                                ? detalleJson.get("recursoDescripcion").getAsString()
                                : "";

                CategoriaRecurso categoria = new CategoriaRecurso(
                        categoriaId,
                        categoriaDescripcion
                );

                Recurso recurso = new Recurso(
                        recursoId,
                        categoria,
                        recursoDescripcion
                );

                reserva.getDetalles().add(
                        new DetalleReserva(categoria, recurso)
                );
            }
        }

        return reserva;
    }
}