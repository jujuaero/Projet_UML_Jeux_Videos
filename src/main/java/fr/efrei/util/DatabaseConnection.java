package fr.efrei.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private Dotenv dotenv = null;

    private DatabaseConnection() {
        try {
            String host = "127.0.0.1";
            String port = "3306";
            String dbName = "CapeTownGaming";
            String username = "root";
            String password = "Password123!";

            try {
                dotenv = Dotenv.configure()
                        .directory("./")
                        .ignoreIfMissing()
                        .load();

                host = dotenv.get("DB_HOST", host);
                port = dotenv.get("DB_PORT", port);
                dbName = dotenv.get("DB_NAME", dbName);
                username = dotenv.get("DB_USERNAME", username);
                password = dotenv.get("DB_PASSWORD", password);
            } catch (Exception envError) {
                System.out.println("Fichier .env non lu, utilisation des valeurs par défaut");
                System.out.println("  (Raison: " + envError.getMessage() + ")");
            }


            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);

            System.out.println("✓ Connexion à la base de données réussie !");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL non trouvé !");
            System.err.println("Solution: Exécutez 'mvn clean install' ou rechargez le projet Maven dans votre IDE");
        } catch (SQLException e) {
            System.err.println("✗ Erreur de connexion à la base de données !");
            String dbName = (dotenv != null) ? dotenv.get("DB_NAME", "CapeTownGaming") : "CapeTownGaming";
            System.err.println("\nCause possible: " + e.getMessage());
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

