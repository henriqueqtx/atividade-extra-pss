package com.ufes.delivery.view;

import java.awt.event.ActionListener;
import javax.swing.JTable;

public interface IGestaoUsuariosView {
    String getBuscaNome();
    void addBuscarListener(ActionListener listener);
    void addAutorizarListener(ActionListener listener);
    void addDesautorizarListener(ActionListener listener);
    void addExcluirListener(ActionListener listener);
    void addNovoListener(ActionListener listener);
    void addFecharListener(ActionListener listener);
    JTable getTabelaUsuarios();
    void mostrarMensagem(String mensagem);
    void fechar();
    void setVisible(boolean visible);
}
