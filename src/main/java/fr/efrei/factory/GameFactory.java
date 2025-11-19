package fr.efrei.factory;

import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.util.Helper;

public final class GameFactory {
    private GameFactory() {}

    public static Game create(String id, String title, String genre) {
        return create(id, title, genre, null);
    }

    public static Game create(String id, String title, String genre, GamePlatform platform) {
        String finalId = (id == null || id.isBlank()) ? Helper.IdGenerator.uuid() : id;
        validateNotBlank(title, "title");
        validateNotBlank(genre, "genre");

        return new Game.Builder()
                .setId(finalId)
                .setTitle(title.trim())
                .setGenre(genre.trim())
                .setPlatform(platform)
                .setAvailable(true)
                .build();
    }

    private static void validateNotBlank(String v, String field) {
        if (v == null || v.isBlank())
            throw new IllegalArgumentException("Invalid " + field + ": required");
    }
}
