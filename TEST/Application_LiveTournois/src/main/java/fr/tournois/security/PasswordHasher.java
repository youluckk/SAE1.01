package fr.tournois.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Classe utilitaire pour le hachage sécurisé des mots de passe
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class PasswordHasher {
    private static final int COST = 12; // Coût de hachage (plus il est élevé, plus c'est sécurisé mais lent)

    /**
     * Hache un mot de passe en utilisant BCrypt
     * @param password Le mot de passe en clair
     * @return Le mot de passe haché
     */
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    /**
     * Vérifie si un mot de passe en clair correspond à un hash
     * @param password Le mot de passe en clair
     * @param hashedPassword Le hash du mot de passe stocké
     * @return true si le mot de passe correspond, false sinon
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
    }
}
