package backend.database;

import model.Funcionario;
import repository.FuncionarioRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioBDRepository implements FuncionarioRepository {

    @Override
    public List<Funcionario> listarTodos() {

        List<Funcionario> lista = new ArrayList<>();

        String sql = "SELECT id, nombre, telefono FROM funcionarios";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(new Funcionario(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar funcionarios: " + e.getMessage(), e);
        }

        return lista;
    }

    @Override
    public Funcionario buscarPorId(String id) {

        String sql = "SELECT id, nombre, telefono FROM funcionarios WHERE id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Funcionario(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("telefono")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar funcionario: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void guardar(Funcionario funcionario) {

        String sql = """
                INSERT INTO funcionarios (id, nombre, telefono)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                nombre = VALUES(nombre),
                telefono = VALUES(telefono)
                """;

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, funcionario.getId());
            ps.setString(2, funcionario.getNombre());
            ps.setString(3, funcionario.getTelefono());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar funcionario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String id) {

        String sql = "DELETE FROM funcionarios WHERE id = ?";

        try (
                Connection conexion = ConexionBD.obtenerConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar funcionario: " + e.getMessage(), e);
        }
    }
}
