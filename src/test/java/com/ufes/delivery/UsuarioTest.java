package com.ufes.delivery;

import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    public void testHashSenha() {
        String hash1 = SecurityUtils.hashSenha("senha123");
        String hash2 = SecurityUtils.hashSenha("senha123");
        String hash3 = SecurityUtils.hashSenha("outrasenha");

        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
        assertNotEquals(hash1, hash3);
    }

    @Test
    public void testValidarNomeValido() {
        assertDoesNotThrow(() -> Usuario.validarNome("Maria Silva"));
        assertDoesNotThrow(() -> Usuario.validarNome("D'Angelo"));
        assertDoesNotThrow(() -> Usuario.validarNome("Jean-Luc"));
    }

    @Test
    public void testValidarNomeInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarNome("M")); // too short
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarNome(""));
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarNome("Maria123")); // numbers not allowed
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarNome("Maria@Silva")); // special char not allowed
    }

    @Test
    public void testValidarUsuarioValido() {
        assertDoesNotThrow(() -> Usuario.validarUsuario("maria123"));
        assertDoesNotThrow(() -> Usuario.validarUsuario("admin"));
    }

    @Test
    public void testValidarUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarUsuario("ma")); // too short
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarUsuario("maria silva")); // spaces not allowed
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarUsuario("Maria")); // uppercase not allowed
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarUsuario("maria_123")); // underscores not allowed
    }

    @Test
    public void testValidarSenhaLimpaValida() {
        assertDoesNotThrow(() -> Usuario.validarSenhaLimpa("senha123"));
        assertDoesNotThrow(() -> Usuario.validarSenhaLimpa("umasenhagrande123!"));
    }

    @Test
    public void testValidarSenhaLimpaInvalida() {
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarSenhaLimpa("123")); // too short
        assertThrows(IllegalArgumentException.class, () -> Usuario.validarSenhaLimpa(""));
    }
}
