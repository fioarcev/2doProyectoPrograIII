package unit;

import org.junit.jupiter.api.Test;
import service.ReservaService;
import unit.estadisticas.ReservaRepositoryEnMemoria;
import service.DisponibilidadService;
import model.Reserva;
import model.EstadoReserva;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaServiceTest {
    @Test
    void buscarReservaPorIdNuloDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService recursoService = new ReservaService();
            recursoService.buscarPorId(null);
        });
    }

    @Test
    void registrarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null, null);
            reservaService.registrar(null);
        });
    }

    @Test
    void actualizarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null, null);
            reservaService.actualizar(null);
        });
    }

    @Test
    void eliminarReservaNulaDebeLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReservaService reservaService = new ReservaService(null, null);
            reservaService.eliminar(null);
        });
    }

    @Test
    void horaInicioIgualOMayorQueFinDebeLanzarExcepcion() {
        ReservaRepositoryEnMemoria repo = new ReservaRepositoryEnMemoria();
        ReservaService servicio = new ReservaService(new DisponibilidadService(null, repo), repo);

        Reserva r = new Reserva("R1", "Reunión", LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(9, 0), "F1", EstadoReserva.ACTIVA);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(r));
    }

    @Test
    void idDeReservaDuplicadoDebeLanzarExcepcion() {
        ReservaRepositoryEnMemoria repo = new ReservaRepositoryEnMemoria();
        ReservaService servicio = new ReservaService(new DisponibilidadService(null, repo), repo);

        Reserva r1 = new Reserva("R1", "Reunión", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), "F1", EstadoReserva.ACTIVA);
        Reserva r2 = new Reserva("R1", "Otra", LocalDate.now().plusDays(1),
                LocalTime.of(11, 0), LocalTime.of(12, 0), "F1", EstadoReserva.ACTIVA);

        servicio.registrar(r1);
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(r2));
    }
}
