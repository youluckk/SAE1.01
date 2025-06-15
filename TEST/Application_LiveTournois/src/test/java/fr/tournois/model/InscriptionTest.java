package fr.tournois.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class InscriptionTest {
    @Test
    void testSetAndGetTournoiAndEquipe() {
        Tournoi tournoi = new Tournoi();
        tournoi.setId(42);
        tournoi.setNom("Tournoi Test");
        Equipe equipe = new Equipe();
        equipe.setId(7);
        equipe.setNom("Equipe Test");
        Inscription inscription = new Inscription();
        inscription.setTournoi(tournoi);
        inscription.setEquipe(equipe);
        assertEquals(42, inscription.getTournoi().getId());
        assertEquals("Tournoi Test", inscription.getTournoi().getNom());
        assertEquals(7, inscription.getEquipe().getId());
        assertEquals("Equipe Test", inscription.getEquipe().getNom());
    }

    @Test
    void testSetAndGetStatutAndSeed() {
        Inscription inscription = new Inscription();
        inscription.setStatut("Confirmé");
        inscription.setSeed(3);
        assertEquals("Confirmé", inscription.getStatut());
        assertEquals(3, inscription.getSeed());
    }

    @Test
    void testDateInscription() {
        Inscription inscription = new Inscription();
        LocalDateTime now = LocalDateTime.now();
        inscription.setDateInscription(now);
        assertEquals(now, inscription.getDateInscription());
    }
}
