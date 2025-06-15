package fr.tournois.ui.util;

import fr.tournois.model.Equipe;
import fr.tournois.model.Joueur;
import fr.tournois.model.Tournoi;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PdfTournoiGenerator {

    public static void genererPDFTournoi(Tournoi tournoi, String cheminFichier) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();
            
            // Titre principal
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Tournoi : " + tournoi.getNom(), titleFont);
            title.setSpacingAfter(20f);
            document.add(title);
            
            // Informations du tournoi
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            
            Paragraph lieu = new Paragraph("Lieu : " + tournoi.getLieu(), normalFont);
            lieu.setSpacingAfter(10f);
            document.add(lieu);
            
            String dates = "Dates : " + tournoi.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + " - " + tournoi.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Paragraph datesP = new Paragraph(dates, normalFont);
            datesP.setSpacingAfter(10f);
            document.add(datesP);
            
            String infos = "Format : " + tournoi.getFormat() + " | Statut : " + tournoi.getStatut() +
                    " | Prize Pool : " + tournoi.getPrixPool() + "€";
            Paragraph infosP = new Paragraph(infos, normalFont);
            infosP.setSpacingAfter(20f);
            document.add(infosP);
            
            // Liste des équipes
            Font equipeFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font joueurFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            
            for (Equipe equipe : tournoi.getEquipes()) {
                // Nom de l'équipe
                Paragraph equipeP = new Paragraph("Équipe : " + equipe.getNom() + " [" + equipe.getTag() + "] - " + equipe.getPays(), equipeFont);
                equipeP.setSpacingAfter(10f);
                document.add(equipeP);
                
                // Liste des joueurs
                if (equipe.getJoueurs() != null) {
                    for (Joueur joueur : equipe.getJoueurs()) {
                        Paragraph joueurP = new Paragraph("    - " + joueur.getPseudo(), joueurFont);
                        joueurP.setSpacingAfter(5f);
                        document.add(joueurP);
                    }
                }
                
                // Espacement entre équipes
                Paragraph spacing = new Paragraph(" ", normalFont);
                spacing.setSpacingAfter(10f);
                document.add(spacing);
            }
            
        } catch (DocumentException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }
        
        System.out.println("PDF généré : " + cheminFichier);
    }
}