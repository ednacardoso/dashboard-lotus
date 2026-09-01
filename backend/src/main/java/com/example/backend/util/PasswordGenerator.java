package com.example.backend.util;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_+=<>?";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    private static final Pattern UPPER_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWER_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[!@#$%^&*()\\-_+=<>?]");

    private static final SecureRandom random = new SecureRandom();

    private PasswordGenerator() {
    }

    public static String generateStrongPassword() {
        StringBuilder password = new StringBuilder(12);

        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        while (password.length() < 12) {
            password.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    public static void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres");
        }
        if (!UPPER_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra maiúscula");
        }
        if (!LOWER_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra minúscula");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos um número");
        }
        if (!SYMBOL_PATTERN.matcher(password).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos um caractere especial");
        }
    }
}
