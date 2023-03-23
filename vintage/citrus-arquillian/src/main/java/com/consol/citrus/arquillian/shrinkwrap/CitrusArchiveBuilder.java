/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.shrinkwrap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.consol.citrus.CitrusVersion;
import org.jboss.shrinkwrap.resolver.api.FormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;

/**
 * Resolves Citrus artifacts with transitive dependencies to file set which can be used as library resource in
 * shrink wrap archives.
 *
 * @author Christoph Deppisch
 * @since 2.2
 */
public final class CitrusArchiveBuilder {

    /** Should include transitive dependency libraries */
    private boolean transitivity = true;

    /** Should resolve library dependencies in offline mode */
    private boolean offline = true;

    /** Citrus version to use */
    private String version = CitrusVersion.version();

    /** List of Citrus artifact names to resolve */
    private List<String> artifactCoordinates = new ArrayList<>();

    /** Additional dependencies to load with this builder */
    private List<MavenDependency> additionalDependencies = new ArrayList<>();

    /** Additional dependencies to explicitly exclude with this builder */
    private List<MavenDependency> excludedDependencies = new ArrayList<>();

    /**
     * Default constructor.
     */
    private CitrusArchiveBuilder() {
    }

    /**
     * Constructor using artifact version.
     * @param version
     */
    private CitrusArchiveBuilder(String version) {
        this.version = version;
    }

    /**
     * Initialize with default settings.
     * @return
     */
    public static final CitrusArchiveBuilder latestVersion() {
        return new CitrusArchiveBuilder();
    }

    /**
     * Initialize with default settings.
     * @return
     */
    public static final CitrusArchiveBuilder version(String version) {
        return new CitrusArchiveBuilder(version);
    }

    /**
     * Resolve artifacts for given coordinates.
     * @return
     */
    public File[] build() {
        ConfigurableMavenResolverSystem resolver = Maven.configureResolver()
                .workOffline(offline);

        if (!additionalDependencies.isEmpty()) {
            resolver.addDependencies(additionalDependencies.toArray(new MavenDependency[0]));
        }

        MavenStrategyStage maven = resolver.resolve(artifactCoordinates);
        return applyTransitivity(maven).asFile();
    }

    /**
     * Gets the complete Citrus stack as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder all() {
        core();
        jms();
        kafka();
        http();
        websocket();
        ws();
        ssh();
        ftp();
        mail();
        camel();
        vertx();
        docker();
        kubernetes();
        sql();
        selenium();
        cucumber();
        zookeeper();
        springIntegration();
        rmi();
        jmx();
        restdocs();
        javaDsl();
        groovy();

        return this;
    }

    /**
     * Gets the core Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder core() {
        artifactCoordinates.add(getCoordinates("citrus-core"));
        return this;
    }

    /**
     * Gets the jms Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder jms() {
        artifactCoordinates.add(getCoordinates("citrus-jms"));
        return this;
    }

    /**
     * Gets the kafka Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder kafka() {
        artifactCoordinates.add(getCoordinates("citrus-kafka"));
        return this;
    }

    /**
     * Gets the http Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder http() {
        artifactCoordinates.add(getCoordinates("citrus-http"));
        return this;
    }

    /**
     * Gets the http Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder websocket() {
        artifactCoordinates.add(getCoordinates("citrus-websocket"));
        return this;
    }

    /**
     * Gets the ws Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder ws() {
        artifactCoordinates.add(getCoordinates("citrus-ws"));
        return this;
    }

    /**
     * Gets the ftp Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder ftp() {
        artifactCoordinates.add(getCoordinates("citrus-ftp"));
        return this;
    }

    /**
     * Gets the ssh Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder ssh() {
        artifactCoordinates.add(getCoordinates("citrus-ssh"));
        return this;
    }

    /**
     * Gets the camel Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder camel() {
        artifactCoordinates.add(getCoordinates("citrus-camel"));
        return this;
    }

    /**
     * Gets the docker Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder docker() {
        artifactCoordinates.add(getCoordinates("citrus-docker"));
        return this;
    }

    /**
     * Gets the kubernetes Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder kubernetes() {
        artifactCoordinates.add(getCoordinates("citrus-kubernetes"));
        return this;
    }

    /**
     * Gets the sql Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder sql() {
        artifactCoordinates.add(getCoordinates("citrus-sql"));
        return this;
    }

    /**
     * Gets the selenium Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder selenium() {
        artifactCoordinates.add(getCoordinates("citrus-selenium"));
        return this;
    }

    /**
     * Gets the zookeeper Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder zookeeper() {
        artifactCoordinates.add(getCoordinates("citrus-zookeeper"));
        return this;
    }

    /**
     * Gets the Spring integration Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder springIntegration() {
        artifactCoordinates.add(getCoordinates("citrus-spring-integration"));
        return this;
    }

    /**
     * Gets the cucumber Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder cucumber() {
        artifactCoordinates.add(getCoordinates("citrus-cucumber"));
        return this;
    }

    /**
     * Gets the groovy Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder groovy() {
        artifactCoordinates.add(getCoordinates("citrus-groovy"));
        return this;
    }

    /**
     * Gets the rmi Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder rmi() {
        artifactCoordinates.add(getCoordinates("citrus-rmi"));
        return this;
    }

    /**
     * Gets the jmx Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder jmx() {
        artifactCoordinates.add(getCoordinates("citrus-jmx"));
        return this;
    }

    /**
     * Gets the restdocs Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder restdocs() {
        artifactCoordinates.add(getCoordinates("citrus-restdocs"));
        return this;
    }

    /**
     * Gets the mail Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder mail() {
        artifactCoordinates.add(getCoordinates("citrus-mail"));
        return this;
    }

    /**
     * Gets the vertx Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder vertx() {
        artifactCoordinates.add(getCoordinates("citrus-vertx"));
        return this;
    }

    /**
     * Gets the Java DSL Citrus artifact as resolved Maven dependency set.
     * @return
     */
    public CitrusArchiveBuilder javaDsl() {
        artifactCoordinates.add(getCoordinates("citrus-java-dsl"));
        return this;
    }

