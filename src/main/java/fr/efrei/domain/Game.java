// John ZHAN - Jules BACART - Lucas RINAUDO

package fr.efrei.domain;


import java.io.Serializable;
import java.util.Objects;

public class Game implements Serializable {
    private final String id;
    private final String title;
    private final String genre;
    private boolean available;

    private Game(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.genre = builder.genre;
        this.available = builder.available;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) { this.available = available; }

    public static class Builder {
        private String id;
        private String title;
        private String genre;
        private boolean available = true;

        public Builder setId(String id) { this.id = id; return this; }
        public Builder setTitle(String title) { this.title = title; return this; }
        public Builder setGenre(String genre) { this.genre = genre; return this; }
        public Builder setAvailable(boolean available) { this.available = available; return this; }

        public Game build() { return new Game(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Game)) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Game{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", available=" + available +
                '}';
    }
}
