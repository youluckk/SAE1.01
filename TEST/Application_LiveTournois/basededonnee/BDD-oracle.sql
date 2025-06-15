-- Script de création de la base de données Oracle pour le Gestionnaire de Tournois de Jeux Vidéo
-- Projet SAE BUT Informatique

-- Suppression des tables si elles existent déjà (pour réinitialisation)
DROP TABLE Affectation;
DROP TABLE Inscription;
DROP TABLE Joueur;
DROP TABLE Equipe;
DROP TABLE Staff;
DROP TABLE Tournoi;
DROP TABLE Jeu;
DROP TABLE Utilisateur;

-- Suppression des séquences existantes
DROP SEQUENCE seq_jeu_id;
DROP SEQUENCE seq_tournoi_id;
DROP SEQUENCE seq_equipe_id;
DROP SEQUENCE seq_joueur_id;
DROP SEQUENCE seq_utilisateur_id;
DROP SEQUENCE seq_staff_id;

-- Création des séquences pour les auto-incréments
CREATE SEQUENCE seq_jeu_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_tournoi_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_equipe_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_joueur_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_utilisateur_id START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_staff_id START WITH 1 INCREMENT BY 1;

-- Création de la table Jeu
CREATE TABLE Jeu (
    id_jeu NUMBER(10) PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL,
    editeur VARCHAR2(100),
    annee_sortie NUMBER(4),
    genre VARCHAR2(50),
    description VARCHAR2(300)
);

-- Création de la table Tournoi
CREATE TABLE Tournoi (
    id_tournoi NUMBER(10) PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    lieu VARCHAR2(100),
    format VARCHAR2(50) NOT NULL,
    nb_equipes_max NUMBER(10),
    statut VARCHAR2(20) DEFAULT 'En préparation',
    prix_pool NUMBER(10,2),
    id_jeu NUMBER(10),
    CONSTRAINT fk_tournoi_jeu FOREIGN KEY (id_jeu) REFERENCES Jeu(id_jeu) ON DELETE SET NULL
);

-- Création de la table Equipe
CREATE TABLE Equipe (
    id_equipe NUMBER(10) PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL,
    tag VARCHAR2(10),
    logo VARCHAR2(255),
    date_creation DATE,
    pays VARCHAR2(50),
    description VARCHAR2(300)
);

-- Création de la table Joueur
CREATE TABLE Joueur (
    id_joueur NUMBER(10) PRIMARY KEY,
    nom VARCHAR2(50) NOT NULL,
    prenom VARCHAR2(50) NOT NULL,
    pseudo VARCHAR2(50) NOT NULL,
    date_naissance DATE,
    nationalite VARCHAR2(50),
    email VARCHAR2(100),
    id_equipe NUMBER(10),
    role VARCHAR2(50),
    CONSTRAINT fk_joueur_equipe FOREIGN KEY (id_equipe) REFERENCES Equipe(id_equipe) ON DELETE SET NULL
);

-- Création de la table Utilisateur
CREATE TABLE Utilisateur (
    id_utilisateur NUMBER(10) PRIMARY KEY,
    pseudo VARCHAR2(50) NOT NULL UNIQUE,
    passwd VARCHAR2(255) NOT NULL,
    role VARCHAR2(12) CHECK (role IN ('ADMIN', 'ORGANISATEUR')) NOT NULL,
    date_creation DATE DEFAULT SYSDATE,
    derniere_connexion DATE,
    actif NUMBER(1) DEFAULT 1
);

-- Création de la table Staff
CREATE TABLE Staff (
    id_staff NUMBER(10) PRIMARY KEY,
    nom VARCHAR2(50) NOT NULL,
    prenom VARCHAR2(50) NOT NULL,
    fonction VARCHAR2(50) NOT NULL,
    email VARCHAR2(100),
    telephone VARCHAR2(20),
    id_utilisateur NUMBER(10),
    CONSTRAINT fk_staff_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES Utilisateur(id_utilisateur) ON DELETE SET NULL
);

-- Table d'association entre Tournoi et Equipe
CREATE TABLE Inscription (
    id_tournoi NUMBER(10),
    id_equipe NUMBER(10),
    date_inscription DATE DEFAULT SYSDATE,
    statut VARCHAR2(20) DEFAULT 'Inscrit',
    seed NUMBER(10),
    CONSTRAINT pk_inscription PRIMARY KEY (id_tournoi, id_equipe),
    CONSTRAINT fk_inscription_tournoi FOREIGN KEY (id_tournoi) REFERENCES Tournoi(id_tournoi) ON DELETE CASCADE,
    CONSTRAINT fk_inscription_equipe FOREIGN KEY (id_equipe) REFERENCES Equipe(id_equipe) ON DELETE CASCADE
);

