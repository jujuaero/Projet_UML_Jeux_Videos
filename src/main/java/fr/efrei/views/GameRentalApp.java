package fr.efrei.views;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Employee;
import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.GameType;
import fr.efrei.domain.Rental;
import fr.efrei.domain.Sale;
import fr.efrei.factory.CustomerFactory;
import fr.efrei.repository.CustomerRepository;
import fr.efrei.repository.EmployeeRepository;
import fr.efrei.repository.GameRepository;
import fr.efrei.repository.RentalRepository;
import fr.efrei.repository.SaleRepository;
import fr.efrei.util.DatabaseConnection;
import fr.efrei.util.Helper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static fr.efrei.util.Helper.pause;

public class GameRentalApp {

    public static void main(String[] args) {
        System.out.println("=== CapeTown Gaming - Employee System ===");

        //Singleton instances
        DatabaseConnection.getInstance();

        EmployeeRepository employeeRepo = EmployeeRepository.getInstance();
        CustomerRepository customerRepo = CustomerRepository.getInstance();
        GameRepository gameRepo = GameRepository.getInstance();
        RentalRepository rentalRepo = RentalRepository.getInstance();
        SaleRepository saleRepo = SaleRepository.getInstance();

        // Employee login
        Employee employee = employeeLogin(employeeRepo);
        if (employee == null) {
            System.out.println("Login failed. Exiting...");
            return;
        }

        System.out.println("\nWelcome, " + employee.getName() + "!");

        while (true) {
            Helper.line();
            System.out.println("\n=== EMPLOYEE MENU ===");
            System.out.println("1) Select/Add Customer");
            System.out.println("2) View All Customers");
            System.out.println("3) View Shop Revenue");
            System.out.println("4) Logout");

            String choice = Helper.read("Your choice");
            if (!Helper.isNumber(choice)) {
                Helper.error("Please enter a number");
                continue;
            }

            int ch = Integer.parseInt(choice);

            if (ch == 4) {
                System.out.println("Logging out...");
                break;
            }

            switch (ch) {
                case 1:
                    Customer customer = selectOrAddCustomer(customerRepo);
                    if (customer != null) {
                        handleCustomerSession(customer, customerRepo, gameRepo, rentalRepo, saleRepo);
                    }
                    break;
                case 2:
                    viewAllCustomers(customerRepo);

                    break;
                case 3:
                    viewRevenue(saleRepo, rentalRepo);
                    break;
                default:
                    Helper.error("Invalid choice");
            }
            pause(1);
        }

        System.out.println("Thank you for using CapeTown Gaming System!");
    }

    private static Employee employeeLogin(EmployeeRepository employeeRepo) {
        System.out.println("\n=== EMPLOYEE LOGIN ===");

        for (int attempts = 0; attempts < 3; attempts++) {
            String email = Helper.read("Email");
            String password = Helper.read("Password");

            Employee employee = employeeRepo.findByEmail(email);
            if (employee != null && employee.getPassword().equals(password)) {
                return employee;
            }

            Helper.error("Invalid credentials. Attempt " + (attempts + 1) + "/3");
        }

        return null;
    }

    private static Customer selectOrAddCustomer(CustomerRepository customerRepo) {
        System.out.println("\n=== CUSTOMER SELECTION ===");
        System.out.println("1) Search existing customer (by phone)");
        System.out.println("2) Add new customer");
        System.out.println("3) Back");

        String choice = Helper.read("Your choice");
        if (!Helper.isNumber(choice)) {
            Helper.error("Please enter a number");
            return null;
        }

        int ch = Integer.parseInt(choice);

        if (ch == 3) return null;

        if (ch == 1) {
            String phone = Helper.read("Customer phone number");
            Customer customer = customerRepo.findByContact(phone);
            if (customer == null) {
                Helper.error("Customer not found");
                return null;
            }
            System.out.println("✓ Customer found: " );
            return customer;
        }

        else if (ch == 2) {
            String name = Helper.read("Customer name");
            String phone = Helper.read("Phone number");
            String password = Helper.read("Password");

            if (customerRepo.findByContact(phone) != null) {
                Helper.error("A customer with this phone number already exists");
                return null;
            }

            Customer customer = CustomerFactory.create(UUID.randomUUID().toString(), name, phone, password);
            customer = customerRepo.save(customer);

            if (customer != null) {
                System.out.println("✓ Customer created successfully!");
                return customer;
            } else {
                Helper.error("Failed to create customer");
                return null;
            }
        }
        pause(1);

        return null;
    }

