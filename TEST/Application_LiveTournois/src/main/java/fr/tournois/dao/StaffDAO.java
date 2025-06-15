package fr.tournois.dao;

import fr.tournois.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffDAO {
    private final Connection connection;
    private final UtilisateurDAO utilisateurDAO;

    private static final String FIND_BY_TELEPHONE_QUERY = 
        "SELECT * FROM Staff WHERE telephone = ?";
    public StaffDAO(Connection connection) {
        this.connection = connection;
        this.utilisateurDAO = new UtilisateurDAO(connection);
    }

    public Staff create(Staff staff) throws DAOException {
        validateStaff(staff);
        
        try {
            // Obtenir d'abord le prochain ID de la séquence
            int newId;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT seq_staff_id.NEXTVAL FROM DUAL")) {
                if (rs.next()) {
                    newId = rs.getInt(1);
                } else {
                    throw new DAOException("Impossible d'obtenir un nouvel ID de la séquence");
                }
            }

            String sql = "INSERT INTO Staff (id_staff, nom, prenom, email, fonction, telephone, id_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
            try (PreparedStatement pst = connection.prepareStatement(sql)) {
                pst.setInt(1, newId);
                pst.setString(2, staff.getNom());
                pst.setString(3, staff.getPrenom());
                pst.setString(4, staff.getEmail());
                pst.setString(5, staff.getFonction());
                pst.setString(6, staff.getTelephone());
                if (staff.getUtilisateur() != null) {
                    pst.setInt(7, staff.getUtilisateur().getId());
                } else {
                    pst.setNull(7, Types.INTEGER);
                }

                int affectedRows = pst.executeUpdate();

                if (affectedRows == 0) {
                    throw new DAOException("La création du staff a échoué");
                }

                connection.commit();

                staff.setId(newId);
            }
            return staff;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la création du staff: " + e.getMessage());
        }
    }

    public Optional<Staff> findById(Integer id) throws DAOException {
        try {
            String sql = "SELECT * FROM Staff WHERE id_staff = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToEntity(rs));
                    } else {
                        throw new DAOException("Le staff n'existe pas");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du staff", e);
        }
    }

    public List<Staff> findAll() throws DAOException {
        List<Staff> staffs = new ArrayList<>();
        String sql = "SELECT * FROM Staff";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                staffs.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération de la liste du staff: " + e.getMessage());
        }
        
        return staffs;
    }

    public Staff update(Staff staff) throws DAOException {
        validateStaff(staff);
        
        String sql = "UPDATE Staff SET nom = ?, prenom = ?, email = ?, fonction = ?, telephone = ?, id_utilisateur = ? " +
                    "WHERE id_staff = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, staff.getNom());
            pst.setString(2, staff.getPrenom());
            pst.setString(3, staff.getEmail());
            pst.setString(4, staff.getFonction());
            pst.setString(5, staff.getTelephone());
            if (staff.getUtilisateur() != null) {
                pst.setInt(6, staff.getUtilisateur().getId());
            } else {
                pst.setNull(6, Types.INTEGER);
            }
            pst.setInt(7, staff.getId());

            int affectedRows = pst.executeUpdate();

            if (affectedRows == 0) {
                throw new DAOException("La mise à jour du staff a échoué");
            }

            connection.commit();

            return staff;
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la mise à jour du staff: " + e.getMessage());
        }
    }

    public void delete(Object entity) throws DAOException {
        try {
            Integer id;
            if (entity instanceof Integer) {
                id = (Integer) entity;
            } else if (entity instanceof Staff) {
                id = ((Staff) entity).getId();
            } else {
                throw new DAOException("Type d'entité non supporté pour la suppression");
            }

            // Vérifier si le staff existe avant de le supprimer
            if (!findById(id).isPresent()) {
                throw new DAOException("Le staff n'existe pas");
            }

            String sql = "DELETE FROM Staff WHERE id_staff = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted == 0) {
                    throw new DAOException("La suppression du staff a échoué");
                }
                connection.commit();
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression du staff", e);
        }
    }

    private void validateStaff(Staff staff) throws DAOException {
        if (staff == null) {
            throw new DAOException("Le staff ne peut pas être null");
        }
        if (staff.getNom() == null || staff.getNom().trim().isEmpty()) {
            throw new DAOException("Le nom du staff est obligatoire");
        }
        if (staff.getPrenom() == null || staff.getPrenom().trim().isEmpty()) {
            throw new DAOException("Le prénom du staff est obligatoire");
        }
        if (staff.getEmail() == null || staff.getEmail().trim().isEmpty()) {
            throw new DAOException("L'email du staff est obligatoire");
        }
        if (staff.getFonction() == null || staff.getFonction().trim().isEmpty()) {
            throw new DAOException("La fonction du staff est obligatoire");
        }
    }

    protected Staff mapResultSetToEntity(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setId(rs.getInt("id_staff"));
        staff.setNom(rs.getString("nom"));
        staff.setPrenom(rs.getString("prenom"));
        staff.setEmail(rs.getString("email"));
        staff.setFonction(rs.getString("fonction"));
        staff.setTelephone(rs.getString("telephone"));
        
        int idUtilisateur = rs.getInt("id_utilisateur");
        if (!rs.wasNull()) {
            try {
                Optional<Utilisateur> utilisateur = utilisateurDAO.findById(idUtilisateur);
                utilisateur.ifPresent(staff::setUtilisateur);
            } catch (DAOException e) {
                // Log l'erreur mais continue
                System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
            }
        }
        
        return staff;
    }

    public List<Staff> findByFonction(String fonction) throws DAOException {
        List<Staff> staffs = new ArrayList<>();
        String sql = "SELECT * FROM Staff WHERE fonction = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, fonction);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    staffs.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du staff par fonction: " + e.getMessage());
        }
        
        return staffs;
    }

    public List<Staff> findByTournoi(Tournoi tournoi) throws DAOException {
        List<Staff> staffs = new ArrayList<>();
        String sql = "SELECT s.* FROM Staff s " +
                    "JOIN Affectation a ON s.id_staff = a.id_staff " +
                    "WHERE a.id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    staffs.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du staff par tournoi: " + e.getMessage());
        }
        
        return staffs;
    }

    public List<Affectation> getAffectations(Staff staff) throws DAOException {
        AffectationDAO affectationDAO = new AffectationDAO(connection);
        return affectationDAO.findByStaff(staff);
    }

 

    public List<Staff> rechercher(String critere) throws DAOException {
        List<Staff> staffs = new ArrayList<>();
        String sql = "SELECT * FROM Staff WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ? OR LOWER(fonction) LIKE ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            String pattern = "%" + critere.toLowerCase() + "%";
            pst.setString(1, pattern);
            pst.setString(2, pattern);
            pst.setString(3, pattern);
            pst.setString(4, pattern);
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    staffs.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du staff: " + e.getMessage());
        }
        
        return staffs;
    }

    public void addStaffToTournoi(Affectation affectation) throws DAOException {
        String sql = "INSERT INTO Affectation (id_staff, id_tournoi, role_specifique, date_debut, date_fin) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, affectation.getStaff().getId());
            pst.setInt(2, affectation.getTournoi().getId());
            pst.setString(3, affectation.getRoleSpecifique());
            pst.setTimestamp(4, Timestamp.valueOf(affectation.getDateDebut()));
            pst.setTimestamp(5, Timestamp.valueOf(affectation.getDateFin()));
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("L'ajout du staff au tournoi a échoué");
            }

            connection.commit();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de l'ajout du staff au tournoi: " + e.getMessage());
        }
    }

    public void removeStaffFromTournoi(Staff staff, Tournoi tournoi) throws DAOException {
        String sql = "DELETE FROM Affectation WHERE id_staff = ? AND id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, staff.getId());
            pst.setInt(2, tournoi.getId());
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("La suppression du staff du tournoi a échoué");
            }

            connection.commit();
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la suppression du staff du tournoi: " + e.getMessage());
        }
    }

    public Affectation getAffectation(Staff staff, Tournoi tournoi) throws DAOException {
        String sql = "SELECT role_specifique, date_debut, date_fin FROM Affectation WHERE id_staff = ? AND id_tournoi = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, staff.getId());
            pst.setInt(2, tournoi.getId());
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Affectation affectation = new Affectation();
                    affectation.setStaff(staff);
                    affectation.setTournoi(tournoi);
                    affectation.setRoleSpecifique(rs.getString("role_specifique"));
                    affectation.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
                    affectation.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
                    return affectation;
                } else {
                    throw new DAOException("Aucune affectation trouvée pour ce staff et ce tournoi");
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la récupération de l'affectation: " + e.getMessage());
        }
    }

    public Staff findByTelephone(String telephone) throws DAOException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_TELEPHONE_QUERY)) {
            statement.setString(1, telephone);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DAOException("Erreur lors de la recherche du staff par téléphone", e);
        }
    }
}
