package fr.efrei.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.UUID;

public final class Helper {


    public final class IdGenerator {
        private IdGenerator() {}
        public static String uuid() { return UUID.randomUUID().toString(); }
    }


    private static final Scanner scanner = new Scanner(System.in);

    private Helper() {}

    /**Reads user input from console and trims spaces.**/

    public static String read(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**Validates that a string is not null or empty.**/

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Error: " + fieldName + " is required.");
        }
    }


    public static boolean isNumber(String value) {
        if (value == null) return false;
        return value.matches("\\d+");
    }


    /** Converts user input to a LocalDate (yyyy-MM-dd).**/

    public static LocalDate readDate(String prompt) {
        System.out.print(prompt + " (yyyy-MM-dd): ");
        String input = scanner.nextLine().trim();

        if (input.isBlank()) {
            return null; // Optional date
        }

        try {
            return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Try again.");
            return readDate(prompt);
        }
    }


    public static void line() {
        System.out.println("--------------------------------------------");
    }

    /**Prints an error message.**/
    public static void error(String message) {
        System.out.println(message);
    }
}
