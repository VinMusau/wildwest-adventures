package com.wildwest.api.common;

/** Shared validation patterns (annotation attributes must be compile-time constants). */
public final class Validation {

    private Validation() {}

    /**
     * Stricter than {@code @Email} alone, which accepts addresses like {@code a@b}:
     * requires a dot in the domain and no spaces.
     */
    public static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public static final String EMAIL_MESSAGE = "must be a valid email address";
}
