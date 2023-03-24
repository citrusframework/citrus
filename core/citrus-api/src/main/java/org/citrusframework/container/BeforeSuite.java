package org.citrusframework.container;

/**
 * @author Christoph Deppisch
 */
public interface BeforeSuite extends TestActionContainer {

    /**
     * Checks if this suite actions should execute according to suite name and included test groups.
     * @param suiteName
     * @param includedGroups
     * @return
     */
    boolean shouldExecute(String suiteName, String[] includedGroups);
}
