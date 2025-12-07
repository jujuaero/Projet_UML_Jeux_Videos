package fr.efrei.repository;

import fr.efrei.domain.Employee;
import fr.efrei.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository implements IEmployeeRepository {
    private static EmployeeRepository instance;

    private EmployeeRepository() {}

    public static EmployeeRepository getInstance() {
        if (instance == null) {
            instance = new EmployeeRepository();
        }
        return instance;
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public Employee save(Employee employee) {
        String sql = "INSERT INTO employees (id, name, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, employee.getId());
            stmt.setString(2, employee.getName());
            stmt.setString(3, employee.getEmail());
            stmt.setString(4, employee.getPassword());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return employee;
            }
        } catch (SQLException e) {
            System.err.println("Error saving employee: " + e.getMessage());
        }

        return null;
    }

    public Employee findById(String id) {
        String sql = "SELECT * FROM employees WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding employee by ID: " + e.getMessage());
        }

        return null;
    }

    public Employee findByEmail(String email) {
        String sql = "SELECT * FROM employees WHERE email = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding employee by email: " + e.getMessage());
        }

        return null;
    }

    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY name";

        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee employee = new Employee(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                employees.add(employee);
            }
        } catch (SQLException e) {
            System.err.println("Error finding all employees: " + e.getMessage());
        }

        return employees;
    }

    public boolean update(Employee employee) {
        String sql = "UPDATE employees SET name = ?, email = ?, password = ? WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, employee.getName());
            stmt.setString(2, employee.getEmail());
            stmt.setString(3, employee.getPassword());
            stmt.setString(4, employee.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(String id) {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
        }

        return false;
    }
}

