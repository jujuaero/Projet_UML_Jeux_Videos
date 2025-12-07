package fr.efrei.repository;

import fr.efrei.domain.Sale;
import java.util.List;

public interface ISaleRepository extends IRepository<Sale> {

    List<Sale> findByCustomer(String customerId);

    List<Sale> findByGame(String gameId);

    double getTotalRevenue();
}