-- Table d'association entre Tournoi et Staff
CREATE TABLE Affectation (
    id_tournoi NUMBER(10),
    id_staff NUMBER(10),
    role_specifique VARCHAR2(50),
    date_debut DATE,
    date_fin DATE,
    CONSTRAINT pk_affectation PRIMARY KEY (id_tournoi, id_staff),
    CONSTRAINT fk_affectation_tournoi FOREIGN KEY (id_tournoi) REFERENCES Tournoi(id_tournoi) ON DELETE CASCADE,
    CONSTRAINT fk_affectation_staff FOREIGN KEY (id_staff) REFERENCES Staff(id_staff) ON DELETE CASCADE
);

-- Insertion des données de test
-- Insertion des jeux
INSERT INTO Jeu (id_jeu, nom, editeur, annee_sortie, genre, description) VALUES
(seq_jeu_id.NEXTVAL, 'Counter-Strike 2', 'Valve', 2023, 'FPS', 'Jeu de tir tactique en équipe');
INSERT INTO Jeu (id_jeu, nom, editeur, annee_sortie, genre, description) VALUES
(seq_jeu_id.NEXTVAL, 'League of Legends', 'Riot Games', 2009, 'MOBA', 'Jeu d''arène de bataille en ligne');
INSERT INTO Jeu (id_jeu, nom, editeur, annee_sortie, genre, description) VALUES
(seq_jeu_id.NEXTVAL, 'Rocket League', 'Psyonix', 2015, 'Sport', 'Football avec des voitures');
INSERT INTO Jeu (id_jeu, nom, editeur, annee_sortie, genre, description) VALUES
(seq_jeu_id.NEXTVAL, 'Valorant', 'Riot Games', 2020, 'FPS', 'FPS tactique avec des capacités spéciales');

-- Insertion des tournois
INSERT INTO Tournoi (id_tournoi, nom, date_debut, date_fin, lieu, format, nb_equipes_max, statut, prix_pool, id_jeu) VALUES
(seq_tournoi_id.NEXTVAL, 'ESL Pro League', TO_DATE('15/06/2025', 'DD/MM/YYYY'), TO_DATE('20/06/2025', 'DD/MM/YYYY'), 'Paris', 'Double Elimination', 16, 'En préparation', 25000.00, 1);
INSERT INTO Tournoi (id_tournoi, nom, date_debut, date_fin, lieu, format, nb_equipes_max, statut, prix_pool, id_jeu) VALUES
(seq_tournoi_id.NEXTVAL, 'CS Masters', TO_DATE('10/07/2025', 'DD/MM/YYYY'), TO_DATE('15/07/2025', 'DD/MM/YYYY'), 'Lyon', 'Round Robin + Playoffs', 8, 'En préparation', 15000.00, 2);
INSERT INTO Tournoi (id_tournoi, nom, date_debut, date_fin, lieu, format, nb_equipes_max, statut, prix_pool, id_jeu) VALUES
(seq_tournoi_id.NEXTVAL, 'Rocket Championship', TO_DATE('05/05/2025', 'DD/MM/YYYY'), TO_DATE('08/05/2025', 'DD/MM/YYYY'), 'Marseille', 'Double Élimination', 12, 'En préparation', 5000.00, 3);
INSERT INTO Tournoi (id_tournoi, nom, date_debut, date_fin, lieu, format, nb_equipes_max, statut, prix_pool, id_jeu) VALUES
(seq_tournoi_id.NEXTVAL, 'Valorant Open', TO_DATE('20/08/2025', 'DD/MM/YYYY'), TO_DATE('25/08/2025', 'DD/MM/YYYY'), 'Lille', 'Groupes + Élimination directe', 24, 'En préparation', 20000.00, 4);

