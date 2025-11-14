package fr.efrei;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.Rental;
import fr.efrei.domain.Staff;
import fr.efrei.factory.CustomerFactory;
import fr.efrei.factory.GameFactory;
import fr.efrei.factory.RentalFactory;
import fr.efrei.factory.StaffFactory;

import java.time.LocalDate;

public class GameRentalApp {

    public static void main(String[] args) {

        System.out.println("=== TEST FACTORIES ===");

        // 1) Test GameFactory
        System.out.println("\n-- Creating Game with GameFactory --");
        Game game1 = GameFactory.create(null, "The Legend of Zelda", "Adventure");
        System.out.println(game1);

        // 2) Test CustomerFactory
        System.out.println("\n-- Creating Customer with CustomerFactory --");
        Customer customer1 = CustomerFactory.create(null, "John Doe", "0812345678");
        System.out.println(customer1);

        // 3) Test StaffFactory
        System.out.println("\n-- Creating Staff with StaffFactory --");
        Staff staff1 = StaffFactory.create(null, "Alice Smith", "Manager");
        System.out.println(staff1);

        // 4) Test RentalFactory (rent the game to the customer)
        System.out.println("\n-- Creating Rental with RentalFactory --");
        Rental rental1 = RentalFactory.create(
                null,
                customer1,
                game1,
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );
        System.out.println(rental1);
        System.out.println("Game availability after rental: " + game1.isAvailable());

        // 5) Try to rent the SAME game again (should throw an error)
        System.out.println("\n-- Trying to rent the same Game again (should fail) --");
        try {
            Rental rental2 = RentalFactory.create(
                    null,
                    customer1,
                    game1, // game already rented (available = false)
                    LocalDate.now(),
                    LocalDate.now().plusDays(3)
            );
            System.out.println(rental2); // normalement n’arrive pas ici
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
        }

        System.out.println("\n=== END OF FACTORY TESTS ===");
    }
}
