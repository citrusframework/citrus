package org.citrusframework.maven.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.maven.api.plugin.testing.MojoExtension;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.lifecycle.internal.MojoDescriptorCreator;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

/**
 * Minimal replacement for the test-setup logic formerly inherited from
 * {@code AbstractMojoTestCase} (deprecated since maven-plugin-testing-harness 3.4.0).
 *
 * <p>Only the pieces {@code TestApiGeneratorMojoIntegrationTest} actually needs are ported:
 * container bootstrap with the plugin descriptor, {@code lookupMojo} with POM
 * {@code <configuration>} injection, and {@code newMojoExecution}. Ported logic mirrors
 * {@code AbstractMojoTestCase} 3.5.1 so scenario behavior is unchanged. POM configuration
 * extraction reuses the non-deprecated public helper on {@code MojoExtension}; the annotation
 * driven {@code InjectMojo} API cannot be used here because the goal's POM varies per
 * parameterized invocation.
 */
final class MojoHarness {

    private ComponentConfigurator configurator;

    private PlexusContainer container;

    private PluginDescriptor pluginDescriptor;

    private final Map<String, MojoDescriptor> mojoDescriptors = new HashMap<>();

    void start() throws Exception {
        ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());

        ContainerConfiguration cc = new DefaultContainerConfiguration()
                .setClassWorld(classWorld)
                .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
                .setAutoWiring(true)
                .setName("maven");

        PlexusContainer newContainer = new DefaultPlexusContainer(cc);
        container = newContainer;

