package fr.efrei.repository;

import fr.efrei.domain.Customer;
import fr.efrei.factory.CustomerFactory;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (id, name, contact_number, password, role) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), contact_number = VALUES(contact_number), password = VALUES(password), role = VALUES(role)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, customer.getId());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getContactNumber());
            stmt.setString(4, customer.getPassword());
            stmt.setString(5, customer.getRole().name());
            stmt.executeUpdate();
            return customer;
        } catch (SQLException e) {
            System.err.println("Error while saving customer: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Customer findByContact(String contactNumber) {
        String sql = "SELECT * FROM customers WHERE contact_number = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, contactNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String roleStr = rs.getString("role");
                fr.efrei.domain.Role role = (roleStr != null) ? fr.efrei.domain.Role.valueOf(roleStr) : fr.efrei.domain.Role.CUSTOMER;
                return CustomerFactory.create(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    rs.getString("password"),
                    role
                );
            }
        } catch (SQLException e) {
            System.err.println("Error while searching for customer: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Customer findById(String id) {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String roleStr = rs.getString("role");
                fr.efrei.domain.Role role = (roleStr != null) ? fr.efrei.domain.Role.valueOf(roleStr) : fr.efrei.domain.Role.CUSTOMER;
                return CustomerFactory.create(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    rs.getString("password"),
                    role
                );
            }
        } catch (SQLException e) {
            System.err.println("Error while searching for customer: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String roleStr = rs.getString("role");
                fr.efrei.domain.Role role = (roleStr != null) ? fr.efrei.domain.Role.valueOf(roleStr) : fr.efrei.domain.Role.CUSTOMER;
                customers.add(CustomerFactory.create(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("contact_number"),
                    rs.getString("password"),
                    role
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving customers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error while deleting customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

