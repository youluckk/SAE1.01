package fr.tournois.model;

import java.time.LocalDateTime;

/**
 * Représente une affectation d'un membre du staff à un tournoi.
 * Une affectation définit le rôle spécifique et la période pendant laquelle
 * le membre du staff est assigné au tournoi.
 */
public class Affectation {
    private Staff staff;
    private Tournoi tournoi;
    private String roleSpecifique;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Tournoi getTournoi() {
        return tournoi;
    }

    public void setTournoi(Tournoi tournoi) {
        this.tournoi = tournoi;
    }

    public String getRoleSpecifique() {
        return roleSpecifique;
    }

    public void setRoleSpecifique(String roleSpecifique) {
        this.roleSpecifique = roleSpecifique;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }
}