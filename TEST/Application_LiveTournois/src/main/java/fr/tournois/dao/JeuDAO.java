package fr.tournois.dao;

import fr.tournois.model.Jeu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la gestion des jeux
 */
public class JeuDAO {

    private final Connection connection;

    public JeuDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Récupère tous les jeux
     * @return Liste de tous les jeux
     * @throws DAOException si erreur lors de la récupération
     */
    public List<Jeu> findAll() throws DAOException {
        List<Jeu> jeux = new ArrayList<>();
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Jeu ORDER BY nom")) {
            
            while (rs.next()) {
                jeux.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération de la liste des jeux: " + e.getMessage());
        }
        
        return jeux;
    }

    /**
     * Récupère un jeu par son identifiant
     * @param id Identifiant du jeu
     * @return Jeu trouvé ou Optional.empty() si absent
     * @throws DAOException si erreur lors de la récupération
     */
    public Optional<Jeu> findById(Integer id) throws DAOException {
        if (id == null) {
            return Optional.empty();
        }

        String query = "SELECT * FROM Jeu WHERE id_jeu = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération du jeu: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Crée un nouveau jeu dans la base de données
     * @param jeu Jeu à créer
     * @throws DAOException si erreur lors de la création
     */
    public void create(Jeu jeu) throws DAOException {
        if (jeu == null) {
            throw new DAOException("Le jeu ne peut pas être null");
        }

        String getIdQuery = "SELECT seq_jeu_id.NEXTVAL FROM DUAL";
        String insertQuery = "INSERT INTO Jeu (id_jeu, nom, editeur, annee_sortie, genre, description) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            Integer newId = null;
            try (PreparedStatement getIdPst = connection.prepareStatement(getIdQuery);
                 ResultSet rs = getIdPst.executeQuery()) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                }
            }
            
            if (newId == null) {
                throw new DAOException("Impossible de récupérer un nouvel ID");
            }
            
