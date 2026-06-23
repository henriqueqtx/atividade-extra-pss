package com.ufes.delivery.view;

import java.awt.event.ActionListener;

public interface ILoginView {
    String getUsuario();
    String getSenha();
    void addAcessarListener(ActionListener listener);
    void addCancelarListener(ActionListener listener);
    void addCadastrarUsuarioListener(ActionListener listener);
    void mostrarMensagem(String mensagem);
    void fechar();
    void limparCampos();
    void setVisible(boolean visible);
}
