package com.ufes.delivery.presenter;

import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.repository.IUsuarioRepository;
import com.ufes.delivery.view.IGestaoUsuariosView;
import com.ufes.delivery.view.ILoginView;
import com.ufes.delivery.view.TelaCadastroUsuario;
import com.ufes.delivery.util.UsuarioLogadoService;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestaoUsuariosPresenter {
    private final IGestaoUsuariosView view;
    private final IUsuarioRepository repository;
    private final ILogger logger;
    private final ILoginView loginView;
    private DefaultTableModel tableModel;

    public GestaoUsuariosPresenter(IGestaoUsuariosView view, IUsuarioRepository repository, ILogger logger, ILoginView loginView) {
        this.view = view;
        this.repository = repository;
        this.logger = logger;
        this.loginView = loginView;

        inicializarTabela();

        this.view.addBuscarListener(new BuscarListener());
        this.view.addAutorizarListener(new AutorizarListener());
        this.view.addDesautorizarListener(new DesautorizarListener());
        this.view.addExcluirListener(new ExcluirListener());
        this.view.addNovoListener(new NovoListener());
        this.view.addFecharListener(new FecharListener());

        // Initial load
        carregarUsuarios("");
    }

    private void inicializarTabela() {
        JTable table = view.getTabelaUsuarios();
        
        // Define custom TableModel to set column classes and editability
        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] { "Sel.", "Nome de Usuario", "Nome", "Autorizado", "Perfil", "Situacao" }
        ) {
            Class<?>[] types = new Class<?>[] {
                Boolean.class, String.class, String.class, Boolean.class, String.class, String.class
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                // Sel. (0) and Perfil (4) are editable
                return columnIndex == 0 || columnIndex == 4;
            }
        };

        table.setModel(tableModel);

        // Configure ComboBox editor on Perfil column (index 4)
        JComboBox<String> comboPerfil = new JComboBox<>(new String[] { "Administrador", "Atendente" });
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboPerfil));

        // Listener for Perfil updates
        tableModel.addTableModelListener(new TableModelListener() {
            private boolean isUpdating = false;

            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdating) return;
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    if (col == 4) { // Perfil column
                        String username = (String) tableModel.getValueAt(row, 1);
                        String novoPerfilExibido = (String) tableModel.getValueAt(row, 4);
                        String novoPerfilDb = "Administrador".equals(novoPerfilExibido) ? "ADMINISTRADOR" : "ATENDENTE";

                        try {
                            isUpdating = true;
                            repository.atualizarPerfil(username, novoPerfilDb);
                            
                            // Audit log
                            registrarAuditoria(
                                "Alteração de perfil de usuário",
                                "Usuário",
                                "Perfil alterado para " + novoPerfilDb,
                                username
                            );
                        } catch (Exception ex) {
                            view.mostrarMensagem("Erro ao atualizar perfil: " + ex.getMessage());
                            // reload to revert UI change
                            carregarUsuarios(view.getBuscaNome());
                        } finally {
                            isUpdating = false;
                        }
                    }
                }
            }
        });
    }

    private void carregarUsuarios(String busca) {
        tableModel.setRowCount(0);
        List<Usuario> lista = repository.buscarPorNomeOuUsuario(busca);
        for (Usuario u : lista) {
            String perfilExibido = "ADMINISTRADOR".equalsIgnoreCase(u.getPerfil()) ? "Administrador" : "Atendente";
            String situacaoExibido = "AUTORIZADO".equalsIgnoreCase(u.getSituacao()) ? "Autorizado" : 
                                    "PENDENTE".equalsIgnoreCase(u.getSituacao()) ? "Pendente" : "Não autorizado";
            boolean autorizado = "AUTORIZADO".equalsIgnoreCase(u.getSituacao());

            tableModel.addRow(new Object[] {
                false, // Sel. checkbox starts false
                u.getUsuario(),
                u.getNome(),
                autorizado,
                perfilExibido,
                situacaoExibido
            });
        }
    }

    private class BuscarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String busca = view.getBuscaNome();
            if (busca != null && !busca.isBlank()) {
                if (busca.trim().length() < 2 || busca.trim().length() > 120) {
                    view.mostrarMensagem("O campo de busca deve conter entre 2 e 120 caracteres quando preenchido.");
                    return;
                }
            }
            carregarUsuarios(busca);
        }
    }

    private class AutorizarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> selecionados = obterSelecionados();
            if (selecionados.isEmpty()) {
                view.mostrarMensagem("Selecione ao menos um usuario para autorizar.");
                return;
            }

            for (String username : selecionados) {
                repository.atualizarSituacao(username, "AUTORIZADO");
                registrarAuditoria("Autorização de usuário", "Usuário", "Situação alterada para AUTORIZADO", username);
            }

            view.mostrarMensagem("Usuarios autorizados com sucesso.");
            carregarUsuarios(view.getBuscaNome());
        }
    }

    private class DesautorizarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> selecionados = obterSelecionados();
            if (selecionados.isEmpty()) {
                view.mostrarMensagem("Selecione ao menos um usuario para desautorizar.");
                return;
            }

            for (String username : selecionados) {
                repository.atualizarSituacao(username, "NAO_AUTORIZADO");
                registrarAuditoria("Desautorização de usuário", "Usuário", "Situação alterada para NAO_AUTORIZADO", username);
            }

            view.mostrarMensagem("Usuarios desautorizados com sucesso.");
            carregarUsuarios(view.getBuscaNome());
        }
    }

    private class ExcluirListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> selecionados = obterSelecionados();
            if (selecionados.isEmpty()) {
                view.mostrarMensagem("Selecione ao menos um usuario para excluir.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Tem certeza que deseja excluir os usuarios selecionados?",
                "Confirmar Exclusao",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                for (String username : selecionados) {
                    repository.excluir(username);
                    registrarAuditoria("Exclusão de usuário", "Usuário", "Usuario excluido com sucesso", username);
                }
                view.mostrarMensagem("Usuarios excluidos com sucesso.");
                carregarUsuarios(view.getBuscaNome());
            }
        }
    }

    private class NovoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            view.setVisible(false);
            TelaCadastroUsuario cadastroView = new TelaCadastroUsuario();
            
            // We pass a mock or normal view but map returning to this view
            CadastroUsuarioPresenter presenter = new CadastroUsuarioPresenter(
                cadastroView,
                repository,
                logger,
                new ILoginView() {
                    // Adapt return to show current GestaoUsuarios screen
                    @Override public String getUsuario() { return ""; }
                    @Override public String getSenha() { return ""; }
                    @Override public void addAcessarListener(ActionListener l) {}
                    @Override public void addCancelarListener(ActionListener l) {}
                    @Override public void addCadastrarUsuarioListener(ActionListener l) {}
                    @Override public void mostrarMensagem(String m) {}
                    @Override public void fechar() {}
                    @Override public void limparCampos() {}
                    @Override public void setVisible(boolean visible) {
                        if (visible) {
                            view.setVisible(true);
                            carregarUsuarios(view.getBuscaNome());
                        }
                    }
                }
            );
            cadastroView.setVisible(true);
        }
    }

    private class FecharListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            UsuarioLogadoService.limparSessao();
            view.fechar();
            if (loginView != null) {
                loginView.limparCampos();
                loginView.setVisible(true);
            }
        }
    }

    private List<String> obterSelecionados() {
        List<String> selecionados = new ArrayList<>();
        int count = tableModel.getRowCount();
        for (int i = 0; i < count; i++) {
            Boolean sel = (Boolean) tableModel.getValueAt(i, 0);
            if (sel != null && sel) {
                selecionados.add((String) tableModel.getValueAt(i, 1));
            }
        }
        return selecionados;
    }

    private void registrarAuditoria(String operacao, String recurso, String resultado, String responsavelAfetado) {
        Map<String, String> dados = new HashMap<>();
        dados.put("usuario_afetado", responsavelAfetado);
        dados.put("resultado", resultado);
        try {
            logger.registrar(new LogEntry(
                UsuarioLogadoService.getNomeUsuario(),
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
