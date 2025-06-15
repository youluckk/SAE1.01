package fr.tournois.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un tournoi dans le système.
 *
 * Cette classe gère les informations relatives à un tournoi, notamment ses dates,
 * son format, ses équipes participantes, son statut et les membres du staff affectés.
 * Elle assure également la gestion des inscriptions et des affectations.
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class Tournoi {
    private Integer id;
    
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String lieu;
    private String format;
    private int nbEquipesMax;
    private String statut;
    private double prixPool;
    private Jeu jeu;
    private List<Inscription> inscriptions;
    private List<Affectation> affectations;
    private List<Equipe> equipes;

    // Constructeurs
    /**
     * Constructeur par défaut.
     * Initialise les listes d'inscriptions, d'affectations et d'équipes.
     * Le statut par défaut est "En préparation".
     */
    public Tournoi() {
        this.inscriptions = new ArrayList<>();
        this.affectations = new ArrayList<>();
        this.equipes = new ArrayList<>();
        this.statut = "En préparation";
    }

    /**
     * Constructeur avec tous les paramètres principaux.
     * @param nom le nom du tournoi
     * @param dateDebut la date de début du tournoi
     * @param dateFin la date de fin du tournoi
     * @param lieu le lieu où se déroule le tournoi
     * @param format le format du tournoi (ex: "5v5", "Battle Royale")
     * @param prixPool le montant total des prix du tournoi
     * @param nbEquipesMax le nombre maximum d'équipes pouvant participer
     * @param jeu le jeu sur lequel se déroule le tournoi
     */
    public Tournoi(String nom, LocalDate dateDebut, LocalDate dateFin, String lieu, String format, 
                  double prixPool, int nbEquipesMax, Jeu jeu) {
        this();
        this.nom = nom;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.format = format;
        this.prixPool = prixPool;
        this.nbEquipesMax = nbEquipesMax;
        this.jeu = jeu;
    }

    // Getters et Setters
    /**
     * Retourne le nom du tournoi
     * @return le nom du tournoi
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du tournoi
     * @param nom le nom à définir
     * @throws IllegalArgumentException si le nom est null ou vide
     */
    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du tournoi ne peut pas être vide");
        }
        this.nom = nom;
    }

    /**
     * Retourne la date de début du tournoi
     * @return la date de début
     */
    public LocalDate getDateDebut() {
        return dateDebut;
    }

    /**
     * Définit la date de début du tournoi
     * @param dateDebut la date de début à définir
     * @throws IllegalArgumentException si la date est null
     */
    public void setDateDebut(LocalDate dateDebut) {
        if (dateDebut == null) {
            throw new IllegalArgumentException("La date de début ne peut pas être nulle");
        }
        this.dateDebut = dateDebut;
    }

    /**
     * Retourne la date de fin du tournoi
     * @return la date de fin
     */
    public LocalDate getDateFin() {
        return dateFin;
    }

    /**
     * Définit la date de fin du tournoi
     * @param dateFin la date de fin à définir
     * @throws IllegalArgumentException si la date est null ou antérieure à la date de début
     */
    public void setDateFin(LocalDate dateFin) {
        if (dateFin == null) {
            throw new IllegalArgumentException("La date de fin ne peut pas être nulle");
        }
        if (dateDebut != null && dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début");
        }
        this.dateFin = dateFin;
    }

    /**
     * Retourne le lieu du tournoi
     * @return le lieu du tournoi
     */
    public String getLieu() {
        return lieu;
    }

    /**
     * Définit le lieu du tournoi
     * @param lieu le lieu à définir
     */
    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    /**
     * Retourne le format du tournoi
     * @return le format du tournoi
     */
    public String getFormat() {
        return format;
    }

    /**
     * Définit le format du tournoi
     * @param format le format à définir
     * @throws IllegalArgumentException si le format est null ou vide
     */
    public void setFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Le format du tournoi ne peut pas être vide");
        }
        this.format = format;
    }

    /**
     * Retourne le nombre maximum d'équipes autorisées
     * @return le nombre maximum d'équipes
     */
    public int getNbEquipesMax() {
        return nbEquipesMax;
    }

    /**
     * Définit le nombre maximum d'équipes autorisées
     * @param nbEquipesMax le nombre maximum d'équipes à définir
     * @throws IllegalArgumentException si le nombre est négatif ou nul
     */
    public void setNbEquipesMax(int nbEquipesMax) {
        if (nbEquipesMax <= 0) {
            throw new IllegalArgumentException("Le nombre maximum d'équipes doit être positif");
        }
        this.nbEquipesMax = nbEquipesMax;
    }

    /**
     * Retourne le statut actuel du tournoi
     * @return le statut du tournoi
     */
    public String getStatut() {
        return statut;
    }

    /**
     * Définit le statut du tournoi
     * @param statut le statut à définir
     * @throws IllegalArgumentException si le statut est null ou vide
     */
    public void setStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            throw new IllegalArgumentException("Le statut ne peut pas être vide");
        }
        this.statut = statut;
    }

    /**
     * Retourne le montant total des prix du tournoi
     * @return le montant du prize pool
     */
    public double getPrixPool() {
        return prixPool;
    }

    /**
     * Définit le montant total des prix du tournoi
     * @param prixPool le montant à définir
     * @throws IllegalArgumentException si le montant est négatif
     */
    public void setPrixPool(double prixPool) {
        if (prixPool < 0) {
            throw new IllegalArgumentException("Le prix pool ne peut pas être négatif");
        }
        this.prixPool = prixPool;
    }

    /**
     * Retourne le jeu du tournoi
     * @return le jeu du tournoi
     */
    public Jeu getJeu() {
        return jeu;
    }

    /**
     * Définit le jeu du tournoi
     * @param jeu le jeu à définir
     */
    public void setJeu(Jeu jeu) {
        this.jeu = jeu;
    }

    /**
     * Retourne la liste des inscriptions au tournoi
     * @return la liste des inscriptions
     */
    public List<Inscription> getInscriptions() {
        return inscriptions;
    }

    /**
     * Définit la liste des inscriptions au tournoi
     * @param inscriptions la liste des inscriptions à définir
     */
    public void setInscriptions(List<Inscription> inscriptions) {
        this.inscriptions = inscriptions != null ? inscriptions : new ArrayList<>();
    }

    /**
     * Retourne la liste des affectations de staff au tournoi
     * @return la liste des affectations
     */
    public List<Affectation> getAffectations() {
        return affectations;
    }

    /**
     * Définit la liste des affectations de staff au tournoi
     * @param affectations la liste des affectations à définir
     */
    public void setAffectations(List<Affectation> affectations) {
        this.affectations = affectations != null ? affectations : new ArrayList<>();
    }

    /**
     * Retourne la liste des équipes participantes
     * @return la liste des équipes
     */
    public List<Equipe> getEquipes() {
        return equipes;
    }

    /**
     * Définit la liste des équipes participantes
     * @param equipes la liste des équipes à définir
     */
    public void setEquipes(List<Equipe> equipes) {
        this.equipes = equipes != null ? equipes : new ArrayList<>();
    }

    // Méthodes métier
    /**
     * Vérifie si le tournoi peut accepter une nouvelle équipe
     * @return true si le nombre maximum d'équipes n'est pas atteint, false sinon
     */
    public boolean peutAjouterEquipe() {
        return inscriptions.size() < nbEquipesMax;
    }

    /**
     * Ajoute une inscription au tournoi
     * @param inscription l'inscription à ajouter
     * @throws IllegalStateException si le nombre maximum d'équipes est atteint
     */
    public void ajouterInscription(Inscription inscription) {
        if (inscription != null && !inscriptions.contains(inscription)) {
            if (!peutAjouterEquipe()) {
                throw new IllegalStateException("Le nombre maximum d'équipes est atteint");
            }
            inscriptions.add(inscription);
        }
    }

    /**
     * Ajoute une affectation de staff au tournoi
     * @param affectation l'affectation à ajouter
     */
    public void ajouterAffectation(Affectation affectation) {
        if (affectation != null && !affectations.contains(affectation)) {
            affectations.add(affectation);
        }
    }

    /**
     * Retourne l'identifiant du tournoi
     * @return l'identifiant du tournoi
     */
    public Integer getId() {
        return id;
    }

    /**
     * Définit l'identifiant du tournoi
     * @param id l'identifiant à définir
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Retourne une représentation textuelle du tournoi
     * @return une chaîne contenant le nom et les dates du tournoi
     */
    @Override
    public String toString() {
        return String.format("%s (%s - %s)", nom, dateDebut, dateFin);
    }
}
