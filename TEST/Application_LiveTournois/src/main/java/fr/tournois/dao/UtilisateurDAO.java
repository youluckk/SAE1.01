package fr.tournois.dao;

import fr.tournois.model.Utilisateur;
import fr.tournois.model.Role;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) pour la gestion des utilisateurs dans la base de données
 * Cette classe encapsule toutes les opérations JDBC liées aux utilisateurs
 * Elle utilise une connexion Oracle et gère les spécificités Oracle comme :
 * - Les séquences pour les ID (seq_utilisateur_id.NEXTVAL)
 * - La fonction SYSDATE pour les dates
 * - La conversion des booléens en NUMBER(1)
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class UtilisateurDAO {
    /*
     * Requête d'insertion d'un utilisateur
     * Utilise seq_utilisateur_id.NEXTVAL pour générer automatiquement l'ID (spécifique à Oracle)
     * Les ? sont des paramètres qui seront remplacés par PreparedStatement pour éviter les injections SQL
     * SYSDATE est utilisé pour la date de création (fonction Oracle)
     */
    private static final String INSERT_QUERY = 
        "INSERT INTO Utilisateur (id_utilisateur, pseudo, passwd, role, date_creation, derniere_connexion, actif) " +
        "VALUES (seq_utilisateur_id.NEXTVAL, ?, ?, ?, SYSDATE, NULL, ?)";
    
    private static final String GET_ID_BY_PSEUDO_QUERY = 
        "SELECT id_utilisateur FROM Utilisateur WHERE pseudo = ?";

    private static final String UPDATE_QUERY = 
        "UPDATE Utilisateur SET pseudo = ?, " +
        "passwd = ?, role = ?, derniere_connexion = ?, actif = ? WHERE id_utilisateur = ?";
    
    private static final String DELETE_QUERY = 
        "DELETE FROM Utilisateur WHERE id_utilisateur = ?";
    
    private static final String FIND_BY_ID_QUERY = 
        "SELECT * FROM Utilisateur WHERE id_utilisateur = ?";
    
    private static final String FIND_ALL_QUERY = 
        "SELECT * FROM Utilisateur";
    
    private static final String FIND_BY_PSEUDO_QUERY = 
        "SELECT * FROM Utilisateur WHERE pseudo = ?";

    private static final String FIND_BY_ROLE_QUERY = 
        "SELECT * FROM Utilisateur WHERE role = ?";

    private static final String FIND_BY_EMAIL_QUERY = 
        "SELECT * FROM Utilisateur WHERE email = ?";

    private final Connection connection;

    /**
     * Constructeur de UtilisateurDAO
     * @param connection La connexion à la base de données Oracle
     *                  Cette connexion doit être ouverte et valide
     *                  La gestion de la connexion (commit/rollback) est faite par l'appelant
     */
    public UtilisateurDAO(Connection connection) {
        // La connexion doit être ouverte et valide
        // La connexion est stockée pour être réutilisée par toutes les méthodes
        // La gestion des transactions (commit/rollback) est faite par l'appelant
        this.connection = connection;
    }

    /**
     * Trouve tous les utilisateurs ayant un rôle spécifique
     * @param role Le rôle à rechercher (ADMIN ou ORGANISATEUR)
     * @return Liste des utilisateurs ayant le rôle spécifié
     * @throws DAOException si erreur SQL ou rôle invalide
     */
    public List<Utilisateur> findByRole(Role role) throws DAOException {
        /*
        Processus JDBC :
        * 1. Vérification que le rôle n'est pas null
        * 2. Préparation de la requête avec PreparedStatement
        * 3. Conversion de l'enum Role en String pour la BDD
        * 4. Exécution de la requête et parcours du ResultSet
        * 5. Construction de la liste des utilisateurs
        *
        * Points importants :
        * - Le rôle est stocké comme une chaîne dans la BDD
        * - La conversion Role.name() est utilisée pour la requête
        * - Une liste vide est retournée si aucun utilisateur trouvé
        */

        // 1. Vérification que le rôle n'est pas null
        if (role == null) {
            throw new DAOException("Le rôle est invalide");
        }

        // 2. Préparation et exécution de la requête
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ROLE_QUERY)) {
            statement.setString(1, role.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Utilisateur> utilisateurs = new ArrayList<>();
                while (resultSet.next()) {
                    utilisateurs.add(mapResultSetToEntity(resultSet));
                }
                return utilisateurs;
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des utilisateurs par rôle", e);
        }
    }

    /**
     * Trouve un utilisateur par son pseudo
     * @param pseudo Le pseudo à rechercher
     * @return Optional contenant l'utilisateur si trouvé, vide sinon
     * @throws DAOException si erreur SQL ou pseudo invalide
     */
    public Optional<Utilisateur> findByPseudo(String pseudo) throws DAOException {
        /*
        Processus JDBC :
        * 1. Vérification que le pseudo n'est pas null/vide
        * 2. Préparation de la requête avec PreparedStatement
        * 3. Exécution de la requête et récupération du ResultSet
        * 4. Conversion du résultat en Optional<Utilisateur>
        *
        * Points importants :
        * - La recherche est sensible à la casse (dépend de la configuration Oracle)
        * - Le pseudo est supposé unique dans la base (contrainte UNIQUE)
        * - Retourne Optional.empty() si aucun utilisateur trouvé
        */

        // 1. Vérification que le pseudo n'est pas null/vide
        if (pseudo == null || pseudo.trim().isEmpty()) {
            throw new DAOException("Le pseudo est invalide");
        }

        // 2. Préparation et exécution de la requête
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_PSEUDO_QUERY)) {
            statement.setString(1, pseudo);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToEntity(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche par pseudo", e);
        }
    }

    /**
     * Crée un nouvel utilisateur dans la base de données
 
     * @param utilisateur L'utilisateur à créer (sans ID)
     * @return L'utilisateur créé avec son ID généré
     * @throws DAOException si une erreur survient (pseudo en double, erreur SQL, etc.)
     */
    public Utilisateur create(Utilisateur utilisateur) throws DAOException {
        /* 
        Processus JDBC :
        * 1. Validation des données de l'utilisateur
        * 2. Vérification de l'unicité du pseudo
        * 3. Préparation de la requête avec PreparedStatement (sécurité contre les injections SQL)
        * 4. Exécution de la requête INSERT
        * 5. Récupération de l'ID généré par Oracle
        * 6. Mise à jour de l'objet Utilisateur avec l'ID
        */
        validateUtilisateur(utilisateur);
        
        // Vérifier si le pseudo existe déjà
        if (findByPseudo(utilisateur.getPseudo()).isPresent()) {
            throw new DAOException("Le pseudo existe déjà");
        }

        try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_QUERY)) {
            insertStmt.setString(1, utilisateur.getPseudo());
            insertStmt.setString(2, utilisateur.getPassword());
            insertStmt.setString(3, utilisateur.getRole().name());
            // La date de création est gérée par SYSDATE dans Oracle
            insertStmt.setInt(4, utilisateur.isActif() ? 1 : 0);  // Conversion boolean vers NUMBER(1)

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("La création de l'utilisateur a échoué, aucune ligne affectée.");
            }
            
            // Récupérer l'ID généré via une requête SELECT
            try (PreparedStatement selectStmt = connection.prepareStatement(GET_ID_BY_PSEUDO_QUERY)) {
                selectStmt.setString(1, utilisateur.getPseudo());
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        connection.commit();
                        utilisateur.setId(rs.getInt("id_utilisateur"));
                        return utilisateur;
                    } else {
                        connection.rollback();
                        throw new DAOException("La création de l'utilisateur a échoué, impossible de récupérer l'ID.");
                    }
                }
            }
        } catch (SQLException e) {

            throw new DAOException("Erreur lors de la création de l'utilisateur (Code: " + e.getErrorCode() + ")", e);
        }
    }

    /**
     * Recherche un utilisateur par son ID
     * @param id L'ID de l'utilisateur
     * @return Optional contenant l'utilisateur si trouvé, vide sinon
     * @throws DAOException si erreur SQL ou ID invalide
     */
    public Optional<Utilisateur> findById(Integer id) throws DAOException {
        /* 
        Processus JDBC :
        * 1. Vérification de la validité de l'ID
        * 2. Préparation de la requête SELECT avec PreparedStatement
        * 3. Exécution de la requête et récupération du ResultSet
        * 4. Si une ligne est trouvée :
        *    - Conversion du ResultSet en objet Utilisateur
        *    - Encapsulation dans un Optional
        * 5. Sinon, retour d'un Optional vide
        *
        * Utilisation de Optional :
        * - Permet une meilleure gestion des valeurs null
        * - Force le code appelant à gérer le cas où l'utilisateur n'existe pas
        */

        // 1. Vérification de la validité de l'ID
        if (id == null || id <= 0) {
            throw new DAOException("L'ID de l'utilisateur est invalide");
        }

        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_QUERY)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToEntity(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche de l'utilisateur par ID", e);
        }
    }

    /**
     * Récupère tous les utilisateurs de la base de données
     * 
     * Processus JDBC :
     * 1. Préparation de la requête SELECT sans paramètres
     * @return Liste contenant tous les utilisateurs de la base
     * @throws DAOException si erreur lors de l'accès à la base
     */
    public List<Utilisateur> findAll() throws DAOException {
        /*
        Processus JDBC :
        * 1. Préparation de la requête SELECT sans paramètres
        * 2. Exécution de la requête et récupération du ResultSet
        * 3. Parcours du ResultSet avec while(rs.next()) :
        *    - Pour chaque ligne, conversion en objet Utilisateur
        *    - Ajout à la liste des résultats
        * 4. Retour de la liste complète
        *
        * Note : Cette méthode peut retourner une liste vide mais jamais null
        */

        // 1. Préparation de la requête SELECT sans paramètres
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_QUERY);
             ResultSet resultSet = statement.executeQuery()) {
            
            List<Utilisateur> utilisateurs = new ArrayList<>();
            while (resultSet.next()) {
                utilisateurs.add(mapResultSetToEntity(resultSet));
            }
            return utilisateurs;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération de tous les utilisateurs", e);
        }
    }

    /**
     * Met à jour un utilisateur dans la base de données
     * @param utilisateur L'utilisateur avec les nouvelles valeurs
     * @return L'utilisateur mis à jour
     * @throws DAOException si erreur SQL ou utilisateur non trouvé
     */
    public Utilisateur update(Utilisateur utilisateur) throws DAOException {
        /*
        Processus JDBC :
        * 1. Validation des données de l'utilisateur
        * 2. Préparation de la requête UPDATE avec PreparedStatement
        * 3. Remplissage des paramètres de la requête dans l'ordre des ? :
        *    - Conversion des types Java vers SQL
        *    - Conversion booléen vers NUMBER(1)
        * 4. Exécution de la requête et vérification du nombre de lignes affectées
        *
        * Sécurité :
        * - Utilisation de PreparedStatement contre les injections SQL
        * - Vérification que l'utilisateur existe avant la mise à jour
        */

        // 1. Validation des données
        validateUtilisateur(utilisateur);

        if (utilisateur.getId() == null || utilisateur.getId() <= 0) {
            throw new DAOException("L'ID de l'utilisateur est invalide");
        }

        // Vérifier si le pseudo existe déjà pour un autre utilisateur
        Optional<Utilisateur> existingUser = findByPseudo(utilisateur.getPseudo());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(utilisateur.getId())) {
            throw new DAOException("Le pseudo existe déjà pour un autre utilisateur");
        }

        try (PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            statement.setString(1, utilisateur.getPseudo());
            statement.setString(2, utilisateur.getPassword());
            statement.setString(3, utilisateur.getRole().name());
            statement.setObject(4, utilisateur.getDerniereConnexion());
            statement.setBoolean(5, utilisateur.isActif());
            statement.setInt(6, utilisateur.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("La mise à jour de l'utilisateur a échoué, aucune ligne affectée.");
            }

            connection.commit();    
            
            return utilisateur;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    /**
     * Supprime un utilisateur de la base de données
     * @param id L'ID de l'utilisateur à supprimer
     * @return true si l'utilisateur a été supprimé, false s'il n'existait pas
     * @throws DAOException si erreur SQL ou ID invalide
     */
    public boolean delete(Integer id) throws DAOException {
        if (id == null || id <= 0) {
            throw new DAOException("L'ID de l'utilisateur est invalide");
        }

        try (PreparedStatement statement = connection.prepareStatement(DELETE_QUERY)) {
            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            connection.commit();

            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }

    /**
     * Recherche un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé ou un Optional vide
     * @throws DAOException si une erreur survient
     */
    public Optional<Utilisateur> findByEmail(String email) throws DAOException {
        /*
        Processus JDBC :
        * 1. Vérification de l'email
        * 2. Préparation de la requête avec PreparedStatement
        * 3. Exécution et récupération du résultat
        * 4. Conversion en Optional<Utilisateur>
        */

        // 1. Vérification de l'email
        if (email == null || email.trim().isEmpty()) {
            throw new DAOException("L'email est invalide");
        }

        // 2. Préparation et exécution de la requête
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_EMAIL_QUERY)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche d'un utilisateur par email", e);
        }
    }

    /**
     * Met à jour la date de dernière connexion de l'utilisateur
     * @param idUtilisateur L'ID de l'utilisateur à mettre à jour
     * @throws DAOException si une erreur survient
     */
    public void updateDerniereConnexion(int idUtilisateur) throws DAOException {
        /*
        Processus JDBC :
        * 1. Préparation de la requête UPDATE
        * 2. Utilisation de SYSDATE pour la date actuelle (Oracle)
        * 3. Exécution de la mise à jour
        */

        // 1. Préparation de la requête avec SYSDATE
        String sql = "UPDATE Utilisateur SET derniere_connexion = SYSDATE WHERE id_utilisateur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            stmt.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de la dernière connexion", e);
        }
    }

    /**
     * Valide les données d'un utilisateur avant insertion/mise à jour
     * 
     * @param utilisateur L'utilisateur à valider
     * @throws DAOException si les données sont invalides
     */
    private void validateUtilisateur(Utilisateur utilisateur) throws DAOException {
        /*
         * Points de validation JDBC :
         * 1. Vérification des valeurs null (protection contre NullPointerException)
         * 2. Vérification des chaînes vides (problèmes potentiels avec la BDD)
         * 3. Validation des énumérations (problèmes potentiels avec les contraintes de la BDD)
         */

        if (utilisateur == null) {
            throw new DAOException("L'utilisateur ne peut pas être null");
        }
        if (utilisateur.getPseudo() == null || utilisateur.getPseudo().trim().isEmpty()) {
            throw new DAOException("Le pseudo est obligatoire");
        }
        if (utilisateur.getPassword() == null || utilisateur.getPassword().trim().isEmpty()) {
            throw new DAOException("Le mot de passe est obligatoire");
        }
        if (utilisateur.getRole() != Role.ADMIN && utilisateur.getRole() != Role.ORGANISATEUR) {
            throw new DAOException("Le rôle est obligatoire");
        }
    }

    /**
     * Convertit une ligne de résultat SQL (ResultSet) en objet Utilisateur
     * 
     * @param rs Le ResultSet positionné sur la ligne à convertir
     * @return Un nouvel objet Utilisateur avec les données de la BDD
     * @throws SQLException si erreur lors de la lecture du ResultSet
     */
    private Utilisateur mapResultSetToEntity(ResultSet rs) throws SQLException {
        /*
        * Processus JDBC :
        * 1. Création d'un nouvel objet Utilisateur
        * 2. Récupération des colonnes du ResultSet par leur nom
        * 3. Conversion des types SQL vers les types Java :
        *    - NUMBER(10) -> int pour l'ID
        *    - VARCHAR2 -> String pour pseudo, password
        *    - DATE -> LocalDateTime pour les dates
        *    - NUMBER(1) -> boolean pour actif
        * 4. Gestion des valeurs NULL possibles (notamment pour derniere_connexion)
        */
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id_utilisateur"));
        utilisateur.setPseudo(rs.getString("pseudo"));
        utilisateur.setPassword(rs.getString("passwd"));
        utilisateur.setRole(Role.valueOf(rs.getString("role")));
        utilisateur.setDateCreation(rs.getObject("date_creation", LocalDateTime.class));
        utilisateur.setDerniereConnexion(rs.getObject("derniere_connexion", LocalDateTime.class));
        utilisateur.setActif(rs.getInt("actif") == 1);  // Conversion NUMBER(1) vers boolean
        return utilisateur;
    }
}
