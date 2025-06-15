package fr.tournois.security;

/**
 * Programme de test pour générer un hash BCrypt
 * À supprimer après utilisation
 */
public class TestHash {
    public static void main(String[] args) {
        // Test pour admin
        String mdpAdmin = "admin123";
        String hashAdmin = PasswordHasher.hashPassword(mdpAdmin);
        System.out.println("=== Test pour admin ===");
        System.out.println("Mot de passe : " + mdpAdmin);
        System.out.println("Hash généré : " + hashAdmin);
        System.out.println("Vérifie avec hash en base : " + 
            PasswordHasher.verifyPassword(mdpAdmin, "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6f.CXpyMOe"));
        
        // Test pour org1
        String mdpPierre = "org1123";
        String hashPierre = PasswordHasher.hashPassword(mdpPierre);
        System.out.println("\n=== Test pour org1 ===");
        System.out.println("Mot de passe : " + mdpPierre);
        System.out.println("Hash généré : " + hashPierre);
        System.out.println("Vérifie avec hash en base : " + 
            PasswordHasher.verifyPassword(mdpPierre, "$2a$12$QZKxf38EZ0jbTvCKjwvgHe.tZgF6FZ3x4WKRK/0FxVNKhPQc2Qo2a"));
        
        // Test pour org2
        String mdpSophie = "org2123";
        String hashSophie = PasswordHasher.hashPassword(mdpSophie);
        System.out.println("\n=== Test pour org2 ===");
        System.out.println("Mot de passe : " + mdpSophie);
        System.out.println("Hash généré : " + hashSophie);
        System.out.println("Vérifie avec hash en base : " + 
            PasswordHasher.verifyPassword(mdpSophie, "$2a$12$BXxGZV3RUZRxqWC8WcUZu.zeZYXGMZ.lI.YKhUsFwJGYgZCJCgC6O"));
        
        // Test pour org3
        String mdpMichel = "org3123";
        String hashMichel = PasswordHasher.hashPassword(mdpMichel);
        System.out.println("\n=== Test pour org3 ===");
        System.out.println("Mot de passe : " + mdpMichel);
        System.out.println("Hash généré : " + hashMichel);
        System.out.println("Vérifie avec hash en base : " + 
            PasswordHasher.verifyPassword(mdpMichel, "$2a$12$9DUm6Zp.oZUXKbQgKqXkJOtZr7Vyn3IIxJqY9KzLGYf8Yz8iK1kGq"));
    }
}
