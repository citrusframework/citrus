package org.citrusframework.common;

/**
 * A marker interface for identifying beans that supply a {@link SpringXmlTestLoaderConfiguration}
 * annotation to configure the bean definition parser for SpringXmlTest parsing.
 * <p>
 * To define specific bean definition parser configurations, you can implement this interface
 * along with the corresponding {@link SpringXmlTestLoaderConfiguration} annotation and provide it
 * as a bean through the parent {@link org.springframework.context.ApplicationContext} used for
 * loading SpringXmlTest.
 *
 *
 * @author Thorsten Schlathoelter
 * @since 4.0
 * @see SpringXmlTestLoader
 */
public interface SpringXmlTestLoaderConfigurer {
}
