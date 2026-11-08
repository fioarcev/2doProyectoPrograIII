package backend.database;

import model.CategoriaRecurso;
import repository.CategoriaRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaBDRepository implements CategoriaRepository {

    @Override
    public List<CategoriaRecurso> listarTodas() {
        return listarTodos();
    }

    @Override
    public List<CategoriaRecurso> listarTodos() {

        List<CategoriaRecurso> lista = new ArrayList<>();

        String sql = "SELECT id, descripcion FROM categorias_recurso";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(new CategoriaRecurso(
                        rs.getString("id"),
                        rs.getString("descripcion")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías: " + e.getMessage(), e);
        }

        return lista;
    }

    @Override
    public CategoriaRecurso buscarPorId(String id) {

        String sql = "SELECT id, descripcion FROM categorias_recurso WHERE id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CategoriaRecurso(
                            rs.getString("id"),
                            rs.getString("descripcion")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {

        String sql = """
                INSERT INTO categorias_recurso (id, descripcion)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                descripcion = VALUES(descripcion)
                """;

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, categoria.getId());
            ps.setString(2, categoria.getDescripcion());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String id) {

        String sql = "DELETE FROM categorias_recurso WHERE id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoría: " + e.getMessage(), e);
        }
    }
}
