package fr.efrei.repository;

import fr.efrei.domain.Sale;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleRepository implements ISaleRepository {
    private static SaleRepository instance;

    private SaleRepository() {}

    public static SaleRepository getInstance() {
        if (instance == null) {
            instance = new SaleRepository();
        }
        return instance;
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Sale save(Sale sale) {
        String sql = "INSERT INTO sales (id, customer_id, game_id, sale_date, price) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, sale.getId());
            stmt.setString(2, sale.getCustomerId());
            stmt.setString(3, sale.getGameId());
            stmt.setDate(4, Date.valueOf(sale.getDate()));
            stmt.setDouble(5, sale.getPrice());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return sale;
            }
        } catch (SQLException e) {
            System.err.println("Error saving sale: " + e.getMessage());
        }

        return null;
    }

    public Sale findById(String id) {
        String sql = "SELECT * FROM sales WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Sale(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            rs.getString("game_id"),
                            rs.getDate("sale_date").toLocalDate(),
                            rs.getDouble("price")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding sale by ID: " + e.getMessage());
        }

        return null;
    }

    public List<Sale> findAll() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Sale sale = new Sale(
                        rs.getString("id"),
                        rs.getString("customer_id"),
                        rs.getString("game_id"),
                        rs.getDate("sale_date").toLocalDate(),
                        rs.getDouble("price")
                );
                sales.add(sale);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all sales: " + e.getMessage());
        }

        return sales;
    }

    public List<Sale> findByCustomer(String customerId) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE customer_id = ? ORDER BY sale_date DESC";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            rs.getString("game_id"),
                            rs.getDate("sale_date").toLocalDate(),
                            rs.getDouble("price")
                    );
                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding sales by customer: " + e.getMessage());
        }

        return sales;
    }

    public List<Sale> findByGame(String gameId) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE game_id = ? ORDER BY sale_date DESC";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, gameId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale(
                            rs.getString("id"),
                            rs.getString("customer_id"),
                            rs.getString("game_id"),
                            rs.getDate("sale_date").toLocalDate(),
                            rs.getDouble("price")
                    );
                    sales.add(sale);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding sales by game: " + e.getMessage());
        }

        return sales;
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(price), 0) as total FROM sales";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
        }

        return 0.0;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM sales WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting sale: " + e.getMessage());
        }

        return false;
    }
}

