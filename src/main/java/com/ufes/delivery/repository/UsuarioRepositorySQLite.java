package com.ufes.delivery.repository;

import com.ufes.delivery.database.DatabaseManager;
import com.ufes.delivery.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UsuarioRepositorySQLite implements IUsuarioRepository {

    @Override
    public void salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, usuario, senha, perfil, situacao) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getUsuario());
            pstmt.setString(3, usuario.getSenha());
            pstmt.setString(4, usuario.getPerfil());
            pstmt.setString(5, usuario.getSituacao());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar usuario no banco de dados", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorUsuario(String usuario) {
        String sql = "SELECT nome, usuario, senha, perfil, situacao FROM usuarios WHERE usuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getString("nome"),
                            rs.getString("usuario"),
                            rs.getString("senha"),
                            rs.getString("perfil"),
                            rs.getString("situacao")
                    );
                    return Optional.of(u);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuario no banco de dados", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existeUsuario() {
        String sql = "SELECT 1 FROM usuarios LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar existencia de usuarios", e);
        }
    }

    @Override
    public java.util.List<Usuario> buscarPorNomeOuUsuario(String busca) {
        java.util.List<Usuario> lista = new java.util.ArrayList<>();
        String sql = "SELECT nome, usuario, senha, perfil, situacao FROM usuarios";
        boolean hasFilter = busca != null && !busca.isBlank();
        if (hasFilter) {
            sql += " WHERE LOWER(nome) LIKE ? OR LOWER(usuario) LIKE ?";
        }
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (hasFilter) {
                String filter = "%" + busca.trim().toLowerCase() + "%";
                pstmt.setString(1, filter);
                pstmt.setString(2, filter);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Usuario(
                            rs.getString("nome"),
                            rs.getString("usuario"),
                            rs.getString("senha"),
                            rs.getString("perfil"),
                            rs.getString("situacao")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuarios no banco de dados", e);
        }
        return lista;
    }

    @Override
    public void atualizarPerfil(String usuario, String perfil) {
        String sql = "UPDATE usuarios SET perfil = ? WHERE usuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, perfil);
            pstmt.setString(2, usuario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar perfil do usuario", e);
        }
    }

    @Override
    public void atualizarSituacao(String usuario, String situacao) {
        String sql = "UPDATE usuarios SET situacao = ? WHERE usuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, situacao);
            pstmt.setString(2, usuario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar situacao do usuario", e);
        }
    }

    @Override
    public void excluir(String usuario) {
        String sql = "DELETE FROM usuarios WHERE usuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir usuario", e);
        }
    }
}
