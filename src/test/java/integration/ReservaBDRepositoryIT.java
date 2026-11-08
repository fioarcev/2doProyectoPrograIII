package integration;

import backend.database.CategoriaBDRepository;
import backend.database.FuncionarioBDRepository;
import backend.database.RecursoBDRepository;
import backend.database.ReservaBDRepository;
import model.CategoriaRecurso;
import model.DetalleReserva;
import model.EstadoReserva;
import model.Funcionario;
import model.Recurso;
import model.Reserva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaBDRepositoryIT {

    private static final LocalDate FECHA = LocalDate.of(2030, 1, 15);

    private final ReservaBDRepository repo = new ReservaBDRepository();

    private CategoriaRecurso categoria;
    private Recurso recurso;

    @BeforeEach
    void preparar() throws Exception {
        BaseDatosTest.limpiar();

        new FuncionarioBDRepository().guardar(new Funcionario("F1", "Ana Mora", "8888-0000"));

        categoria = new CategoriaRecurso("C1", "Salas");
        new CategoriaBDRepository().guardar(categoria);

        recurso = new Recurso("R1", categoria, "Sala A");
        new RecursoBDRepository().guardar(recurso);
    }

    private Reserva crearReserva(String id, String actividad, String funcionarioId) {
        Reserva reserva = new Reserva(
                id, actividad, FECHA,
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                funcionarioId, EstadoReserva.ACTIVA);
        reserva.getDetalles().add(new DetalleReserva(categoria, recurso));
        return reserva;
    }

    @Test
    void guardarYBuscarReservaConSusDetalles() {
        repo.guardar(crearReserva("RES1", "Reunión", "F1"));

        Reserva r = repo.buscarPorId("RES1");

        assertNotNull(r);
        assertEquals("Reunión", r.getActividad());
        assertEquals(FECHA, r.getFecha());
        assertEquals(LocalTime.of(9, 0), r.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), r.getHoraFin());
        assertEquals("F1", r.getFuncionarioId());
        assertEquals(EstadoReserva.ACTIVA, r.getEstado());
        assertEquals(1, r.getDetalles().size());
        assertEquals("R1", r.getDetalles().get(0).getRecurso().getId());
    }

    @Test
    void listarDevuelveLasReservasGuardadas() {
        repo.guardar(crearReserva("RES1", "Reunión", "F1"));

        Reserva segunda = new Reserva(
                "RES2", "Taller", FECHA,
                LocalTime.of(11, 0), LocalTime.of(12, 0),
                "F1", EstadoReserva.ACTIVA);
        segunda.getDetalles().add(new DetalleReserva(categoria, recurso));
        repo.guardar(segunda);

        assertEquals(2, repo.listar().size());
    }

    @Test
    void actualizarReserva() {
        repo.guardar(crearReserva("RES1", "Reunión", "F1"));

        Reserva modificada = crearReserva("RES1", "Reunión cambiada", "F1");
        modificada.setEstado(EstadoReserva.CANCELADA);
        repo.actualizar(modificada);

        Reserva r = repo.buscarPorId("RES1");
        assertEquals("Reunión cambiada", r.getActividad());
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(1, r.getDetalles().size());
    }

    @Test
    void eliminarReservaTambienEliminaSusDetalles() {
        Reserva reserva = crearReserva("RES1", "Reunión", "F1");
        repo.guardar(reserva);

        repo.eliminar(reserva);

        assertNull(repo.buscarPorId("RES1"));
        assertTrue(repo.listar().isEmpty());
    }

    @Test
    void guardarReservaConFuncionarioInexistenteLanzaExcepcion() {
        Reserva reserva = crearReserva("RES1", "Reunión", "NO_EXISTE");

        assertThrows(RuntimeException.class, () -> repo.guardar(reserva));
        assertNull(repo.buscarPorId("RES1"));
    }

    @Test
    void actualizarReservaInexistenteLanzaExcepcion() {
        Reserva reserva = crearReserva("NO_EXISTE", "Reunión", "F1");

        assertThrows(RuntimeException.class, () -> repo.actualizar(reserva));
    }

    @Test
    void eliminarReservaInexistenteLanzaExcepcion() {
        Reserva reserva = crearReserva("NO_EXISTE", "Reunión", "F1");

        assertThrows(RuntimeException.class, () -> repo.eliminar(reserva));
    }
}