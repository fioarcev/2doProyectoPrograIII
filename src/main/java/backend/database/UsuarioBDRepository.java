
package backend.database;

import model.Rol;
import model.Usuario;
import repository.UsuarioRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioBDRepository implements UsuarioRepository {

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, clave, rol FROM usuarios";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(convertirUsuario(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }

        return usuarios;
    }

    @Override
    public Usuario buscarPorId(String id) {
        String sql = "SELECT id, clave, rol FROM usuarios WHERE id = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return convertirUsuario(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        String sql = """
                INSERT INTO usuarios (id, clave, rol)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    clave = VALUES(clave),
                    rol = VALUES(rol)
                """;

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario.getId());
            ps.setString(2, usuario.getClave());
            ps.setString(3, usuario.getRol().name());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    private Usuario convertirUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getString("id"),
                rs.getString("clave"),
                Rol.valueOf(rs.getString("rol"))
        );
    }
}
