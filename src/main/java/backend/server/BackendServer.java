
package backend.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BackendServer {

    private static final int PUERTO = 5000;

    public static void main(String[] args) {

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println("DB_USER configurado: "
                    + (System.getenv("DB_USER") != null));

            System.out.println("DB_PASSWORD configurado: "
                    + (System.getenv("DB_PASSWORD") != null));
            System.out.println("Servidor iniciado...");
            System.out.println("Esperando conexiones en el puerto " + PUERTO);

            while (true) {

                Socket cliente = servidor.accept();

                System.out.println("Nuevo cliente conectado: " + cliente.getInetAddress());

                ClientHandler handler = new ClientHandler(cliente);

                Thread hilo = new Thread(handler);
                hilo.start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }
}
