package integration;

import backend.database.CategoriaBDRepository;
import model.CategoriaRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaBDRepositoryIT {

    private final CategoriaBDRepository repo = new CategoriaBDRepository();

    @BeforeEach
    void limpiar() throws Exception {
        BaseDatosTest.limpiar();
    }

    @Test
    void guardarYBuscarCategoria() {
        repo.guardar(new CategoriaRecurso("C1", "Salas"));

        CategoriaRecurso c = repo.buscarPorId("C1");

        assertNotNull(c);
        assertEquals("Salas", c.getDescripcion());
    }

    @Test
    void guardarConMismoIdActualizaLaDescripcion() {
        repo.guardar(new CategoriaRecurso("C1", "Salas"));
        repo.guardar(new CategoriaRecurso("C1", "Salas de reuniones"));

        assertEquals("Salas de reuniones", repo.buscarPorId("C1").getDescripcion());
        assertEquals(1, repo.listarTodos().size());
    }

    @Test
    void listarTodosDevuelveTodasLasCategorias() {
        repo.guardar(new CategoriaRecurso("C1", "Salas"));
        repo.guardar(new CategoriaRecurso("C2", "Equipos"));

        assertEquals(2, repo.listarTodos().size());
    }

    @Test
    void eliminarCategoria() {
        repo.guardar(new CategoriaRecurso("C1", "Salas"));

        repo.eliminar("C1");

        assertNull(repo.buscarPorId("C1"));
    }

    @Test
    void buscarCategoriaInexistenteDevuelveNull() {
        assertNull(repo.buscarPorId("NO_EXISTE"));
    }
}