            // Insérer le jeu avec l'ID récupéré
            try (PreparedStatement pst = connection.prepareStatement(insertQuery)) {
                pst.setInt(1, newId);
                pst.setString(2, jeu.getNom());
                pst.setString(3, jeu.getEditeur());
                pst.setInt(4, jeu.getAnneeSortie());
                pst.setString(5, jeu.getGenre());
                pst.setString(6, jeu.getDescription());
                
                int result = pst.executeUpdate();
                
                if (result != 1) {
                    connection.rollback();
                    throw new DAOException("Erreur lors de l'insertion du jeu");
                }
                
                jeu.setId(newId);
                connection.commit();
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la création du jeu: " + e.getMessage());
        }
    }

    /**
     * Met à jour un jeu existant
     * @param jeu Jeu à mettre à jour
     * @throws DAOException si erreur lors de la mise à jour
     */
    public void update(Jeu jeu) throws DAOException {
        if (jeu == null || jeu.getId() == null) {
            throw new DAOException("Le jeu et son ID ne peuvent pas être null");
        }

        String query = "UPDATE Jeu SET nom = ?, editeur = ?, annee_sortie = ?, genre = ?, description = ? " +
                      "WHERE id_jeu = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, jeu.getNom());
            pst.setString(2, jeu.getEditeur());
            pst.setInt(3, jeu.getAnneeSortie());
            pst.setString(4, jeu.getGenre());
            pst.setString(5, jeu.getDescription());
            pst.setInt(6, jeu.getId());
            
            int result = pst.executeUpdate();
            
            if (result != 1) {
                connection.rollback();
                throw new DAOException("Aucun jeu trouvé avec cet ID ou erreur lors de la mise à jour");
            }
            
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la mise à jour du jeu: " + e.getMessage());
        }
    }

    /**
     * Supprime un jeu
     * @param jeu Jeu à supprimer
     * @throws DAOException si erreur lors de la suppression
     */
    public void delete(Jeu jeu) throws DAOException {
        if (jeu == null || jeu.getId() == null) {
            throw new DAOException("Le jeu et son ID ne peuvent pas être null");
        }
        
        deleteById(jeu.getId());
    }

    /**
     * Supprime un jeu par son identifiant
     * @param id Identifiant du jeu
     * @throws DAOException si erreur lors de la suppression
     */
    public void deleteById(Integer id) throws DAOException {
        if (id == null) {
            throw new DAOException("L'ID ne peut pas être null");
        }

        // Vérifier s'il y a des tournois associés
        String checkQuery = "SELECT COUNT(*) FROM Tournoi WHERE id_jeu = ?";
        try (PreparedStatement checkPst = connection.prepareStatement(checkQuery)) {
            checkPst.setInt(1, id);
            try (ResultSet rs = checkPst.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DAOException("Impossible de supprimer ce jeu car il est utilisé dans des tournois");
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la vérification des dépendances: " + e.getMessage());
        }

        String query = "DELETE FROM Jeu WHERE id_jeu = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            
            int result = pst.executeUpdate();
            
            if (result != 1) {
                connection.rollback();
                throw new DAOException("Aucun jeu trouvé avec cet ID");
            }
            
            connection.commit();
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la suppression du jeu: " + e.getMessage());
        }
    }

    /**
     * Recherche des jeux par nom (recherche partielle)
     * @param nom Nom ou partie du nom à rechercher
     * @return Liste des jeux correspondants
     * @throws DAOException si erreur lors de la recherche
     */
    public List<Jeu> findByNom(String nom) throws DAOException {
        if (nom == null || nom.trim().isEmpty()) {
            return findAll();
        }

        List<Jeu> jeux = new ArrayList<>();
        String query = "SELECT * FROM Jeu WHERE UPPER(nom) LIKE UPPER(?) ORDER BY nom";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, "%" + nom.trim() + "%");
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    jeux.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des jeux: " + e.getMessage());
        }
        
        return jeux;
    }

    /**
     * Recherche des jeux par genre
     * @param genre Genre à rechercher
     * @return Liste des jeux du genre
     * @throws DAOException si erreur lors de la recherche
     */
    public List<Jeu> findByGenre(String genre) throws DAOException {
        if (genre == null || genre.trim().isEmpty()) {
            return findAll();
        }

        List<Jeu> jeux = new ArrayList<>();
        String query = "SELECT * FROM Jeu WHERE UPPER(genre) = UPPER(?) ORDER BY nom";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, genre.trim());
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    jeux.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des jeux par genre: " + e.getMessage());
        }
        
        return jeux;
    }

    /**
     * Récupère tous les genres disponibles
     * @return Liste des genres
     * @throws DAOException si erreur lors de la récupération
     */
    public List<String> findAllGenres() throws DAOException {
        List<String> genres = new ArrayList<>();
        String query = "SELECT DISTINCT genre FROM Jeu WHERE genre IS NOT NULL ORDER BY genre";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            
            while (rs.next()) {
                String genre = rs.getString("genre");
                if (genre != null && !genre.trim().isEmpty()) {
                    genres.add(genre);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des genres: " + e.getMessage());
        }
        
        return genres;
    }

    /**
     * Mappe un ResultSet vers un objet Jeu
     */
    private Jeu mapResultSetToEntity(ResultSet rs) throws SQLException {
        Jeu jeu = new Jeu();
        jeu.setId(rs.getInt("id_jeu"));
        jeu.setNom(rs.getString("nom"));
        jeu.setEditeur(rs.getString("editeur"));
        jeu.setAnneeSortie(rs.getInt("annee_sortie"));
        jeu.setGenre(rs.getString("genre"));
        jeu.setDescription(rs.getString("description"));
        return jeu;
    }

    /**
     * Récupère la connexion (pour compatibilité avec le code existant)
     */
    public Connection getConnection() {
        return connection;
    }
}