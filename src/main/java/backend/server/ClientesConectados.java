
package backend.server;

import shared.network.MensajeUtil;
import shared.network.Respuesta;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientesConectados {

    private static final Map<ClientHandler, PrintWriter> CLIENTES =
            new ConcurrentHashMap<>();

    public static void registrar(
            ClientHandler cliente, PrintWriter salida) {

        CLIENTES.put(cliente, salida);
    }

    public static void desconectar(ClientHandler cliente) {
        CLIENTES.remove(cliente);
    }

    public static void notificarOtros(
            ClientHandler emisor, Respuesta notificacion) {

        String json = MensajeUtil.convertirAJson(notificacion);

        for (Map.Entry<ClientHandler, PrintWriter> entrada
                : CLIENTES.entrySet()) {

            if (entrada.getKey() != emisor) {

                PrintWriter salida = entrada.getValue();

                synchronized (salida) {
                    salida.println(json);
                    salida.flush();
                }
            }
        }
    }
}
