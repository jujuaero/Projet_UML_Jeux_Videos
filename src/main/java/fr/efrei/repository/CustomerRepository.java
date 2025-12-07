package fr.efrei.repository;

import fr.efrei.domain.Customer;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository implements ICustomerRepository {
    private static CustomerRepository instance;

    private CustomerRepository() {}

    public static CustomerRepository getInstance() {
        if (instance == null) {
            instance = new CustomerRepository();
        }
        return instance;
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (id, name, contact_number, password, loyalty_points) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customer.getId());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getContactNumber());
            stmt.setString(4, customer.getPassword());
            stmt.setInt(5, customer.getLoyaltyPoints());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return customer;
            }
        } catch (SQLException e) {
            System.err.println("Error saving customer: " + e.getMessage());
        }

        return null;
    }

    public Customer findById(String id) {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer.Builder()
                            .setId(rs.getString("id"))
                            .setName(rs.getString("name"))
                            .setContactNumber(rs.getString("contact_number"))
                            .setPassword(rs.getString("password"))
                            .setLoyaltyPoints(rs.getInt("loyalty_points"))
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding customer by ID: " + e.getMessage());
        }

        return null;
    }

    public Customer findByContact(String contactNumber) {
        String sql = "SELECT * FROM customers WHERE contact_number = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, contactNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer.Builder()
                            .setId(rs.getString("id"))
                            .setName(rs.getString("name"))
                            .setContactNumber(rs.getString("contact_number"))
                            .setPassword(rs.getString("password"))
                            .setLoyaltyPoints(rs.getInt("loyalty_points"))
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding customer by contact: " + e.getMessage());
        }

        return null;
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = new Customer.Builder()
                        .setId(rs.getString("id"))
                        .setName(rs.getString("name"))
                        .setContactNumber(rs.getString("contact_number"))
                        .setPassword(rs.getString("password"))
                        .setLoyaltyPoints(rs.getInt("loyalty_points"))
                        .build();
                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all customers: " + e.getMessage());
        }

        return customers;
    }

    public boolean updateLoyaltyPoints(String customerId, int loyaltyPoints) {
        String sql = "UPDATE customers SET loyalty_points = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, loyaltyPoints);
            stmt.setString(2, customerId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating loyalty points: " + e.getMessage());
        }

        return false;
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, contact_number = ?, password = ?, loyalty_points = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getContactNumber());
            stmt.setString(3, customer.getPassword());
            stmt.setInt(4, customer.getLoyaltyPoints());
            stmt.setString(5, customer.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
        }

        return false;
    }
}

