package controller;

import frontend.proxy.*;
import repository.*;
import view.MainView;
import model.Usuario;
import service.CategoriaService;
import service.FuncionarioService;
import service.AuthService;
import view.CambiarClaveView;
import view.LoginView;
import frontend.proxy.BackendProxy;

import frontend.proxy.FuncionarioProxyRepository;
import frontend.proxy.CategoriaProxyRepository;
import frontend.proxy.RecursoProxyRepository;
import frontend.proxy.ReservaProxyRepository;

import repository.FuncionarioRepository;
import repository.CategoriaRepository;
import repository.RecursoRepository;
import repository.ReservaRepository;

public class LoginController {

    private final LoginView vista;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final BackendProxy backendProxy;


    public LoginController(LoginView vista,
                           AuthService authService,
                           UsuarioRepository usuarioRepository,
                           BackendProxy backendProxy) {

        this.vista = vista;
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        this.backendProxy = backendProxy;

        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBotonIngresar().addActionListener(e -> intentarIngresar());
        vista.getBotonCancelar().addActionListener(e -> System.exit(0));
        vista.getBotonCambiar().addActionListener(e -> abrirCambiarClave());
    }

    private void intentarIngresar() {
        try {
            String id = vista.getIdIngresado();
            String clave = vista.getClaveIngresada();

            Usuario usuario = authService.iniciarSesion(id, clave);

            vista.dispose();
            abrirVentanaPrincipal(usuario);

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    private void abrirCambiarClave() {
        String id = vista.getIdIngresado();

        try {
            authService.verificarExiste(id);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return;
        }

        CambiarClaveView vistaClave = new CambiarClaveView(id);
        new CambiarClaveController(vistaClave, authService, id);
        vistaClave.setVisible(true);

        vista.limpiarCampos();
    }

    private void abrirVentanaPrincipal(Usuario usuario) {

        // Repositorios que se comunican con el Backend
        FuncionarioRepository funcionarioRepository =
                new FuncionarioProxyRepository(backendProxy);

        CategoriaRepository categoriaRepository =
                new CategoriaProxyRepository(backendProxy);

        RecursoRepository recursoRepository =
                new RecursoProxyRepository(backendProxy);

        ReservaRepository reservaRepository =
                new ReservaProxyRepository(backendProxy);

        // Servicios
        FuncionarioService funcionarioService =
                new FuncionarioService(
                        funcionarioRepository,
                        usuarioRepository,
                        reservaRepository
                );

        CategoriaService categoriaService =
                new CategoriaService(
                        categoriaRepository,
                        recursoRepository,
                        reservaRepository
                );

        // Ventana principal
        MainView mainView = new MainView(
                usuario,
                funcionarioService,
                categoriaService,
                recursoRepository,
                reservaRepository
        );

        mainView.configurarNotificaciones(backendProxy);

        new MainController(
                mainView,
                authService,
                usuarioRepository,
                backendProxy
        );

        mainView.setVisible(true);
    }
}