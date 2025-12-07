package fr.efrei.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Sale {
    private String id;
    private String customerId;
    private String gameId;
    private LocalDate date;
    private double price;

    public Sale(String id, String customerId, String gameId, LocalDate date, double price) {
        this.id = id;
        this.customerId = customerId;
        this.gameId = gameId;
        this.date = date;
        this.price = price;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getGameId() { return gameId; }
    public LocalDate getDate() { return date; }
    public double getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", date=" + date +
                ", price=" + price +
                '}';
    }
}
