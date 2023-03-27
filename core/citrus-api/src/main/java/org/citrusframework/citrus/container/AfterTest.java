package org.citrusframework.citrus.container;

/**
 * @author Christoph Deppisch
 */
public interface AfterTest extends TestActionContainer {

    /**
     * Checks if this suite actions should execute according to suite name and included test groups.
     * @param testName
     * @param packageName
     * @param includedGroups
     * @return
     */
    boolean shouldExecute(String testName, String packageName, String[] includedGroups);
}
