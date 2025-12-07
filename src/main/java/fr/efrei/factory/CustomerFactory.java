package fr.efrei.factory;

import fr.efrei.domain.Customer;
import fr.efrei.util.Helper;

public final class CustomerFactory {
    private CustomerFactory() {}

    public static Customer create(String id, String name, String contactNumber) {
        return create(id, name, contactNumber, "");
    }

    public static Customer create(String id, String name, String contactNumber, String password) {
        String finalId = (id == null || id.isBlank()) ? Helper.IdGenerator.uuid() : id;
        validateNotBlank(name, "name");
        validateNotBlank(contactNumber, "contactNumber");

        return new Customer.Builder()
                .setId(finalId)
                .setName(name.trim())
                .setContactNumber(contactNumber.trim())
                .setPassword(password != null ? password : "")
                .setLoyaltyPoints(0)
                .build();
    }

    private static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
    }
}

