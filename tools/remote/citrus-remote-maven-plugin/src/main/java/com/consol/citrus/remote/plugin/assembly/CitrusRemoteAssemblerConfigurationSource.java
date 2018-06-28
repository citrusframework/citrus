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

package com.consol.citrus.remote.plugin.assembly;

import com.consol.citrus.remote.plugin.config.AssemblyConfiguration;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugins.assembly.utils.InterpolationConstants;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenReaderFilter;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.codehaus.plexus.interpolation.fixed.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

/**
 * @since 2.7.4
 */
public class CitrusRemoteAssemblerConfigurationSource implements AssemblerConfigurationSource {

    private final MavenProject project;
    private final MavenSession session;
    private final MavenReaderFilter readerFilter;
    private final List<MavenProject> reactorProjects;
    private final AssemblyConfiguration assemblyConfig;

    // Required by configuration source and duplicated from AbstractAssemblyMojo (which is unfortunately not extracted to be usable
    private FixedStringSearchInterpolator commandLinePropertiesInterpolator;
    private FixedStringSearchInterpolator envInterpolator;
    private FixedStringSearchInterpolator rootInterpolator;
    private FixedStringSearchInterpolator mainProjectInterpolator;

    public CitrusRemoteAssemblerConfigurationSource(AssemblyConfiguration assemblyConfig,
                                                    MavenProject project,
                                                    MavenSession session,
                                                    MavenReaderFilter readerFilter,
                                                    List<MavenProject> reactorProjects) {
        this.assemblyConfig = assemblyConfig;
        this.project = project;
        this.session = session;
        this.readerFilter = readerFilter;
        this.reactorProjects = reactorProjects;
    }

    @Override
    public String[] getDescriptors() {
        if (assemblyConfig != null) {
          String descriptor = assemblyConfig.getDescriptor().getFile();

          if (descriptor != null) {
            return new String[] { new File(descriptor).getAbsolutePath() };
          }
        }
        return new String[0];
    }

    @Override
    public String[] getDescriptorReferences() {
        if (assemblyConfig != null) {
            String descriptorRef = assemblyConfig.getDescriptor().getRef();
            if (descriptorRef != null) {
                return new String[]{ descriptorRef };
            }
        }
        return null;
    }

    // ============================================================================================

    @Override
    public File getOutputDirectory() {
        return assemblyConfig.getOutputDirectory();
    }

    @Override
    public File getWorkingDirectory() {
        return assemblyConfig.getWorkingDirectory();
    }

    @Override
    public File getTemporaryRootDirectory() {
        return assemblyConfig.getTemporaryRootDirectory();
    }

    @Override
    public String getFinalName() {
        return ".";
    }

    @Override
    public ArtifactRepository getLocalRepository() {
        return session.getLocalRepository();
    }

    @Override
    public List<MavenProject> getReactorProjects() {
        return reactorProjects;
    }

    @Override
    public List<ArtifactRepository> getRemoteRepositories() {
        return project.getRemoteArtifactRepositories();
    }

    @Override
    public MavenSession getMavenSession() {
        return session;
    }

    @Override
    public MavenArchiveConfiguration getJarArchiveConfiguration() {
        return assemblyConfig.getArchive();
    }

    @Override
    public String getEncoding() {
        return project.getProperties().getProperty("project.build.sourceEncoding");
    }

    @Override
    public String getEscapeString() {
        return null;
    }

    @Override
    public List<String> getDelimiters() {
        return null;
    }


    @Nonnull public FixedStringSearchInterpolator getCommandLinePropsInterpolator()
    {
        if (commandLinePropertiesInterpolator == null) {
            this.commandLinePropertiesInterpolator = createCommandLinePropertiesInterpolator();
        }
        return commandLinePropertiesInterpolator;
    }

    @Nonnull
    public FixedStringSearchInterpolator getEnvInterpolator()
    {
        if (envInterpolator == null) {
            this.envInterpolator = createEnvInterpolator();
        }
        return envInterpolator;
    }

    @Nonnull public FixedStringSearchInterpolator getRepositoryInterpolator()
    {
        if (rootInterpolator == null) {
            this.rootInterpolator = createRepositoryInterpolator();
        }
        return rootInterpolator;
    }


