package org.citrusframework;

/**
 * Interface marks test case to support group bindings. These are used to enable/disable tests based on group settings.
 * @author Christoph Deppisch
 */
public interface TestGroupAware {

    /**
     * Gets the groups.
     * @return
     */
    String[] getGroups();

    /**
     * Sets the test groups.
     * @param groups
     */
    void setGroups(String[] groups);
}
