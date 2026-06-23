package com.ufes.delivery;

import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.database.DatabaseManager;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.presenter.CadastroUsuarioPresenter;
import com.ufes.delivery.presenter.LoginPresenter;
import com.ufes.delivery.repository.UsuarioRepositorySQLite;
import com.ufes.delivery.presenter.GestaoUsuariosPresenter;
import com.ufes.delivery.view.ICadastroUsuarioView;
import com.ufes.delivery.view.ILoginView;
import com.ufes.delivery.view.IGestaoUsuariosView;
import javax.swing.JTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PresentersTest {

    private UsuarioRepositorySQLite repository;
    private MockLogger logger;

    @BeforeEach
    public void setUp() {
        DatabaseManager.setUrl("jdbc:sqlite:presenter_test.db");
        DatabaseManager.inicializarBanco();
        repository = new UsuarioRepositorySQLite();
        logger = new MockLogger();
    }

    @AfterEach
    public void tearDown() {
        java.io.File file = new java.io.File("presenter_test.db");
        if (file.exists()) {
            System.gc();
            file.delete();
        }
    }

    // Mock Views
    private static class MockLoginView implements ILoginView {
        String usuarioText = "";
        String senhaText = "";
        String ultimaMensagem = "";
        boolean fecharChamado = false;
        boolean visibleState = true;
        ActionListener acessarListener;
        ActionListener cancelarListener;
        ActionListener cadastrarListener;

        @Override public String getUsuario() { return usuarioText; }
        @Override public String getSenha() { return senhaText; }
        @Override public void addAcessarListener(ActionListener l) { acessarListener = l; }
        @Override public void addCancelarListener(ActionListener l) { cancelarListener = l; }
        @Override public void addCadastrarUsuarioListener(ActionListener l) { cadastrarListener = l; }
        @Override public void mostrarMensagem(String m) { ultimaMensagem = m; }
        @Override public void fechar() { fecharChamado = true; }
        @Override public void limparCampos() { usuarioText = ""; senhaText = ""; }
        @Override public void setVisible(boolean v) { visibleState = v; }
    }

    private static class MockCadastroView implements ICadastroUsuarioView {
        String nomeText = "";
        String usuarioText = "";
        String senhaText = "";
        String ultimaMensagem = "";
        boolean fecharChamado = false;
        boolean visibleState = true;
        ActionListener confirmarListener;
        ActionListener cancelarListener;

        @Override public String getNomeCivil() { return nomeText; }
        @Override public String getUsuario() { return usuarioText; }
        @Override public String getSenha() { return senhaText; }
        @Override public void addConfirmarListener(ActionListener l) { confirmarListener = l; }
        @Override public void addCancelarListener(ActionListener l) { cancelarListener = l; }
        @Override public void mostrarMensagem(String m) { ultimaMensagem = m; }
        @Override public void fechar() { fecharChamado = true; }
        @Override public void limparCampos() { nomeText = ""; usuarioText = ""; senhaText = ""; }
        @Override public void setVisible(boolean v) { visibleState = v; }
    }

    private static class MockGestaoUsuariosView implements IGestaoUsuariosView {
        String buscaText = "";
        String ultimaMensagem = "";
        boolean fecharChamado = false;
        boolean visibleState = true;
        JTable table = new JTable();
        ActionListener buscarListener;
        ActionListener autorizarListener;
        ActionListener desautorizarListener;
        ActionListener excluirListener;
        ActionListener novoListener;
        ActionListener fecharListener;

        @Override public String getBuscaNome() { return buscaText; }
        @Override public void addBuscarListener(ActionListener l) { buscarListener = l; }
        @Override public void addAutorizarListener(ActionListener l) { autorizarListener = l; }
        @Override public void addDesautorizarListener(ActionListener l) { desautorizarListener = l; }
        @Override public void addExcluirListener(ActionListener l) { excluirListener = l; }
        @Override public void addNovoListener(ActionListener l) { novoListener = l; }
        @Override public void addFecharListener(ActionListener l) { fecharListener = l; }
        @Override public JTable getTabelaUsuarios() { return table; }
        @Override public void mostrarMensagem(String m) { ultimaMensagem = m; }
        @Override public void fechar() { fecharChamado = true; }
        @Override public void setVisible(boolean v) { visibleState = v; }
    }

    private static class MockLogger implements ILogger {
        List<LogEntry> logs = new ArrayList<>();
        @Override
        public void registrar(LogEntry entry) throws IOException {
            logs.add(entry);
        }
    }

    @Test
    public void testCadastroPrimeiroUsuarioSucesso() {
        MockLoginView loginView = new MockLoginView();
        MockCadastroView view = new MockCadastroView();
        CadastroUsuarioPresenter presenter = new CadastroUsuarioPresenter(view, repository, logger, loginView);

        view.nomeText = "Administrador Master";
        view.usuarioText = "adminmaster";
        view.senhaText = "senhaforte123";

        // trigger confirm
        view.confirmarListener.actionPerformed(null);

        assertTrue(repository.existeUsuario());
        Usuario u = repository.buscarPorUsuario("adminmaster").get();
        assertEquals("ADMINISTRADOR", u.getPerfil());
        assertEquals("AUTORIZADO", u.getSituacao());
        assertTrue(view.fecharChamado);
        assertTrue(loginView.visibleState);
        assertTrue(view.ultimaMensagem.contains("sucesso"));
        assertEquals(1, logger.logs.size());
        assertTrue(logger.logs.get(0).getOperacao().contains("Cadastro de usuário"));
    }

    @Test
    public void testCadastroSegundoUsuarioPendente() {
        // Create first user
        repository.salvar(Usuario.criarPrimeiroUsuario("Admin", "admin", "senha12345"));

        MockLoginView loginView = new MockLoginView();
        MockCadastroView view = new MockCadastroView();
        CadastroUsuarioPresenter presenter = new CadastroUsuarioPresenter(view, repository, logger, loginView);

        view.nomeText = "Atendente Joao";
        view.usuarioText = "joao123";
        view.senhaText = "joaosenha12";

        view.confirmarListener.actionPerformed(null);

        Usuario u = repository.buscarPorUsuario("joao123").get();
        assertEquals("ATENDENTE", u.getPerfil());
        assertEquals("PENDENTE", u.getSituacao());
    }

    @Test
    public void testCadastroUsuarioDuplicadoFalha() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Admin", "admin", "senha12345"));

        MockLoginView loginView = new MockLoginView();
        MockCadastroView view = new MockCadastroView();
        CadastroUsuarioPresenter presenter = new CadastroUsuarioPresenter(view, repository, logger, loginView);

        view.nomeText = "Outro Admin";
        view.usuarioText = "admin"; // duplicated
        view.senhaText = "outrasenha123";

        view.confirmarListener.actionPerformed(null);

        assertTrue(view.ultimaMensagem.contains("ja esta em uso"));
    }

    @Test
    public void testLoginSucesso() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Admin Master", "admin", "senha12345"));

        MockLoginView view = new MockLoginView();
        LoginPresenter presenter = new LoginPresenter(view, repository, logger);

        view.usuarioText = "admin";
        view.senhaText = "senha12345";

        view.acessarListener.actionPerformed(null);

        assertTrue(view.fecharChamado);
        assertTrue(view.ultimaMensagem.contains("sucesso"));
    }

    @Test
    public void testLoginUsuarioPendenteBloqueia() {
        repository.salvar(Usuario.criarNovoUsuario("Atendente", "atendente", "senha12345"));

        MockLoginView view = new MockLoginView();
        LoginPresenter presenter = new LoginPresenter(view, repository, logger);

        view.usuarioText = "atendente";
        view.senhaText = "senha12345";

        view.acessarListener.actionPerformed(null);

        assertFalse(view.fecharChamado);
        assertTrue(view.ultimaMensagem.contains("depende de autorizacao"));
    }

    @Test
    public void testLoginFormatoInvalido() {
        MockLoginView view = new MockLoginView();
        LoginPresenter presenter = new LoginPresenter(view, repository, logger);

        view.usuarioText = "Admin Com Maiuscula";
        view.senhaText = "senha12345";

        view.acessarListener.actionPerformed(null);

        assertTrue(view.ultimaMensagem.contains("letras minusculas e algarismos sem espacos"));
    }

    @Test
    public void testGestaoUsuariosBusca() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345"));
        repository.salvar(Usuario.criarNovoUsuario("Maria Silva", "maria", "senha12345"));

        MockGestaoUsuariosView view = new MockGestaoUsuariosView();
        GestaoUsuariosPresenter presenter = new GestaoUsuariosPresenter(view, repository, logger, null);

        // initial load has 2 rows
        assertEquals(2, view.table.getModel().getRowCount());

        // search "maria"
        view.buscaText = "maria";
        view.buscarListener.actionPerformed(null);
        assertEquals(1, view.table.getModel().getRowCount());
        assertEquals("maria", view.table.getModel().getValueAt(0, 1));
    }

    @Test
    public void testGestaoUsuariosAutorizarEDesautorizar() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345")); // admin/authorized
        repository.salvar(Usuario.criarNovoUsuario("Maria Silva", "maria", "senha12345")); // atendente/pendente

        MockGestaoUsuariosView view = new MockGestaoUsuariosView();
        GestaoUsuariosPresenter presenter = new GestaoUsuariosPresenter(view, repository, logger, null);

        // Select the second row ("maria")
        view.table.getModel().setValueAt(true, 1, 0); // set Sel = true for maria
        
        // Trigger Autorizar
        view.autorizarListener.actionPerformed(null);

        assertEquals("Autorizado", view.table.getModel().getValueAt(1, 5));
        Usuario u = repository.buscarPorUsuario("maria").get();
        assertEquals("AUTORIZADO", u.getSituacao());

        // Now Desautorizar
        view.table.getModel().setValueAt(true, 1, 0); // select again
        view.desautorizarListener.actionPerformed(null);

        assertEquals("Não autorizado", view.table.getModel().getValueAt(1, 5));
        u = repository.buscarPorUsuario("maria").get();
        assertEquals("NAO_AUTORIZADO", u.getSituacao());
    }

    @Test
    public void testGestaoUsuariosAlterarPerfil() {
        repository.salvar(Usuario.criarPrimeiroUsuario("Carlos Souza", "carlos", "senha12345"));

        MockGestaoUsuariosView view = new MockGestaoUsuariosView();
        GestaoUsuariosPresenter presenter = new GestaoUsuariosPresenter(view, repository, logger, null);

        // Change profile of row 0 ("carlos") to "Atendente"
        view.table.getModel().setValueAt("Atendente", 0, 4);

        Usuario u = repository.buscarPorUsuario("carlos").get();
        assertEquals("ATENDENTE", u.getPerfil());
    }
}
