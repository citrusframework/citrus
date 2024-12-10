package org.citrusframework.openapi;

/**
 * Enum representing different types of auto-fill behavior for OpenAPI parameters/body.
 * This enum defines how missing or required parameters/body should be auto-filled.
 */
public enum AutoFillType {
    /**
     * No auto-fill will be performed for any parameters/body.
     */
    NONE,

    /**
     * Auto-fill will be applied only to required parameters/body that are missing.
     */
    REQUIRED,

    /**
     * Auto-fill will be applied to all parameters/body, whether they are required or not.
     */
    ALL
}
