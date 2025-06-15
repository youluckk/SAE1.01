package fr.tournois.security;

import fr.tournois.model.Role;
import fr.tournois.model.Utilisateur;

/**
 * Singleton gérant le contexte de sécurité de l'application.
 * Maintient l'état de l'utilisateur connecté et fournit des méthodes
 * pour vérifier les droits d'accès.
 *
 * @author F. Pelleau &amp; A. Péninou
 * @since V0.0
 */
public class SecurityContext {
    private static SecurityContext instance;
    private Utilisateur currentUser;

    /**
     * Constructeur privé pour le pattern Singleton.
     */
    private SecurityContext() {
        // Constructeur privé pour empêcher l'instanciation directe
    }

    /**
     * Obtient l'instance unique de SecurityContext.
     * @return l'instance de SecurityContext
     */
    public static SecurityContext getInstance() {
        if (instance == null) {
            instance = new SecurityContext();
        }
        return instance;
    }

    /**
     * Définit l'utilisateur actuellement connecté.
     * @param user l'utilisateur à définir comme utilisateur courant
     */
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
    }

    /**
     * Déconnecte l'utilisateur courant.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Vérifie si l'utilisateur courant est un administrateur.
     * @return true si l'utilisateur est un administrateur, false sinon
     */
    public boolean isAdmin() {
        return currentUser != null && Role.ADMIN.equals(currentUser.getRole());
    }

    /**
     * Vérifie si l'utilisateur courant est un organisateur.
     * @return true si l'utilisateur est un organisateur, false sinon
     */
    public boolean isOrganisateur() {
        return currentUser != null && Role.ORGANISATEUR.equals(currentUser.getRole());
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     * @return true si un utilisateur est connecté, false sinon
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Obtient l'utilisateur actuellement connecté.
     * @return l'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public Utilisateur getCurrentUser() {
        return currentUser;
    }
}
