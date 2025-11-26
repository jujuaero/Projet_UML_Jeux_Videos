package fr.efrei.repository;

import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.factory.GameFactory;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameRepository {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Game save(Game game) {
        String sql = "INSERT INTO games (id, title, genre, platform, is_available, type, price) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE title = VALUES(title), genre = VALUES(genre), " +
                     "platform = VALUES(platform), is_available = VALUES(is_available), type = VALUES(type), price = VALUES(price)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, game.getId());
            stmt.setString(2, game.getTitle());
            stmt.setString(3, game.getGenre());
            stmt.setString(4, game.getPlatform().name());
            stmt.setBoolean(5, game.isAvailable());
            stmt.setString(6, game.getType().name());
            stmt.setDouble(7, game.getPrice());
            stmt.executeUpdate();
            return game;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde du jeu : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Game findById(String id) {
        String sql = "SELECT * FROM games WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Game game = GameFactory.create(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    GamePlatform.valueOf(rs.getString("platform")),
                    rs.getBoolean("is_available"),
                    fr.efrei.domain.GameType.valueOf(rs.getString("type")),
                    rs.getDouble("price")
                );
                return game;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du jeu : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Game game = GameFactory.create(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    GamePlatform.valueOf(rs.getString("platform")),
                    rs.getBoolean("is_available"),
                    fr.efrei.domain.GameType.valueOf(rs.getString("type")),
                    rs.getDouble("price")
                );
                games.add(game);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des jeux : " + e.getMessage());
            e.printStackTrace();
        }
        return games;
    }

    public List<Game> findAvailableByPlatform(GamePlatform platform) {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE is_available = true";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                GamePlatform gamePlatform = GamePlatform.valueOf(rs.getString("platform"));
                if (gamePlatform.isCompatibleWith(platform)) {
                    Game game = GameFactory.create(
                        rs.getString("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        gamePlatform,
                        rs.getBoolean("is_available"),
                        fr.efrei.domain.GameType.valueOf(rs.getString("type")),
                        rs.getDouble("price")
                    );
                    games.add(game);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des jeux : " + e.getMessage());
        }
        return games;
    }

    public List<Game> findAvailableForSaleByPlatform(GamePlatform platform) {
        String sql = "SELECT * FROM games WHERE platform = ? AND is_available = TRUE AND type = 'SALE'";
        List<Game> games = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, platform.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game game = GameFactory.create(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    GamePlatform.valueOf(rs.getString("platform")),
                    rs.getBoolean("is_available"),
                    fr.efrei.domain.GameType.valueOf(rs.getString("type")),
                    rs.getDouble("price")
                );
                games.add(game);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des jeux à vendre : " + e.getMessage());
            e.printStackTrace();
        }
        return games;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM games WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du jeu : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAvailability(String gameId, boolean available) {
        String sql = "UPDATE games SET is_available = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, available);
            stmt.setString(2, gameId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la disponibilité : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
