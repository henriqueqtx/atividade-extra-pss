package com.ufes.delivery;

import com.ufes.delivery.database.DatabaseManager;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.repository.UsuarioRepositorySQLite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioRepositoryTest {
    private UsuarioRepositorySQLite repository;

    @BeforeEach
    public void setUp() {
        DatabaseManager.setUrl("jdbc:sqlite:delivery_test.db");
        DatabaseManager.inicializarBanco();
        repository = new UsuarioRepositorySQLite();
    }

    @AfterEach
    public void tearDown() {
        java.io.File file = new java.io.File("delivery_test.db");
        if (file.exists()) {
            System.gc();
            file.delete();
        }
    }

    @Test
    public void testExisteUsuarioVazio() {
        assertFalse(repository.existeUsuario());
    }

    @Test
    public void testSalvarEBuscarUsuario() {
        Usuario u = Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345");
        repository.salvar(u);

        assertTrue(repository.existeUsuario());

        Optional<Usuario> opt = repository.buscarPorUsuario("carlos");
        assertTrue(opt.isPresent());
        Usuario recuperado = opt.get();
        assertEquals("Carlos Souza", recuperado.getNome());
        assertEquals("carlos", recuperado.getUsuario());
        assertEquals("ADMINISTRADOR", recuperado.getPerfil());
        assertEquals("AUTORIZADO", recuperado.getSituacao());
        assertNotEquals("senha12345", recuperado.getSenha()); // should be hashed!
    }

    @Test
    public void testSalvarUsuarioDuplicadoLançaException() {
        Usuario u1 = Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345");
        repository.salvar(u1);

        Usuario u2 = Usuario.criarNovoUsuario("Carlos Oliveira", "carlos", "outrasenha");
        assertThrows(RuntimeException.class, () -> repository.salvar(u2));
    }

    @Test
    public void testBuscarPorNomeOuUsuario() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345"));
        repository.salvar(Usuario.criarNovoUsuario("Maria Silva", "maria", "senha12345"));

        // Match civil name
        java.util.List<Usuario> res1 = repository.buscarPorNomeOuUsuario("carlos");
        assertEquals(1, res1.size());
        assertEquals("carlos", res1.get(0).getUsuario());

        // Match username
        java.util.List<Usuario> res2 = repository.buscarPorNomeOuUsuario("maria");
        assertEquals(1, res2.size());
        assertEquals("maria", res2.get(0).getUsuario());

        // Match all (empty query)
        java.util.List<Usuario> res3 = repository.buscarPorNomeOuUsuario("");
        assertEquals(2, res3.size());

        // Match case-insensitive
        java.util.List<Usuario> res4 = repository.buscarPorNomeOuUsuario("MARIA");
        assertEquals(1, res4.size());
    }

    @Test
    public void testAtualizarPerfilESituacao() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345"));

        repository.atualizarPerfil("carlos", "ATENDENTE");
        repository.atualizarSituacao("carlos", "NAO_AUTORIZADO");

        Usuario u = repository.buscarPorUsuario("carlos").get();
        assertEquals("ATENDENTE", u.getPerfil());
        assertEquals("NAO_AUTORIZADO", u.getSituacao());
    }

    @Test
    public void testExcluir() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345"));
        assertTrue(repository.buscarPorUsuario("carlos").isPresent());

        repository.excluir("carlos");
        assertFalse(repository.buscarPorUsuario("carlos").isPresent());
    }
}
