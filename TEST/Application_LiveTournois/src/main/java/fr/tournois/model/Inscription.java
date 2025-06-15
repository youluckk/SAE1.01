package fr.tournois.model;

import java.time.LocalDateTime;

/**
 * Représente l'inscription d'une équipe à un tournoi.
 *
 * Cette classe gère les informations relatives à la participation d'une équipe
 * à un tournoi, notamment son statut et sa position de tête de série.
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class Inscription {
    private Integer id;
    private Tournoi tournoi;
    private Equipe equipe;
    private LocalDateTime dateInscription;
    private String statut;  // Confirmé, En attente, Annulé, etc.
    private int seed;       // Position de tête de série

    // Constructeurs
    /**
     * Constructeur par défaut
     * Initialise l'identifiant à null
     */
    public Inscription() {
        this.id = null;
    }

    /**
     * Constructeur avec paramètres
     * @param tournoi le tournoi concerné
     * @param equipe l'équipe inscrite
     * @param statut le statut de l'inscription (Confirmé, En attente, Annulé, etc.)
     * @param seed la position de tête de série
     */
    public Inscription(Tournoi tournoi, Equipe equipe, String statut, int seed) {
        this.tournoi = tournoi;
        this.equipe = equipe;
        this.statut = statut;
        this.seed = seed;
    }

    // Getters et Setters
    /**
     * Retourne l'identifiant de l'inscription
     * @return l'identifiant de l'inscription
     */
    public Integer getId() {
        return id;
    }

    /**
     * Définit l'identifiant de l'inscription
     * @param id l'identifiant à définir
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne le tournoi concerné par l'inscription
     * @return le tournoi
     */
    public Tournoi getTournoi() {
        return tournoi;
    }

    /**
     * Définit le tournoi concerné par l'inscription
     * @param tournoi le tournoi à définir
     */
    public void setTournoi(Tournoi tournoi) {
        this.tournoi = tournoi;
    }

    /**
     * Retourne l'équipe inscrite
     * @return l'équipe
     */
    public Equipe getEquipe() {
        return equipe;
    }

    /**
     * Définit l'équipe inscrite
     * @param equipe l'équipe à définir
     */
    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    /**
     * Retourne la date d'inscription
     * @return la date d'inscription
     */
    public LocalDateTime getDateInscription() {
        return dateInscription;
    }

    /**
     * Définit la date d'inscription
     * @param dateInscription la date d'inscription à définir
     */
    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    /**
     * Retourne le statut de l'inscription
     * @return le statut (Confirmé, En attente, Annulé, etc.)
     */
    public String getStatut() {
        return statut;
    }

    /**
     * Définit le statut de l'inscription
     * @param statut le statut à définir
     */
    public void setStatut(String statut) {
        this.statut = statut;
    }

    /**
     * Retourne la position de tête de série
     * @return la position de tête de série
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Définit la position de tête de série
     * @param seed la position à définir
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * Retourne une représentation textuelle de l'inscription
     * @return une chaîne contenant les informations de l'inscription
     */
    @Override
    public String toString() {
        return "Inscription{" +
                "id=" + getId() +
                ", tournoi=" + (tournoi != null ? tournoi.getNom() : "null") +
                ", equipe=" + (equipe != null ? equipe.getNom() : "null") +
                ", dateInscription=" + dateInscription +
                ", statut='" + statut + '\'' +
                ", seed=" + seed +
                '}';
    }
}
