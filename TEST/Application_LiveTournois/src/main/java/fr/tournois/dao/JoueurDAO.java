package fr.tournois.dao;

import fr.tournois.model.Equipe;
import fr.tournois.model.Joueur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JoueurDAO {

    private Connection connection;

    public JoueurDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Ajoute un joueur dans la base de données
     * 
     * @param joueur Joueur à ajouter
     * @throws SQLException si erreur lors de l'insertion
     */
    // CREATE
    public void ajouterJoueur(Joueur joueur) throws SQLException {
        // Requête pour récupérer le prochain ID de la séquence Oracle
        String getIdQuery = "SELECT SEQ_JOUEUR_ID.NEXTVAL FROM DUAL"; // Mettez le en majuscule ici pour être sûr
        Integer newId = null;
        

        // Récupérer le nouvel ID de la séquence
        try (PreparedStatement getIdPst = connection.prepareStatement(getIdQuery);
                ResultSet rs = getIdPst.executeQuery()) {
            if (rs.next()) {
                newId = rs.getInt(1);
            }
        }

        // Vérifier si un ID a bien été généré
        if (newId == null) {
            throw new SQLException("Impossible de récupérer un nouvel ID pour le joueur.");
        }

        // Assigner l'ID généré à l'objet Joueur Java
        joueur.setId(newId);
        String sql = "INSERT INTO joueur (id_joueur, nom, prenom, pseudo, date_naissance, id_equipe) VALUES (?, ?, ?, ?, ?, ?)";

         
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, joueur.getId());
            stmt.setString(2, joueur.getNom());
            stmt.setString(3, joueur.getPrenom());
            stmt.setString(4, joueur.getPseudo());
            stmt.setDate(5, new java.sql.Date(joueur.getDateNaissance().getTime()));
            if (joueur.getEquipe() != null) {
                stmt.setInt(6, joueur.getEquipe().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();
            connection.commit();

        }
    }

    /**
     * Récupère un joueur par son identifiant
     * 
     * @param id Identifiant du joueur
     * @return Joueur trouvé ou null si absent
     * @throws SQLException si erreur lors de la lecture
     */
    // READ - Un joueur
    public Joueur getJoueurParId(int id) throws SQLException {
        String sql = "SELECT * FROM joueur WHERE id_joueur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construireJoueurDepuisResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère tous les joueurs de la base
     * 
     * @return Liste de tous les joueurs
     * @throws SQLException si erreur lors de la lecture
     */
    // READ - Tous les joueurs
    public List<Joueur> getTousLesJoueurs() throws SQLException {
        List<Joueur> joueurs = new ArrayList<>();
        String sql = "SELECT * FROM joueur";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                joueurs.add(construireJoueurAvecEquipeDepuisResultSet(rs));
            }
        }
        return joueurs;
    }

    /**
     * Met à jour les informations d'un joueur
     * 
     * @param joueur Joueur à mettre à jour
     * @throws SQLException si erreur lors de la mise à jour
     */
    // UPDATE
    public void mettreAJourJoueur(Joueur joueur) throws SQLException {
        String sql = "UPDATE joueur SET nom = ?, prenom = ?, pseudo = ?, date_naissance = ?, id_equipe = ? WHERE id_joueur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, joueur.getNom());
            stmt.setString(2, joueur.getPrenom());
            stmt.setString(3, joueur.getPseudo());
            stmt.setDate(4, new java.sql.Date(joueur.getDateNaissance().getTime()));
            if (joueur.getEquipe() != null) {
                stmt.setInt(5, joueur.getEquipe().getId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setInt(6, joueur.getId());

            stmt.executeUpdate();
            connection.commit();
        }
    }

    /**
     * Supprime un joueur de la base de données
     * 
     * @param id Identifiant du joueur à supprimer
     * @throws SQLException si erreur lors de la suppression
     */
    // DELETE
    public void supprimerJoueur(int id) throws SQLException {
        String sql = "DELETE FROM joueur WHERE id_joueur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            connection.commit();
        }
    }

    // Utilitaire pour construire un objet Joueur depuis un ResultSet
    private Joueur construireJoueurDepuisResultSet(ResultSet rs) throws SQLException {
        Joueur joueur = new Joueur();
        joueur.setId(rs.getInt("id_joueur"));
        joueur.setNom(rs.getString("nom"));
        joueur.setPrenom(rs.getString("prenom"));
        joueur.setPseudo(rs.getString("pseudo"));
        joueur.setDateNaissance(rs.getDate("date_naissance"));

        int equipeId = rs.getInt("id_equipe");
        if (!rs.wasNull()) {
            // Attention : éviter la récursion infinie
            // Ne pas récupérer l'équipe complète avec ses joueurs ici
            Equipe equipe = new Equipe();
            equipe.setId(equipeId);
            joueur.setEquipe(equipe);
        }

        return joueur;
    }
    private Joueur construireJoueurAvecEquipeDepuisResultSet(ResultSet rs) throws SQLException {
        Joueur joueur = new Joueur();
        joueur.setId(rs.getInt("id_joueur"));
        joueur.setNom(rs.getString("nom"));
        joueur.setPrenom(rs.getString("prenom"));
        joueur.setPseudo(rs.getString("pseudo"));
        joueur.setDateNaissance(rs.getDate("date_naissance"));

        int equipeId = rs.getInt("id_equipe");
        if (!rs.wasNull()) {
            Equipe equipe = new Equipe();
            equipe.setId(equipeId);
            equipe.setNom(rs.getString("nom")); // ← AJOUT CRUCIAL !
            joueur.setEquipe(equipe);
        }
        return joueur;
    }

    /**
     * Récupère les joueurs d'une équipe par l'identifiant de l'équipe
     * 
     * @param equipeId Identifiant de l'équipe
     * @return Liste des joueurs de l'équipe
     * @throws SQLException si erreur lors de la lecture
     */
    // Méthode pour récupérer les joueurs d'une équipe
    public List<Joueur> getJoueursParEquipeId(int equipeId) throws SQLException {
        List<Joueur> joueurs = new ArrayList<>();
        String sql = "SELECT * FROM joueur WHERE id_equipe = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, equipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Ici on construit un joueur simplifié pour éviter la récursion
                    Joueur joueur = new Joueur();
                    joueur.setId(rs.getInt("id_joueur"));
                    joueur.setNom(rs.getString("nom"));
                    joueur.setPrenom(rs.getString("prenom"));
                    joueur.setPseudo(rs.getString("pseudo"));
                    joueur.setDateNaissance(rs.getDate("date_naissance"));
                    // Ne pas définir l'équipe ici pour éviter la récursion
                    joueurs.add(joueur);
                }
            }
        }
        return joueurs;
    }
}