package com.ufes.delivery.presenter;

import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.repository.IUsuarioRepository;
import com.ufes.delivery.view.ICadastroUsuarioView;
import com.ufes.delivery.view.ILoginView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class CadastroUsuarioPresenter {
    private final ICadastroUsuarioView view;
    private final IUsuarioRepository repository;
    private final ILogger logger;
    private final ILoginView loginView;

    public CadastroUsuarioPresenter(ICadastroUsuarioView view, IUsuarioRepository repository, ILogger logger, ILoginView loginView) {
        this.view = view;
        this.repository = repository;
        this.logger = logger;
        this.loginView = loginView;

        this.view.addConfirmarListener(new ConfirmarListener());
        this.view.addCancelarListener(new CancelarListener());
    }

    private class ConfirmarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String nome = view.getNomeCivil();
            String usuario = view.getUsuario();
            String senha = view.getSenha();

            // Validate mandatory empty fields (Scenario 4)
            if (nome == null || nome.isBlank() || usuario == null || usuario.isBlank() || senha == null || senha.isEmpty()) {
                StringBuilder msg = new StringBuilder("Campos obrigatorios nao preenchidos: ");
                if (nome == null || nome.isBlank()) {
                    msg.append("Nome; ");
                }
                if (usuario == null || usuario.isBlank()) {
                    msg.append("Usuario; ");
                }
                if (senha == null || senha.isEmpty()) {
                    msg.append("Senha; ");
                }
                
                registrarAuditoria("Falha no cadastro de usuário", "Usuário", "Dados obrigatorios ausentes", usuario);
                view.mostrarMensagem(msg.toString());
                return;
            }

            // Validate formatting via domain model or helper
            try {
                Usuario.validarNome(nome);
                Usuario.validarUsuario(usuario);
                Usuario.validarSenhaLimpa(senha);
            } catch (IllegalArgumentException ex) {
                registrarAuditoria("Falha no cadastro de usuário", "Usuário", "Erro de validação: " + ex.getMessage(), usuario);
                view.mostrarMensagem(ex.getMessage());
                return;
            }

            // Validate uniqueness (Scenario 3)
            if (repository.buscarPorUsuario(usuario).isPresent()) {
                registrarAuditoria("Falha no cadastro de usuário", "Usuário", "Nome de usuario ja em uso", usuario);
                view.mostrarMensagem("O nome de usuario ja esta em uso");
                return;
            }

            // Determine if first user (Scenario 1 & 2)
            Usuario novoUsuario;
            boolean primeiro = !repository.existeUsuario();
            if (primeiro) {
                novoUsuario = Usuario.criarPrimeiroUsuario(nome, usuario, senha);
            } else {
                novoUsuario = Usuario.criarNovoUsuario(nome, usuario, senha);
            }

            // Persist
            repository.salvar(novoUsuario);

            // Audit
            String resultadoMsg = "Cadastro realizado. Perfil: " + novoUsuario.getPerfil() + ", Situacao: " + novoUsuario.getSituacao();
            registrarAuditoria("Cadastro de usuário", "Usuário", resultadoMsg, usuario);

            view.mostrarMensagem("Cadastro realizado com sucesso! " + 
                    (primeiro ? "Primeiro usuario como Administrador Autorizado." : "Usuario cadastrado como Atendente Pendente."));

            // Clear and return to login (Scenario 5)
            retornarParaLogin();
        }
    }

    private class CancelarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            retornarParaLogin();
        }
    }

    private void retornarParaLogin() {
        view.limparCampos();
        view.fechar();
        if (loginView != null) {
            loginView.setVisible(true);
        }
    }

    private void registrarAuditoria(String operacao, String recurso, String resultado, String responsavel) {
        Map<String, String> dados = new HashMap<>();
        dados.put("resultado", resultado);
        try {
            logger.registrar(new LogEntry(
                responsavel != null ? responsavel : "Não autenticado",
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
