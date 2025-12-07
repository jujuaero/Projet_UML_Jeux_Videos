package fr.efrei.repository;

import fr.efrei.domain.Rental;
import java.util.List;

public interface IRentalRepository extends IRepository<Rental> {

    List<Rental> findActiveByCustomer(String customerId);

    List<Rental> findByCustomer(String customerId);
}