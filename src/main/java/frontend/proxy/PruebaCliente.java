
package frontend.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class PruebaCliente {

    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                     StandardCharsets.UTF_8));

                PrintWriter salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                Scanner teclado = new Scanner(System.in)
        ) {

            System.out.println("Conectado al servidor.");

            while (true) {

                System.out.print("Escriba un mensaje: ");
                String mensaje = teclado.nextLine();

                salida.println(mensaje);

                String respuesta = entrada.readLine();

                System.out.println("Respuesta: " + respuesta);

                if (mensaje.equalsIgnoreCase("salir")) {
                    break;
                }
            }

        } catch (IOException e) {

            System.out.println("No se pudo conectar: " + e.getMessage());
        }
    }
}
