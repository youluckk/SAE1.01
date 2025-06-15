package fr.tournois.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JeuTest {

    @Test
    public void testConstructeurSansParametres() {
        Jeu jeu = new Jeu();
        assertNull(jeu.getId());
    }

    @Test
    public void testConstructeurAvecParametres() {
        Jeu jeu = new Jeu("FIFA", "EA Sports", 2020, "Sport", "Jeu de football");
        assertEquals("FIFA", jeu.getNom());
        assertEquals("EA Sports", jeu.getEditeur());
        assertEquals(2020, jeu.getAnneeSortie());
        assertEquals("Sport", jeu.getGenre());
        assertEquals("Jeu de football", jeu.getDescription());
    }

    @Test
    public void testSettersEtGetters() {
        Jeu jeu = new Jeu();
        jeu.setId(10);
        jeu.setNom("Mario Kart");
        jeu.setEditeur("Nintendo");
        jeu.setAnneeSortie(2017);
        jeu.setGenre("Course");
        jeu.setDescription("Jeu de course multijoueur");

        assertEquals(Integer.valueOf(10), jeu.getId());
        assertEquals("Mario Kart", jeu.getNom());
        assertEquals("Nintendo", jeu.getEditeur());
        assertEquals(2017, jeu.getAnneeSortie());
        assertEquals("Course", jeu.getGenre());
        assertEquals("Jeu de course multijoueur", jeu.getDescription());
    }

    @Test
    public void testSetNomVide() {
        Jeu jeu = new Jeu();
        assertThrows(IllegalArgumentException.class, () -> jeu.setNom(""));
    }

    @Test
    public void testSetNomNull() {
        Jeu jeu = new Jeu();
        assertThrows(IllegalArgumentException.class, () -> jeu.setNom(null));
    }

    @Test
    public void testToString() {
        Jeu jeu = new Jeu("Zelda", "Nintendo", 1998, "Aventure", "Un classique");
        String attendu = "Zelda (Nintendo, 1998)";
        assertEquals(attendu, jeu.toString());
    }

    @Test
    public void testNomAvecEspaces() {
        Jeu jeu = new Jeu();
        jeu.setNom("  Overwatch 2  ");
        assertEquals("  Overwatch 2  ", jeu.getNom());
    }

    @Test
    public void testSetAnneeSortieNegative() {
        Jeu jeu = new Jeu();
        assertThrows(IllegalArgumentException.class, () -> jeu.setAnneeSortie(-1990));
    }

    @Test
    public void testSetDescriptionNull() {
        Jeu jeu = new Jeu();
        jeu.setDescription(null);
        assertNull(jeu.getDescription());
    }
}