    @Nonnull
    public FixedStringSearchInterpolator getMainProjectInterpolator()
    {
        if (mainProjectInterpolator == null) {
            this.mainProjectInterpolator = mainProjectInterpolator(getProject());
        }
        return mainProjectInterpolator;
    }

    @Override
    public MavenProject getProject() {
        return project;
    }

    @Override
    public File getBasedir() {
        return project.getBasedir();
    }

    @Override
    public boolean isIgnoreDirFormatExtensions() {
        return true;
    }

    @Override
    public boolean isDryRun() {
        return false;
    }

    @Override
    public List<String> getFilters() {
        return Collections.emptyList();
    }

    @Override
    public boolean isIncludeProjectBuildFilters() {
        return true;
    }

    @Override
    public File getDescriptorSourceDirectory() {
        return null;
    }

    @Override
    public File getArchiveBaseDirectory() {
        return null;
    }

    @Override
    public String getTarLongFileMode() {
        return "warn";
    }

    @Override
    public File getSiteDirectory() {
        return null;
    }

    @Override
    public boolean isAssemblyIdAppended() {
        return true;
    }

    @Override
    public boolean isIgnoreMissingDescriptor() {
        return false;
    }

    @Override
    public String getArchiverConfig() {
        return null;
    }

    @Override
    public MavenReaderFilter getMavenReaderFilter() {
        return readerFilter;
    }

    @Override
    public boolean isUpdateOnly() {
        return false;
    }

    @Override
    public boolean isUseJvmChmod() {
        return false;
    }

    @Override
    public boolean isIgnorePermissions() {
        return false;
    }

    // =======================================================================
    // Taken from AbstractAssemblyMojo

    private FixedStringSearchInterpolator mainProjectInterpolator(MavenProject mainProject)
    {
        if (mainProject != null) {
            // 5
            return FixedStringSearchInterpolator.create(
                new org.codehaus.plexus.interpolation.fixed.PrefixedObjectValueSource(
                    InterpolationConstants.PROJECT_PREFIXES, mainProject, true ),

                // 6
                new org.codehaus.plexus.interpolation.fixed.PrefixedPropertiesValueSource(
                    InterpolationConstants.PROJECT_PROPERTIES_PREFIXES, mainProject.getProperties(), true ) );
        }
        else {
            return FixedStringSearchInterpolator.empty();
        }
    }

    private FixedStringSearchInterpolator createRepositoryInterpolator()
    {
        final Properties settingsProperties = new Properties();
        final MavenSession session = getMavenSession();

        if (getLocalRepository() != null) {
            settingsProperties.setProperty("localRepository", getLocalRepository().getBasedir());
            settingsProperties.setProperty("settings.localRepository", getLocalRepository().getBasedir());
        }
        else if (session != null && session.getSettings() != null) {
            settingsProperties.setProperty("localRepository", session.getSettings().getLocalRepository() );
            settingsProperties.setProperty("settings.localRepository", getLocalRepository().getBasedir() );
        }
        return FixedStringSearchInterpolator.create(new PropertiesBasedValueSource(settingsProperties));
    }

    private FixedStringSearchInterpolator createCommandLinePropertiesInterpolator()
    {
        Properties commandLineProperties = System.getProperties();
        final MavenSession session = getMavenSession();

        if (session != null) {
            commandLineProperties = new Properties();
            if (session.getSystemProperties() != null) {
                commandLineProperties.putAll(session.getSystemProperties());
            }
            if (session.getUserProperties() != null) {
                commandLineProperties.putAll(session.getUserProperties());
            }
        }
        PropertiesBasedValueSource cliProps = new PropertiesBasedValueSource( commandLineProperties );
        return FixedStringSearchInterpolator.create( cliProps );
    }

    private FixedStringSearchInterpolator createEnvInterpolator() {
        PrefixedPropertiesValueSource envProps = new PrefixedPropertiesValueSource(Collections.singletonList("env."),
                                                                                   CommandLineUtils.getSystemEnvVars(false), true );
        return FixedStringSearchInterpolator.create( envProps );
    }
}
