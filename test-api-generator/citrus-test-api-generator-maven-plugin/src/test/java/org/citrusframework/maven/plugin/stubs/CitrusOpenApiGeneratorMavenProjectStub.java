package org.citrusframework.maven.plugin.stubs;

import org.apache.maven.model.Build;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CitrusOpenApiGeneratorMavenProjectStub extends MavenProjectStub {

    private final String config;

    public CitrusOpenApiGeneratorMavenProjectStub(String config) {
        this.config = config;
        initModel();
        initBuild();
    }

    private void initBuild() {
        Build build = new Build();
        build.setDirectory(getBasedir() + "/target");
        build.setSourceDirectory(getBasedir() + "/src");
        setBuild(build);
    }

    private void initModel() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            setModel(reader.read(new FileReader("pom.xml")));
        } catch (IOException | XmlPullParserException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public File getBasedir() {
        return new File(PlexusTestCase.getBasedir() + "/target/" + config);
    }
}
