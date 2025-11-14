// John ZHAN - Jules BACART - Lucas RINAUDO

package fr.efrei.domain;

import java.io.Serializable;
import java.util.Objects;


public class Staff implements Serializable {
    private final String staffId;
    private final String name;
    private final String role;

    private Staff(Builder builder) {
        this.staffId = builder.staffId;
        this.name = builder.name;
        this.role = builder.role;
    }

    public String getStaffId() { return staffId; }
    public String getName() { return name; }
    public String getRole() { return role; }

    public static class Builder {
        private String staffId;
        private String name;
        private String role;

        public Builder setStaffId(String staffId) { this.staffId = staffId; return this; }
        public Builder setName(String name) { this.name = name; return this; }
        public Builder setRole(String role) { this.role = role; return this; }

        public Staff build() { return new Staff(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff staff = (Staff) o;
        return Objects.equals(staffId, staff.staffId);
    }

    @Override
    public int hashCode() { return Objects.hash(staffId); }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId='" + staffId + '\'' +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
