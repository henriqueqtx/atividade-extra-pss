package com.ufes.delivery.repository;

import com.ufes.delivery.model.Usuario;
import java.util.Optional;

public interface IUsuarioRepository {
    void salvar(Usuario usuario);
    Optional<Usuario> buscarPorUsuario(String usuario);
    boolean existeUsuario();
    java.util.List<Usuario> buscarPorNomeOuUsuario(String busca);
    void atualizarPerfil(String usuario, String perfil);
    void atualizarSituacao(String usuario, String situacao);
    void excluir(String usuario);
}
