package org.citrusframework;

import org.citrusframework.config.CitrusSpringConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Context provider registered via resource path lookup. When module is on classpath this provider will be used to instantiate
 * Citrus.
 *
 * Provider creates a CitrusContext that is backed with a Spring application context. Provider caches the last application context
 * that has created a context. When very same application context creates another CitrusContext use the cached instance. This
 * caching should give us some performance improvements and less instance duplications.
 *
 * @author Christoph Deppisch
 */
public class CitrusSpringContextProvider implements CitrusContextProvider {

    private static CitrusSpringContext context;

    private final ApplicationContext applicationContext;

    public CitrusSpringContextProvider() {
        this.applicationContext = null;
    }

    public CitrusSpringContextProvider(Class<? extends CitrusSpringConfig> configClass) {
        this(new AnnotationConfigApplicationContext(configClass));
    }

    public CitrusSpringContextProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public CitrusContext create() {
        if (applicationContext == null) {
            context = CitrusSpringContext.create();
        } else if (context == null) {
            context = CitrusSpringContext.create(applicationContext);
        } else if (!context.getApplicationContext().equals(applicationContext)) {
            context = CitrusSpringContext.create(applicationContext);
        }

        return context;
    }
}
