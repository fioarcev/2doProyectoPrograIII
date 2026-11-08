package integration;

import backend.database.CategoriaBDRepository;
import backend.database.RecursoBDRepository;
import model.CategoriaRecurso;
import model.Recurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecursoBDRepositoryIT {

    private final CategoriaBDRepository categorias = new CategoriaBDRepository();
    private final RecursoBDRepository repo = new RecursoBDRepository();

    private CategoriaRecurso salas;

    @BeforeEach
    void preparar() throws Exception {
        BaseDatosTest.limpiar();

        salas = new CategoriaRecurso("C1", "Salas");
        categorias.guardar(salas);
    }

    @Test
    void guardarYBuscarRecurso() {
        repo.guardar(new Recurso("R1", salas, "Sala A"));

        Recurso r = repo.buscarPorId("R1");

        assertNotNull(r);
        assertEquals("Sala A", r.getDescripcion());
        assertEquals("C1", r.getCategoria().getId());
    }

    @Test
    void actualizarRecurso() {
        repo.guardar(new Recurso("R1", salas, "Sala A"));

        repo.actualizar(new Recurso("R1", salas, "Sala A remodelada"));

        assertEquals("Sala A remodelada", repo.buscarPorId("R1").getDescripcion());
    }

    @Test
    void eliminarRecurso() {
        Recurso recurso = new Recurso("R1", salas, "Sala A");
        repo.guardar(recurso);

        repo.eliminar(recurso);

        assertNull(repo.buscarPorId("R1"));
    }

    @Test
    void buscarPorCategoriaSoloDevuelveLosDeEsaCategoria() {
        CategoriaRecurso equipos = new CategoriaRecurso("C2", "Equipos");
        categorias.guardar(equipos);

        repo.guardar(new Recurso("R1", salas, "Sala A"));
        repo.guardar(new Recurso("R2", salas, "Sala B"));
        repo.guardar(new Recurso("R3", equipos, "Proyector"));

        assertEquals(2, repo.buscarPorCategoria("C1").size());
        assertEquals(1, repo.buscarPorCategoria("C2").size());
    }

    @Test
    void guardarRecursoConIdDuplicadoLanzaExcepcion() {
        repo.guardar(new Recurso("R1", salas, "Sala A"));

        assertThrows(RuntimeException.class,
                () -> repo.guardar(new Recurso("R1", salas, "Otra sala")));
    }

    @Test
    void guardarRecursoConCategoriaInexistenteLanzaExcepcion() {
        CategoriaRecurso falsa = new CategoriaRecurso("NO_EXISTE", "Falsa");

        assertThrows(RuntimeException.class,
                () -> repo.guardar(new Recurso("R1", falsa, "Sala A")));
    }
}