package integration;

import backend.database.*;
import backend.server.ClientHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import frontend.proxy.BackendProxy;
import model.*;
import org.junit.jupiter.api.*;
import shared.network.Respuesta;
import shared.network.Solicitud;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class NotificacionReservaIT {

    private ServerSocket servidor;

    @BeforeEach
    void iniciar() throws Exception {
        BaseDatosTest.limpiar();

        servidor = new ServerSocket(0);
        Thread hilo = new Thread(() -> {
            try {
                while (true) {
                    Socket s = servidor.accept();
                    new Thread(new ClientHandler(s)).start();
                }
            } catch (Exception ignorada) { }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    @AfterEach
    void cerrar() throws Exception {
        servidor.close();
    }

    @Test
    void otroClienteRecibeNotificacionAlReservar() throws Exception {
        new FuncionarioBDRepository().guardar(new Funcionario("F1", "Ana", "8888-0000"));
        CategoriaRecurso cat = new CategoriaRecurso("C1", "Salas");
        new CategoriaBDRepository().guardar(cat);
        new RecursoBDRepository().guardar(new Recurso("R1", cat, "Sala A"));

        BackendProxy emisor = new BackendProxy("localhost", servidor.getLocalPort());
        BackendProxy receptor = new BackendProxy("localhost", servidor.getLocalPort());

        CompletableFuture<Respuesta> recibida = new CompletableFuture<>();
        receptor.setListenerNotificaciones(recibida::complete);

        JsonObject datos = new JsonObject();
        datos.addProperty("id", "RES1");
        datos.addProperty("actividad", "Reunión");
        datos.addProperty("fecha", java.time.LocalDate.now().plusDays(1).toString());
        datos.addProperty("horaInicio", "09:00");
        datos.addProperty("horaFin", "10:00");
        datos.addProperty("funcionarioId", "F1");
        datos.addProperty("estado", "ACTIVA");
        JsonArray detalles = new JsonArray();
        JsonObject d = new JsonObject();
        d.addProperty("categoriaId", "C1");
        d.addProperty("recursoId", "R1");
        detalles.add(d);
        datos.add("detalles", detalles);

        Respuesta respuesta = emisor.enviarSolicitud(new Solicitud("RESERVA_GUARDAR", datos));
        assertTrue(respuesta.isExitoso(), respuesta.getMensaje());

        Respuesta notificacion = recibida.get(5, TimeUnit.SECONDS);
        assertTrue(notificacion.isNotificacion());
        assertEquals("Reunión", notificacion.getDatos().get("actividad").getAsString());
        emisor.cerrarConexion();
        receptor.cerrarConexion();
    }
}
