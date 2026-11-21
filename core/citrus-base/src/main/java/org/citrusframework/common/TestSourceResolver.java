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

package org.citrusframework.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.citrusframework.CitrusSettings;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.spi.ClasspathResourceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.StringUtils;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Resolves the test entries given on a {@link CitrusTestSource} annotation into an ordered list of
 * {@link ResolvedTestSource} entries. Entries are resolved in the following order: test names, explicit
 * source files, and files found in the packages to scan (sorted by path for deterministic ordering).
 *
 * @since 5.3
 */
public final class TestSourceResolver {

    /**
     * Prevent instantiation of utility class.
     */
    private TestSourceResolver() {
        // prevent instantiation
    }

    /**
     * Resolves all test entries given on the provided test source annotation.
     * @param testSource the annotation holding test names, sources and packages to scan.
     * @param method the test method used for fallback test name and package resolution.
     * @return the ordered list of resolved entries, never empty.
     */
    public static List<ResolvedTestSource> resolve(CitrusTestSource testSource, Method method) {
        List<ResolvedTestSource> entries = new ArrayList<>();

        String packageName = method.getDeclaringClass().getPackage().getName();
        if (StringUtils.hasText(testSource.packageName())) {
            packageName = testSource.packageName();
        }

        for (String name : testSource.name()) {
            entries.add(new ResolvedTestSource(name, packageName, null));
        }

        for (String source : testSource.sources()) {
            Resource file = FileUtils.getFileResource(source);

            String sourceFilePackageName = "";
            if (source.startsWith(Resources.CLASSPATH_RESOURCE_PREFIX)) {
                sourceFilePackageName = source.substring(Resources.CLASSPATH_RESOURCE_PREFIX.length());
            }

            if (StringUtils.hasText(sourceFilePackageName) && sourceFilePackageName.contains("/")) {
                sourceFilePackageName = sourceFilePackageName.substring(0, sourceFilePackageName.lastIndexOf("/"));
            }

            entries.add(new ResolvedTestSource(
                    FileUtils.getBaseName(FileUtils.getFileName(file.location())),
                    sourceFilePackageName.replace("/", "."), source));
        }

        for (String packageScan : testSource.packageScan()) {
            List<Path> fileResources = findFileResources(packageScan, CitrusSettings.getTestFileNamePattern(testSource.type()));
            fileResources.sort(Comparator.comparing(Path::toString));

            for (Path fileResource : fileResources) {
                String filePath = getPackageScanFilePath(fileResource, packageScan);

                entries.add(new ResolvedTestSource(
                        FileUtils.getBaseName(String.valueOf(fileResource.getFileName())), filePath, null));
            }
        }

        if (entries.isEmpty()) {
            entries.add(new ResolvedTestSource(method.getName(), packageName, null));
        }

        return entries;
    }

    /**
     * Finds all file resources in the given package matching the test file name patterns of the test source type.
     */
    private static List<Path> findFileResources(String packageScan, Set<String> fileNamePattern) {
        List<Path> fileResources = new ArrayList<>();

        try {
            for (String pattern : fileNamePattern) {
                fileResources.addAll(new ClasspathResourceResolver()
                        .getResources(packageScan.replace('.', File.separatorChar), pattern));
            }
        } catch (RuntimeException | IOException e) {
            throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageScan + "'", e);
        }

        return fileResources;
    }

    /**
     * Gets the package like path of the given file resource relative to the scanned package.
     */
    private static String getPackageScanFilePath(Path fileResource, String packageScan) {
        try {
            String filePath = fileResource.getParent().toFile().getCanonicalPath();

            if (packageScan.startsWith("file:")) {
                filePath = "file:" + filePath;
            }

            return FileUtils.getPackagePath(filePath, packageScan);
        } catch (IOException e) {
            throw new CitrusRuntimeException("Unable to locate file resources for test package '" + packageScan + "'", e);
        }
    }
}
