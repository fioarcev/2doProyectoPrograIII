
package frontend.proxy;

import shared.network.MensajeUtil;
import shared.network.Solicitud;
import shared.network.Respuesta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BackendProxy {

    private final Socket socket;
    private final BufferedReader entrada;
    private final PrintWriter salida;

    private final BlockingQueue<Respuesta> respuestasPendientes =
            new LinkedBlockingQueue<>();

    private volatile Consumer<Respuesta> listenerNotificaciones;

    private volatile boolean conectado = true;

    public BackendProxy(String host, int puerto) throws IOException {

        socket = new Socket(host, puerto);

        entrada = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

        salida = new PrintWriter(
                socket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
        );

        Thread lector = new Thread(
                this::escucharMensajes,
                "BackendProxy-Lector"
        );

        lector.setDaemon(true);
        lector.start();
    }

    public synchronized Respuesta enviarSolicitud(
            Solicitud solicitud) throws IOException {

        if (!conectado || socket.isClosed()) {
            throw new IOException("No hay conexión con el Backend.");
        }

        String json = MensajeUtil.convertirAJson(solicitud);

        salida.println(json);

        if (salida.checkError()) {
            throw new IOException("No se pudo enviar la solicitud.");
        }

        try {
            Respuesta respuesta = respuestasPendientes.take();

            if (!respuesta.isExitoso()
                    && respuesta.getMensaje() != null
                    && respuesta.getMensaje().startsWith("ERROR_CONEXION:")) {

                throw new IOException(respuesta.getMensaje());
            }

            return respuesta;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Se interrumpió la espera de respuesta del Backend.", e
            );
        }
    }

    private void escucharMensajes() {

        try {
            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {

                Respuesta respuesta =
                        MensajeUtil.convertirDesdeJson(
                                mensaje,
                                Respuesta.class
                        );

                if (respuesta.isNotificacion()) {

                    Consumer<Respuesta> listener =
                            listenerNotificaciones;

                    if (listener != null) {
                        listener.accept(respuesta);
                    }

                } else {
                    respuestasPendientes.offer(respuesta);
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Error recibiendo mensajes del Backend: "
                            + e.getMessage()
            );

        } finally {

            conectado = false;

            respuestasPendientes.offer(
                    new Respuesta(
                            false,
                            "ERROR_CONEXION: Se perdió la conexión con el Backend.",
                            null
                    )
            );
        }
    }

    public void setListenerNotificaciones(
            Consumer<Respuesta> listener) {

        this.listenerNotificaciones = listener;
    }

    public void cerrarConexion() {

        conectado = false;

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(
                    "Error al cerrar la conexión: " + e.getMessage()
            );
        }
    }
}