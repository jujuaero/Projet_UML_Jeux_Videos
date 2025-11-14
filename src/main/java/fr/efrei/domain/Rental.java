// John ZHAN - Jules BACART - Lucas RINAUDO

package fr.efrei.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


public class Rental implements Serializable {
    private final String rentalId;
    private final Customer customer;
    private final Game game;
    private final LocalDate rentalDate;
    private final LocalDate returnDate; // can be null if not returned yet
    private boolean returned;

    private Rental(Builder builder) {
        this.rentalId = builder.rentalId;
        this.customer = builder.customer;
        this.game = builder.game;
        this.rentalDate = builder.rentalDate;
        this.returnDate = builder.returnDate;
        this.returned = builder.returned;
    }

    public String getRentalId() { return rentalId; }
    public Customer getCustomer() { return customer; }
    public Game getGame() { return game; }
    public LocalDate getRentalDate() { return rentalDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isReturned() { return returned; }

    public void setReturned(boolean returned) { this.returned = returned; }

    public static class Builder {
        private String rentalId;
        private Customer customer;
        private Game game;
        private LocalDate rentalDate;
        private LocalDate returnDate;
        private boolean returned = false;

        public Builder setRentalId(String rentalId) { this.rentalId = rentalId; return this; }
        public Builder setCustomer(Customer customer) { this.customer = customer; return this; }
        public Builder setGame(Game game) { this.game = game; return this; }
        public Builder setRentalDate(LocalDate rentalDate) { this.rentalDate = rentalDate; return this; }
        public Builder setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; return this; }
        public Builder setReturned(boolean returned) { this.returned = returned; return this; }

        public Rental build() { return new Rental(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rental)) return false;
        Rental rental = (Rental) o;
        return Objects.equals(rentalId, rental.rentalId);
    }

    @Override
    public int hashCode() { return Objects.hash(rentalId); }

    @Override
    public String toString() {
        return "Rental{" +
                "rentalId='" + rentalId + '\'' +
                ", customer=" + customer.getName() +
                ", game=" + game.getTitle() +
                ", rentalDate=" + rentalDate +
                ", returnDate=" + returnDate +
                ", returned=" + returned +
                '}';
    }
}
