package fr.tournois.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class TournoiTest {
    @Test
    void testConstructeurParDefaut() {
        Tournoi tournoi = new Tournoi();
        assertEquals("En préparation", tournoi.getStatut());
        assertNotNull(tournoi.getEquipes());
        assertNotNull(tournoi.getAffectations());
        assertNotNull(tournoi.getInscriptions());
    }

    @Test
    void testSettersEtGetters() {
        Tournoi tournoi = new Tournoi();
        tournoi.setNom("Tournoi Test");
        assertEquals("Tournoi Test", tournoi.getNom());
        tournoi.setLieu("Paris");
        assertEquals("Paris", tournoi.getLieu());
        tournoi.setFormat("5v5");
        assertEquals("5v5", tournoi.getFormat());
        tournoi.setNbEquipesMax(8);
        assertEquals(8, tournoi.getNbEquipesMax());
        tournoi.setPrixPool(1000.0);
        assertEquals(1000.0, tournoi.getPrixPool());
        tournoi.setDateDebut(LocalDate.of(2025, 6, 1));
        assertEquals(LocalDate.of(2025, 6, 1), tournoi.getDateDebut());
        tournoi.setDateFin(LocalDate.of(2025, 6, 2));
        assertEquals(LocalDate.of(2025, 6, 2), tournoi.getDateFin());
    }

    @Test
    void testSetNbEquipesMaxInvalide() {
        Tournoi tournoi = new Tournoi();
        assertThrows(IllegalArgumentException.class, () -> tournoi.setNbEquipesMax(0));
    }

    @Test
    void testSetFormatInvalide() {
        Tournoi tournoi = new Tournoi();
        assertThrows(IllegalArgumentException.class, () -> tournoi.setFormat(""));
    }

    @Test
    void testSetPrixPoolInvalide() {
        Tournoi tournoi = new Tournoi();
        assertThrows(IllegalArgumentException.class, () -> tournoi.setPrixPool(-10));
    }
}
