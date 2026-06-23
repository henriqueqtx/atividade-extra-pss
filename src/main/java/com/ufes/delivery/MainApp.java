package com.ufes.delivery;

import com.mycompany.logsauditoria.JsonLogger;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.database.DatabaseManager;
import com.ufes.delivery.presenter.LoginPresenter;
import com.ufes.delivery.repository.UsuarioRepositorySQLite;
import com.ufes.delivery.view.TelaLogin;

public class MainApp {
    public static void main(String[] args) {
        // Set Look and Feel (Nimbus)
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        // Initialize SQLite Database and create tables if not exists
        DatabaseManager.inicializarBanco();

        // Initialize Auditoria Logger
        ILogger logger = new JsonLogger("delivery_auditoria.json");

        // Initialize repository
        UsuarioRepositorySQLite repository = new UsuarioRepositorySQLite();

        // Launch Login form wrapped in MVP Presenter
        java.awt.EventQueue.invokeLater(() -> {
            TelaLogin loginFrame = new TelaLogin();
            new LoginPresenter(loginFrame, repository, logger);
            loginFrame.setVisible(true);
        });
    }
}
