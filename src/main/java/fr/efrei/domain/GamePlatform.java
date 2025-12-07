
package fr.efrei.domain;

public enum GamePlatform {
    XBOX_ONE("Xbox", "One"),
    XBOX_SERIES_X("Xbox", "SeriesX"),
    XBOX_SERIES_S("Xbox", "SeriesS"),
    PS4("PlayStation", "4"),
    PS5("PlayStation", "5"),
    PC_WINDOWS("PC", "Windows"),
    PC_MAC("PC", "Mac"),
    PC_LINUX("PC", "Linux"),
    PC_ANY("PC", "any");

    private final String family;
    private final String generation;

    GamePlatform(String family, String generation) {
        this.family = family;
        this.generation = generation;
    }

    public String getFamily() { return family; }
    public String getGeneration() { return generation; }

    public boolean isCompatibleWith(GamePlatform other) {
        if (other == null) return false;
        if (!this.family.equals(other.family)) return false;
        return "any".equals(this.generation) || "any".equals(other.generation) || this.generation.equals(other.generation);
    }

    @Override
    public String toString() { return family + " " + generation; }
}

