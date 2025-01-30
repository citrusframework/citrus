package org.citrusframework.openapi.validation;

/**
 * Enum that defines the policy for handling OpenAPI validation errors.
 * This enum controls the behavior of the system when validation errors are encountered
 * during OpenAPI validation, allowing for different levels of strictness.
 */
public enum OpenApiValidationPolicy {

    /**
     * No validation will be performed.
     * Any validation errors will be ignored, and the system will continue
     * without reporting or failing due to OpenAPI validation issues.
     */
    IGNORE,

    /**
     * Perform validation and report any errors.
     * Validation errors will be reported, but the system will continue running.
     * This option allows for debugging or monitoring purposes without halting the application.
     */
    REPORT,

    /**
     * Perform validation and fail if any errors are encountered.
     * Validation errors will cause the application to fail startup,
     * ensuring that only valid OpenAPI specifications are allowed to proceed.
     */
    STRICT,
}

