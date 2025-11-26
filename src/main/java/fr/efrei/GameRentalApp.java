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
            String contact = Helper.read("Contact (ou 'exit' pour quitter)");
            if ("exit".equalsIgnoreCase(contact)) {
                System.out.println("Au revoir.");
                return;
            }

            Customer customer = null;
            Customer existingCustomer = customerRepo.findByContact(contact);

            if (existingCustomer != null) {
                int attempts = 0;
                boolean auth = false;
                while (attempts < 3 && !auth) {
                    String pass = Helper.read("Mot de passe");
                    if (existingCustomer.getPassword().equals(pass)) {
                        auth = true;
                        customer = existingCustomer;
                        System.out.println("Connexion réussie. Bienvenue " + customer.getName() + " !");
                    } else {
                        attempts++;
                        Helper.error("Mot de passe incorrect. Tentative " + attempts + "/3");
                    }
                }
                if (!auth) {
                    Helper.error("Échec de l'authentification. Retour à l'accueil.");
                    continue;
                }
            } else {
                String name = Helper.read("Nom");
                String pass = Helper.read("Choisissez un mot de passe");
                if (pass.isBlank()) {
                    Helper.error("Mot de passe requis.");
                    continue;
                }
                try {
                    customer = CustomerFactory.create(null, name, contact, pass);
                    customer = customerRepo.save(customer);
                    if (customer != null) {
                        System.out.println("Inscription OK. Bienvenue " + customer.getName());
                    } else {
                        Helper.error("Erreur lors de l'inscription.");
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                    Helper.error("Erreur d'inscription: " + e.getMessage());
                    continue;
                }
            }

            while (true) {
                System.out.println("Que voulez-vous faire ?\n1) Louer\n2) Rendre\n3) Acheter\n4) Se déconnecter\n5) Quitter");
                String choice = Helper.read("Choix (1-5)");
                if (!Helper.isNumber(choice)) { Helper.error("Choix invalide"); continue; }
                int ch = Integer.parseInt(choice);

                if (ch == 5) {
                    System.out.println("Au revoir.");
                    return;
                }

                if (ch == 4) {
                    System.out.println("Déconnexion...");
                    break;
                }

                if (ch == 3) {
                    System.out.println("Choisissez la plateforme :\n1) Xbox Series X\n2) PlayStation 5\n3) PC Windows");
                    String p = Helper.read("Votre choix (1-3)");
                    GamePlatform requestedPlatform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) requestedPlatform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) requestedPlatform = GamePlatform.PS5;

                    List<Game> available = gameRepo.findAvailableForSaleByPlatform(requestedPlatform);

                    if (available.isEmpty()) {
                        Helper.error("Aucun jeu à vendre pour cette plateforme.");
                        continue;
                    }

                    System.out.println("Jeux à vendre :");
                    for (int i = 0; i < available.size(); i++) {
                        Game gg = available.get(i);
                        System.out.println((i + 1) + ") " + gg.getTitle() + " - " + gg.getPlatform() + " | Prix : " + String.format("%.2f €", gg.getPrice()));
                    }
                    String sel = Helper.read("Numéro du jeu à acheter");
                    if (!Helper.isNumber(sel)) { Helper.error("Sélection invalide"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= available.size()) { Helper.error("Index hors limites"); continue; }
                    Game chosen = available.get(idx);

                    if (gameRepo.updateAvailability(chosen.getId(), false)) {
                        System.out.println("Achat OK: " + chosen.getTitle());
                    } else {
                        Helper.error("Erreur lors de l'achat du jeu.");
                    }
                    continue;
                }

                if (ch == 2) {
                    List<Rental> active = rentalRepo.findActiveByCustomer(customer.getId());

                    if (active.isEmpty()) {
                        Helper.error("Vous n'avez aucune location active.");
                        continue;
                    }
                    System.out.println("Vos locations actives :");
                    for (int i = 0; i < active.size(); i++) {
                        Rental r = active.get(i);
                        System.out.println((i + 1) + ") " + r.getGame().getTitle() + " - retour prévu: " + r.getReturnDate());
                    }
                    String sel = Helper.read("Numéro à rendre");
                    if (!Helper.isNumber(sel)) { Helper.error("Sélection invalide"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= active.size()) { Helper.error("Index hors limites"); continue; }
                    Rental toReturn = active.get(idx);

                    if (rentalRepo.markAsReturned(toReturn.getRentalId())) {
                        gameRepo.updateAvailability(toReturn.getGame().getId(), true);
                        System.out.println("Jeu rendu: " + toReturn.getGame().getTitle());
                    } else {
                        Helper.error("Erreur lors du retour du jeu.");
                    }
                    continue;
                }

                if (ch == 1) {
                    System.out.println("Choisissez la plateforme :\n1) Xbox Series X\n2) PlayStation 5\n3) PC Windows");
                    String p = Helper.read("Votre choix (1-3)");
                    GamePlatform requestedPlatform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) requestedPlatform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) requestedPlatform = GamePlatform.PS5;

                    List<Game> available = gameRepo.findAvailableByPlatform(requestedPlatform);

                    if (available.isEmpty()) {
                        Helper.error("Aucun jeu disponible pour cette plateforme.");
                        continue;
                    }

                    System.out.println("Jeux disponibles :");
                    for (int i = 0; i < available.size(); i++) {
                        Game gg = available.get(i);
                        System.out.println((i + 1) + ") " + gg.getTitle() + " - " + gg.getPlatform() + " | Prix : " + String.format("%.2f €", gg.getPrice()));
                    }
                    String sel = Helper.read("Numéro du jeu à louer");
                    if (!Helper.isNumber(sel)) { Helper.error("Sélection invalide"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= available.size()) { Helper.error("Index hors limites"); continue; }
                    Game chosen = available.get(idx);

                    System.out.println("Durée : 1) 1 jour  2) 1 semaine  3) 1 mois");
                    String dur = Helper.read("Choix (1-3)");
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
                            System.out.println("Location OK: " + rental.getGame().getTitle() + " jusqu'au " + rental.getReturnDate());
                        } else {
                            Helper.error("Erreur lors de la sauvegarde de la location.");
                        }
                    } catch (Exception e) {
                        Helper.error("Erreur location: " + e.getMessage());
                    }
                    continue;
                }

                Helper.error("Choix invalide");
            }
        }
    }
}