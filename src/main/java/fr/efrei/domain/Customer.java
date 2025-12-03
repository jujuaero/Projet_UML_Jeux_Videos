
package fr.efrei.domain;

import java.io.Serializable;
import java.util.Objects;

public class Customer implements Serializable {
    private final String id;
    private final String name;
    private final String contactNumber;
    private final String password;
    private Role role;

    private Customer(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.contactNumber = builder.contactNumber;
        this.password = builder.password;
        this.role = builder.role != null ? builder.role : Role.CUSTOMER;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getContactNumber() { return contactNumber; }
    public String getContact() { return contactNumber; }
    public String getPassword() { return password; }

    public static class Builder {
        private String id;
        private String name;
        private String contactNumber;
        private String password;
        private Role role;

        public Builder setId(String id) { this.id = id; return this; }
        public Builder setName(String name) { this.name = name; return this; }
        public Builder setContactNumber(String contactNumber) { this.contactNumber = contactNumber; return this; }
        public Builder setPassword(String password) { this.password = password; return this; }
        public Builder setRole(Role role) { this.role = role; return this; }

        public Customer build() { return new Customer(this); }
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer that = (Customer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