-- Insertion des équipes
INSERT INTO Equipe (id_equipe, nom, tag, logo, date_creation, pays, description) VALUES
(seq_equipe_id.NEXTVAL, 'Wolves Gaming', 'WLV', 'wolves.png', TO_DATE('15/01/2020', 'DD/MM/YYYY'), 'France', 'Équipe professionnelle française');
INSERT INTO Equipe (id_equipe, nom, tag, logo, date_creation, pays, description) VALUES
(seq_equipe_id.NEXTVAL, 'Arctic Bears', 'ARC', 'bears.png', TO_DATE('30/11/2019', 'DD/MM/YYYY'), 'Suède', 'Équipe scandinave montante');
INSERT INTO Equipe (id_equipe, nom, tag, logo, date_creation, pays, description) VALUES
(seq_equipe_id.NEXTVAL, 'Rapid Gaming', 'RPD', 'rapid.png', TO_DATE('20/03/2021', 'DD/MM/YYYY'), 'Espagne', 'Nouvelle équipe prometteuse');
INSERT INTO Equipe (id_equipe, nom, tag, logo, date_creation, pays, description) VALUES
(seq_equipe_id.NEXTVAL, 'Titan Esports', 'TTN', 'titan.png', TO_DATE('10/07/2018', 'DD/MM/YYYY'), 'Allemagne', 'Organisation historique');
INSERT INTO Equipe (id_equipe, nom, tag, logo, date_creation, pays, description) VALUES
(seq_equipe_id.NEXTVAL, 'Star Warriors', 'STR', 'star.png', TO_DATE('05/01/2022', 'DD/MM/YYYY'), 'Italie', 'Équipe italienne en développement');

-- Insertion des utilisateurs
-- INSERT INTO Utilisateur (id_utilisateur, pseudo, passwd, role) VALUES
-- (seq_utilisateur_id.NEXTVAL, 'admin', 'hashed_password_123', 'ADMIN');

-- mdp : admin123
INSERT INTO Utilisateur (id_utilisateur, pseudo, passwd, role) VALUES
(seq_utilisateur_id.NEXTVAL, 'admin', '$2a$12$yLIGyzwXV9v5oyuV3IopsuYo6grkyL69tTZozU02ZJ9pnnbxqfH.e', 'ADMIN');

-- mdp : org1123
INSERT INTO Utilisateur (id_utilisateur, pseudo, passwd, role) VALUES
(seq_utilisateur_id.NEXTVAL, 'org1', '$2a$12$jHdpOqTNJcyXiYdhWmODKu9B0B.BOeskBW9UIa2Kgbc1xDIB6WKwe', 'ORGANISATEUR');

-- mdp : org2123
INSERT INTO Utilisateur (id_utilisateur, pseudo, passwd, role) VALUES
(seq_utilisateur_id.NEXTVAL, 'org2', '$2a$12$rJ1/Q55EqRY6c99B2KVtAeaPHFTFN48o4X9/nc935gZn5gPHTboX2', 'ORGANISATEUR');

-- mdp : org3123
INSERT INTO Utilisateur (id_utilisateur, pseudo, passwd, role) VALUES
(seq_utilisateur_id.NEXTVAL, 'org3', '$2a$12$MMBcFNy33pMLI39IWdQGK.KFbfKgP9vkNFEB2L0C2uiijSpuR8bGO', 'ORGANISATEUR');

-- Insertion du staff
INSERT INTO Staff (id_staff, nom, prenom, fonction, email, telephone, id_utilisateur) VALUES
(seq_staff_id.NEXTVAL, 'Dubois', 'Pierre', 'Organisateur', 'pierre.dubois@tournois.org', '+33612345678', 1);
INSERT INTO Staff (id_staff, nom, prenom, fonction, email, telephone, id_utilisateur) VALUES
(seq_staff_id.NEXTVAL, 'Lambert', 'Sophie', 'Arbitre', 'sophie.lambert@tournois.org', '+33698765432', 2);
INSERT INTO Staff (id_staff, nom, prenom, fonction, email, telephone, id_utilisateur) VALUES
(seq_staff_id.NEXTVAL, 'Bernard', 'Michel', 'Technicien', 'michel.bernard@tournois.org', '+33654321987', 3);
INSERT INTO Staff (id_staff, nom, prenom, fonction, email, telephone, id_utilisateur) VALUES
(seq_staff_id.NEXTVAL, 'Petit', 'Julie', 'Arbitre', 'julie.petit@tournois.org', '+33678901234', 4);
INSERT INTO Staff (id_staff, nom, prenom, fonction, email, telephone) VALUES
(seq_staff_id.NEXTVAL, 'Moreau', 'Antoine', 'Commentateur', 'antoine.moreau@tournois.org', '+33632109876');

