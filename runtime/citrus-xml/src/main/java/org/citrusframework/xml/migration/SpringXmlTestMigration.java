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

package org.citrusframework.xml.migration;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Migrates Spring bean XML test cases (citrus-spring test case schema) to the plain XML test DSL.
 * <p>
 * The migration uses an XSLT stylesheet to transform the test case. Migrated tests keep the file name of the
 * Spring bean XML test so the default test source lookup of the XML test loader is able to find the test.
 * Templates that are defined in the Spring bean XML test source are migrated to separate template files
 * named after the source file and the template name (e.g. {@code MyTestIT-myTemplate.xml}).
 * <p>
 * Elements without a counterpart in the plain XML DSL (e.g. the java or input action and actions from other
 * Spring XML namespaces such as http or ws) are copied as is and marked with a migration warning comment so users are able to
 * migrate these elements manually.
 * <p>
 * The class provides a main method so the migration is able to run from the command line:
 * <pre>
 * java org.citrusframework.xml.migration.SpringXmlTestMigration &lt;source&gt; &lt;target-directory&gt;
 * </pre>
 * The source is either a single Spring bean XML test file or a directory that gets scanned recursively for Spring bean XML tests.
 */
public class SpringXmlTestMigration {

    /** XSLT stylesheet performing the migration */
    private static final String STYLESHEET = "org/citrusframework/xml/migration/spring-xml-test-migration.xsl";

    /** Stylesheet parameter selecting a template in the Spring bean XML source */
    private static final String TEMPLATE_PARAM = "template";

    /** Stylesheet parameter enabling the marker attributes that hold the namespace of unprefixed payload elements */
    private static final String NAMESPACE_FIXUP_PARAM = "namespaceFixup";

    /**
     * Namespace of the marker attributes. The JDK built-in XSLT processor generates prefixes for computed element names
     * without prefix and ignores an empty namespace, so the payload element names get fixed after the transformation.
     */
    private static final String NAMESPACE_FIXUP_NS = "urn:citrusframework:migration:namespace-fixup";
    private static final String NAMESPACE_FIXUP_ATTRIBUTE = "namespace";

    private static final String SPRING_BEANS_NS = "http://www.springframework.org/schema/beans";
    private static final String SPRING_TEST_NS = "http://www.citrusframework.org/schema/testcase";

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String XML_FILE_EXTENSION = ".xml";

    private static final Pattern WARNING_PATTERN = Pattern.compile("<!-- MIGRATION WARNING: (.*?) -->");
    private static final Pattern INVALID_FILE_NAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    /** Text with these characters is written as CDATA section for better readability */
    private static final Pattern MARKUP_CHARACTERS = Pattern.compile("[<>&]");

    private final Templates stylesheet;

    /** Elements that the stylesheet writes as CDATA sections in expanded name notation, e.g. {namespace}local-name */
    private final Set<String> cdataSectionElements;

