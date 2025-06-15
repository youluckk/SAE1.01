package fr.tournois.model;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur du système avec ses informations de connexion et son rôle.
 * Cette classe gère les informations d'authentification et de traçabilité des utilisateurs.
 *
 * Les utilisateurs peuvent avoir deux rôles :
 * - ADMIN : administrateur avec tous les droits
 * - ORGANISATEUR : peut gérer les tournois et les staffs
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class Utilisateur {
    /** Identifiant unique de l'utilisateur en base de données */
    private Integer id;
    
    /** Nom d'utilisateur unique pour la connexion */
    private String pseudo;
    
    /** Mot de passe hashé de l'utilisateur */
    private String passwd;
    
    /** Rôle de l'utilisateur (ADMIN ou ORGANISATEUR) */
    private Role role;
    
    /** Date de création du compte utilisateur */
    private LocalDateTime dateCreation;
    
    /** Date de dernière connexion de l'utilisateur */
    private LocalDateTime derniereConnexion;
    
    /** Indique si le compte est actif (true) ou désactivé (false) */
    private boolean actif;

    /**
     * Constructeur par défaut.
     * Initialise un nouvel utilisateur avec la date de création à maintenant
     * et le statut actif à true.
     */
    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
    }

    /**
     * Constructeur avec paramètres principaux.
     * @param pseudo Le nom d'utilisateur
     * @param passwd Le mot de passe (sera hashé avant stockage)
     * @param role Le rôle de l'utilisateur
     */
    public Utilisateur(String pseudo, String passwd, Role role) {
        this();
        this.pseudo = pseudo;
        this.passwd = passwd;
        this.role = role;
    }


    /**
     * @return L'identifiant unique de l'utilisateur
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id L'identifiant unique à définir
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return Le nom d'utilisateur
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * @param pseudo Le nom d'utilisateur à définir
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /**
     * @return Le mot de passe hashé
     */
    public String getPassword() {
        return passwd;
    }

    /**
     * @param password Le mot de passe à définir (sera hashé)
     */
    public void setPassword(String password) {
        this.passwd = password;
    }

    /**
     * @return Le rôle de l'utilisateur
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role Le rôle à définir
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return La date de création du compte
     */
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    /**
     * @param dateCreation La date de création à définir
     */
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * @return La date de dernière connexion
     */
    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    /**
     * @param derniereConnexion La date de dernière connexion à définir
     */
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    /**
     * @return true si le compte est actif, false sinon
     */
    public boolean isActif() {
        return actif;
    }

    /**
     * @param actif Le statut d'activation à définir
     */
    public void setActif(boolean actif) {
        this.actif = actif;
    }


    /**
     * Retourne une représentation textuelle de l'utilisateur.
     * @return Le pseudo de l'utilisateur
     */
    @Override
    public String toString() {
        return String.format("%s", pseudo);
    }
}
