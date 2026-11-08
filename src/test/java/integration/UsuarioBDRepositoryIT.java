package integration;

import backend.database.UsuarioBDRepository;
import model.Rol;
import model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioBDRepositoryIT {

    private final UsuarioBDRepository repo = new UsuarioBDRepository();

    @BeforeEach
    void limpiar() throws Exception {
        BaseDatosTest.limpiar();
    }

    @Test
    void guardarYBuscarUsuario() {
        repo.guardar(new Usuario("U1", "clave1", Rol.FUNCIONARIO));

        Usuario u = repo.buscarPorId("U1");

        assertNotNull(u);
        assertEquals("clave1", u.getClave());
        assertEquals(Rol.FUNCIONARIO, u.getRol());
    }

    @Test
    void guardarConMismoIdActualizaLaClave() {
        repo.guardar(new Usuario("U1", "clave1", Rol.FUNCIONARIO));
        repo.guardar(new Usuario("U1", "claveNueva", Rol.FUNCIONARIO));

        assertEquals("claveNueva", repo.buscarPorId("U1").getClave());
        assertEquals(1, repo.listarTodos().size());
    }

    @Test
    void listarTodosDevuelveTodosLosUsuarios() {
        repo.guardar(new Usuario("U1", "a", Rol.FUNCIONARIO));
        repo.guardar(new Usuario("U2", "b", Rol.ADMINISTRADOR));

        assertEquals(2, repo.listarTodos().size());
    }

    @Test
    void eliminarUsuario() {
        repo.guardar(new Usuario("U1", "clave1", Rol.FUNCIONARIO));

        repo.eliminar("U1");

        assertNull(repo.buscarPorId("U1"));
    }
}