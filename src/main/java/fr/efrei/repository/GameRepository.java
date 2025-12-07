package fr.efrei.repository;

import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.GameType;
import fr.efrei.factory.GameFactory;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameRepository implements IGameRepository {

    private static GameRepository instance;

    private GameRepository() {}

    public static GameRepository getInstance() {
        if (instance == null) {
            instance = new GameRepository();
        }
        return instance;
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Game save(Game game) {
        String sql = "INSERT INTO games (id, title, genre, platform, is_available, type, price) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, game.getId());
            stmt.setString(2, game.getTitle());
            stmt.setString(3, game.getGenre());
            stmt.setString(4, game.getPlatform().name());
            stmt.setBoolean(5, game.isAvailable());
            stmt.setString(6, game.getType().name());
            stmt.setDouble(7, game.getPrice());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return game;
            }
        } catch (SQLException e) {
            System.err.println("Error saving game: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Game findById(String id) {
        String sql = "SELECT * FROM games WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return GameFactory.create(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    GamePlatform.valueOf(rs.getString("platform")),
                    rs.getBoolean("is_available"),
                    GameType.valueOf(rs.getString("type")),
                    rs.getDouble("price")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Game> findAll() {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY title";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Game game = GameFactory.create(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    GamePlatform.valueOf(rs.getString("platform")),
                    rs.getBoolean("is_available"),
                    GameType.valueOf(rs.getString("type")),
                    rs.getDouble("price")
                );
                games.add(game);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all games: " + e.getMessage());
        }

        return games;
    }

    @Override
    public List<Game> findByPlatformAndType(GamePlatform platform, GameType type) {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE platform = ? AND type = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, platform.name());
            stmt.setString(2, type.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                games.add(GameFactory.create(
                    rs.getString("id"),
                    rs.getString("title"),
                    rs.getString("genre"),
                    GamePlatform.valueOf(rs.getString("platform")),
                    rs.getBoolean("is_available"),
                    GameType.valueOf(rs.getString("type")),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public boolean update(Game game) {
        String sql = "UPDATE games SET title = ?, genre = ?, platform = ?, is_available = ?, type = ?, price = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, game.getTitle());
            stmt.setString(2, game.getGenre());
            stmt.setString(3, game.getPlatform().name());
            stmt.setBoolean(4, game.isAvailable());
            stmt.setString(5, game.getType().name());
            stmt.setDouble(6, game.getPrice());
            stmt.setString(7, game.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating game: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM games WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting game: " + e.getMessage());
        }

        return false;
    }
}

