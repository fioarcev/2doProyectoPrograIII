package controller;

import frontend.proxy.BackendProxy;
import frontend.proxy.UsuarioProxyRepository;
import service.AuthService;
import view.LoginView;

import javax.swing.*;
import java.io.IOException;

public class MainLogin {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                BackendProxy backendProxy =
                        new BackendProxy("localhost", 5000);

                UsuarioProxyRepository usuarioRepository =
                        new UsuarioProxyRepository(backendProxy);

                AuthService authService =
                        new AuthService(usuarioRepository);

                LoginView loginView = new LoginView();

                LoginController loginController =
                        new LoginController(
                                loginView,
                                authService,
                                usuarioRepository,
                                backendProxy
                        );

                loginView.setVisible(true);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pudo conectar con el Backend: " + e.getMessage(),
                        "Error de conexión",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}