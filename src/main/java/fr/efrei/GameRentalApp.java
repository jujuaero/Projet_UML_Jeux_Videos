package fr.efrei;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.Rental;
import fr.efrei.factory.CustomerFactory;
import fr.efrei.factory.RentalFactory;
import fr.efrei.repository.CustomerRepository;
import fr.efrei.repository.GameRepository;
import fr.efrei.repository.RentalRepository;
import fr.efrei.util.DatabaseConnection;
import fr.efrei.util.Helper;

import java.time.LocalDate;
import java.util.List;

public class GameRentalApp {

    public static void main(String[] args) {
        System.out.println("Welcome to our shop");

        DatabaseConnection.getInstance();

        CustomerRepository customerRepo = new CustomerRepository();
        GameRepository gameRepo = new GameRepository();
        RentalRepository rentalRepo = new RentalRepository();

        while (true) {
            Helper.line();
            String contact = Helper.read("Contact (or 'exit' to quit)");
            if ("exit".equalsIgnoreCase(contact)) {
                System.out.println("Goodbye.");
                return;
            }

            Customer customer = null;
            Customer existingCustomer = customerRepo.findByContact(contact);

            if (existingCustomer != null) {
                int attempts = 0;
                boolean auth = false;
                while (attempts < 3 && !auth) {
                    String pass = Helper.read("Password");
                    if (existingCustomer.getPassword().equals(pass)) {
                        auth = true;
                        customer = existingCustomer;
                        System.out.println("Login successful. Welcome " + customer.getName() + "!");
                    } else {
                        attempts++;
                        Helper.error("Incorrect password. Attempt " + attempts + "/3");
                    }
                }
                if (!auth) {
                    Helper.error("Authentication failed. Back to home.");
                    continue;
                }
            } else {
                String name = Helper.read("Name");
                String pass = Helper.read("Choose a password");
                if (pass.isBlank()) {
                    Helper.error("Password is required.");
                    continue;
                }
                try {
                    customer = CustomerFactory.create(null, name, contact, pass);
                    customer = customerRepo.save(customer);
                    if (customer != null) {
                        System.out.println("Registration OK. Welcome " + customer.getName());
                    } else {
                        Helper.error("Error while registering.");
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                    Helper.error("Registration error: " + e.getMessage());
                    continue;
                }
            }

            while (true) {
                System.out.println("What do you want to do?\n1) Rent\n2) Return\n3) Buy\n4) Log out\n5) Quit");
                String choice = Helper.read("Choice (1-5)");
                if (!Helper.isNumber(choice)) { Helper.error("Invalid option"); continue; }
                int ch = Integer.parseInt(choice);

                if (ch == 5) {
                    System.out.println("Goodbye.");
                    return;
                }

                if (ch == 4) {
                    System.out.println("Logging out...");
                    break;
                }

                if (ch == 3) {
                    System.out.println("Choose a platform:\n1) Xbox Series X\n2) PlayStation 5\n3) Windows PC");
                    String p = Helper.read("Your choice (1-3)");
                    GamePlatform requestedPlatform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) requestedPlatform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) requestedPlatform = GamePlatform.PS5;

                    List<Game> available = gameRepo.findAvailableForSaleByPlatform(requestedPlatform);

                    if (available.isEmpty()) {
                        Helper.error("No games for sale on this platform.");
                        continue;
                    }

                    System.out.println("Games for sale:");
                    for (int i = 0; i < available.size(); i++) {
                        Game gg = available.get(i);
                        System.out.println((i + 1) + ") " + gg.getTitle() + " - " + gg.getPlatform() + " | Price: " + String.format("%.2f €", gg.getPrice()));
                    }
                    String sel = Helper.read("Number of the game to buy");
                    if (!Helper.isNumber(sel)) { Helper.error("Invalid selection"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= available.size()) { Helper.error("Index out of range"); continue; }
                    Game chosen = available.get(idx);

                    if (gameRepo.updateAvailability(chosen.getId(), false)) {
                        System.out.println("Purchase OK: " + chosen.getTitle());
                    } else {
                        Helper.error("Error while buying the game.");
                    }
                    continue;
                }

                if (ch == 2) {
                    List<Rental> active = rentalRepo.findActiveByCustomer(customer.getId());

                    if (active.isEmpty()) {
                        Helper.error("You have no active rentals.");
                        continue;
                    }
                    System.out.println("Your active rentals:");
                    for (int i = 0; i < active.size(); i++) {
                        Rental r = active.get(i);
                        System.out.println((i + 1) + ") " + r.getGame().getTitle() + " - due date: " + r.getReturnDate());
                    }
                    String sel = Helper.read("Number to return");
                    if (!Helper.isNumber(sel)) { Helper.error("Invalid selection"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= active.size()) { Helper.error("Index out of range"); continue; }
                    Rental toReturn = active.get(idx);

                    if (rentalRepo.markAsReturned(toReturn.getRentalId())) {
                        gameRepo.updateAvailability(toReturn.getGame().getId(), true);
                        System.out.println("Game returned: " + toReturn.getGame().getTitle());
                    } else {
                        Helper.error("Error while returning the game.");
                    }
                    continue;
                }

                if (ch == 1) {
                    System.out.println("Choose a platform:\n1) Xbox Series X\n2) PlayStation 5\n3) Windows PC");
                    String p = Helper.read("Your choice (1-3)");
                    GamePlatform requestedPlatform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) requestedPlatform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) requestedPlatform = GamePlatform.PS5;

                    List<Game> available = gameRepo.findAvailableByPlatform(requestedPlatform);

                    if (available.isEmpty()) {
                        Helper.error("No games available for this platform.");
                        continue;
                    }

                    System.out.println("Available games:");
                    for (int i = 0; i < available.size(); i++) {
                        Game gg = available.get(i);
                        System.out.println((i + 1) + ") " + gg.getTitle() + " - " + gg.getPlatform() + " | Price: " + String.format("%.2f €", gg.getPrice()));
                    }
                    String sel = Helper.read("Number of the game to rent");
                    if (!Helper.isNumber(sel)) { Helper.error("Invalid selection"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= available.size()) { Helper.error("Index out of range"); continue; }
                    Game chosen = available.get(idx);

                    System.out.println("Duration: 1) 1 day  2) 1 week  3) 1 month");
                    String dur = Helper.read("Choice (1-3)");
                    int days = 1;
                    if ("2".equals(dur)) days = 7;
                    else if ("3".equals(dur)) days = 30;

                    LocalDate now = LocalDate.now();
                    LocalDate plannedReturn = now.plusDays(days);

                    try {
                        Rental rental = RentalFactory.create(null, customer, chosen, requestedPlatform, now, plannedReturn);
                        rental = rentalRepo.save(rental);

                        if (rental != null) {
                            gameRepo.updateAvailability(chosen.getId(), false);
                            System.out.println("Rental OK: " + rental.getGame().getTitle() + " until " + rental.getReturnDate());
                        } else {
                            Helper.error("Error while saving the rental.");
                        }
                    } catch (Exception e) {
                        Helper.error("Rental error: " + e.getMessage());
                    }
                    continue;
                }

                Helper.error("Invalid option");
            }
        }
    }
}