# Fonctionnalités par Version

## Version 0 - CRUD Initial

### ORGANISATEUR :
- Gérer les tournois
  - Créer un tournoi
  - Modifier un tournoi
  - Supprimer un tournoi
  - Consulter les tournois
- Gérer les staff
  - Créer un staff
  - Modifier un staff
  - Supprimer un staff
  - Consulter les staff
- Gérer les affectations
  - Affecter un staff
  - Modifier une affectation
  - Supprimer une affectation
  - Consulter les affectations

### ADMIN :
*Hérite de toutes les fonctionnalités ORGANISATEUR, plus :*
- Gérer les utilisateurs
  - Créer un utilisateur
  - Modifier un utilisateur
  - Supprimer un utilisateur
  - Consulter les utilisateurs

## Version 1 - Gestion des Équipes et Jeux

### ORGANISATEUR :
- Gérer les équipes
  - Créer une équipe
  - Modifier une équipe
  - Supprimer une équipe
  - Consulter les équipes
- Gérer les jeux
  - Créer un jeu
  - Modifier un jeu
  - Supprimer un jeu
  - Consulter les jeux
- Affecter un joueur à une équipe

### ADMIN :
*Hérite de toutes les fonctionnalités ORGANISATEUR, plus :*
- Gérer les joueurs
  - Créer un joueur
  - Modifier un joueur
  - Supprimer un joueur
  - Consulter les joueurs

## Version 2 - Documents et Inscriptions

### ORGANISATEUR :
- Gérer les inscriptions aux tournois
  - Inscrire une équipe
  - Modifier une inscription
  - Supprimer une inscription
  - Consulter les inscriptions
- Générer PDF tournoi

### ADMIN :
*Hérite de toutes les fonctionnalités ORGANISATEUR, plus :*
- Générer PDF joueur
- Générer PDF liste joueurs