        boolean started = false;
        try {
            configurator = newContainer.lookup(ComponentConfigurator.class, "basic");
            Context context = newContainer.getContext();
            Map<Object, Object> map = context.getContextData();

            try (InputStream is = getClass().getResourceAsStream("/" + getPluginDescriptorLocation());
                    Reader reader = new BufferedReader(new XmlStreamReader(is));
                    InterpolationFilterReader interpolationReader = new InterpolationFilterReader(reader, map, "${", "}")) {

                pluginDescriptor = new PluginDescriptorBuilder().build(interpolationReader);

                Artifact artifact = new DefaultArtifact(
                        pluginDescriptor.getGroupId(),
                        pluginDescriptor.getArtifactId(),
                        pluginDescriptor.getVersion(),
                        null,
                        "jar",
                        null,
                        new DefaultArtifactHandler("jar"));

                artifact.setFile(getPluginArtifactFile());
                pluginDescriptor.setPluginArtifact(artifact);
                pluginDescriptor.setArtifacts(Collections.singletonList(artifact));

                for (ComponentDescriptor<?> desc : pluginDescriptor.getComponents()) {
                    newContainer.addComponentDescriptor(desc);
                }

                for (MojoDescriptor mojoDescriptor : pluginDescriptor.getMojos()) {
                    mojoDescriptors.put(mojoDescriptor.getGoal(), mojoDescriptor);
                }
            }

            started = true;
        } finally {
            if (!started) {
                dispose();
            }
        }
    }

    void stop() {
        dispose();
    }

    private void dispose() {
        if (container != null) {
            container.dispose();
            container = null;
        }
        configurator = null;
        pluginDescriptor = null;
        mojoDescriptors.clear();
    }

    /**
     * Lookup the mojo leveraging the actual subproject pom, injecting the plugin's
     * {@code <configuration>} block parsed from the given pom file.
     */
    <T extends Mojo> T mojo(String goal, File pom) throws Exception {
        requireStarted();

        Xpp3Dom pomDom;
        try (Reader reader = new XmlStreamReader(pom)) {
            pomDom = Xpp3DomBuilder.build(reader);
        }

        PlexusConfiguration pluginConfiguration = new XmlPlexusConfiguration(
                MojoExtension.extractPluginConfiguration(pluginDescriptor.getArtifactId(), pomDom));

        return lookupMojo(
                pluginDescriptor.getGroupId(),
                pluginDescriptor.getArtifactId(),
                pluginDescriptor.getVersion(),
                goal,
                pluginConfiguration);
    }

    @SuppressWarnings("unchecked")
    private <T extends Mojo> T lookupMojo(
            String groupId, String artifactId, String version, String goal, PlexusConfiguration pluginConfiguration)
            throws Exception {
        requireStarted();

        // pluginkey = groupId : artifactId : version : goal
        T mojo = (T) container.lookup(Mojo.class, groupId + ":" + artifactId + ":" + version + ":" + goal);

        if (pluginConfiguration != null) {
            ExpressionEvaluator evaluator = new StubEvaluator(baseDir());

            configurator.configureComponent(
                    mojo, pluginConfiguration, evaluator, container.getContainerRealm());
        }

        return mojo;
    }

    private void requireStarted() {
        if (container == null || pluginDescriptor == null) {
            throw new IllegalStateException("container is null, make sure start() is called");
        }
    }

    MojoExecution execution(String goal) {
        requireStarted();

        MojoDescriptor mojoDescriptor = mojoDescriptors.get(goal);
        Objects.requireNonNull(mojoDescriptor,
                () -> String.format("The MojoDescriptor for the goal %s cannot be null.", goal));
        MojoExecution execution = new MojoExecution(mojoDescriptor);
        finalizeMojoConfiguration(execution);
        return execution;
    }

    // Copy of o.a.m.l.i.DefaultLifecycleExecutionPlanCalculator#finalizeMojoConfiguration(MojoExecution),
    // as previously inherited via AbstractMojoTestCase#newMojoExecution.
    private void finalizeMojoConfiguration(MojoExecution mojoExecution) {
        MojoDescriptor mojoDescriptor = mojoExecution.getMojoDescriptor();

        Xpp3Dom executionConfiguration = mojoExecution.getConfiguration();
        if (executionConfiguration == null) {
            executionConfiguration = new Xpp3Dom("configuration");
        }

        Xpp3Dom defaultConfiguration = new Xpp3Dom(MojoDescriptorCreator.convert(mojoDescriptor));

        Xpp3Dom finalConfiguration = new Xpp3Dom("configuration");

        if (mojoDescriptor.getParameters() != null) {
            for (Parameter parameter : mojoDescriptor.getParameters()) {
                Xpp3Dom parameterConfiguration = executionConfiguration.getChild(parameter.getName());

                if (parameterConfiguration == null) {
                    parameterConfiguration = executionConfiguration.getChild(parameter.getAlias());
                }

                Xpp3Dom parameterDefaults = defaultConfiguration.getChild(parameter.getName());

                parameterConfiguration = Xpp3Dom.mergeXpp3Dom(parameterConfiguration, parameterDefaults, Boolean.TRUE);

                if (parameterConfiguration != null) {
                    parameterConfiguration = new Xpp3Dom(parameterConfiguration, parameter.getName());

                    if (StringUtils.isEmpty(parameterConfiguration.getAttribute("implementation"))
                            && StringUtils.isNotEmpty(parameter.getImplementation())) {
                        parameterConfiguration.setAttribute("implementation", parameter.getImplementation());
                    }

                    finalConfiguration.addChild(parameterConfiguration);
                }
            }
        }

        mojoExecution.setConfiguration(finalConfiguration);
    }

    /**
     * Returns best-effort plugin artifact file: parent directory of the META-INF directory
     * holding the plugin descriptor, falling back to the test basedir.
     */
    private File getPluginArtifactFile() throws java.io.IOException {
        final String pluginDescriptorLocation = getPluginDescriptorLocation();
        final URL resource = getClass().getResource("/" + pluginDescriptorLocation);

        File file = null;

        // attempt to resolve relative to META-INF/maven/plugin.xml first
        if (resource != null) {
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                String path = resource.getPath();
                if (path.endsWith(pluginDescriptorLocation)) {
                    file = new File(path.substring(0, path.length() - pluginDescriptorLocation.length()));
                }
            } else if ("jar".equalsIgnoreCase(resource.getProtocol())) {
                try {
                    URL jarfile = URI.create(resource.getPath()).toURL();
                    if ("file".equalsIgnoreCase(jarfile.getProtocol())) {
                        String path = jarfile.getPath();
                        if (path.endsWith(pluginDescriptorLocation)) {
                            file = new File(path.substring(0, path.length() - pluginDescriptorLocation.length() - 2));
                        }
                    }
                } catch (MalformedURLException | IllegalArgumentException e) {
                    // not jar:file:/ URL, too bad
                }
            }
        }

        // fallback to test project basedir if couldn't resolve relative to META-INF/maven/plugin.xml
        if (file == null || !file.exists()) {
            file = new File(baseDir());
        }

        return file.getCanonicalFile();
    }

    private String getPluginDescriptorLocation() {
        return "META-INF/maven/plugin.xml";
    }

    /**
     * Module base directory. Surefire and failsafe set the {@code basedir} system property
     * to the module directory; fall back to the working directory otherwise.
     */
    String baseDir() {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File("").getAbsolutePath();
        }
        return basedir;
    }

    /**
     * Port of the deprecated {@code ResolverExpressionEvaluatorStub}: resolves
     * {@code basedir} expressions against the module directory and leaves
     * everything else untouched.
     */
    private static final class StubEvaluator implements ExpressionEvaluator {

        private final String basedir;

        StubEvaluator(String basedir) {
            this.basedir = basedir;
        }

        @Override
        public Object evaluate(String expr) throws ExpressionEvaluationException {
            Object value = null;

            if (expr == null) {
                return null;
            }

            String expression = stripTokens(expr);

            if (expression.equals(expr)) {
                int index = expr.indexOf("${");
                if (index >= 0) {
                    int lastIndex = expr.indexOf("}", index);
                    if (lastIndex >= 0) {
                        String retVal = expr.substring(0, index);

                        if (index > 0 && expr.charAt(index - 1) == '$') {
                            retVal += expr.substring(index + 1, lastIndex + 1);
                        } else {
                            retVal += evaluate(expr.substring(index, lastIndex + 1));
                        }

                        retVal += evaluate(expr.substring(lastIndex + 1));
                        return retVal;
                    }
                }

                // Was not an expression
                if (expression.indexOf("$$") > -1) {
                    return expression.replaceAll("\\$\\$", "\\$");
                }
            }

            if ("basedir".equals(expression) || "project.basedir".equals(expression)) {
                return basedir;
            } else if (expression.startsWith("basedir") || expression.startsWith("project.basedir")) {
                int pathSeparator = expression.indexOf("/");

                if (pathSeparator > 0) {
                    value = basedir + expression.substring(pathSeparator);
                } else {
                    System.out.println("Got expression '" + expression + "' that was not recognised");
                }
                return value;
            } else if ("localRepository".equals(expression)) {
                File localRepo = new File(basedir, "target/local-repo");
                return new MavenArtifactRepository(
                        "localRepository",
                        "file://" + localRepo.getAbsolutePath(),
                        new DefaultRepositoryLayout(),
                        null,
                        null);
            } else {
                return expr;
            }
        }

        private String stripTokens(String expr) {
            if (expr.startsWith("${") && expr.indexOf("}") == expr.length() - 1) {
                expr = expr.substring(2, expr.length() - 1);
            }

            return expr;
        }

        @Override
        public File alignToBaseDirectory(File file) {
            if (file.getAbsolutePath().startsWith(basedir)) {
                return file;
            } else if (file.isAbsolute()) {
                return file;
            } else {
                return new File(basedir, file.getPath());
            }
        }
    }
}
