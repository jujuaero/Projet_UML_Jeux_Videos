-- Script SQL pour créer la base de données CapeTownGaming
-- À exécuter dans MySQL Workbench

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS CapeTownGaming
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE CapeTownGaming;

-- Table des clients
CREATE TABLE IF NOT EXISTS customers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des jeux
CREATE TABLE IF NOT EXISTS games (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100) NOT NULL,
    platform ENUM('XBOX_ONE', 'XBOX_SERIES_X', 'XBOX_SERIES_S', 'PS4', 'PS5', 'PC_WINDOWS', 'PC_MAC', 'PC_LINUX') NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table des locations
CREATE TABLE IF NOT EXISTS rentals (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    game_id VARCHAR(36) NOT NULL,
    platform ENUM('XBOX_ONE', 'XBOX_SERIES_X', 'XBOX_SERIES_S', 'PS4', 'PS5', 'PC_WINDOWS', 'PC_MAC', 'PC_LINUX') NOT NULL,
    rental_date DATE NOT NULL,
    return_date DATE NOT NULL,
    is_returned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index pour améliorer les performances
CREATE INDEX idx_customer_contact ON customers(contact_number);
CREATE INDEX idx_game_platform ON games(platform, is_available);
CREATE INDEX idx_rental_customer ON rentals(customer_id);
CREATE INDEX idx_rental_game ON rentals(game_id);
CREATE INDEX idx_rental_active ON rentals(is_returned);

-- Insertion de quelques jeux de démonstration
INSERT INTO games (id, title, genre, platform, is_available) VALUES
('game-1', 'Halo Infinite', 'Shooter', 'XBOX_SERIES_X', TRUE),
('game-2', 'Forza Horizon 5', 'Racing', 'XBOX_SERIES_X', TRUE),
('game-3', 'God of War Ragnarök', 'Action', 'PS5', TRUE),
('game-4', 'The Last of Us Part II', 'Action', 'PS4', TRUE),
('game-5', 'Cyberpunk 2077', 'RPG', 'PC_WINDOWS', TRUE),
('game-6', 'Elden Ring', 'RPG', 'PC_WINDOWS', TRUE),
('game-7', 'Spider-Man Miles Morales', 'Action', 'PS5', TRUE),
('game-8', 'Gears 5', 'Shooter', 'XBOX_ONE', TRUE);

-- Vérification des tables créées
SHOW TABLES;

-- Affichage de la structure des tables
DESCRIBE customers;
DESCRIBE games;
DESCRIBE rentals;

