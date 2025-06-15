package fr.tournois.model;

/**
 * Représente un jeu vidéo
 */
public class Jeu {
    private Integer id;
    private String nom;
    private String editeur;
    private int anneeSortie;
    private String genre;
    private String description;

    // Constructeurs
    public Jeu() {
        this.id = null;
    }

    public Jeu(String nom, String editeur, int anneeSortie, String genre, String description) {
        this.nom = nom;
        this.editeur = editeur;
        this.anneeSortie = anneeSortie;
        this.genre = genre;
        this.description = description;
    }

    // Getters et Setters
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
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du jeu ne peut pas être vide");
        }
        this.nom = nom;
    }

    public String getEditeur() {
        return editeur;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }

    public int getAnneeSortie() {
        return anneeSortie;
    }

    public void setAnneeSortie(int anneeSortie) {
        if (anneeSortie < 0) {
            throw new IllegalArgumentException("L'année de sortie ne peut pas être négative");
        }
        this.anneeSortie = anneeSortie;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %d)", nom, editeur, anneeSortie);
    }
}
