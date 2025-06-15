package fr.tournois.dao;

import fr.tournois.model.Inscription;
import fr.tournois.model.Tournoi;
import fr.tournois.model.Equipe;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe d'accès aux données pour les inscriptions.
 * Gère les opérations CRUD sur les inscriptions d'équipes aux tournois.
 */
public class InscriptionDAO {
    
    private final Connection connection;

    public InscriptionDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Retourne la connexion utilisée par ce DAO
     * @return la connexion à la base de données
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Crée une nouvelle inscription
     * @param inscription l'inscription à créer
     * @return l'inscription créée avec son ID
     * @throws DAOException si erreur lors de la création
     */
    public Inscription create(Inscription inscription) throws DAOException {
        validateInscription(inscription);
        
        if (isEquipeInscrite(inscription.getTournoi().getId(), inscription.getEquipe().getId())) {
            throw new DAOException("L'équipe est déjà inscrite à ce tournoi");
        }
        
        if (getPlacesRestantes(inscription.getTournoi()) <= 0) {
            throw new DAOException("Le tournoi est complet (16 équipes maximum)");
        }

        String sql = "INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, inscription.getTournoi().getId());
            pst.setInt(2, inscription.getEquipe().getId());
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(4, inscription.getStatut() != null ? inscription.getStatut() : "En attente");
            pst.setInt(5, 0); 

            int result = pst.executeUpdate();
            if (result != 1) {
                throw new DAOException("Erreur lors de la création de l'inscription");
            }

            connection.commit();
            inscription.setDateInscription(LocalDateTime.now());
            return inscription;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la création de l'inscription: " + e.getMessage());
        }
    }

    /**
     * Met à jour une inscription existante
     * @param inscription l'inscription à mettre à jour
     * @return l'inscription mise à jour
     * @throws DAOException si erreur lors de la mise à jour
     */
    public Inscription update(Inscription inscription) throws DAOException {
        validateInscription(inscription);

        String sql = "UPDATE Inscription SET statut = ?, seed = ? WHERE id_tournoi = ? AND id_equipe = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, inscription.getStatut());
            pst.setInt(2, 0); // Seed toujours à 0
            pst.setInt(3, inscription.getTournoi().getId());
            pst.setInt(4, inscription.getEquipe().getId());

            int result = pst.executeUpdate();
            if (result != 1) {
                throw new DAOException("Aucune inscription trouvée ou erreur lors de la mise à jour");
            }

            connection.commit();
            return inscription;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la mise à jour de l'inscription: " + e.getMessage());
        }
    }

    /**
     * Supprime une inscription
     * @param tournoiId ID du tournoi
     * @param equipeId ID de l'équipe
     * @throws DAOException si erreur lors de la suppression
     */
    public void delete(Integer tournoiId, Integer equipeId) throws DAOException {
        String sql = "DELETE FROM Inscription WHERE id_tournoi = ? AND id_equipe = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoiId);
            pst.setInt(2, equipeId);

            int result = pst.executeUpdate();
            if (result != 1) {
                throw new DAOException("Aucune inscription trouvée à supprimer");
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la suppression de l'inscription: " + e.getMessage());
        }
    }

    /**
     * Trouve une inscription par tournoi et équipe
     * @param tournoiId ID du tournoi
     * @param equipeId ID de l'équipe
     * @return Optional contenant l'inscription si trouvée
     * @throws DAOException si erreur lors de la recherche
     */
    public Optional<Inscription> findByTournoiAndEquipe(Integer tournoiId, Integer equipeId) throws DAOException {
        String sql = "SELECT i.*, " +
                    "t.id_tournoi, t.nom as tournoi_nom, " +
                    "e.id_equipe, e.nom as equipe_nom " +
                    "FROM Inscription i " +
                    "JOIN Tournoi t ON i.id_tournoi = t.id_tournoi " +
                    "JOIN Equipe e ON i.id_equipe = e.id_equipe " +
                    "WHERE i.id_tournoi = ? AND i.id_equipe = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoiId);
            pst.setInt(2, equipeId);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche de l'inscription: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    /**
     * Récupère toutes les inscriptions
     * @return Liste de toutes les inscriptions
     * @throws DAOException si erreur lors de la récupération
     */
    public List<Inscription> findAll() throws DAOException {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT i.*, " +
                    "t.id_tournoi, t.nom as tournoi_nom, " +
                    "e.id_equipe, e.nom as equipe_nom " +
                    "FROM Inscription i " +
                    "JOIN Tournoi t ON i.id_tournoi = t.id_tournoi " +
                    "JOIN Equipe e ON i.id_equipe = e.id_equipe " +
                    "ORDER BY i.date_inscription DESC";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                inscriptions.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des inscriptions: " + e.getMessage());
        }
        
        return inscriptions;
    }

    /**
     * Récupère les inscriptions d'un tournoi spécifique
     * @param tournoiId ID du tournoi
     * @return Liste des inscriptions du tournoi
     * @throws DAOException si erreur lors de la récupération
     */
    public List<Inscription> findByTournoi(Integer tournoiId) throws DAOException {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT i.*, " +
                    "t.id_tournoi, t.nom as tournoi_nom, " +
                    "e.id_equipe, e.nom as equipe_nom " +
                    "FROM Inscription i " +
                    "JOIN Tournoi t ON i.id_tournoi = t.id_tournoi " +
                    "JOIN Equipe e ON i.id_equipe = e.id_equipe " +
                    "WHERE i.id_tournoi = ? " +
                    "ORDER BY i.seed ASC";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoiId);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    inscriptions.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des inscriptions du tournoi: " + e.getMessage());
        }
        
        return inscriptions;
    }

    /**
     * Récupère les inscriptions d'une équipe spécifique
     * @param equipeId ID de l'équipe
     * @return Liste des inscriptions de l'équipe
     * @throws DAOException si erreur lors de la récupération
     */
    public List<Inscription> findByEquipe(Integer equipeId) throws DAOException {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT i.*, " +
                    "t.id_tournoi, t.nom as tournoi_nom, " +
                    "e.id_equipe, e.nom as equipe_nom " +
                    "FROM Inscription i " +
                    "JOIN Tournoi t ON i.id_tournoi = t.id_tournoi " +
                    "JOIN Equipe e ON i.id_equipe = e.id_equipe " +
                    "WHERE i.id_equipe = ? " +
                    "ORDER BY i.date_inscription DESC";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, equipeId);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    inscriptions.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des inscriptions de l'équipe: " + e.getMessage());
        }
        
        return inscriptions;
    }

    /**
     * Vérifie si une équipe est déjà inscrite à un tournoi
     * @param tournoiId ID du tournoi
     * @param equipeId ID de l'équipe
     * @return true si l'équipe est déjà inscrite
     * @throws DAOException si erreur lors de la vérification
     */
    public boolean isEquipeInscrite(Integer tournoiId, Integer equipeId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM Inscription WHERE id_tournoi = ? AND id_equipe = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoiId);
            pst.setInt(2, equipeId);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la vérification de l'inscription: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Calcule le nombre de places restantes dans un tournoi (16 max)
     * @param tournoi le tournoi
     * @return nombre de places restantes
     * @throws DAOException si erreur lors du calcul
     */
    public int getPlacesRestantes(Tournoi tournoi) throws DAOException {
        String sql = "SELECT COUNT(*) FROM Inscription WHERE id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int nbInscrits = rs.getInt(1);
                    return 16 - nbInscrits; 
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors du calcul des places restantes: " + e.getMessage());
        }
        
        return 0;
    }

    /**
     * Convertit un ResultSet en objet Inscription
     * @param rs le ResultSet
     * @return l'inscription
     * @throws SQLException si erreur lors de la conversion
     */
    private Inscription mapResultSetToEntity(ResultSet rs) throws SQLException {
        Inscription inscription = new Inscription();
        
        inscription.setStatut(rs.getString("statut"));
        inscription.setSeed(rs.getInt("seed"));
        
        Timestamp dateInscription = rs.getTimestamp("date_inscription");
        if (dateInscription != null) {
            inscription.setDateInscription(dateInscription.toLocalDateTime());
        }
        
        Tournoi tournoi = new Tournoi();
        tournoi.setId(rs.getInt("id_tournoi"));
        tournoi.setNom(rs.getString("tournoi_nom"));
        inscription.setTournoi(tournoi);
        
        Equipe equipe = new Equipe();
        equipe.setId(rs.getInt("id_equipe"));
        equipe.setNom(rs.getString("equipe_nom"));
        inscription.setEquipe(equipe);
        
        return inscription;
    }

    /**
     * Valide les données d'une inscription
     * @param inscription l'inscription à valider
     * @throws DAOException si les données sont invalides
     */
    private void validateInscription(Inscription inscription) throws DAOException {
        if (inscription == null) {
            throw new DAOException("L'inscription ne peut pas être null");
        }
        if (inscription.getTournoi() == null || inscription.getTournoi().getId() == null) {
            throw new DAOException("Le tournoi est obligatoire");
        }
        if (inscription.getEquipe() == null || inscription.getEquipe().getId() == null) {
            throw new DAOException("L'équipe est obligatoire");
        }
        if (inscription.getStatut() == null || inscription.getStatut().trim().isEmpty()) {
            inscription.setStatut("En attente");
        }
    }
}