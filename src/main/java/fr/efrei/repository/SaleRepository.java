package fr.efrei.repository;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.Sale;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleRepository {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private CustomerRepository customerRepo = new CustomerRepository();
    private GameRepository gameRepo = new GameRepository();

    public Sale save(Sale sale) {
        String sql = "INSERT INTO sales (id, customer_id, game_id, sale_date, price) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, sale.getId());
            stmt.setString(2, sale.getCustomer().getId());
            stmt.setString(3, sale.getGame().getId());
            stmt.setDate(4, Date.valueOf(sale.getDate()));
            stmt.setDouble(5, sale.getPrice());
            stmt.executeUpdate();
            return sale;
        } catch (SQLException e) {
            System.err.println("Error saving sale: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Sale> findAll() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer c = customerRepo.findById(rs.getString("customer_id"));
                Game g = gameRepo.findById(rs.getString("game_id"));

                if (c != null && g != null) {
                    Sale s = new Sale(
                            rs.getString("id"),
                            c,
                            g,
                            rs.getDate("sale_date").toLocalDate(),
                            rs.getDouble("price")
                    );
                    sales.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting sales: " + e.getMessage());
        }
        return sales;
    }

    // calculate total money from sales
    public double calculateTotalSalesRevenue() {
        String sql = "SELECT SUM(price) as total FROM sales";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating revenue: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalSalesCount() {
        String sql = "SELECT COUNT(*) as count FROM sales";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error counting sales: " + e.getMessage());
        }
        return 0;
    }
}
