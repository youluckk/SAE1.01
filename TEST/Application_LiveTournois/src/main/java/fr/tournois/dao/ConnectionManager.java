package fr.tournois.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestionnaire de connexions à la base de données.
 * Cette classe implémente le pattern Singleton pour assurer une gestion unique des connexions.
 * Elle gère :
 * - La lecture de la configuration depuis un fichier properties
 * - L'établissement et la fermeture des connexions
 * - Le support des transactions (commit/rollback)
 * 
 * La configuration est lue depuis le fichier /config/database.properties qui doit contenir :
 * - db.url : L'URL de connexion à la base
 * - db.username : Le nom d'utilisateur
 * - db.password : Le mot de passe
 * - db.driver : La classe du driver JDBC
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class ConnectionManager {
    private static final String CONFIG_FILE = "/config/database.properties";
    private static ConnectionManager instance;
    private Connection connection;
    private Properties properties;

    /**
     * Constructeur privé (pattern Singleton).
     * Charge la configuration de la base de données au moment de l'instanciation.
     */
    private ConnectionManager() {
        loadProperties();
    }

    /**
     * Charge la configuration depuis le fichier properties.
     * Le fichier doit se trouver dans le classpath sous /config/database.properties.
     * @throws RuntimeException si le fichier est introuvable ou en cas d'erreur de lecture
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Impossible de trouver " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement de la configuration", e);
        }
    }

    /**
     * Retourne l'instance unique du ConnectionManager (pattern Singleton).
     * Crée l'instance si elle n'existe pas encore.
     * @return L'instance unique du ConnectionManager
     */
    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    /**
     * Obtient une connexion à la base de données.
     * Si une connexion existe déjà et est valide, elle est réutilisée.
     * Sinon, une nouvelle connexion est créée avec les paramètres du fichier de configuration.
     * 
     * @return Une connexion valide à la base de données
     * @throws SQLException si la connexion échoue ou si le driver est introuvable
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = properties.getProperty("db.url");
            String user = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            String driver = properties.getProperty("db.driver");

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC non trouvé", e);
            }

            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
        }
        return connection;
    }

    /*
     * Annule la transaction en cours.
     * Effectue un rollback des modifications en attente et réactive l'auto-commit.
     * Si aucune transaction n'est en cours, cette méthode n'a aucun effet.
     * 
     * @throws SQLException si une erreur survient lors du rollback
     */
    private void rollbackTransaction() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.rollback();
        }
    }

    /**
     * Ferme la connexion à la base de données.
     * Si une transaction est en cours, effectue un rollback avant la fermeture.
     * Après la fermeture, la connexion est mise à null pour permettre
     * une nouvelle connexion lors du prochain appel à getConnection().
     * 
     * @throws SQLException si une erreur survient lors de la fermeture
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            rollbackTransaction();
            connection.close();
            connection = null;
        }
    }

    /**
     * Méthode de commodité pour fermer la connexion.
     * Equivalent à closeConnection().
     * 
     * @throws SQLException si une erreur survient lors de la fermeture
     */
    public void close() throws SQLException {
        closeConnection();
    }
}
