package fr.efrei.factory;


import fr.efrei.domain.Customer;
import fr.efrei.util.Helper;

public final class CustomerFactory {
    private CustomerFactory() {}

    public static Customer create(String id, String name, String contactNumber) {
        String finalId = (id == null || id.isBlank()) ? Helper.IdGenerator.uuid() : id;
        validateNotBlank(name, "name");
        validateNotBlank(contactNumber, "contactNumber");

        return new Customer.Builder()
                .setId(finalId)
                .setName(name.trim())
                .setContactNumber(contactNumber.trim())
                .build();
    }

    private static void validateNotBlank(String v, String field) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("Invalid " + field + ": required");
    }
}
