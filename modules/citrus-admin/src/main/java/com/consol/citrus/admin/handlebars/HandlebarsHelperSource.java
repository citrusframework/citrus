package com.consol.citrus.admin.handlebars;

import org.springframework.util.StringUtils;

/**
 * Basic handlebar helpers on server side.
 *
 * @author Christoph Deppisch
 */
public class HandlebarsHelperSource {

    /**
     * Helper to translate folder path to package representation.
     * @param context
     * @return
     */
    public String folderToPackage(String context) {
        if (StringUtils.hasText(context)) {
            return context.replaceAll("/", ".");
        } else {
            return context;
        }
    }
}
