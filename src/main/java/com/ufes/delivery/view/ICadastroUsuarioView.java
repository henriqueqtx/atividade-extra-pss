package com.ufes.delivery.view;

import java.awt.event.ActionListener;

public interface ICadastroUsuarioView {
    String getNomeCivil();
    String getUsuario();
    String getSenha();
    void addConfirmarListener(ActionListener listener);
    void addCancelarListener(ActionListener listener);
    void mostrarMensagem(String mensagem);
    void fechar();
    void limparCampos();
    void setVisible(boolean visible);
}
