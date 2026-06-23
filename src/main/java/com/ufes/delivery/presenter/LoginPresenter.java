package com.ufes.delivery.presenter;

import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.repository.IUsuarioRepository;
import com.ufes.delivery.view.ICadastroUsuarioView;
import com.ufes.delivery.view.ILoginView;
import com.ufes.delivery.view.TelaCadastroUsuario;
import com.ufes.delivery.view.TelaGestaoUsuarios;
import com.ufes.delivery.util.SecurityUtils;
import com.ufes.delivery.util.UsuarioLogadoService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LoginPresenter {
    private final ILoginView view;
    private final IUsuarioRepository repository;
    private final ILogger logger;

    public LoginPresenter(ILoginView view, IUsuarioRepository repository, ILogger logger) {
        this.view = view;
        this.repository = repository;
        this.logger = logger;

        this.view.addAcessarListener(new AcessarListener());
        this.view.addCancelarListener(new CancelarListener());
        this.view.addCadastrarUsuarioListener(new CadastrarUsuarioListener());
    }

    private class AcessarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getUsuario();
            String password = view.getSenha();

            // Validate mandatory fields
            if (username == null || username.isBlank() || password == null || password.isEmpty()) {
                view.mostrarMensagem("Dados obrigatorios ausentes. Preencha o usuario e a senha.");
                return;
            }

            // Validate format (Scenario 3)
            // "O nome de usuário deve usar letras minúsculas e algarismos sem espaços"
            if (!username.matches("^[a-z0-9]+$")) {
                view.mostrarMensagem("O nome de usuario deve usar letras minusculas e algarismos sem espacos");
                return;
            }

            // Look up user
            Optional<Usuario> optUser = repository.buscarPorUsuario(username);
            if (optUser.isEmpty()) {
                registrarAuditoria("Falha de autenticação", "Sessão", "Credenciais invalidas", username);
                view.mostrarMensagem("As credenciais sao invalidas");
                return;
            }

            Usuario user = optUser.get();
            // Validate password hash (Scenario 4)
            String hashedInput = SecurityUtils.hashSenha(password);
            if (!user.getSenha().equals(hashedInput)) {
                registrarAuditoria("Falha de autenticação", "Sessão", "Credenciais invalidas", username);
                view.mostrarMensagem("As credenciais sao invalidas");
                return;
            }

            // Check situation (Scenario 5)
            if ("PENDENTE".equalsIgnoreCase(user.getSituacao()) || "NAO_AUTORIZADO".equalsIgnoreCase(user.getSituacao())) {
                registrarAuditoria("Falha de autenticação", "Sessão", "Tentativa de acesso nao autorizada (Usuario pendente ou nao autorizado)", username);
                view.mostrarMensagem("O acesso depende de autorizacao administrativa");
                return;
            }

            // Success (Scenario 1 & 2)
            LocalDateTime now = LocalDateTime.now();
            UsuarioLogadoService.setUsuarioLogado(user.getUsuario(), user.getPerfil(), now);
            registrarAuditoria("Login de usuario", "Sessão", "Sucesso. Perfil: " + user.getPerfil(), username);

            view.mostrarMensagem("Login realizado com sucesso! Bem-vindo, " + user.getNome() + " (" + user.getPerfil() + ").");
            view.fechar();
            if ("ADMINISTRADOR".equalsIgnoreCase(user.getPerfil())) {
                TelaGestaoUsuarios gestaoView = new TelaGestaoUsuarios();
                new GestaoUsuariosPresenter(gestaoView, repository, logger, view);
                gestaoView.setVisible(true);
            }
        }
    }

    private class CancelarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.fechar();
            System.exit(0);
        }
    }

    private class CadastrarUsuarioListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setVisible(false);
            TelaCadastroUsuario telaCadastro = new TelaCadastroUsuario();
            CadastroUsuarioPresenter presenter = new CadastroUsuarioPresenter(telaCadastro, repository, logger, view);
            telaCadastro.setVisible(true);
        }
    }

    private void registrarAuditoria(String operacao, String recurso, String resultado, String responsavel) {
        Map<String, String> dados = new HashMap<>();
        dados.put("resultado", resultado);
        try {
            logger.registrar(new LogEntry(
                responsavel != null ? responsavel : "Desconhecido",
                LocalDate.now(),
                LocalTime.now(),
                operacao + " (" + recurso + ")",
                dados
            ));
        } catch (IOException e) {
            System.err.println("Erro ao registrar log de auditoria: " + e.getMessage());
        }
    }
}
