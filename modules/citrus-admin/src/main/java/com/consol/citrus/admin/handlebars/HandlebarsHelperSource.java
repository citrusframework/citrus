package com.consol.citrus.admin.handlebars;

import org.springframework.util.StringUtils;

/**
 * Basic handlebar helpers on server side.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class HandlebarsHelperSource {

    /**
     * Helper to translate folder path to package representation.
     * @param context
     * @return
     */
    public String folderToPackage(String context) {
        if (StringUtils.hasText(context)) {
            return context.replace('/', '.');
        } else {
            return context;
        }
    }
}
