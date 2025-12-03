package fr.efrei.repository;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.Rental;
import fr.efrei.factory.RentalFactory;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalRepository {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private final CustomerRepository customerRepository = new CustomerRepository();
    private final GameRepository gameRepository = new GameRepository();

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
            System.err.println("Error while saving rental: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Rental findById(String id) {
        String sql = "SELECT * FROM rentals WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = customerRepository.findById(rs.getString("customer_id"));
                Game game = gameRepository.findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    Rental rental = new Rental.Builder()
                            .setRentalId(rs.getString("id"))
                            .setCustomer(customer)
                            .setGame(game)
                            .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                            .setRentalDate(rs.getDate("rental_date").toLocalDate())
                            .setReturnDate(rs.getDate("return_date").toLocalDate())
                            .setReturned(rs.getBoolean("is_returned"))
                            .build();
                    return rental;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while searching for rental: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Rental> findAll() {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = customerRepository.findById(rs.getString("customer_id"));
                Game game = gameRepository.findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    Rental rental = new Rental.Builder()
                            .setRentalId(rs.getString("id"))
                            .setCustomer(customer)
                            .setGame(game)
                            .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                            .setRentalDate(rs.getDate("rental_date").toLocalDate())
                            .setReturnDate(rs.getDate("return_date").toLocalDate())
                            .setReturned(rs.getBoolean("is_returned"))
                            .build();
                    rentals.add(rental);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving rentals: " + e.getMessage());
            e.printStackTrace();
        }
        return rentals;
    }

    public List<Rental> findActiveByCustomer(String customerId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customer_id = ? AND is_returned = false";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer customer = customerRepository.findById(customerId);
                Game game = gameRepository.findById(rs.getString("game_id"));

                if (customer != null && game != null) {
                    Rental rental = new Rental.Builder()
                            .setRentalId(rs.getString("id"))
                            .setCustomer(customer)
                            .setGame(game)
                            .setPlatform(GamePlatform.valueOf(rs.getString("platform")))
                            .setRentalDate(rs.getDate("rental_date").toLocalDate())
                            .setReturnDate(rs.getDate("return_date").toLocalDate())
                            .setReturned(false)
                            .build();
                    rentals.add(rental);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving active rentals: " + e.getMessage());
            e.printStackTrace();
        }
        return rentals;
    }

    public boolean markAsReturned(String rentalId) {
        String sql = "UPDATE rentals SET is_returned = true WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, rentalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error while returning rental: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM rentals WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error while deleting rental: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public double calculateTotalRevenue() {
        String sql = "SELECT SUM(g.price) as total FROM rentals r " +
                     "JOIN games g ON r.game_id = g.id WHERE r.is_returned = TRUE";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error while calculating revenue: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalRentalsCount() {
        String sql = "SELECT COUNT(*) as count FROM rentals";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error while counting rentals: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int getActiveRentalsCount() {
        String sql = "SELECT COUNT(*) as count FROM rentals WHERE is_returned = FALSE";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error while counting active rentals: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}

