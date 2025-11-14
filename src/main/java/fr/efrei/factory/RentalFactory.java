package fr.efrei.factory;



import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.Rental;
import fr.efrei.util.Helper;

import java.time.LocalDate;

public final class RentalFactory {
    private RentalFactory() {}

    public static Rental create(String rentalId, Customer customer, Game game,
                                LocalDate rentalDate, LocalDate plannedReturnDate) {
        String finalId = (rentalId == null || rentalId.isBlank()) ? Helper.IdGenerator.uuid() : rentalId;

        if (customer == null) throw new IllegalArgumentException("customer is required");
        if (game == null) throw new IllegalArgumentException("game is required");
        if (rentalDate == null) rentalDate = LocalDate.now();
        if (!game.isAvailable())
            throw new IllegalStateException("Game '" + game.getTitle() + "' is not available");

        // Business rule: when renting, the game becomes unavailable
        game.setAvailable(false);

        return new Rental.Builder()
                .setRentalId(finalId)
                .setCustomer(customer)
                .setGame(game)
                .setRentalDate(rentalDate)
                .setReturnDate(plannedReturnDate) // can be null
                .setReturned(false)
                .build();
    }
}
