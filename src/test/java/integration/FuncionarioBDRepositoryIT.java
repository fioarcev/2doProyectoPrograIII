package integration;

import backend.database.FuncionarioBDRepository;
import model.Funcionario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioBDRepositoryIT {

    private final FuncionarioBDRepository repo = new FuncionarioBDRepository();

    @BeforeEach
    void limpiar() throws Exception {
        BaseDatosTest.limpiar();
    }

    @Test
    void guardarYBuscarFuncionario() {
        repo.guardar(new Funcionario("F1", "Ana Mora", "8888-0000"));

        Funcionario f = repo.buscarPorId("F1");
        assertNotNull(f);
        assertEquals("Ana Mora", f.getNombre());
    }

    @Test
    void eliminarFuncionario() {
        repo.guardar(new Funcionario("F2", "Luis Rojas", "7777-0000"));
        repo.eliminar("F2");
        assertNull(repo.buscarPorId("F2"));
    }
}