package backend.database;

import model.Usuario;

public class PruebaUsuarioBD {

    public static void main(String[] args) {

        UsuarioBDRepository repositorio =
                new UsuarioBDRepository();

        System.out.println("Usuarios registrados:");

        for (Usuario usuario : repositorio.listarTodos()) {
            System.out.println("ID: " + usuario.getId() + " | Rol: " + usuario.getRol());
        }

        System.out.println("\nBuscando administrador...");

        Usuario admin = repositorio.buscarPorId("admin");

        if (admin != null) {
            System.out.println("Usuario encontrado.");
            System.out.println("ID: " + admin.getId());
            System.out.println("Rol: " + admin.getRol());
        } else {
            System.out.println("No se encontró el administrador.");
        }
    }
}
