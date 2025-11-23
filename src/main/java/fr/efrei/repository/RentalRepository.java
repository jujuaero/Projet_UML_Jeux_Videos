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
            System.err.println("Erreur lors de la sauvegarde de la location : " + e.getMessage());
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
            System.err.println("Erreur lors de la recherche de la location : " + e.getMessage());
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
            System.err.println("Erreur lors de la récupération des locations : " + e.getMessage());
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
            System.err.println("Erreur lors de la récupération des locations actives : " + e.getMessage());
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
            System.err.println("Erreur lors du retour de la location : " + e.getMessage());
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
            System.err.println("Erreur lors de la suppression de la location : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

