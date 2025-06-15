package fr.tournois.dao;

import fr.tournois.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Classe d'accès aux données pour les tournois.
 * Gère les opérations CRUD et les relations avec les autres entités (jeux, équipes, staff).
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class TournoiDAO {

    private final Connection connection;

    /**
     * Constructeur
     * @param connection connexion à la base de données à utiliser
     */
    public TournoiDAO(Connection connection) {
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
     * Recherche un tournoi par son identifiant
     * @param id Identifiant du tournoi
     * @return Optional contenant le tournoi si trouvé
     * @throws DAOException si erreur de base de données
     */
    public Optional<Tournoi> findById(Integer id) throws DAOException {
        try {
            String sql = "SELECT t.*, " +
                        "j.id_jeu AS jeu_id, j.nom AS jeu_nom, j.editeur AS jeu_editeur, j.annee_sortie AS jeu_annee_sortie, j.genre AS jeu_genre " +
                        "FROM Tournoi t " +
                        "LEFT JOIN Jeu j ON t.id_jeu = j.id_jeu " +
                        "WHERE t.id_tournoi = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Tournoi tournoi = mapResultSetToEntity(rs);
                        loadAffectations(tournoi);
                        loadInscriptions(tournoi);
                        return Optional.of(tournoi);
                    } else {
                        throw new DAOException("Le tournoi n'existe pas");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du tournoi", e);
        }
    }

    /**
     * Convertit une ligne de résultat SQL en objet Tournoi
     * @param rs Résultat de la requête SQL
     * @return Objet Tournoi avec les données du ResultSet
     * @throws SQLException si erreur lors de la lecture des données
     */
    private Tournoi mapResultSetToEntity(ResultSet rs) throws SQLException {
        Tournoi tournoi = new Tournoi();
        tournoi.setId(rs.getInt("id_tournoi"));
        tournoi.setNom(rs.getString("nom"));
        tournoi.setDateDebut(rs.getDate("date_debut").toLocalDate());  // Oracle DATE type
        tournoi.setDateFin(rs.getDate("date_fin").toLocalDate());  // Oracle DATE type
        tournoi.setLieu(rs.getString("lieu"));
        tournoi.setFormat(rs.getString("format"));
        tournoi.setNbEquipesMax(rs.getInt("nb_equipes_max"));
        tournoi.setStatut(rs.getString("statut"));
        tournoi.setPrixPool(rs.getDouble("prix_pool"));

        // Mapping du jeu si présent
        try {
            int idJeu = rs.getInt("jeu_id");
            if (!rs.wasNull()) {
                Jeu jeu = new Jeu();
                jeu.setId(idJeu);
                jeu.setNom(rs.getString("jeu_nom"));
                jeu.setEditeur(rs.getString("jeu_editeur"));
                jeu.setAnneeSortie(rs.getInt("jeu_annee_sortie"));
                jeu.setGenre(rs.getString("jeu_genre"));
                tournoi.setJeu(jeu);
            }
        } catch (SQLException ignored) {}

        return tournoi;
    }

    /**
     * Recherche tous les tournois dans la base de données
     * @return Liste des tournois trouvés
     * @throws DAOException si erreur lors de la lecture
     */
    public List<Tournoi> findAll() throws DAOException {
        List<Tournoi> tournois = new ArrayList<>();
        String sql = "SELECT t.*, " +
                    "j.id_jeu AS jeu_id, " +
                    "j.nom AS jeu_nom, " +
                    "j.editeur AS jeu_editeur, " +
                    "j.annee_sortie AS jeu_annee_sortie, " +
                    "j.genre AS jeu_genre, " +
                    "j.description AS jeu_description " +
                    "FROM Tournoi t " +
                    "LEFT JOIN Jeu j ON t.id_jeu = j.id_jeu";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Tournoi tournoi = mapResultSetToEntity(rs);
                loadInscriptions(tournoi);
                tournois.add(tournoi);
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture des tournois: " + e.getMessage());
        }
        
        return tournois;
    }

    /**
     * Crée un nouveau tournoi dans la base de données
     * @param tournoi Tournoi à créer
     * @return Tournoi créé avec son ID généré
     * @throws DAOException si erreur lors de la création ou données invalides
     */
    public Tournoi create(Tournoi tournoi) throws DAOException {
        validateTournoi(tournoi);
        
        try {
            // Obtenir d'abord le prochain ID de la séquence
            int newId;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT seq_tournoi_id.NEXTVAL FROM DUAL")) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                } else {
                    throw new DAOException("Impossible d'obtenir un nouvel ID de la séquence");
                }
            }

            String sql = "INSERT INTO Tournoi (id_tournoi, nom, date_debut, date_fin, lieu, format, nb_equipes_max, statut, prix_pool, id_jeu) " +
                        "VALUES (?, ?, TO_DATE(?, 'DD/MM/YYYY'), TO_DATE(?, 'DD/MM/YYYY'), ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, newId);
                pst.setString(2, tournoi.getNom());
                pst.setDate(3, Date.valueOf(tournoi.getDateDebut()));
                pst.setDate(4, Date.valueOf(tournoi.getDateFin()));
                pst.setString(5, tournoi.getLieu());
                pst.setString(6, tournoi.getFormat());
                pst.setInt(7, tournoi.getNbEquipesMax());
                pst.setString(8, normalizeStatut(tournoi.getStatut()));
                pst.setDouble(9, tournoi.getPrixPool());
                if (tournoi.getJeu() != null) {
                    pst.setInt(10, tournoi.getJeu().getId());
                } else {
                    pst.setNull(10, Types.INTEGER);
                }

                int affectedRows = pst.executeUpdate();

                if (affectedRows == 0) {
                    throw new DAOException("La création du tournoi a échoué");
                }

                connection.commit();

                tournoi.setId(newId);
                return tournoi;
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création du tournoi: " + e.getMessage());
        }
    }

    /**
     * Met à jour un tournoi existant dans la base de données
     * @param tournoi Tournoi à mettre à jour
     * @return Tournoi mis à jour
     * @throws DAOException si erreur lors de la mise à jour ou données invalides
     */
    public Tournoi update(Tournoi tournoi) throws DAOException {
        validateTournoi(tournoi);
        
        String normalizedStatus = normalizeStatut(tournoi.getStatut());
        tournoi.setStatut(normalizedStatus);
        
        String sql = "UPDATE Tournoi SET " +
                    "nom = ?, " +
                    "date_debut = ?, " +
                    "date_fin = ?, " +
                    "lieu = ?, " +
                    "format = ?, " +
                    "nb_equipes_max = ?, " +
                    "statut = ?, " +
                    "prix_pool = ?, " +
                    "id_jeu = ? " +
                    "WHERE id_tournoi = ?";

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, tournoi.getNom());
            pst.setDate(2, java.sql.Date.valueOf(tournoi.getDateDebut()));
            pst.setDate(3, java.sql.Date.valueOf(tournoi.getDateFin()));
            pst.setString(4, tournoi.getLieu());
            pst.setString(5, tournoi.getFormat());
            pst.setInt(6, tournoi.getNbEquipesMax());
            pst.setString(7, normalizedStatus);
            pst.setDouble(8, tournoi.getPrixPool());
            pst.setInt(9, tournoi.getJeu().getId());
            pst.setInt(10, tournoi.getId());

            if (pst.executeUpdate() == 0) {
                throw new DAOException("La mise à jour du tournoi a échoué, aucune ligne modifiée.");
            }

            // Recharger le tournoi depuis la base pour avoir les données à jour
            Optional<Tournoi> updated = findById(tournoi.getId());
            if (updated.isPresent()) {
                connection.commit();
                return updated.get();
            } else {
                throw new DAOException("Impossible de recharger le tournoi après la mise à jour.");
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour du tournoi: " + e.getMessage());
        }
    }

    /**
     * Supprime un tournoi de la base de données
     * @param tournoi Tournoi à supprimer
     * @throws DAOException si erreur lors de la suppression ou tournoi inexistant
     */
    public void delete(Tournoi tournoi) throws DAOException {
        try {
            Integer id = tournoi.getId();

            // Vérifier si le tournoi existe avant de le supprimer
            if (!findById(id).isPresent()) {
                throw new DAOException("Le tournoi n'existe pas");
            }

            String sql = "DELETE FROM Tournoi WHERE id_tournoi = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted == 0) {
                    throw new DAOException("La suppression du tournoi a échoué");
                }
                connection.commit();
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression du tournoi", e);
        }
    }

    /**
     * Recherche les tournois en cours
     * @return Liste des tournois en cours
     * @throws DAOException si erreur lors de la recherche
     */
    public List<Tournoi> findTournoisEnCours() throws DAOException {
        List<Tournoi> tournois = new ArrayList<>();
        String sql = "SELECT t.*, " +
                    "j.id_jeu as j_id_jeu, " +
                    "j.nom as j_nom, " +
                    "j.editeur as j_editeur, " +
                    "j.annee_sortie as j_annee_sortie, " +
                    "j.genre as j_genre, " +
                    "j.description as j_description " +
                    "FROM Tournoi t " +
                    "LEFT JOIN Jeu j ON t.id_jeu = j.id_jeu " +
                    "WHERE t.statut = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, "En cours");
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Tournoi tournoi = mapResultSetToEntity(rs);
                    loadInscriptions(tournoi);
                    tournois.add(tournoi);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des tournois en cours: " + e.getMessage());
        }
        
        return tournois;
    }

    /**
     * Recherche les tournois à venir
     * @return Liste des tournois à venir
     * @throws DAOException si erreur lors de la recherche
     */
    public List<Tournoi> findTournoisAVenir() throws DAOException {
        List<Tournoi> tournois = new ArrayList<>();
        String sql = "SELECT t.*, j.* FROM Tournoi t " +
                    "LEFT JOIN Jeu j ON t.id_jeu = j.id_jeu " +
                    "WHERE t.date_debut > CURRENT_DATE";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Tournoi tournoi = mapResultSetToEntity(rs);
                loadInscriptions(tournoi);
                tournois.add(tournoi);
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des tournois à venir: " + e.getMessage());
        }
        
        return tournois;
    }

    /**
     * Récupère les affectations d'un tournoi
     * @param tournoi Tournoi concerné
     * @return Liste des affectations
     * @throws DAOException si erreur lors de la récupération
     */
    public List<Affectation> getAffectations(Tournoi tournoi) throws DAOException {
        AffectationDAO affectationDAO = new AffectationDAO(connection);
        return affectationDAO.findByTournoi(tournoi);
    }

    /**
     * Ajoute une nouvelle affectation de staff à un tournoi
     * @param tournoiId ID du tournoi
     * @param staff Staff à affecter
     * @param roleSpecifique Rôle spécifique pour cette affectation
     * @param dateDebut Date de début de l'affectation
     * @param dateFin Date de fin de l'affectation
     * @throws DAOException si erreur lors de l'ajout ou tournoi non trouvé
     */
    public void addAffectation(Integer tournoiId, Staff staff, String roleSpecifique, LocalDateTime dateDebut, LocalDateTime dateFin) throws DAOException {
        Optional<Tournoi> optTournoi = findById(tournoiId);
        if (!optTournoi.isPresent()) {
            throw new DAOException("Le tournoi n'existe pas");
        }

        Tournoi tournoi = optTournoi.get();
        String sql = "INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES (?, ?, ?, TO_DATE(?, 'DD/MM/YYYY HH24:MI:SS'), TO_DATE(?, 'DD/MM/YYYY HH24:MI:SS'))";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tournoiId);
            stmt.setInt(2, staff.getId());
            stmt.setString(3, roleSpecifique);
            stmt.setString(4, dateDebut.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            stmt.setString(5, dateFin.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            stmt.executeUpdate();

            connection.commit();

            // Créer l'affectation et l'ajouter au tournoi
            Affectation nouvelleAffectation = new Affectation();
            nouvelleAffectation.setTournoi(tournoi);
            nouvelleAffectation.setStaff(staff);
            nouvelleAffectation.setRoleSpecifique(roleSpecifique);
            nouvelleAffectation.setDateDebut(dateDebut);
            nouvelleAffectation.setDateFin(dateFin);

            // Ajouter l'affectation au tournoi
            tournoi.ajouterAffectation(nouvelleAffectation);

        } catch (SQLException e) {
            throw new DAOException("Erreur lors de l'ajout de l'affectation", e);
        }
    }

    /**
     * Valide les données d'un tournoi avant création/mise à jour
     * @param tournoi le tournoi à valider
     * @throws DAOException si les données sont invalides
     */
    private void validateTournoi(Tournoi tournoi) throws DAOException {
        if (tournoi.getNom() == null || tournoi.getNom().trim().isEmpty()) {
            throw new DAOException("Le nom du tournoi est obligatoire");
        }
        if (tournoi.getFormat() == null || tournoi.getFormat().trim().isEmpty()) {
            throw new DAOException("Le format du tournoi est obligatoire");
        }
        if (tournoi.getDateDebut() == null) {
            throw new DAOException("La date de début du tournoi est obligatoire");
        }
        if (tournoi.getDateFin() == null) {
            throw new DAOException("La date de fin du tournoi est obligatoire");
        }
        if (tournoi.getLieu() == null || tournoi.getLieu().trim().isEmpty()) {
            throw new DAOException("Le lieu du tournoi est obligatoire");
        }
        if (tournoi.getNbEquipesMax() <= 0) {
            throw new DAOException("Le nombre d'équipes maximum doit être supérieur à 0");
        }
        if (tournoi.getPrixPool() < 0) {
            throw new DAOException("Le prix du pool ne peut pas être négatif");
        }
    }

    /**
     * Charge les inscriptions d'un tournoi
     * 
     * Cette méthode récupère les inscriptions d'un tournoi et les ajoute à l'objet Tournoi.
     * 
     * @param tournoi le tournoi pour lequel charger les inscriptions
     * @throws DAOException si erreur lors de la récupération des inscriptions
     */
    private void loadInscriptions(Tournoi tournoi) throws DAOException {
        String sql = "SELECT e.* FROM Equipe e " +
                    "JOIN Inscription i ON e.id_equipe = i.id_equipe " +
                    "WHERE i.id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                List<Equipe> equipes = new ArrayList<>();
                while (rs.next()) {
                    Equipe equipe = new Equipe();
                    equipe.setId(rs.getInt("id_equipe"));
                    equipe.setNom(rs.getString("nom"));
                    equipe.setDateCreation(rs.getDate("date_creation").toLocalDate());  // Oracle DATE type
                    equipes.add(equipe);
                }
                tournoi.setEquipes(equipes);
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des équipes du tournoi: " + e.getMessage());
        }
    }

    /**
     * Récupère la liste des équipes inscrites à un tournoi
     * @param tournoi Tournoi dont on veut les équipes
     * @return Liste des équipes inscrites au tournoi
     * @throws DAOException si erreur lors de la récupération
     */
    public List<Equipe> findEquipesByTournoi(Tournoi tournoi) throws DAOException {
        String sql = "SELECT e.* FROM Equipe e " +
                    "JOIN Inscription i ON e.id_equipe = i.id_equipe " +
                    "WHERE i.id_tournoi = ?";
        
        List<Equipe> equipes = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Equipe equipe = new Equipe();
                    equipe.setId(rs.getInt("id_equipe"));
                    equipe.setNom(rs.getString("nom"));
                    equipe.setDateCreation(rs.getDate("date_creation").toLocalDate());  // Oracle DATE type
                    equipes.add(equipe);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération des équipes du tournoi: " + e.getMessage());
        }
        return equipes;
    }

    /**
     * Inscrit une équipe à un tournoi
     * @param tournoi Tournoi auquel inscrire l'équipe
     * @param equipe Équipe à inscrire
     * @throws DAOException si le tournoi est complet ou en cas d'erreur
     */
    public void inscrireEquipe(Tournoi tournoi, Equipe equipe) throws DAOException {
        // Vérifier s'il reste des places
        if (getPlacesRestantes(tournoi) <= 0) {
            throw new DAOException("Le tournoi est complet");
        }

        String sql = "INSERT INTO Inscription (id_tournoi, id_equipe) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            pst.setInt(2, equipe.getId());
            pst.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de l'inscription de l'équipe: " + e.getMessage());
        }
    }

    /**
     * Désinscrit une équipe d'un tournoi
     * @param tournoi Tournoi duquel désinscrire l'équipe
     * @param equipe Équipe à désinscrire
     * @throws DAOException si erreur lors de la désinscription
     */
    public void desinscrireEquipe(Tournoi tournoi, Equipe equipe) throws DAOException {
        String sql = "DELETE FROM Inscription WHERE id_tournoi = ? AND id_equipe = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            pst.setInt(2, equipe.getId());
            pst.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la désinscription de l'équipe: " + e.getMessage());
        }
    }

    /**
     * Charge les affectations d'un tournoi depuis la base de données
     * @param tournoi le tournoi pour lequel charger les affectations
     * @throws DAOException si erreur lors de la récupération des affectations
     */
    private void loadAffectations(Tournoi tournoi) throws DAOException {
        String sql = "SELECT a.*, s.* FROM Affectation a " +
                    "JOIN Staff s ON a.id_staff = s.id_staff " +
                    "WHERE a.id_tournoi = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, tournoi.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Staff staff = new Staff();
                    staff.setId(rs.getInt("id_staff"));
                    staff.setNom(rs.getString("nom"));
                    staff.setPrenom(rs.getString("prenom"));
                    staff.setEmail(rs.getString("email"));
                    staff.setFonction(rs.getString("fonction"));
                    staff.setTelephone(rs.getString("telephone"));

                    Affectation affectation = new Affectation();
                    affectation.setStaff(staff);
                    affectation.setTournoi(tournoi);
                    affectation.setRoleSpecifique(rs.getString("role_specifique"));
                    affectation.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
                    affectation.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());

                    tournoi.ajouterAffectation(affectation);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors du chargement des affectations", e);
        }
    }

    /**
     * Normalise le statut d'un tournoi
     * @param statut le statut à normaliser
     * @return le statut normalisé
     */
    private String normalizeStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            return "En préparation";
        }
        String statutNormalise = statut.trim().toLowerCase();
        switch (statutNormalise) {
            case "en préparation":
            case "en cours":
            case "terminé":
            case "annulé":
                return statutNormalise;
            default:
                return "En préparation";
        }
    }

    /**
     * Calcule le nombre de places restantes dans un tournoi
     * @param tournoi Tournoi dont on veut connaître les places restantes
     * @return Nombre de places encore disponibles
     * @throws DAOException si erreur lors du calcul
     */
    public int getPlacesRestantes(Tournoi tournoi) throws DAOException {
        String sql = "SELECT COUNT(*) FROM Inscription WHERE id_tournoi = ?";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int nbInscrits = rs.getInt(1);
                    return tournoi.getNbEquipesMax() - nbInscrits;
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors du calcul des places restantes: " + e.getMessage());
        }
    }
}
