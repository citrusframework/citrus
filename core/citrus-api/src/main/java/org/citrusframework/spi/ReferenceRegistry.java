package org.citrusframework.spi;

/**
 * Bind objects to registry for later reference. Objects declared in registry can be injected in various ways (e.g. annotations).
 * Usually used in combination with {@link org.citrusframework.spi.ReferenceResolver}.
 *
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface ReferenceRegistry {

    void bind(String name, Object value);

    /**
     * Get proper bean name for future bind operation on registry.
     * @param bindAnnotation
     * @param defaultName
     * @return
     */
    static String getName(BindToRegistry bindAnnotation, String defaultName) {
        if (bindAnnotation.name() != null && !bindAnnotation.name().isBlank()) {
            return bindAnnotation.name();
        }

        return defaultName;
    }
}
