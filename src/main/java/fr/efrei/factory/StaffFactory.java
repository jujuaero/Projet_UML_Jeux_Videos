package fr.efrei.factory;


import fr.efrei.domain.Staff;
import fr.efrei.util.Helper;

public final class StaffFactory {
    private StaffFactory() {}

    public static Staff create(String staffId, String name, String role) {
        String finalId = (staffId == null || staffId.isBlank()) ? Helper.IdGenerator.uuid() : staffId;
        validateNotBlank(name, "name");
        validateNotBlank(role, "role");

        return new Staff.Builder()
                .setStaffId(finalId)
                .setName(name.trim())
                .setRole(role.trim())
                .build();
    }

    private static void validateNotBlank(String v, String field) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("Invalid " + field + ": required");
    }
}