    public SpringXmlTestMigration() {
        try (InputStream xsl = SpringXmlTestMigration.class.getClassLoader().getResourceAsStream(STYLESHEET)) {
            if (xsl == null) {
                throw new CitrusRuntimeException("Unable to find Spring XML test migration stylesheet: " + STYLESHEET);
            }

            // use the JDK built-in XSLT processor so the migration output does not depend on other processors on the classpath
            TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            stylesheet = transformerFactory.newTemplates(new StreamSource(xsl));
            cdataSectionElements = Stream.of(stylesheet.getOutputProperties()
                            .getProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "").trim().split("\\s+"))
                    .map(SpringXmlTestMigration::toExpandedName)
                    .collect(Collectors.toSet());
        } catch (IOException | TransformerConfigurationException e) {
            throw new CitrusRuntimeException("Failed to load Spring XML test migration stylesheet", e);
        }
    }

    /**
     * Converts the given element name to the expanded name notation {namespace}local-name.
     * Processors report the CDATA section elements either in this notation or as namespace:local-name.
     */
    private static String toExpandedName(String name) {
        int separator = name.lastIndexOf(':');
        if (name.startsWith("{") || separator < 0) {
            return name;
        }

        return "{%s}%s".formatted(name.substring(0, separator), name.substring(separator + 1));
    }

    /**
     * Main CLI method.
     * @param args the source file or directory and the target directory.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: SpringXmlTestMigration <source> <target-directory>");
            System.err.println("  <source>            Spring bean XML test file or directory to scan recursively for Spring bean XML tests");
            System.err.println("  <target-directory>  directory to write the migrated plain XML tests to");
            System.exit(1);
            return;
        }

        MigrationResult result = new SpringXmlTestMigration().migrate(Path.of(args[0]), Path.of(args[1]));

        for (MigratedFile migrated : result.migrated()) {
            System.out.printf("Migrated %s -> %s%n", migrated.source(), migrated.target());
            migrated.warnings().forEach(warning -> System.out.printf("  WARNING: %s%n", warning));
        }

        for (FailedFile failed : result.failed()) {
            System.err.printf("Failed to migrate %s: %s%n", failed.source(), failed.error());
        }

        System.out.printf("Migrated %d file(s), skipped %d file(s), failed %d file(s)%n",
                result.migrated().size(), result.skipped().size(), result.failed().size());

        if (!result.failed().isEmpty()) {
            System.exit(1);
        }
    }

    /**
     * Migrates the given Spring bean XML test file or all Spring bean XML tests in the given directory and its subdirectories.
     * The migrated tests are written to the target directory keeping the relative directory structure of the source directory.
     * XML files that are not Spring bean XML tests or templates (e.g. Spring bean configuration files) are skipped.
     * @param source Spring bean XML test file or directory.
     * @param targetDirectory directory to write the migrated plain XML tests to.
     * @return the migration result.
     */
    public MigrationResult migrate(Path source, Path targetDirectory) {
        if (!Files.exists(source)) {
            throw new CitrusRuntimeException("Spring XML test source does not exist: " + source);
        }

        Path sourcePath = source.toAbsolutePath().normalize();
        Path sourceDirectory = Files.isDirectory(sourcePath) ? sourcePath : sourcePath.getParent();
        if (sourceDirectory.equals(targetDirectory.toAbsolutePath().normalize())) {
            throw new CitrusRuntimeException("Target directory must not be the source directory: " + targetDirectory);
        }

        List<Path> sourceFiles;
        if (Files.isDirectory(sourcePath)) {
            try (Stream<Path> files = Files.walk(sourcePath)) {
                sourceFiles = files.filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().endsWith(XML_FILE_EXTENSION))
                        .sorted()
                        .toList();
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read Spring XML test sources from directory: " + source, e);
            }
        } else {
            sourceFiles = List.of(sourcePath);
        }

        List<MigratedFile> migrated = new ArrayList<>();
        List<Path> skipped = new ArrayList<>();
        List<FailedFile> failed = new ArrayList<>();
        for (Path sourceFile : sourceFiles) {
            Path targetFileDirectory = targetDirectory.resolve(sourceDirectory.relativize(sourceFile.getParent()).toString());
            try {
                Document document;
                try (InputStream sourceStream = Files.newInputStream(sourceFile)) {
                    document = parse(new InputSource(sourceStream));
                }

                if (!isSpringXmlTest(document)) {
                    skipped.add(sourceFile);
                    continue;
                }

                migrated.addAll(migrate(document, sourceFile, targetFileDirectory));
            } catch (IOException | CitrusRuntimeException e) {
                failed.add(new FailedFile(sourceFile, e.getMessage()));
            }
        }

        return new MigrationResult(migrated, skipped, failed);
    }

    /**
     * Migrates the test case in the given Spring bean XML test source to the plain XML test DSL.
     * @param springXmlTest the Spring bean XML test source.
     * @return the plain XML test.
     */
    public String migrateTest(String springXmlTest) {
        return transform(parse(new InputSource(new StringReader(springXmlTest))), null);
    }

    /**
     * Migrates the template with the given name in the Spring bean XML source to the plain XML template DSL.
     * @param springXml the Spring bean XML source defining the template.
     * @param templateName the name of the template to migrate.
     * @return the plain XML template.
     */
    public String migrateTemplate(String springXml, String templateName) {
        return transform(parse(new InputSource(new StringReader(springXml))), Objects.requireNonNull(templateName, "Missing template name"));
    }

    /**
     * Extracts the migration warnings from the given migrated plain XML test or template.
     * @param migrated the migrated plain XML source.
     * @return the list of migration warnings.
     */
    public static List<String> getWarnings(String migrated) {
        List<String> warnings = new ArrayList<>();
        Matcher matcher = WARNING_PATTERN.matcher(migrated);
        while (matcher.find()) {
            warnings.add(matcher.group(1));
        }
        return warnings;
    }

    private List<MigratedFile> migrate(Document document, Path sourceFile, Path targetDirectory) throws IOException {
        String fileName = sourceFile.getFileName().toString();
        String baseName = fileName.substring(0, fileName.length() - XML_FILE_EXTENSION.length());

        // migrate the test and all templates before writing any file, so a failure does not leave a partial migration
        Map<Path, String> migrated = new LinkedHashMap<>();
        if (document.getElementsByTagNameNS(SPRING_TEST_NS, "testcase").getLength() > 0) {
            migrated.put(targetDirectory.resolve(fileName), transform(document, null));
        }

        NodeList templates = document.getElementsByTagNameNS(SPRING_TEST_NS, "template");
        for (int i = 0; i < templates.getLength(); i++) {
            String templateName = ((Element) templates.item(i)).getAttribute("name");
            String templateFileName = baseName + "-" + INVALID_FILE_NAME_CHARS.matcher(templateName).replaceAll("_") + XML_FILE_EXTENSION;
            migrated.put(targetDirectory.resolve(templateFileName), transform(document, templateName));
        }

        List<MigratedFile> migratedFiles = new ArrayList<>();
        for (Map.Entry<Path, String> entry : migrated.entrySet()) {
            Files.createDirectories(entry.getKey().getParent());
            Files.writeString(entry.getKey(), entry.getValue(), StandardCharsets.UTF_8);
            migratedFiles.add(new MigratedFile(sourceFile, entry.getKey(), getWarnings(entry.getValue())));
        }

        return migratedFiles;
    }

    private String transform(Document document, String templateName) {
        if (templateName == null) {
            int testCases = document.getElementsByTagNameNS(SPRING_TEST_NS, "testcase").getLength();
            if (testCases != 1) {
                throw new CitrusRuntimeException("Spring XML test source must define exactly one test case, but found %d".formatted(testCases));
            }
        } else {
            long templates = countTemplates(document, templateName);
            if (templates == 0) {
                throw new CitrusRuntimeException("Unable to find template '%s' in Spring XML test source".formatted(templateName));
            } else if (templates > 1) {
                throw new CitrusRuntimeException("Spring XML test source defines template '%s' more than once".formatted(templateName));
            }
        }

        try {
            Transformer transformer = stylesheet.newTransformer();
            transformer.setErrorListener(new MigrationErrorListener());
            transformer.setParameter(NAMESPACE_FIXUP_PARAM, NAMESPACE_FIXUP_NS);
            if (templateName != null) {
                transformer.setParameter(TEMPLATE_PARAM, templateName);
            }

            DOMResult result = new DOMResult();
            transformer.transform(new DOMSource(document), result);
            return serialize((Document) result.getNode());
        } catch (TransformerException e) {
            throw new CitrusRuntimeException("Failed to migrate Spring XML test: " + e.getMessage(), e);
        }
    }

    /**
     * Serializes the migrated document with indentation. As the output settings of the stylesheet do not apply to DOM results,
     * the text of the stylesheet's CDATA section elements is converted to CDATA sections before serialization
     * when the text contains markup characters.
     */
    private String serialize(Document migrated) throws TransformerException {
        Element root = migrated.getDocumentElement();
        postProcess(migrated, root);

        StringBuilder xml = new StringBuilder(XML_DECLARATION).append(System.lineSeparator());
        for (Node node = migrated.getFirstChild(); node != root; node = node.getNextSibling()) {
            if (node instanceof Comment comment) {
                xml.append("<!--").append(comment.getData()).append("-->").append(System.lineSeparator());
            }
        }

        Transformer serializer = TransformerFactory.newDefaultInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        serializer.transform(new DOMSource(root), new StreamResult(writer));
        return xml.append(writer.toString().strip()).append(System.lineSeparator()).toString();
    }

    /**
     * Fixes the names of unprefixed payload elements with the namespace from the marker attribute,
     * removes the marker attributes and creates the CDATA sections.
     */
    private void postProcess(Document document, Element element) {
        Element processed = element;

        Attr fixup = element.getAttributeNodeNS(NAMESPACE_FIXUP_NS, NAMESPACE_FIXUP_ATTRIBUTE);
        if (fixup != null) {
            element.removeAttributeNode(fixup);

            String namespace = fixup.getValue();
            String generatedPrefix = element.getPrefix();
            if (generatedPrefix != null && namespace.equals(element.getAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, generatedPrefix))) {
                element.removeAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, generatedPrefix);
            }

            processed = (Element) document.renameNode(element, namespace.isEmpty() ? null : namespace, element.getLocalName());
        }

        NamedNodeMap attributes = processed.getAttributes();
        for (int i = attributes.getLength() - 1; i >= 0; i--) {
            Attr attribute = (Attr) attributes.item(i);
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI()) && NAMESPACE_FIXUP_NS.equals(attribute.getValue())) {
                processed.removeAttributeNode(attribute);
            }
        }

        if (cdataSectionElements.contains("{%s}%s".formatted(processed.getNamespaceURI(), processed.getLocalName()))
                && processed.getChildNodes().getLength() == 1 && processed.getFirstChild() instanceof Text text
                && MARKUP_CHARACTERS.matcher(text.getData()).find()) {
            processed.replaceChild(document.createCDATASection(text.getData()), text);
        }

        for (Node child = processed.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element childElement) {
                postProcess(document, childElement);
            }
        }
    }

    private static long countTemplates(Document document, String templateName) {
        NodeList templates = document.getElementsByTagNameNS(SPRING_TEST_NS, "template");
        return IntStream.range(0, templates.getLength())
                .mapToObj(i -> (Element) templates.item(i))
                .filter(template -> templateName.equals(template.getAttribute("name")))
                .count();
    }

    private static boolean isSpringXmlTest(Document document) {
        Element root = document.getDocumentElement();
        return SPRING_BEANS_NS.equals(root.getNamespaceURI()) && "beans".equals(root.getLocalName())
                && (root.getElementsByTagNameNS(SPRING_TEST_NS, "testcase").getLength() > 0
                    || root.getElementsByTagNameNS(SPRING_TEST_NS, "template").getLength() > 0);
    }

    private static Document parse(InputSource source) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            factory.setNamespaceAware(true);
            factory.setCoalescing(true);
            factory.setExpandEntityReferences(false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            return builder.parse(source);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new CitrusRuntimeException("Failed to parse Spring XML test source: " + e.getMessage(), e);
        }
    }

    /**
     * Error listener raising transformation errors as exceptions instead of printing them to the console.
     */
    private static class MigrationErrorListener implements ErrorListener {
        @Override
        public void warning(TransformerException exception) {
            // ignore warnings
        }

        @Override
        public void error(TransformerException exception) throws TransformerException {
            throw exception;
        }

        @Override
        public void fatalError(TransformerException exception) throws TransformerException {
            throw exception;
        }
    }

    /**
     * Result of a migration run.
     * @param migrated the migrated tests and templates.
     * @param skipped the XML files that are not Spring bean XML tests.
     * @param failed the files that failed to migrate.
     */
    public record MigrationResult(List<MigratedFile> migrated, List<Path> skipped, List<FailedFile> failed) {
        public MigrationResult {
            migrated = List.copyOf(migrated);
            skipped = List.copyOf(skipped);
            failed = List.copyOf(failed);
        }
    }

    /**
     * Migrated test or template.
     * @param source the Spring bean XML source file.
     * @param target the migrated plain XML file.
     * @param warnings the migration warnings for elements that need to be migrated manually.
     */
    public record MigratedFile(Path source, Path target, List<String> warnings) {
        public MigratedFile {
            warnings = List.copyOf(warnings);
        }
    }

    /**
     * Spring bean XML source that failed to migrate.
     * @param source the Spring bean XML source file.
     * @param error the error message.
     */
    public record FailedFile(Path source, String error) {
    }
}
