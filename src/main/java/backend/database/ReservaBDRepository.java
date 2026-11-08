package backend.database;

import model.CategoriaRecurso;
import model.DetalleReserva;
import model.EstadoReserva;
import model.Recurso;
import model.Reserva;
import repository.ReservaRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaBDRepository implements ReservaRepository {

    @Override
    public void guardar(Reserva reserva) {
        String sql = """
                INSERT INTO reservas
                (id, actividad, fecha, hora_inicio, hora_fin, funcionario_id, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, reserva.getId());
                ps.setString(2, reserva.getActividad());
                ps.setDate(3, Date.valueOf(reserva.getFecha()));
                ps.setTime(4, Time.valueOf(reserva.getHoraInicio()));
                ps.setTime(5, Time.valueOf(reserva.getHoraFin()));
                ps.setString(6, reserva.getFuncionarioId());
                ps.setString(7, reserva.getEstado().name());
                ps.executeUpdate();

                guardarDetalles(cn, reserva);
                cn.commit();

            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar la reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reserva> listar() {
        String sql = """
                SELECT id, actividad, fecha, hora_inicio, hora_fin,
                       funcionario_id, estado
                FROM reservas
                ORDER BY fecha, hora_inicio
                """;

        List<Reserva> reservas = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reserva reserva = crearReserva(rs);
                cargarDetalles(cn, reserva);
                reservas.add(reserva);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las reservas: " + e.getMessage(), e);
        }

        return reservas;
    }

    @Override
    public Reserva buscarPorId(String id) {
        String sql = """
                SELECT id, actividad, fecha, hora_inicio, hora_fin,
                       funcionario_id, estado
                FROM reservas
                WHERE id = ?
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reserva reserva = crearReserva(rs);
                    cargarDetalles(cn, reserva);
                    return reserva;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la reserva: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void actualizar(Reserva reserva) {
        String sql = """
                UPDATE reservas
                SET actividad = ?, fecha = ?, hora_inicio = ?, hora_fin = ?,
                    funcionario_id = ?, estado = ?
                WHERE id = ?
                """;

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, reserva.getActividad());
                ps.setDate(2, Date.valueOf(reserva.getFecha()));
                ps.setTime(3, Time.valueOf(reserva.getHoraInicio()));
                ps.setTime(4, Time.valueOf(reserva.getHoraFin()));
                ps.setString(5, reserva.getFuncionarioId());
                ps.setString(6, reserva.getEstado().name());
                ps.setString(7, reserva.getId());

                int filas = ps.executeUpdate();

                if (filas == 0) {
                    throw new SQLException("No existe una reserva con ese ID.");
                }

                try (PreparedStatement eliminar = cn.prepareStatement(
                        "DELETE FROM detalle_reserva WHERE reserva_id = ?")) {
                    eliminar.setString(1, reserva.getId());
                    eliminar.executeUpdate();
                }

                guardarDetalles(cn, reserva);
                cn.commit();

            } catch (Exception e) {
                cn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar la reserva: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Reserva reserva) {

        String sqlDetalles =
                "DELETE FROM detalle_reserva WHERE reserva_id = ?";

        String sqlReserva =
                "DELETE FROM reservas WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion()) {

            conexion.setAutoCommit(false);

            try {
                // Primero eliminamos los detalles asociados.
                try (PreparedStatement psDetalles =
                             conexion.prepareStatement(sqlDetalles)) {

                    psDetalles.setString(1, reserva.getId());
                    psDetalles.executeUpdate();
                }

                // Después eliminamos la reserva.
                try (PreparedStatement psReserva =
                             conexion.prepareStatement(sqlReserva)) {

                    psReserva.setString(1, reserva.getId());

                    int filasEliminadas = psReserva.executeUpdate();

                    if (filasEliminadas == 0) {
                        throw new SQLException(
                                "No se encontró la reserva que desea eliminar."
                        );
                    }
                }

                // Confirmamos ambas operaciones.
                conexion.commit();

            } catch (SQLException e) {

                conexion.rollback();
                throw e;

            } finally {
                conexion.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al eliminar la reserva: " + e.getMessage(), e
            );
        }
    }

    private void guardarDetalles(Connection cn, Reserva reserva) throws SQLException {
        String sql = """
                INSERT INTO detalle_reserva (reserva_id, recurso_id, categoria_id)
                VALUES (?, ?, ?)
                """;

        if (reserva.getDetalles() == null) {
            return;
        }

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (DetalleReserva detalle : reserva.getDetalles()) {
                ps.setString(1, reserva.getId());
                ps.setString(2, detalle.getRecurso().getId());
                ps.setString(3, detalle.getCategoria().getId());
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private Reserva crearReserva(ResultSet rs) throws SQLException {
        return new Reserva(
                rs.getString("id"),
                rs.getString("actividad"),
                rs.getDate("fecha").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fin").toLocalTime(),
                rs.getString("funcionario_id"),
                EstadoReserva.valueOf(rs.getString("estado"))
        );
    }

    private void cargarDetalles(Connection cn, Reserva reserva) throws SQLException {
        String sql = """
                SELECT d.categoria_id, d.recurso_id,
                       c.descripcion AS categoria_descripcion,
                       r.descripcion AS recurso_descripcion
                FROM detalle_reserva d
                INNER JOIN categorias_recurso c ON d.categoria_id = c.id
                INNER JOIN recursos r ON d.recurso_id = r.id
                WHERE d.reserva_id = ?
                """;

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, reserva.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoriaRecurso categoria = new CategoriaRecurso(
                            rs.getString("categoria_id"),
                            rs.getString("categoria_descripcion")
                    );

                    Recurso recurso = new Recurso(
                            rs.getString("recurso_id"),
                            categoria,
                            rs.getString("recurso_descripcion")
                    );

                    reserva.getDetalles().add(
                            new DetalleReserva(categoria, recurso)
                    );
                }
            }
        }
    }
}