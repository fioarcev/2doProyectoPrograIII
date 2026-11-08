
import javax.swing.*;

import controller.LoginController;
import view.LoginView;

import service.AuthService;

import repository.UsuarioRepository;

import frontend.proxy.BackendProxy;
import frontend.proxy.UsuarioProxyRepository;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                // Conexión con el Backend.
                BackendProxy proxy =
                        new BackendProxy("localhost", 5000);

                // Repositorio remoto de usuarios.
                UsuarioRepository usuarioRepository =
                        new UsuarioProxyRepository(proxy);

                // Servicio de autenticación.
                AuthService authService =
                        new AuthService(usuarioRepository);

                // Vista de inicio de sesión.
                LoginView loginView = new LoginView();

                // Controlador del login.
                new LoginController(
                        loginView,
                        authService,
                        usuarioRepository,
                        proxy
                );

                loginView.setVisible(true);

            } catch (IOException e) {

                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo conectar con el Backend.\n"
                                + "Verifique que el servidor esté iniciado.\n\n"
                                + e.getMessage(),
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE
                );

            }
        });
    }
}

