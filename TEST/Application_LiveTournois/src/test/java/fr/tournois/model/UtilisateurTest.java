package fr.tournois.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilisateurTest {
    @Test
    void testConstructeurParDefaut() {
        Utilisateur user = new Utilisateur();
        assertTrue(user.isActif());
        assertNotNull(user.getDateCreation());
    }

    @Test
    void testSettersEtGetters() {
        Utilisateur user = new Utilisateur();
        user.setPseudo("toto");
        assertEquals("toto", user.getPseudo());
        user.setId(42);
        assertEquals(42, user.getId());
    }
}
