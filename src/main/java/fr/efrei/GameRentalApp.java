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

        // init db connection
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
                // existing user - check password
                int attempts = 0;
                boolean authenticated = false;

                while (attempts < 3 && !authenticated) {
                    String pass = Helper.read("Password");
                    if (existingCustomer.getPassword().equals(pass)) {
                        authenticated = true;
                        customer = existingCustomer;
                        System.out.println("Login successful. Welcome back " + customer.getName() + "!");
                    } else {
                        attempts++;
                        Helper.error("Wrong password. Try " + attempts + "/3");
                    }
                }

                if (!authenticated) {
                    Helper.error("Too many failed attempts. Going back...");
                    continue;
                }
            } else {
                // new user - let's register them
                String name = Helper.read("Name");
                String pass = Helper.read("Choose a password");

                if (pass.isBlank()) {
                    Helper.error("You need a password!");
                    continue;
                }

                try {
                    customer = CustomerFactory.create(null, name, contact, pass);
                    customer = customerRepo.save(customer);
                    if (customer != null) {
                        System.out.println("Account created! Welcome " + customer.getName());
                    } else {
                        Helper.error("Oops, something went wrong during registration.");
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                    Helper.error("Registration error: " + e.getMessage());
                    continue;
                }
            }

            // check if admin
            if (customer.isAdmin()) {
                handleAdminMenu(customer, gameRepo, rentalRepo, customerRepo);
                continue;
            }

            // regular customer menu
            while (true) {
                System.out.println("\nWhat would you like to do?");
                System.out.println("1) Rent a game");
                System.out.println("2) Return a game");
                System.out.println("3) Buy a game");
                System.out.println("4) Log out");
                System.out.println("5) Quit");

                String choice = Helper.read("Your choice");
                if (!Helper.isNumber(choice)) { 
                    Helper.error("Please enter a number"); 
                    continue; 
                }

                int ch = Integer.parseInt(choice);

                if (ch == 5) {
                    System.out.println("Goodbye!");
                    return;
                }

                if (ch == 4) {
                    System.out.println("Logging out...");
                    break;
                }

                if (ch == 3) {
                    // buying a game
                    System.out.println("\nWhich platform?");
                    System.out.println("1) Xbox Series X");
                    System.out.println("2) PlayStation 5");
                    System.out.println("3) Windows PC");

                    String p = Helper.read("Platform");
                    GamePlatform platform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) platform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) platform = GamePlatform.PS5;

                    List<Game> gamesForSale = gameRepo.findAvailableForSaleByPlatform(platform);

                    if (gamesForSale.isEmpty()) {
                        Helper.error("Sorry, no games available for sale on this platform right now.");
                        continue;
                    }

                    System.out.println("\nGames for sale:");
                    for (int i = 0; i < gamesForSale.size(); i++) {
                        Game g = gamesForSale.get(i);
                        System.out.println((i + 1) + ") " + g.getTitle() + " - " + String.format("%.2f €", g.getPrice()));
                    }

                    String sel = Helper.read("Which game do you want to buy?");
                    if (!Helper.isNumber(sel)) { 
                        Helper.error("Please enter a valid number"); 
                        continue; 
                    }

                    int index = Integer.parseInt(sel) - 1;
                    if (index < 0 || index >= gamesForSale.size()) { 
                        Helper.error("That number is not in the list"); 
                        continue; 
                    }

                    Game selectedGame = gamesForSale.get(index);

                    if (gameRepo.updateAvailability(selectedGame.getId(), false)) {
                        System.out.println("Great! You bought: " + selectedGame.getTitle());
                    } else {
                        Helper.error("Something went wrong with the purchase...");
                    }
                    continue;
                }

                if (ch == 2) {
                    // return a game
                    List<Rental> activeRentals = rentalRepo.findActiveByCustomer(customer.getId());

                    if (activeRentals.isEmpty()) {
                        Helper.error("You don't have any active rentals.");
                        continue;
                    }

                    System.out.println("\nYour current rentals:");
                    for (int i = 0; i < activeRentals.size(); i++) {
                        Rental r = activeRentals.get(i);
                        System.out.println((i + 1) + ") " + r.getGame().getTitle() + " (due: " + r.getReturnDate() + ")");
                    }

                    String sel = Helper.read("Which one do you want to return?");
                    if (!Helper.isNumber(sel)) { 
                        Helper.error("Please enter a number"); 
                        continue; 
                    }

                    int index = Integer.parseInt(sel) - 1;
                    if (index < 0 || index >= activeRentals.size()) { 
                        Helper.error("Invalid number"); 
                        continue; 
                    }

                    Rental rental = activeRentals.get(index);

                    if (rentalRepo.markAsReturned(rental.getRentalId())) {
                        gameRepo.updateAvailability(rental.getGame().getId(), true);
                        System.out.println("Thanks! " + rental.getGame().getTitle() + " has been returned.");
                    } else {
                        Helper.error("Couldn't process the return...");
                    }
                    continue;
                }

                if (ch == 1) {
                    // rent a game
                    System.out.println("\nSelect your platform:");
                    System.out.println("1) Xbox Series X");
                    System.out.println("2) PlayStation 5");
                    System.out.println("3) Windows PC");

                    String p = Helper.read("Platform");
                    GamePlatform platform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) platform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) platform = GamePlatform.PS5;

                    List<Game> availableGames = gameRepo.findAvailableByPlatform(platform);

                    if (availableGames.isEmpty()) {
                        Helper.error("No games available for rental on this platform.");
                        continue;
                    }

                    System.out.println("\nGames you can rent:");
                    for (int i = 0; i < availableGames.size(); i++) {
                        Game g = availableGames.get(i);
                        System.out.println((i + 1) + ") " + g.getTitle() + " - " + String.format("%.2f €", g.getPrice()));
                    }

                    String sel = Helper.read("Which game?");
                    if (!Helper.isNumber(sel)) { 
                        Helper.error("Enter a valid number"); 
                        continue; 
                    }

                    int index = Integer.parseInt(sel) - 1;
                    if (index < 0 || index >= availableGames.size()) { 
                        Helper.error("That's not a valid choice"); 
                        continue; 
                    }

                    Game selectedGame = availableGames.get(index);

                    System.out.println("\nHow long do you want to rent it?");
                    System.out.println("1) 1 day");
                    System.out.println("2) 1 week");
                    System.out.println("3) 1 month");

                    String duration = Helper.read("Duration");
                    int days = 1;
                    if ("2".equals(duration)) days = 7;
                    else if ("3".equals(duration)) days = 30;

                    LocalDate today = LocalDate.now();
                    LocalDate returnDate = today.plusDays(days);

                    try {
                        Rental rental = RentalFactory.create(null, customer, selectedGame, platform, today, returnDate);
                        rental = rentalRepo.save(rental);

                        if (rental != null) {
                            gameRepo.updateAvailability(selectedGame.getId(), false);
                            System.out.println("Perfect! You rented " + rental.getGame().getTitle() + " until " + rental.getReturnDate());
                        } else {
                            Helper.error("Something went wrong...");
                        }
                    } catch (Exception e) {
                        Helper.error("Error: " + e.getMessage());
                    }
                    continue;
                }

                Helper.error("Not a valid option");
            }
        }
    }

    private static void handleAdminMenu(Customer admin, GameRepository gameRepo, 
                                       RentalRepository rentalRepo, CustomerRepository customerRepo) {
        while (true) {
            Helper.line();
            System.out.println("=== ADMIN PANEL ===");
            System.out.println("1) Add a game");
            System.out.println("2) Delete a game");
            System.out.println("3) View all games");
            System.out.println("4) View revenue");
            System.out.println("5) Manage users");
            System.out.println("6) Log out");
            System.out.println("7) Quit");

            String choice = Helper.read("What do you want to do?");
            if (!Helper.isNumber(choice)) { 
                Helper.error("Please enter a number"); 
                continue; 
            }

            int option = Integer.parseInt(choice);

            if (option == 7) {
                System.out.println("Bye!");
                System.exit(0);
            }

            if (option == 6) {
                System.out.println("Logging out...");
                return;
            }

            if (option == 1) {
                // add new game
                String title = Helper.read("Game title");
                String genre = Helper.read("Genre");

                System.out.println("\nPlatform:");
                System.out.println("1) Xbox Series X");
                System.out.println("2) PlayStation 5");
                System.out.println("3) Windows PC");
                String p = Helper.read("Choose platform");

                GamePlatform platform = GamePlatform.PC_WINDOWS;
                if ("1".equals(p)) platform = GamePlatform.XBOX_SERIES_X;
                else if ("2".equals(p)) platform = GamePlatform.PS5;

                String priceInput = Helper.read("Price in €");
                double price = 0.0;
                try {
                    price = Double.parseDouble(priceInput);
                } catch (NumberFormatException e) {
                    Helper.error("That's not a valid price");
                    continue;
                }

                System.out.println("\nType:");
                System.out.println("1) For rent");
                System.out.println("2) For sale");
                String typeChoice = Helper.read("Type");
                fr.efrei.domain.GameType type = fr.efrei.domain.GameType.RENTAL;
                if ("2".equals(typeChoice)) type = fr.efrei.domain.GameType.SALE;

                String avail = Helper.read("Is it available? (y/n)");
                boolean available = "y".equalsIgnoreCase(avail);

                Game newGame = gameRepo.addGame(title, genre, platform, price, available, type);
                if (newGame != null) {
                    System.out.println("Done! Added: " + newGame.getTitle());
                } else {
                    Helper.error("Couldn't add the game");
                }
                continue;
            }

            if (option == 2) {
                // delete a game
                List<Game> games = gameRepo.findAll();
                if (games.isEmpty()) {
                    Helper.error("No games in the database");
                    continue;
                }

                System.out.println("\nAll games:");
                for (int i = 0; i < games.size(); i++) {
                    Game g = games.get(i);
                    String status = g.isAvailable() ? "available" : "not available";
                    System.out.println((i + 1) + ") " + g.getTitle() + " (" + g.getPlatform() + ") - " + 
                                     String.format("%.2f €", g.getPrice()) + " [" + status + "]");
                }

                String selection = Helper.read("Which game do you want to delete?");
                if (!Helper.isNumber(selection)) { 
                    Helper.error("Enter a number"); 
                    continue; 
                }

                int index = Integer.parseInt(selection) - 1;
                if (index < 0 || index >= games.size()) { 
                    Helper.error("Invalid number"); 
                    continue; 
                }

                Game gameToDelete = games.get(index);
                String confirm = Helper.read("Are you sure you want to delete '" + gameToDelete.getTitle() + "'? (y/n)");

                if ("y".equalsIgnoreCase(confirm)) {
                    if (gameRepo.deleteGame(gameToDelete.getId())) {
                        System.out.println("Deleted!");
                    } else {
                        Helper.error("Couldn't delete the game");
                    }
                }
                continue;
            }

            if (option == 3) {
                // view all games
                List<Game> games = gameRepo.findAll();
                if (games.isEmpty()) {
                    System.out.println("No games yet");
                    continue;
                }

                System.out.println("\n=== ALL GAMES ===");
                for (int i = 0; i < games.size(); i++) {
                    Game g = games.get(i);
                    String status = g.isAvailable() ? "Available" : "Not available";
                    System.out.println("game-" + (i + 1) + " | " + g.getTitle() + " (" + g.getPlatform() + ") - " + 
                                     String.format("%.2f €", g.getPrice()) + " - " + status);
                }
                continue;
            }

            if (option == 4) {
                // show revenue stats
                double totalRevenue = rentalRepo.calculateTotalRevenue();
                int total = rentalRepo.getTotalRentalsCount();
                int active = rentalRepo.getActiveRentalsCount();
                int returned = total - active;

                System.out.println("\n=== REVENUE STATS ===");
                System.out.println("Total revenue: " + String.format("%.2f €", totalRevenue));
                System.out.println("Total rentals: " + total);
                System.out.println("Currently rented: " + active);
                System.out.println("Returned: " + returned);
                continue;
            }

            if (option == 5) {
                // user management
                List<Customer> users = customerRepo.findAll();
                if (users.isEmpty()) {
                    System.out.println("No users yet");
                    continue;
                }

                System.out.println("\n=== USER MANAGEMENT ===");
                System.out.println("1) View all users");
                System.out.println("2) Delete a user");
                System.out.println("3) Back");

                String action = Helper.read("What do you want to do?");

                if ("1".equals(action)) {
                    System.out.println("\nAll users:");
                    for (Customer c : users) {
                        System.out.println("- " + c.getName() + " (" + c.getContact() + ") - " + c.getRole());
                    }

                } else if ("2".equals(action)) {
                    System.out.println("\nUsers:");
                    for (int i = 0; i < users.size(); i++) {
                        Customer c = users.get(i);
                        System.out.println((i + 1) + ") " + c.getName() + " (" + c.getContact() + ") - " + c.getRole());
                    }

                    String selection = Helper.read("Which user to delete?");
                    if (!Helper.isNumber(selection)) { 
                        Helper.error("Enter a number"); 
                        continue; 
                    }

                    int index = Integer.parseInt(selection) - 1;
                    if (index < 0 || index >= users.size()) { 
                        Helper.error("Invalid number"); 
                        continue; 
                    }

                    Customer userToDelete = users.get(index);

                    // can't delete yourself
                    if (userToDelete.getId().equals(admin.getId())) {
                        Helper.error("You can't delete your own account!");
                        continue;
                    }

                    String confirm = Helper.read("Delete '" + userToDelete.getName() + "'? (y/n)");
                    if ("y".equalsIgnoreCase(confirm)) {
                        if (customerRepo.delete(userToDelete.getId())) {
                            System.out.println("User deleted");
                        } else {
                            Helper.error("Couldn't delete user");
                        }
                    }
                }
                continue;
            }

            Helper.error("That's not a valid option");
        }
    }
}