package com.consol.citrus.admin.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Post process handlebars view resolver bean as it is currently not possible to set helper properties on
 * this bean as usual. Will be fixed with next HandlebarsJava release version
 * (Issue: https://github.com/jknack/handlebars.java/issues/186)
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class HandlebarsHelperPostProcessor implements BeanPostProcessor {
    /** Helper registry */
    private Map<String, Helper<?>> helpers = new HashMap<String, Helper<?>>();

    /** List of helper sources */
    private List<Object> helperSources = new ArrayList<Object>();

    /**
     * Set helpers and helperSources on handlebars view resolver bean.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (beanName.equals("handlebarsViewResolver") && bean instanceof HandlebarsViewResolver) {
            HandlebarsViewResolver viewResolver = ((HandlebarsViewResolver)bean);

            for (Map.Entry<String, Helper<?>> helperEntry : helpers.entrySet()) {
                viewResolver.registerHelper(helperEntry.getKey(), helperEntry.getValue());
            }

            for (Object helperSource : helperSources) {
                viewResolver.registerHelpers(helperSource);
            }
        }

        return bean;
    }

    /**
     * Do nothing here
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Sets the helper sources registry.
     * @param helperSources
     */
    public void setHelperSources(List<Object> helperSources) {
        this.helperSources = helperSources;
    }

    /**
     * Sets the helper registry.
     * @param helpers
     */
    public void setHelpers(Map<String, Helper<?>> helpers) {
        this.helpers = helpers;
    }
}
