package backend.database;

import model.CategoriaRecurso;
import model.Recurso;
import repository.RecursoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecursoBDRepository implements RecursoRepository {

    @Override
    public void guardar(Recurso recurso) {
        String sql = "INSERT INTO recursos (id, categoria_id, descripcion) VALUES (?, ?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recurso.getId());
            ps.setString(2, recurso.getCategoria().getId());
            ps.setString(3, recurso.getDescripcion());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar el recurso: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recurso> listar() {
        String sql = """
                SELECT r.id, r.descripcion, c.id AS categoria_id, c.descripcion AS categoria_descripcion
                FROM recursos r
                INNER JOIN categorias_recurso c ON r.categoria_id = c.id
                """;

        List<Recurso> recursos = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CategoriaRecurso categoria = new CategoriaRecurso(
                        rs.getString("categoria_id"),
                        rs.getString("categoria_descripcion"));

                recursos.add(new Recurso(
                        rs.getString("id"),
                        categoria,
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los recursos: " + e.getMessage(), e);
        }

        return recursos;
    }

    @Override
    public List<Recurso> listarTodos() {
        return listar();
    }

    @Override
    public Recurso buscarPorId(String id) {
        String sql = """
                SELECT r.id, r.descripcion, c.id AS categoria_id, c.descripcion AS categoria_descripcion
                FROM recursos r
                INNER JOIN categorias_recurso c ON r.categoria_id = c.id
                WHERE r.id = ?
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CategoriaRecurso categoria = new CategoriaRecurso(
                            rs.getString("categoria_id"),
                            rs.getString("categoria_descripcion")
                    );

                    return new Recurso(rs.getString("id"), categoria, rs.getString("descripcion")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el recurso: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void actualizar(Recurso recurso) {
        String sql = """
                UPDATE recursos
                SET categoria_id = ?, descripcion = ?
                WHERE id = ?
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recurso.getCategoria().getId());
            ps.setString(2, recurso.getDescripcion());
            ps.setString(3, recurso.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el recurso: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Recurso recurso) {
        String sql = "DELETE FROM recursos WHERE id = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, recurso.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el recurso: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recurso> buscarPorCategoria(String categoriaId) {
        String sql = """
                SELECT r.id, r.descripcion, c.id AS categoria_id, c.descripcion AS categoria_descripcion
                FROM recursos r
                INNER JOIN categorias_recurso c ON r.categoria_id = c.id
                WHERE c.id = ?
                """;

        List<Recurso> recursos = new ArrayList<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, categoriaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CategoriaRecurso categoria = new CategoriaRecurso(
                            rs.getString("categoria_id"),
                            rs.getString("categoria_descripcion")
                    );

                    recursos.add(new Recurso(
                            rs.getString("id"),
                            categoria,
                            rs.getString("descripcion")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar recursos por categoría: " + e.getMessage(), e);
        }

        return recursos;
    }
}