    /**
     * Sets the transitivity on this resolver.
     * @param transitivity
     * @return
     */
    public CitrusArchiveBuilder transitivity(boolean transitivity) {
        this.transitivity = transitivity;
        return this;
    }

    /**
     * Sets the offline mode on this resolver.
     * @param offlineMode
     * @return
     */
    public CitrusArchiveBuilder workOffline(boolean offlineMode) {
        this.offline = offlineMode;
        return this;
    }

    /**
     * Add additional dependency to load.
     * @param dependency
     * @return
     */
    public CitrusArchiveBuilder addDependency(MavenDependency dependency) {
        this.additionalDependencies.add(dependency);
        return this;
    }

    /**
     * Add additional dependencies to load.
     * @param dependencies
     * @return
     */
    public CitrusArchiveBuilder addDependency(MavenDependency... dependencies) {
        Stream.of(dependencies).forEach(this::addDependency);
        return this;
    }

    /**
     * Add additional dependency to explicitly exclude.
     * @param dependency
     * @return
     */
    public CitrusArchiveBuilder excludeDependency(MavenDependency dependency) {
        this.excludedDependencies.add(dependency);
        return this;
    }

    /**
     * Add additional dependencies to explicitly exclude.
     * @param dependencies
     * @return
     */
    public CitrusArchiveBuilder excludeDependency(MavenDependency... dependencies) {
        Stream.of(dependencies).forEach(this::excludeDependency);
        return this;
    }

    /**
     * Based on transitivity setting apply transitivity to Maven strategy.
     * @param maven
     * @return
     */
    private FormatStage applyTransitivity(MavenStrategyStage maven) {
        if (!excludedDependencies.isEmpty()) {
            return maven.using(new RejectDependenciesStrategy(excludedDependencies.stream()
                                                                    .map(MavenDependency::toCanonicalForm)
                                                                    .toArray(String[]::new)));
        } else if (transitivity) {
            return maven.withTransitivity();
        } else {
            return maven.withoutTransitivity();
        }
    }

    /**
     * Constructs Maven artifact coordinates with current version.
     * @param artifactId
     * @return
     */
    private String getCoordinates(String artifactId) {
        return String.format("com.consol.citrus:%s:%s", artifactId, version);
    }
}
