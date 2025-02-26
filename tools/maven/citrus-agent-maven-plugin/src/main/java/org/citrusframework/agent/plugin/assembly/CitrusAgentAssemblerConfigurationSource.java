/*
 * Copyright the original author or authors.
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

package org.citrusframework.agent.plugin.assembly;

import java.io.File;
import java.util.List;
import java.util.Properties;

import jakarta.annotation.Nonnull;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugins.assembly.model.Assembly;
import org.apache.maven.plugins.assembly.utils.InterpolationConstants;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenReaderFilter;
import org.citrusframework.agent.plugin.config.AssemblyConfiguration;
import org.codehaus.plexus.interpolation.fixed.FixedStringSearchInterpolator;
import org.codehaus.plexus.interpolation.fixed.PrefixedPropertiesValueSource;
import org.codehaus.plexus.interpolation.fixed.PropertiesBasedValueSource;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CitrusAgentAssemblerConfigurationSource implements AssemblerConfigurationSource {

    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final MavenReaderFilter mavenReaderFilter;
    private final List<MavenProject> reactorProjects;
    private final AssemblyConfiguration assemblyConfig;

    // Required by configuration source and duplicated from AbstractAssemblyMojo (which is unfortunately not extracted to be usable
    private FixedStringSearchInterpolator commandLinePropertiesInterpolator;
    private FixedStringSearchInterpolator envInterpolator;
    private FixedStringSearchInterpolator rootInterpolator;
    private FixedStringSearchInterpolator mainProjectInterpolator;

    public CitrusAgentAssemblerConfigurationSource(AssemblyConfiguration assemblyConfig,
                                                   MavenProject mavenProject,
                                                   MavenSession mavenSession,
                                                   MavenReaderFilter mavenReaderFilter,
                                                   List<MavenProject> reactorProjects) {
        this.assemblyConfig = assemblyConfig;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.mavenReaderFilter = mavenReaderFilter;
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

        return new String[0];
    }

    @Override
    public List<Assembly> getInlineDescriptors() {
        return emptyList();
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
    public List<MavenProject> getReactorProjects() {
        return reactorProjects;
    }

    @Override
    public MavenSession getMavenSession() {
        return mavenSession;
    }

    @Override
    public MavenArchiveConfiguration getJarArchiveConfiguration() {
        return assemblyConfig.getArchive();
    }

    @Override
    public String getEncoding() {
        return mavenProject.getProperties().getProperty("project.build.sourceEncoding");
    }

    @Override
    public String getEscapeString() {
        return null;
    }

    @Override
    public List<String> getDelimiters() {
        return emptyList();
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
    public Integer getOverrideUid() {
        return 0;
    }

    @Override
    public String getOverrideUserName() {
        return "";
    }

    @Override
    public Integer getOverrideGid() {
        return 0;
    }

    @Override
    public String getOverrideGroupName() {
        return mavenProject.getGroupId();
    }

    @Override
    public boolean isRecompressZippedFiles() {
        return false;
    }

    @Override
    public String getMergeManifestMode() {
        return "merge";
    }

    @Override
    public MavenProject getProject() {
        return mavenProject;
    }

    @Override
    public File getBasedir() {
        return mavenProject.getBasedir();
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
        return emptyList();
    }

    @Override
    public Properties getAdditionalProperties() {
        return null;
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
        return mavenReaderFilter;
    }

    @Override
    public boolean isUpdateOnly() {
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

        if (mavenSession.getLocalRepository() != null) {
            settingsProperties.setProperty("localRepository", mavenSession.getLocalRepository().getBasedir());
            settingsProperties.setProperty("settings.localRepository", mavenSession.getLocalRepository().getBasedir());
        }
        else if (session != null && session.getSettings() != null) {
            settingsProperties.setProperty("localRepository", session.getSettings().getLocalRepository() );
            settingsProperties.setProperty("settings.localRepository", mavenSession.getLocalRepository().getBasedir() );
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
        PrefixedPropertiesValueSource envProps = new PrefixedPropertiesValueSource(
                singletonList("env."),
                System.getProperties(),
                true);
        return FixedStringSearchInterpolator.create(envProps);
    }
}
