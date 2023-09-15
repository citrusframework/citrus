package org.citrusframework.cucumber.backend.spring;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.CitrusInstanceProcessor;
import org.citrusframework.CitrusSpringContext;
import org.citrusframework.cucumber.backend.CitrusBackend;
import org.citrusframework.cucumber.container.StepTemplate;
import org.citrusframework.cucumber.step.xml.XmlStepDefinition;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.resource.ClasspathSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Christoph Deppisch
 */
public class CitrusSpringBackend extends CitrusBackend {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusSpringBackend.class);

    /**
     * Constructor using resource loader.
     *
     * @param lookup
     * @param container
     */
    public CitrusSpringBackend(Lookup lookup, Container container) {
        super(lookup, container);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        if (CitrusInstanceManager.hasInstance()) {
            new XmlStepInstanceProcessor(glue, gluePaths, lookup).process(CitrusInstanceManager.getOrDefault());
        } else {
            CitrusInstanceManager.addInstanceProcessor(new XmlStepInstanceProcessor(glue, gluePaths, lookup));
        }

        super.loadGlue(glue, gluePaths);
    }

    /**
     * Initialization hook performs before suite actions and XML step initialization. Called as soon as citrus instance is requested
     * from outside for the first time. Performs only once.
     */
    private static class XmlStepInstanceProcessor implements CitrusInstanceProcessor {

        private final Glue glue;
        private final List<URI> gluePaths;
        private final Lookup lookup;

        XmlStepInstanceProcessor(Glue glue, List<URI> gluePaths, Lookup lookup) {
            this.glue = glue;
            this.gluePaths = gluePaths;
            this.lookup = lookup;
        }

        @Override
        public void process(Citrus instance) {
            for (URI gluePath : gluePaths) {
                String xmlStepConfigLocation = "classpath*:" + ClasspathSupport.resourceNameOfPackageName(ClasspathSupport.packageName(gluePath)) + "/**/*Steps.xml";

                logger.info(String.format("Loading XML step definitions %s", xmlStepConfigLocation));

                ApplicationContext ctx;
                if (instance.getCitrusContext() instanceof CitrusSpringContext) {
                    ctx = new ClassPathXmlApplicationContext(new String[]{ xmlStepConfigLocation }, true, ((CitrusSpringContext) instance.getCitrusContext()).getApplicationContext());
                } else {
                    ctx = new ClassPathXmlApplicationContext(new String[]{ xmlStepConfigLocation }, true);
                }

                Map<String, StepTemplate> xmlSteps = ctx.getBeansOfType(StepTemplate.class);

                for (StepTemplate stepTemplate : xmlSteps.values()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Found XML step definition: %s %s", stepTemplate.getName(), stepTemplate.getPattern().pattern()));
                    }
                    glue.addStepDefinition(new XmlStepDefinition(stepTemplate, lookup));
                }
            }
        }
    }
}