    private static void handleCustomerSession(Customer customer, CustomerRepository customerRepo,
                                              GameRepository gameRepo, RentalRepository rentalRepo,
                                              SaleRepository saleRepo) {
        while (true) {
            Helper.line();
            System.out.println("\n=== CUSTOMER: " + customer.getName() + " ===");
            System.out.println("Loyalty Points: " + customer.getLoyaltyPoints());
            System.out.println("\n1) Rent a Game");
            System.out.println("2) Buy a Game");
            System.out.println("3) Return a Rental");
            System.out.println("4) View Customer's Rentals");
            System.out.println("5) Back to Employee Menu");

            String choice = Helper.read("Your choice");
            if (!Helper.isNumber(choice)) {
                Helper.error("Please enter a number");
                continue;
            }

            int ch = Integer.parseInt(choice);

            if (ch == 5) break;

            switch (ch) {
                case 1:
                    rentGame(customer, customerRepo, gameRepo, rentalRepo);
                    break;
                case 2:
                    buyGame(customer, customerRepo, gameRepo, saleRepo);
                    break;
                case 3:
                    returnGame(customer, customerRepo, gameRepo, rentalRepo);
                    break;
                case 4:
                    viewCustomerRentals(customer, rentalRepo);
                    break;
                default:
                    Helper.error("Invalid choice");
            }
            pause(1);
        }
        pause(1);
    }

    private static void rentGame(Customer customer, CustomerRepository customerRepo,
                                GameRepository gameRepo, RentalRepository rentalRepo) {
        System.out.println("\n=== RENT A GAME ===");

        // Choose platform
        GamePlatform platform = choosePlatform();
        if (platform == null) return;

        // Show available games for rental
        List<Game> games = gameRepo.findByPlatformAndType(platform, GameType.RENTAL);
        games = games.stream().filter(Game::isAvailable).toList();

        if (games.isEmpty()) {
            Helper.error("No games available for this platform");
            pause(1/2);
            return;
        }

        System.out.println("\nAvailable games:");
        for (int i = 0; i < games.size(); i++) {
            Game g = games.get(i);
            System.out.println((i + 1) + ") " + g.getTitle() + " - " + g.getGenre() + " - $" + g.getPrice() + "/day");
        }
        pause(3/2);

        String gameChoice = Helper.read("Select game (number)");
        if (!Helper.isNumber(gameChoice)) {
            Helper.error("Invalid selection");
            pause(1);
            return;
        }

        int gameIdx = Integer.parseInt(gameChoice) - 1;
        if (gameIdx < 0 || gameIdx >= games.size()) {
            Helper.error("Invalid selection");
            pause(1/2);
            return;
        }

        Game selectedGame = games.get(gameIdx);

        // Choose rental duration
        System.out.println("\nRental duration:");
        System.out.println("1) 1 day - $" + selectedGame.getPrice());
        System.out.println("2) 1 week - $" + (selectedGame.getPrice() * 7 * 0.85)); // 15% discount
        System.out.println("3) 1 month - $" + (selectedGame.getPrice() * 30 * 0.70)); // 30% discount

        String durationChoice = Helper.read("Your choice");
        if (!Helper.isNumber(durationChoice)) {
            Helper.error("Invalid choice");
            pause(1/2);
            return;
        }

        int days = 1;
        double price = selectedGame.getPrice();

        switch (Integer.parseInt(durationChoice)) {
            case 1: days = 1; price = selectedGame.getPrice(); break;
            case 2: days = 7; price = selectedGame.getPrice() * 7 * 0.85; break;
            case 3: days = 30; price = selectedGame.getPrice() * 30 * 0.70; break;
            default:
                Helper.error("Invalid choice");
                pause(1/2);
                return;
        }

        // Ask about loyalty points
        double finalPrice = price;
        if (customer.getLoyaltyPoints() >= 100) {
            int pointsToUse = (customer.getLoyaltyPoints() / 100) * 100; // Use multiples of 100
            double discount = pointsToUse / 100.0; // 100 points = $10 discount

            System.out.println("\nYou have " + customer.getLoyaltyPoints() + " loyalty points");
            System.out.println("You can use up to " + pointsToUse + " points for $" + discount + " discount");
            pause(1/3);

            String usePoints = Helper.read("Use loyalty points? (yes/no)");
            if ("yes".equalsIgnoreCase(usePoints)) {
                finalPrice -= discount;
                if (finalPrice < 0) finalPrice = 0;
                customer.useLoyaltyPoints(pointsToUse);
                System.out.println("✓ Applied $" + discount + " discount!");
                pause(1/3);
            }
        }

        System.out.println("\nTotal: $" + String.format("%.2f", finalPrice));
        String confirm = Helper.read("Confirm rental? (yes/no)");

        if (!"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Rental cancelled");
            pause(1/2);
            return;
        }

