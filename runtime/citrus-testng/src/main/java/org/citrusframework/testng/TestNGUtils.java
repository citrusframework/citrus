package org.citrusframework.testng;

import org.testng.SkipException;

/**
 * @author Thorsten Schlathoelter
 */
public class TestNGUtils {

    /**
     * Skip a test depending on operating system
     * @param os
     * @param reasonForSkip
     */
    public static void skipForOs(String os, String reasonForSkip) {
        if (System.getProperty("os.name").toLowerCase().contains(os.toLowerCase())) {
            throw new SkipException(reasonForSkip);
        }
    }
}
