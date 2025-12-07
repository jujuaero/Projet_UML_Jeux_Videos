package fr.efrei.repository;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.Rental;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalRepository implements IRentalRepository {

    private static RentalRepository instance;

    private RentalRepository() {}

    public static RentalRepository getInstance() {
        if (instance == null) {
            instance = new RentalRepository();
        }
        return instance;
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Rental save(Rental rental) {
        String sql = "INSERT INTO rentals (id, customer_id, game_id, platform, rental_date, return_date, is_returned) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE return_date = VALUES(return_date), is_returned = VALUES(is_returned)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, rental.getRentalId());
            stmt.setString(2, rental.getCustomer().getId());
            stmt.setString(3, rental.getGame().getId());
            stmt.setString(4, rental.getPlatform().name());
            stmt.setDate(5, Date.valueOf(rental.getRentalDate()));
            stmt.setDate(6, Date.valueOf(rental.getReturnDate()));
            stmt.setBoolean(7, rental.isReturned());
            stmt.executeUpdate();
            return rental;
        } catch (SQLException e) {
            System.err.println("Error saving rental: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Rental> findAll() {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = CustomerRepository.getInstance().findById(rs.getString("customer_id"));
                Game game = GameRepository.getInstance().findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    rentals.add(new Rental.Builder()
                        .setRentalId(rs.getString("id"))
                        .setCustomer(customer)
                        .setGame(game)
                        .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                        .setRentalDate(rs.getDate("rental_date").toLocalDate())
                        .setReturnDate(rs.getDate("return_date").toLocalDate())
                        .setReturned(rs.getBoolean("is_returned"))
                        .build());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rentals: " + e.getMessage());
        }
        return rentals;
    }

    @Override
    public Rental findById(String id) {
        String sql = "SELECT * FROM rentals WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = CustomerRepository.getInstance().findById(rs.getString("customer_id"));
                Game game = GameRepository.getInstance().findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    return new Rental.Builder()
                        .setRentalId(rs.getString("id"))
                        .setCustomer(customer)
                        .setGame(game)
                        .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                        .setRentalDate(rs.getDate("rental_date").toLocalDate())
                        .setReturnDate(rs.getDate("return_date").toLocalDate())
                        .setReturned(rs.getBoolean("is_returned"))
                        .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding rental by ID: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Rental> findActiveByCustomer(String customerId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customer_id = ? AND is_returned = false";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer customer = CustomerRepository.getInstance().findById(customerId);
                Game game = GameRepository.getInstance().findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    rentals.add(new Rental.Builder()
                        .setRentalId(rs.getString("id"))
                        .setCustomer(customer)
                        .setGame(game)
                        .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                        .setRentalDate(rs.getDate("rental_date").toLocalDate())
                        .setReturnDate(rs.getDate("return_date").toLocalDate())
                        .setReturned(false)
                        .build());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving active rentals: " + e.getMessage());
        }
        return rentals;
    }

    @Override
    public boolean update(Rental rental) {
        String sql = "UPDATE rentals SET is_returned = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setBoolean(1, rental.isReturned());
            stmt.setString(2, rental.getRentalId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating rental: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Rental> findByCustomer(String customerId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customer_id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer customer = CustomerRepository.getInstance().findById(customerId);
                Game game = GameRepository.getInstance().findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    rentals.add(new Rental.Builder()
                        .setRentalId(rs.getString("id"))
                        .setCustomer(customer)
                        .setGame(game)
                        .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                        .setRentalDate(rs.getDate("rental_date").toLocalDate())
                        .setReturnDate(rs.getDate("return_date").toLocalDate())
                        .setReturned(rs.getBoolean("is_returned"))
                        .build());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rentals: " + e.getMessage());
        }
        return rentals;
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM rentals WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting rental: " + e.getMessage());
        }

        return false;
    }
}

