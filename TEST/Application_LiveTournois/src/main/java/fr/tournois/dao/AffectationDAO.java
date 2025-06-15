package fr.tournois.dao;

import fr.tournois.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AffectationDAO {
    private final Connection connection;

    private static final String FIND_BY_TOURNOI_AND_STAFF_QUERY = 
    "SELECT a.*, t.*, s.* FROM Affectation a " +
    "JOIN Tournoi t ON a.id_tournoi = t.id_tournoi " +
    "JOIN Staff s ON a.id_staff = s.id_staff " +
    "WHERE a.id_tournoi = ? AND a.id_staff = ?";
    public AffectationDAO(Connection connection) {
        this.connection = connection;
    }

    public Affectation create(Affectation affectation) throws DAOException {
        validateAffectation(affectation);
        
        String sql = "INSERT INTO Affectation (id_affectation, id_staff, id_tournoi, role_specifique, date_debut, date_fin) " +
                    "VALUES (seq_affectation_id.NEXTVAL, ?, ?, ?, TO_DATE(?, 'DD/MM/YYYY HH24:MI:SS'), TO_DATE(?, 'DD/MM/YYYY HH24:MI:SS'))";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, affectation.getStaff().getId());
            pst.setInt(2, affectation.getTournoi().getId());
            pst.setString(3, affectation.getRoleSpecifique());
            pst.setString(4, affectation.getDateDebut() != null ? affectation.getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null);  // Format Oracle
            pst.setString(5, affectation.getDateFin() != null ? affectation.getDateFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null);  // Format Oracle

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new DAOException("La création de l'affectation a échoué");
            }

            connection.commit();

            return findByStaffAndTournoi(affectation.getStaff().getId(), affectation.getTournoi().getId())
                .orElseThrow(() -> new DAOException("Impossible de recharger l'affectation après création"));
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création de l'affectation: " + e.getMessage());
        }
    }

    public Optional<Affectation> findByStaffAndTournoi(Integer staffId, Integer tournoiId) throws DAOException {
        String sql = "SELECT a.*, " +
                    "s.nom as staff_nom, s.prenom as staff_prenom, s.email as staff_email, s.fonction as staff_fonction, " +
                    "t.nom as tournoi_nom, t.format as tournoi_format, t.statut as tournoi_statut " +
                    "FROM Affectation a " +
                    "JOIN Staff s ON a.id_staff = s.id_staff " +
                    "JOIN Tournoi t ON a.id_tournoi = t.id_tournoi " +
                    "WHERE a.id_staff = ? AND a.id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, staffId);
            pst.setInt(2, tournoiId);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la lecture de l'affectation: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Affectation> findAll() throws DAOException {
        List<Affectation> affectations = new ArrayList<>();
        String sql = "SELECT a.*, " +
                    "s.nom as staff_nom, s.prenom as staff_prenom, s.email as staff_email, s.fonction as staff_fonction, " +
                    "t.nom as tournoi_nom, t.format as tournoi_format, t.statut as tournoi_statut " +
                    "FROM Affectation a " +
                    "JOIN Staff s ON a.id_staff = s.id_staff " +
                    "JOIN Tournoi t ON a.id_tournoi = t.id_tournoi";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                affectations.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération de la liste des affectations: " + e.getMessage());
        }
        
        return affectations;
    }

    public Affectation update(Affectation affectation) throws DAOException {
        validateAffectation(affectation);
        
        String sql = "UPDATE Affectation SET role_specifique = ?, date_debut = TO_DATE(?, 'DD/MM/YYYY HH24:MI:SS'), date_fin = TO_DATE(?, 'DD/MM/YYYY HH24:MI:SS') WHERE id_staff = ? AND id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, affectation.getRoleSpecifique());
            pst.setString(2, affectation.getDateDebut() != null ? affectation.getDateDebut().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null);  // Format Oracle
            pst.setString(3, affectation.getDateFin() != null ? affectation.getDateFin().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null);  // Format Oracle
            pst.setInt(4, affectation.getStaff().getId());
            pst.setInt(5, affectation.getTournoi().getId());

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new DAOException("La mise à jour de l'affectation a échoué");
            }

            connection.commit();

            return findByStaffAndTournoi(affectation.getStaff().getId(), affectation.getTournoi().getId())
                .orElseThrow(() -> new DAOException("Impossible de recharger l'affectation après mise à jour"));
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour de l'affectation: " + e.getMessage());
        }
    }

    public boolean delete(Integer staffId, Integer tournoiId) throws DAOException {
        String sql = "DELETE FROM Affectation WHERE id_staff = ? AND id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, staffId);
            pst.setInt(2, tournoiId);
            
            int affectedRows = pst.executeUpdate();

            connection.commit();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression de l'affectation: " + e.getMessage());
        }
    }

    private void validateAffectation(Affectation affectation) throws DAOException {
        if (affectation == null) {
            throw new DAOException("L'affectation ne peut pas être null");
        }
        if (affectation.getStaff() == null) {
            throw new DAOException("Le staff est obligatoire");
        }
        if (affectation.getTournoi() == null) {
            throw new DAOException("Le tournoi est obligatoire");
        }
        if (affectation.getRoleSpecifique() == null || affectation.getRoleSpecifique().trim().isEmpty()) {
            throw new DAOException("Le rôle spécifique est obligatoire");
        }
    }

    private Affectation mapResultSetToEntity(ResultSet rs) throws SQLException {
        Affectation affectation = new Affectation();

        Staff staff = new Staff();
        staff.setId(rs.getInt("id_staff"));
        staff.setNom(rs.getString("staff_nom"));
        staff.setPrenom(rs.getString("staff_prenom"));
        staff.setEmail(rs.getString("staff_email"));
        staff.setFonction(rs.getString("staff_fonction"));
        affectation.setStaff(staff);

        Tournoi tournoi = new Tournoi();
        tournoi.setId(rs.getInt("id_tournoi"));
        tournoi.setNom(rs.getString("tournoi_nom"));
        tournoi.setFormat(rs.getString("tournoi_format"));
        tournoi.setStatut(rs.getString("tournoi_statut"));
        affectation.setTournoi(tournoi);

        affectation.setRoleSpecifique(rs.getString("role_specifique"));
        
        Timestamp dateDebut = rs.getTimestamp("date_debut");
        if (dateDebut != null) {
            affectation.setDateDebut(dateDebut.toLocalDateTime());
        }
        
        Timestamp dateFin = rs.getTimestamp("date_fin");
        if (dateFin != null) {
            affectation.setDateFin(dateFin.toLocalDateTime());
        }

        return affectation;
    }

    /**
     * Recherche toutes les affectations d'un membre du staff
     * @param staff Le membre du staff
     * @return La liste des affectations
     * @throws DAOException si une erreur survient
     */
    public List<Affectation> findByStaff(Staff staff) throws DAOException {
        String sql = "SELECT a.*, " +
                    "s.nom as staff_nom, s.prenom as staff_prenom, s.email as staff_email, s.fonction as staff_fonction, " +
                    "t.nom as tournoi_nom, t.format as tournoi_format, t.statut as tournoi_statut " +
                    "FROM Affectation a " +
                    "JOIN Staff s ON a.id_staff = s.id_staff " +
                    "JOIN Tournoi t ON a.id_tournoi = t.id_tournoi " +
                    "WHERE a.id_staff = ?";

        List<Affectation> affectations = new ArrayList<>();
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, staff.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    affectations.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des affectations par staff", e);
        }
        
        return affectations;
    }

    /**
     * Recherche toutes les affectations d'un tournoi
     * @param tournoi Le tournoi
     * @return La liste des affectations
     * @throws DAOException si une erreur survient
     */
    public List<Affectation> findByTournoi(Tournoi tournoi) throws DAOException {
        String sql = "SELECT a.*, " +
                    "s.nom as staff_nom, s.prenom as staff_prenom, s.email as staff_email, s.fonction as staff_fonction, " +
                    "t.nom as tournoi_nom, t.format as tournoi_format, t.statut as tournoi_statut " +
                    "FROM Affectation a " +
                    "JOIN Staff s ON a.id_staff = s.id_staff " +
                    "JOIN Tournoi t ON a.id_tournoi = t.id_tournoi " +
                    "WHERE a.id_tournoi = ?";

        List<Affectation> affectations = new ArrayList<>();
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    affectations.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des affectations par tournoi", e);
        }
        
        return affectations;
    }

    public List<Affectation> findByRoleSpecifique(String roleSpecifique) throws DAOException {
        if (roleSpecifique == null || roleSpecifique.trim().isEmpty()) {
            throw new DAOException("Le rôle spécifique ne peut pas être vide");
        }

        List<Affectation> affectations = new ArrayList<>();
        String sql = "SELECT a.*, " +
                    "s.nom as staff_nom, s.prenom as staff_prenom, s.email as staff_email, s.fonction as staff_fonction, " +
                    "t.nom as tournoi_nom, t.format as tournoi_format, t.statut as tournoi_statut " +
                    "FROM Affectation a " +
                    "JOIN Staff s ON a.id_staff = s.id_staff " +
                    "JOIN Tournoi t ON a.id_tournoi = t.id_tournoi " +
                    "WHERE a.role_specifique = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, roleSpecifique);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    affectations.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche des affectations par rôle: " + e.getMessage());
        }
        
        return affectations;
    }

    /**
     * Recherche une affectation par tournoi et staff
     * @param tournoi Le tournoi
     * @param staff Le staff
     * @return L'affectation trouvée ou un Optional vide
     * @throws DAOException si une erreur survient
     */
    public Optional<Affectation> findByTournoiAndStaff(Tournoi tournoi, Staff staff) throws DAOException {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_TOURNOI_AND_STAFF_QUERY)) {
            stmt.setInt(1, tournoi.getId());
            stmt.setInt(2, staff.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche d'une affectation par tournoi et staff", e);
        }
    }
}
