package fr.efrei.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .load();

            String host = dotenv.get("DB_HOST");
            String port = dotenv.get("DB_PORT");
            String dbName = dotenv.get("DB_NAME");
            String username = dotenv.get("DB_USERNAME");
            String password = dotenv.get("DB_PASSWORD");

            if (host == null || port == null || dbName == null || username == null || password == null) {
                throw new IllegalStateException(
                    "Fichier .env incomplet ! Variables requises: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD"
                );
            }

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);

            System.out.println("✓ Connexion à la base de données réussie !");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la connexion à la base de données !");
            System.err.println("Cause: " + e.getMessage());
            if (e instanceof ClassNotFoundException) {
                System.err.println("Solution: Installez le driver MySQL avec 'mvn clean install'");
            } else if (e.getMessage() != null && e.getMessage().contains(".env")) {
                System.err.println("Solution: Créez un fichier .env à la racine avec les variables DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD");
            }
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.connection.isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                instance = new DatabaseConnection();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur lors de la fermeture de la connexion.");
        }
    }
}
