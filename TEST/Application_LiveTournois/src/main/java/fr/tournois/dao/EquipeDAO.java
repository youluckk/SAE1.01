package fr.tournois.dao;

import fr.tournois.model.Equipe;
import fr.tournois.model.Joueur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeDAO {

    private Connection connection;

    public EquipeDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE
    public void create(Equipe equipe) throws DAOException {
        if (equipe == null) {
            throw new DAOException("L'équipe ne peut pas être null");
        }

        String getIdQuery = "SELECT seq_equipe_id.NEXTVAL FROM DUAL";
        // Ajout des nouvelles colonnes
        String insertQuery = "INSERT INTO Equipe (id_equipe, nom, tag, logo, description, pays, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";

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

            try (PreparedStatement pst = connection.prepareStatement(insertQuery)) {
                pst.setInt(1, newId);
                pst.setString(2, equipe.getNom());
                pst.setString(3, equipe.getTag());
                pst.setString(4, equipe.getLogo());
                pst.setString(5, equipe.getDescription());
                pst.setString(6, equipe.getPays());
                pst.setDate(7, equipe.getDateCreation() != null ? Date.valueOf(equipe.getDateCreation()) : null);

                int result = pst.executeUpdate();

                if (result != 1) {
                    connection.rollback();
                    throw new DAOException("Erreur lors de l'insertion de l'équipe");
                }

                equipe.setId(newId);
                connection.commit();
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la création de l'équipe: " + e.getMessage());
        }
    }

    // UPDATE
    public void update(Equipe equipe) throws DAOException {
        if (equipe == null || equipe.getId() == null) {
            throw new DAOException("L'équipe et son ID ne peuvent pas être null");
        }

        // Mise à jour de toutes les colonnes
        String query = "UPDATE Equipe SET nom = ?, tag = ?, logo = ?, description = ?, pays = ?, date_creation = ? WHERE id_equipe = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, equipe.getNom());
            pst.setString(2, equipe.getTag());
            pst.setString(3, equipe.getLogo());
            pst.setString(4, equipe.getDescription());
            pst.setString(5, equipe.getPays());
            pst.setDate(6, equipe.getDateCreation() != null ? Date.valueOf(equipe.getDateCreation()) : null);
            pst.setInt(7, equipe.getId());

            int result = pst.executeUpdate();

            if (result != 1) {
                connection.rollback();
                throw new DAOException("Aucune équipe trouvée avec cet ID ou erreur lors de la mise à jour");
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new DAOException("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
            throw new DAOException("Erreur lors de la mise à jour de l'équipe: " + e.getMessage());
        }
    }

    // READ - Une équipe
    public Equipe getEquipeParId(int id) throws SQLException {
        String sql = "SELECT * FROM equipe WHERE id_equipe = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construireEquipeDepuisResultSet(rs);
                }
            }
        }
        return null;
    }

    // READ - Toutes les équipes
    public List<Equipe> getToutesLesEquipes() throws SQLException {
        List<Equipe> equipes = new ArrayList<>();
        String sql = "SELECT * FROM equipe";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                equipes.add(construireEquipeDepuisResultSet(rs));
            }
        }
        return equipes;
    }

    public void supprimerEquipe(int id) throws SQLException {
        String sql = "DELETE FROM equipe WHERE id_equipe = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            connection.commit(); // Forcer le commit
        } catch (SQLException e) {
            connection.rollback(); // Annuler en cas d'erreur
            throw e;
        }
    }

    // Utilitaire : construction d'un objet Equipe
    private Equipe construireEquipeDepuisResultSet(ResultSet rs) throws SQLException {
        Equipe equipe = new Equipe();
        equipe.setId(rs.getInt("id_equipe"));
        equipe.setNom(rs.getString("nom"));
        equipe.setTag(rs.getString("tag"));
        equipe.setLogo(rs.getString("logo"));
        equipe.setDescription(rs.getString("description"));
        equipe.setPays(rs.getString("pays"));
        
        Date dateCreation = rs.getDate("date_creation");
        if (dateCreation != null) {
            equipe.setDateCreation(dateCreation.toLocalDate());
        }

        // Récupérer les joueurs associés à cette équipe
        JoueurDAO joueurDAO = new JoueurDAO(connection);
        List<Joueur> joueurs = joueurDAO.getJoueursParEquipeId(equipe.getId());
        equipe.setJoueurs(joueurs);

        return equipe;
    }
}