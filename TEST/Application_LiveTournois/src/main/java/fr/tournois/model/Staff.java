package fr.tournois.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un membre du staff (organisateur, arbitre, etc.).
 *
 * Cette classe gère les informations relatives à un membre du staff,
 * notamment ses informations personnelles, sa fonction, ses affectations aux tournois
 * et son compte utilisateur associé.
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class Staff {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String fonction;
    private String telephone;
    private List<Affectation> affectations;
    private Utilisateur utilisateur;
    private Role role;

    // Constructeurs
    /**
     * Constructeur par défaut
     * Initialise l'identifiant à null et crée une liste vide d'affectations
     */
    public Staff() {
        this.id = null;
        this.affectations = new ArrayList<>();
    }

    /**
     * Constructeur avec paramètres de base
     * @param nom le nom du membre du staff
     * @param prenom le prénom du membre du staff
     * @param email l'email du membre du staff
     * @param fonction la fonction du membre du staff
     */
    public Staff(String nom, String prenom, String email, String fonction) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.fonction = fonction;
    }

    /**
     * Constructeur avec paramètres incluant le téléphone
     * @param nom le nom du membre du staff
     * @param prenom le prénom du membre du staff
     * @param email l'email du membre du staff
     * @param fonction la fonction du membre du staff
     * @param telephone le numéro de téléphone du membre du staff
     */
    public Staff(String nom, String prenom, String email, String fonction, String telephone) {
        this(nom, prenom, email, fonction);
        this.telephone = telephone;
    }

    /**
     * Constructeur avec paramètres incluant l'utilisateur associé
     * @param nom le nom du membre du staff
     * @param prenom le prénom du membre du staff
     * @param email l'email du membre du staff
     * @param fonction la fonction du membre du staff
     * @param telephone le numéro de téléphone du membre du staff
     * @param utilisateur l'utilisateur associé au membre du staff
     */
    public Staff(String nom, String prenom, String email, String fonction, String telephone, Utilisateur utilisateur) {
        this(nom, prenom, email, fonction, telephone);
        this.utilisateur = utilisateur;
    }

    /**
     * Constructeur avec tous les paramètres
     * @param nom le nom du membre du staff
     * @param prenom le prénom du membre du staff
     * @param email l'email du membre du staff
     * @param fonction la fonction du membre du staff
     * @param telephone le numéro de téléphone du membre du staff
     * @param utilisateur l'utilisateur associé au membre du staff
     * @param role le rôle du membre du staff
     */
    public Staff(String nom, String prenom, String email, String fonction, String telephone, Utilisateur utilisateur, Role role) {
        this(nom, prenom, email, fonction, telephone, utilisateur);
        this.role = role;
    }

    // Getters et Setters
    /**
     * Retourne l'identifiant du membre du staff
     * @return l'identifiant du membre du staff
     */
    public Integer getId() {
        return id;
    }

    /**
     * Définit l'identifiant du membre du staff
     * @param id l'identifiant à définir
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le nom du membre du staff
     * @return le nom du membre du staff
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du membre du staff
     * @param nom le nom à définir
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne le prénom du membre du staff
     * @return le prénom du membre du staff
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * Définit le prénom du membre du staff
     * @param prenom le prénom à définir
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /**
     * Retourne l'email du membre du staff
     * @return l'email du membre du staff
     */
    public String getEmail() {
        return email;
    }

    /**
     * Définit l'email du membre du staff
     * @param email l'email à définir
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retourne la fonction du membre du staff
     * @return la fonction du membre du staff
     */
    public String getFonction() {
        return fonction;
    }

    /**
     * Définit la fonction du membre du staff
     * @param fonction la fonction à définir
     */
    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    /**
     * Retourne le numéro de téléphone du membre du staff
     * @return le numéro de téléphone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Définit le numéro de téléphone du membre du staff
     * @param telephone le numéro de téléphone à définir
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Retourne la liste des affectations du membre du staff aux tournois
     * @return la liste des affectations
     */
    public List<Affectation> getAffectations() {
        return affectations;
    }

    /**
     * Définit la liste des affectations du membre du staff
     * @param affectations la liste des affectations à définir
     */
    public void setAffectations(List<Affectation> affectations) {
        this.affectations = affectations != null ? affectations : new ArrayList<>();
    }

    /**
     * Retourne l'utilisateur associé au membre du staff
     * @return l'utilisateur associé
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    /**
     * Définit l'utilisateur associé au membre du staff
     * @param utilisateur l'utilisateur à associer
     */
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    /**
     * Retourne le rôle du membre du staff
     * @return le rôle du membre du staff
     */
    public Role getRole() {
        return role;
    }

    /**
     * Définit le rôle du membre du staff
     * @param role le rôle à définir
     */
    public void setRole(Role role) {
        this.role = role;
    }

    // Méthodes métier
    /**
     * Retourne une représentation textuelle du membre du staff
     * @return une chaîne contenant les informations du membre du staff
     */
    @Override
    public String toString() {
        return "Staff{" +
                "id=" + getId() +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", fonction='" + fonction + '\'' +
                ", telephone='" + telephone + '\'' +
                ", nbAffectations=" + (affectations != null ? affectations.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff staff = (Staff) o;
        return id != null && id.equals(staff.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
