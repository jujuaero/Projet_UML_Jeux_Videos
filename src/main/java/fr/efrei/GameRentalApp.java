package fr.efrei;

import fr.efrei.domain.Customer;
import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.Rental;
import fr.efrei.factory.CustomerFactory;
import fr.efrei.factory.GameFactory;
import fr.efrei.factory.RentalFactory;
import fr.efrei.util.Helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRentalApp {

    public static void main(String[] args) {
        System.out.println("Welcome to our shop");

        // jeux dispo
        List<Game> games = new ArrayList<>();
        games.add(GameFactory.create(null, "Halo Infinite", "Shooter", GamePlatform.XBOX_SERIES_X));
        games.add(GameFactory.create(null, "God of War", "Action", GamePlatform.PS5));
        games.add(GameFactory.create(null, "Cyberpunk 2077", "RPG", GamePlatform.PC_WINDOWS));

        List<Customer> customers = new ArrayList<>();
        List<Rental> rentals = new ArrayList<>();
        Map<String, String> credentials = new HashMap<>(); // contact -> password

        while (true) {
            Helper.line();
            String contact = Helper.read("Contact (ou 'exit' pour quitter)");
            if ("exit".equalsIgnoreCase(contact)) {
                System.out.println("Au revoir.");
                return;
            }

            Customer customer = null;
            // si contact existant => login
            if (credentials.containsKey(contact)) {
                int attempts = 0;
                boolean auth = false;
                while (attempts < 3 && !auth) {
                    String pass = Helper.read("Mot de passe");
                    String real = credentials.get(contact);
                    if (real != null && real.equals(pass)) {
                        auth = true;
                        // retrouver le customer
                        for (Customer c : customers) {
                            if (c.getContactNumber().equals(contact)) { customer = c; break; }
                        }
                        System.out.println("Connexion réussie. Bienvenue " + (customer != null ? customer.getName() : "client") + " !");
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
                // nouveau client => demander nom + mot de passe
                String name = Helper.read("Nom");
                String pass = Helper.read("Choisissez un mot de passe");
                if (pass.isBlank()) {
                    Helper.error("Mot de passe requis.");
                    continue;
                }
                try {
                    customer = CustomerFactory.create(null, name, contact);
                    customers.add(customer);
                    credentials.put(contact, pass);
                    System.out.println("Inscription OK. Bienvenue " + customer.getName());
                } catch (IllegalArgumentException e) {
                    Helper.error("Erreur d'inscription: " + e.getMessage());
                    continue; // recommence
                }
            }

            // Menu simple qui boucle tant que l'utilisateur est connecté
            while (true) {
                System.out.println("Que voulez-vous faire ?\n1) Louer\n2) Rendre\n3) Se déconnecter\n4) Quitter");
                String choice = Helper.read("Choix (1-4)");
                if (!Helper.isNumber(choice)) { Helper.error("Choix invalide"); continue; }
                int ch = Integer.parseInt(choice);

                if (ch == 4) {
                    System.out.println("Au revoir.");
                    return; // quitte l'application
                }

                if (ch == 3) {
                    System.out.println("Déconnexion...");
                    break; // retourne à l'accueil pour un autre utilisateur
                }

                if (ch == 2) {
                    // Retourner un jeu
                    List<Rental> active = new ArrayList<>();
                    for (Rental r : rentals) {
                        if (!r.isReturned() && r.getCustomer().equals(customer)) active.add(r);
                    }
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
                    toReturn.setReturned(true);
                    toReturn.getGame().setAvailable(true);
                    System.out.println("Jeu rendu: " + toReturn.getGame().getTitle());
                    continue; // reste connecté
                }

                if (ch == 1) {
                    // Louer
                    System.out.println("Choisissez la plateforme :\n1) Xbox Series X\n2) PlayStation 5\n3) PC Windows");
                    String p = Helper.read("Votre choix (1-3)");
                    GamePlatform requestedPlatform = GamePlatform.PC_WINDOWS;
                    if ("1".equals(p)) requestedPlatform = GamePlatform.XBOX_SERIES_X;
                    else if ("2".equals(p)) requestedPlatform = GamePlatform.PS5;

                    // Jeux compatibles
                    List<Game> available = new ArrayList<>();
                    for (Game g : games) {
                        if (g.isAvailable() && g.getPlatform() != null && g.getPlatform().isCompatibleWith(requestedPlatform)) available.add(g);
                    }
                    if (available.isEmpty()) { Helper.error("Aucun jeu disponible."); continue; }
                    System.out.println("Jeux disponibles :");
                    for (int i = 0; i < available.size(); i++) {
                        Game gg = available.get(i);
                        System.out.println((i + 1) + ") " + gg.getTitle() + " - " + gg.getPlatform());
                    }
                    String sel = Helper.read("Numéro du jeu à louer");
                    if (!Helper.isNumber(sel)) { Helper.error("Sélection invalide"); continue; }
                    int idx = Integer.parseInt(sel) - 1;
                    if (idx < 0 || idx >= available.size()) { Helper.error("Index hors limites"); continue; }
                    Game chosen = available.get(idx);

                    System.out.println("Durée : 1) 1 jour  2) 1 semaine  3) 1 mois");
                    String dur = Helper.read("Choix (1-3)");
                    int days = 1; if ("2".equals(dur)) days = 7; else if ("3".equals(dur)) days = 30;

                    LocalDate now = LocalDate.now();
                    LocalDate plannedReturn = now.plusDays(days);

                    try {
                        Rental rental = RentalFactory.create(null, customer, chosen, requestedPlatform, now, plannedReturn);
                        rentals.add(rental);
                        System.out.println("Location OK: " + rental.getGame().getTitle() + " jusqu'au " + rental.getReturnDate());
                    } catch (Exception e) {
                        Helper.error("Erreur location: " + e.getMessage());
                    }
                    continue; // reste connecté
                }

                Helper.error("Choix invalide");
            }
        }
    }
}