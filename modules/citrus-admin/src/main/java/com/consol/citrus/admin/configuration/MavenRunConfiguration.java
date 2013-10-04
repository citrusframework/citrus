package com.consol.citrus.admin.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Maven run configuration with system properties, profiles.
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MavenRunConfiguration extends AbstractRunConfiguration {

    /** List of active Maven profiles */
    private List<String> profiles = new ArrayList<String>();

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

}
