package fr.efrei.domain;

import java.time.LocalDate;

public class Sale {
    private String id;
    private Customer customer;
    private Game game;
    private LocalDate date;
    private double price;

    public Sale(String id, Customer customer, Game game, LocalDate date, double price) {
        this.id = id;
        this.customer = customer;
        this.game = game;
        this.date = date;
        this.price = price;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Game getGame() { return game; }
    public LocalDate getDate() { return date; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return "Sale: " + game.getTitle() + " to " + customer.getName() + 
               " on " + date + " for " + price + "€";
    }
}