-- Insertion des joueurs
INSERT INTO Joueur (id_joueur, nom, prenom, pseudo, date_naissance, nationalite, email, id_equipe, role) VALUES
(seq_joueur_id.NEXTVAL, 'Martin', 'Thomas', 'Wolf', TO_DATE('15/03/1999', 'DD/MM/YYYY'), 'France', 'wolf@wolves.gg', 1, 'Capitaine');
INSERT INTO Joueur (id_joueur, nom, prenom, pseudo, date_naissance, nationalite, email, id_equipe, role) VALUES
(seq_joueur_id.NEXTVAL, 'Müller', 'Jan', 'Omega', TO_DATE('20/05/2001', 'DD/MM/YYYY'), 'Allemagne', 'omega@wolves.gg', 2, 'Sniper');
INSERT INTO Joueur (id_joueur, nom, prenom, pseudo, date_naissance, nationalite, email, id_equipe, role) VALUES
(seq_joueur_id.NEXTVAL, 'Garcia', 'Carlos', 'Flash', TO_DATE('10/09/1998', 'DD/MM/YYYY'), 'Espagne', 'flash@rapid.gg', 3, 'Capitaine');
INSERT INTO Joueur (id_joueur, nom, prenom, pseudo, date_naissance, nationalite, email, id_equipe, role) VALUES
(seq_joueur_id.NEXTVAL, 'Smith', 'John', 'Thunder', TO_DATE('05/12/1996', 'DD/MM/YYYY'), 'Royaume-Uni', 'thunder@titans.gg', 4, 'Capitaine');
INSERT INTO Joueur (id_joueur, nom, prenom, pseudo, date_naissance, nationalite, email, id_equipe, role) VALUES
(seq_joueur_id.NEXTVAL, 'Rossi', 'Marco', 'Eagle', TO_DATE('22/07/1999', 'DD/MM/YYYY'), 'Italie', 'eagle@stars.gg', 5, 'Capitaine');

-- Inscription des équipes aux tournois
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(1, 1, SYSTIMESTAMP, 'Confirmé', 1);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(1, 2, SYSTIMESTAMP, 'Confirmé', 2);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(1, 3, SYSTIMESTAMP, 'Confirmé', 3);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(1, 4, SYSTIMESTAMP, 'Confirmé', 4);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(2, 2, SYSTIMESTAMP, 'Confirmé', 1);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(2, 4, SYSTIMESTAMP, 'Confirmé', 2);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(2, 5, SYSTIMESTAMP, 'Confirmé', 3);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(3, 1, SYSTIMESTAMP, 'Confirmé', 1);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(3, 3, SYSTIMESTAMP, 'Confirmé', 2);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(3, 5, SYSTIMESTAMP, 'Confirmé', 3);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(4, 1, SYSTIMESTAMP, 'Confirmé', 1);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(4, 2, SYSTIMESTAMP, 'Confirmé', 2);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(4, 3, SYSTIMESTAMP, 'Confirmé', 3);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(4, 4, SYSTIMESTAMP, 'Confirmé', 4);
INSERT INTO Inscription (id_tournoi, id_equipe, date_inscription, statut, seed) VALUES
(4, 5, SYSTIMESTAMP, 'Confirmé', 5);

-- Affectation du staff aux tournois
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(1, 1, 'Responsable tournoi', TO_DATE('15/06/2025 08:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('20/06/2025 20:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(1, 2, 'Arbitre principal', TO_DATE('15/06/2025 09:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('20/06/2025 19:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(1, 3, 'Support technique', TO_DATE('15/06/2025 07:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('20/06/2025 21:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(2, 1, 'Responsable tournoi', TO_DATE('10/07/2025 08:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('15/07/2025 20:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(2, 4, 'Arbitre principal', TO_DATE('10/07/2025 09:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('15/07/2025 19:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(3, 2, 'Arbitre principal', TO_DATE('05/05/2025 09:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('08/05/2025 19:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(3, 5, 'Commentateur officiel', TO_DATE('05/05/2025 10:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('08/05/2025 18:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(4, 1, 'Responsable tournoi', TO_DATE('20/08/2025 08:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('25/08/2025 20:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(4, 2, 'Arbitre - Groupe A', TO_DATE('20/08/2025 09:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('25/08/2025 19:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(4, 4, 'Arbitre - Groupe B', TO_DATE('20/08/2025 09:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('25/08/2025 19:00:00', 'DD/MM/YYYY HH24:MI:SS'));
INSERT INTO Affectation (id_tournoi, id_staff, role_specifique, date_debut, date_fin) VALUES
(4, 5, 'Commentateur officiel', TO_DATE('20/08/2025 10:00:00', 'DD/MM/YYYY HH24:MI:SS'), TO_DATE('25/08/2025 18:00:00', 'DD/MM/YYYY HH24:MI:SS'));

-- Commit des insertions
COMMIT; 