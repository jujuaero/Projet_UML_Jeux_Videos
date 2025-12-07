package fr.efrei.repository;

import fr.efrei.domain.Customer;

public interface ICustomerRepository extends IRepository<Customer> {

    Customer findByContact(String contactNumber);

    boolean updateLoyaltyPoints(String customerId, int loyaltyPoints);
}

