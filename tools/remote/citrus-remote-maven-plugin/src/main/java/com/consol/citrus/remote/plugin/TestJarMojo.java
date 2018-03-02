/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.remote.plugin;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
@Mojo(name = "test-jar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST)
public class TestJarMojo extends AbstractCitrusRemoteAssemblyMojo {

    @Parameter(property = "citrus.skip.test.jar", defaultValue = "false")
    protected boolean skipTestJar;

    @Component( role = Archiver.class, hint = "jar" )
    private JarArchiver jarArchiver;

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        if (shouldSkip()) {
            return;
        }

        if (hasTestJar() || getAssembly().isTestJarProvided()) {
            getLog().info(String.format("Skip test-jar creation as it is already attached to the project (classifier='%s')", getTestJar().getClassifier()));
        } else {
            createTestJarArchive();
        }

        super.doExecute();
    }

    protected boolean shouldSkip() {
        return skipTestJar;
    }

    @Override
    protected String getDefaultDescriptorRef() {
        return "test-jar";
    }

    private boolean hasTestJar() {
        return project.getAttachedArtifacts()
                .stream()
                .filter(Artifact::hasClassifier)
                .map(Artifact::getClassifier)
                .anyMatch(c -> c.equals(getTestJar().getClassifier()));
    }

    /**
     * Creates default test-jar for project sources. This is a basis for the
     * executable tests artifact creation in superclass.
     *
     * @throws MojoExecutionException
     */
    public void createTestJarArchive() throws MojoExecutionException {
        File jarFile = new File(getOutputDirectory(), getFinalName() + "-" + getTestJar().getClassifier() + ".jar");
        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(jarFile);

        try {
            if (!getTestJar().getTestClassesDirectory().exists()) {
                getLog().warn( "Tests jar will be empty - no content was marked for inclusion!" );
            } else {
                archiver.getArchiver().addDirectory( getTestJar().getTestClassesDirectory(), getTestJar().getIncludes(), getTestJar().getExcludes() );
            }

            archiver.createArchive(session, project, new MavenArchiveConfiguration());
            projectHelper.attachArtifact(project, "test-jar", getTestJar().getClassifier(), jarFile);
        } catch (Exception e) {
            throw new MojoExecutionException( "Error assembling tests jar", e );
        }
    }
}
