package fr.tournois.model;

import java.util.Date;

/**
 * Représente un joueur participant à un tournoi.
 * Un joueur est identifié par son nom, prénom, pseudo, date de naissance et son équipe.
 */
public class Joueur {
    private Integer id;
    private String nom;
    private String prenom;
    private String pseudo;
    private Date dateNaissance;
    private Equipe equipe;

    public Joueur() {}

    public Joueur(Integer id, String nom, String prenom, String pseudo, Date dateNaissance, Equipe equipe) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.pseudo = pseudo;
        this.dateNaissance = dateNaissance;
        this.equipe = equipe;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    // ➕ Ajout de méthodes utilitaires

    public Integer getEquipeId() {
        return (equipe != null) ? equipe.getId() : null;
    }

    public void setEquipeId(Integer equipeId) {
        if (this.equipe == null) {
            this.equipe = new Equipe();
        }
        this.equipe.setId(equipeId);
    }

    @Override
    public String toString() {
        return "Joueur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", equipe=" + (equipe != null ? equipe.getNom() : "Aucune") +'}';
    }

}