        // Create rental
        LocalDate returnDate = LocalDate.now().plusDays(days);
        Rental rental = new Rental.Builder()
            .setRentalId(UUID.randomUUID().toString())
            .setCustomer(customer)
            .setGame(selectedGame)
            .setPlatform(platform)
            .setRentalDate(LocalDate.now())
            .setReturnDate(returnDate)
            .setReturned(false)
            .build();

        rental = rentalRepo.save(rental);
        if (rental != null) {
            selectedGame.setAvailable(false);
            gameRepo.update(selectedGame);

            // Add loyalty points (10% of price)
            int pointsEarned = (int)(finalPrice * 10);
            customer.addLoyaltyPoints(pointsEarned);
            customerRepo.updateLoyaltyPoints(customer.getId(), customer.getLoyaltyPoints());

            System.out.println("✓ Rental successful!");
            System.out.println("Return date: " + returnDate);
            System.out.println("Earned " + pointsEarned + " loyalty points!");
        } else {
            Helper.error("Failed to create rental");
        }
        pause(1);
    }

    private static void buyGame(Customer customer, CustomerRepository customerRepo,
                               GameRepository gameRepo, SaleRepository saleRepo) {
        System.out.println("\n=== BUY A GAME ===");

        GamePlatform platform = choosePlatform();
        if (platform == null) return;

        List<Game> games = gameRepo.findByPlatformAndType(platform, GameType.SALE);
        games = games.stream().filter(Game::isAvailable).toList();

        if (games.isEmpty()) {
            Helper.error("No games available for sale on this platform");
            pause(1/2);
            return;
        }

        System.out.println("\nAvailable games:");
        for (int i = 0; i < games.size(); i++) {
            Game g = games.get(i);
            System.out.println((i + 1) + ") " + g.getTitle() + " - " + g.getGenre() + " - $" + g.getPrice());
            pause(2);
        }

        String gameChoice = Helper.read("Select game (number)");
        if (!Helper.isNumber(gameChoice)) {
            Helper.error("Invalid selection");
            pause(1/2);
            return;
        }

        int gameIdx = Integer.parseInt(gameChoice) - 1;
        if (gameIdx < 0 || gameIdx >= games.size()) {
            Helper.error("Invalid selection");
            pause(1/2);
            return;
        }

        Game selectedGame = games.get(gameIdx);
        double finalPrice = selectedGame.getPrice();

        // Ask about loyalty points
        if (customer.getLoyaltyPoints() >= 100) {
            int pointsToUse = (customer.getLoyaltyPoints() / 100) * 100;
            double discount = pointsToUse / 100.0;

            System.out.println("\nYou have " + customer.getLoyaltyPoints() + " loyalty points");
            System.out.println("You can use up to " + pointsToUse + " points for $" + discount + " discount");

            String usePoints = Helper.read("Use loyalty points? (yes/no)");
            if ("yes".equalsIgnoreCase(usePoints)) {
                finalPrice -= discount;
                if (finalPrice < 0) finalPrice = 0;
                customer.useLoyaltyPoints(pointsToUse);
                System.out.println("✓ Applied $" + discount + " discount!");
                pause(1/3);
            }
        }

        System.out.println("\nTotal: $" + String.format("%.2f", finalPrice));
        String confirm = Helper.read("Confirm purchase? (yes/no)");

        if (!"yes".equalsIgnoreCase(confirm)) {
            System.out.println("Purchase cancelled");
            pause(1/2);
            return;
        }

        Sale sale = new Sale(
            UUID.randomUUID().toString(),
            customer,
            selectedGame,
            LocalDate.now(),
            finalPrice
        );

        sale = saleRepo.save(sale);
        if (sale != null) {
            selectedGame.setAvailable(false);
            gameRepo.update(selectedGame);

            // Add loyalty points (10% of price)
            int pointsEarned = (int)(finalPrice * 10);
            customer.addLoyaltyPoints(pointsEarned);
            customerRepo.updateLoyaltyPoints(customer.getId(), customer.getLoyaltyPoints());

            System.out.println("✓ Purchase successful!");
            System.out.println("Earned " + pointsEarned + " loyalty points!");
            pause(1/2);
        } else {
            Helper.error("Failed to process sale");
            pause(1/2);
        }
    }

    private static void returnGame(Customer customer, CustomerRepository customerRepo,
                                  GameRepository gameRepo, RentalRepository rentalRepo) {
        System.out.println("\n=== RETURN A GAME ===");

        List<Rental> activeRentals = rentalRepo.findActiveByCustomer(customer.getId());

        if (activeRentals.isEmpty()) {
            Helper.error("No active rentals for this customer");
            pause(1/2);
            return;
        }

        System.out.println("\nActive rentals:");
        for (int i = 0; i < activeRentals.size(); i++) {
            Rental r = activeRentals.get(i);
            System.out.println((i + 1) + ") " + r.getGame().getTitle() +
                             " - Rented on: " + r.getRentalDate() +
                             " - Due: " + r.getReturnDate());
            pause(1/2);
        }

        String choice = Helper.read("Select rental to return (number)");
        if (!Helper.isNumber(choice)) {
            Helper.error("Invalid selection");
            pause(1/2);
            return;
        }

        int idx = Integer.parseInt(choice) - 1;
        if (idx < 0 || idx >= activeRentals.size()) {
            Helper.error("Invalid selection");
            pause(1/2);
            return;
        }

        Rental rental = activeRentals.get(idx);
        rental.setReturned(true);

        if (rentalRepo.update(rental)) {
            Game game = rental.getGame();
            game.setAvailable(true);
            gameRepo.update(game);

            // Bonus points for returning on time
            if (!LocalDate.now().isAfter(rental.getReturnDate())) {
                customer.addLoyaltyPoints(50);
                customerRepo.updateLoyaltyPoints(customer.getId(), customer.getLoyaltyPoints());
                System.out.println("✓ Game returned successfully!");
                System.out.println("Bonus: +50 loyalty points for returning on time!");
                pause(1/2);
            } else {
                System.out.println("✓ Game returned (late)");
                pause(1/3);
            }
        } else {
            Helper.error("Failed to process return");
            pause(1/2);
        }
    }

    private static void viewCustomerRentals(Customer customer, RentalRepository rentalRepo) {
        System.out.println("\n=== CUSTOMER RENTALS ===");

        List<Rental> rentals = rentalRepo.findByCustomer(customer.getId());

        if (rentals.isEmpty()) {
            System.out.println("No rental history");
            pause(1/2);
            return;
        }

        System.out.println("\nRental History:");
        for (Rental r : rentals) {
            String status = r.isReturned() ? "Returned" : "Active";
            System.out.println("- " + r.getGame().getTitle() +
                             " | Rented: " + r.getRentalDate() +
                             " | Due: " + r.getReturnDate() +
                             " | Status: " + status);
        }
        pause(3/2);
    }

    private static void viewAllCustomers(CustomerRepository customerRepo) {
        System.out.println("\n=== ALL CUSTOMERS ===");

        List<Customer> customers = customerRepo.findAll();

        if (customers.isEmpty()) {
            System.out.println("No customers in database");
            pause(1/2);
            return;
        }

        System.out.println("\nTotal customers: " + customers.size());
        for (Customer c : customers) {
            System.out.println("- " + c.getName() +
                             " | Phone: " + c.getContactNumber() +
                             " | Loyalty Points: " + c.getLoyaltyPoints());
        }
        pause(3/2);
    }

    private static void viewRevenue(SaleRepository saleRepo, RentalRepository rentalRepo) {
        System.out.println("\n=== SHOP REVENUE ===");

        double salesRevenue = saleRepo.getTotalRevenue();
        int totalSales = saleRepo.findAll().size();
        int totalRentals = rentalRepo.findAll().size();

        System.out.println("\nSales Revenue: $" + String.format("%.2f", salesRevenue));
        System.out.println("Total Sales: " + totalSales);
        System.out.println("Total Rentals: " + totalRentals);
        System.out.println("Total Transactions: " + (totalSales + totalRentals));
        pause(3/2);
    }

    private static GamePlatform choosePlatform() {
        System.out.println("\n=== SELECT PLATFORM ===");
        System.out.println("1) Xbox One");
        System.out.println("2) Xbox Series X");
        System.out.println("3) Xbox Series S");
        System.out.println("4) PlayStation 4");
        System.out.println("5) PlayStation 5");
        System.out.println("6) PC Windows");
        System.out.println("7) PC Mac");
        System.out.println("8) PC Linux");
        pause(1);

        String choice = Helper.read("Your choice");
        if (!Helper.isNumber(choice)) {
            Helper.error("Invalid choice");
            return null;
        }

        return switch (Integer.parseInt(choice)) {
            case 1 -> GamePlatform.XBOX_ONE;
            case 2 -> GamePlatform.XBOX_SERIES_X;
            case 3 -> GamePlatform.XBOX_SERIES_S;
            case 4 -> GamePlatform.PS4;
            case 5 -> GamePlatform.PS5;
            case 6 -> GamePlatform.PC_WINDOWS;
            case 7 -> GamePlatform.PC_MAC;
            case 8 -> GamePlatform.PC_LINUX;
            default -> {
                Helper.error("Invalid choice");
                pause(1/2);
                yield null;
            }
        };
    }
}

