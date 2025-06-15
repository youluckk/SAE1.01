package fr.tournois.ui.controller; // Ensure this package is correct, it should be fr.tournois.ui for GenererPDF

import fr.tournois.model.Joueur;
import fr.tournois.model.Equipe;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.time.LocalDate;
import java.sql.Date; // Ensure you import java.sql.Date

public class GenererPDF {

    /**
     * Génère un fichier PDF pour un joueur donné.
     * Style: Une seule ligne avec toutes les infos principales.
     *
     * @param joueur Le joueur pour lequel générer le PDF.
     * @throws Exception En cas d'erreur lors de la génération du PDF (IOException, DocumentException, etc.).
     */
    public static void genererPdfPourJoueur(Joueur joueur) throws Exception {
        if (joueur == null) {
            throw new IllegalArgumentException("Le joueur ne peut pas être null.");
        }

        Document document = new Document();
        // Nettoyer le pseudo pour le nom de fichier afin d'éviter les caractères invalides
        String nomFichier = joueur.getPseudo() + ".pdf";
        
        PdfWriter.getInstance(document, new FileOutputStream(nomFichier));
        document.open();

        document.addTitle("Fiche Joueur : " + joueur.getPseudo());
        document.newPage(); // Crée une nouvelle page pour le contenu

        // --- Contenu principal du PDF pour un joueur (une seule ligne) ---
        StringBuilder joueurInfo = new StringBuilder();
        joueurInfo.append("Pseudo : ").append(joueur.getPseudo());
        joueurInfo.append(", Nom : ").append(joueur.getNom());
        joueurInfo.append(", Prénom : ").append(joueur.getPrenom());
        
         String dateNais;
        if (joueur.getDateNaissance() != null) {
            dateNais = convertSqlDateToLocalDate((Date) joueur.getDateNaissance()).toString();
        } else {
            dateNais = "N/A";
        }
        joueurInfo.append(", Date Naissance : ").append(dateNais);

        // Information sur l'équipe (si le joueur est associé à une équipe)
        if (joueur.getEquipe() != null && joueur.getEquipe().getNom() != null) {
            joueurInfo.append(", Équipe : ").append(joueur.getEquipe().getNom());
        } else {
            joueurInfo.append(", Équipe : Aucune");
        }

        document.add(new Paragraph(joueurInfo.toString())); // Ajoute la ligne unique d'informations

        document.close();
    }

    /**
     * Génère un fichier PDF listant les joueurs.
     * Style: Chaque joueur sur une ligne simple et concise.
     *
     * @param listeJoueurs La liste des joueurs à inclure dans le PDF.
     * @param titre Le titre du document PDF.
     * @param nomFichier Le nom du fichier PDF à générer.
     * @throws Exception En cas d'erreur lors de la génération du PDF (IOException, DocumentException, etc.).
     */
    public static void genererPdfListeJoueurs(List<Joueur> listeJoueurs, String titre, String nomFichier) throws Exception {
        if (listeJoueurs == null || listeJoueurs.isEmpty()) {
            throw new IllegalArgumentException("La liste de joueurs est vide ou nulle.");
        }
        if (nomFichier == null || nomFichier.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du fichier ne peut pas être vide.");
        }
        
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(nomFichier.replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf"));
        document.open();

        document.addTitle(titre);
        document.add(new Paragraph(titre)); // Ajoute le titre principal du document
        document.add(new Paragraph(" ")); // Espace

        // Pour chaque joueur dans la liste, on crée une ligne de texte
        for (Joueur joueur : listeJoueurs) {
            StringBuilder joueurLigne = new StringBuilder();
            joueurLigne.append("Pseudo: ").append(joueur.getPseudo());
            joueurLigne.append(" / Nom: ").append(joueur.getNom());
            joueurLigne.append(" / Prénom: ").append(joueur.getPrenom());
            
            String dateNais;
            if (joueur.getDateNaissance() != null) {
                dateNais = convertSqlDateToLocalDate((Date) joueur.getDateNaissance()).toString();
            } else {
                dateNais = "N/A";
            }
            joueurLigne.append(" / Date Naissance: ").append(dateNais);

            // Équipe
            if (joueur.getEquipe() != null && joueur.getEquipe().getNom() != null) {
                joueurLigne.append(" / Équipe: ").append(joueur.getEquipe().getNom());
            } else {
                joueurLigne.append(" / Équipe: Aucune");
            }
            
            document.add(new Paragraph(joueurLigne.toString()));
            document.add(new Paragraph(" ")); // Une ligne vide pour séparer chaque joueur
        }
        document.close();
    }

    /**
     * Méthode utilitaire pour convertir date sql en date 
     * @param sqlDate La date au format java.sql.Date (souvent issue de la BDD).
     * @return La date convertie en LocalDate, ou null si l'entrée est null.
     */
    private static LocalDate convertSqlDateToLocalDate(Date sqlDate) { 
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }
}