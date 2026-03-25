# Citrus - AI Agent Guidelines

This file provides guidance to AI agents such as Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Citrus is a Java-based integration testing framework for message-based enterprise applications. 
It supports testing various message transports (HTTP, Kafka, JMS, SOAP, etc.) and validation of different data formats (XML, JSON, YAML, etc.).

**Key Technologies**: 
- Java 17+ 
- Maven 3.9+ 
- JUnit Jupiter 
- Apache Camel 
- Spring Framework 
- Testcontainers

## Build and Test Commands

### Building the Project

```bash
# Build and install all modules (runs unit and integration tests)
./mvnw clean install

# Build without running tests
./mvnw clean install -DskipTests

# Build a specific module
./mvnw clean install -pl <module-name> -am

# Examples:
./mvnw clean install -pl core/citrus-base -am
./mvnw clean install -pl endpoints/citrus-http -am
```

### Running Tests

```bash
# Run all tests in a module
./mvnw test -pl <module-name>

# Run integration tests only
./mvnw verify -pl <module-name>

# Run a specific test class
./mvnw test -Dtest=<TestClassName> -pl <module-name>

# Run a specific integration test
./mvnw verify -Dit.test=<TestClassName>IT -pl <module-name>

# Examples:
./mvnw verify -Dit.test=HttpClientIT -pl endpoints/citrus-http
./mvnw test -Dtest=EchoActionTest -pl core/citrus-base
```

**Test Naming Convention**: Java integration tests end with `IT.java`, unit tests end with `Test.java`. Maven uses surefire for unit tests and failsafe for integration tests.

### Running Single Tests

To run a single test method:
```bash
./mvnw test -Dtest=ClassName#methodName -pl <module-name>
./mvnw verify -Dit.test=ClassNameIT#methodName -pl <module-name>
```

## Project Architecture

### Module Structure

The project is organized into several top-level modules:

- **`core/`** - Core framework components
  - `citrus-api` - Core interfaces and abstractions (TestAction, TestActionRunner, etc.)
  - `citrus-base` - Base implementation classes
  - `citrus-spring` - Spring Framework integration

- **`runtime/`** - Test runtime integrations
    - `citrus-junit5` - JUnit Jupiter integration (most common for new tests)
    - `citrus-testng` - TestNG integration
    - `citrus-cucumber` - Cucumber BDD integration
    - `citrus-quarkus` - Quarkus integration
    - `citrus-xml`, `citrus-yaml`, `citrus-groovy` - Alternative test DSLs

- **`endpoints/`** - Message transport implementations
  - Each endpoint module supports a specific protocol (HTTP, Kafka, JMS, FTP, WebSocket, etc.)
  - Common endpoints: `citrus-http`, `citrus-kafka`, `citrus-jms`, `citrus-ws` (SOAP)

- **`connectors/`** - External tool integrations
  - `citrus-kubernetes`, `citrus-knative` - Kubernetes testing
  - `citrus-docker`, `citrus-testcontainers` - Container testing
  - `citrus-selenium` - Browser automation
  - `citrus-sql` - Database testing
  - `citrus-openapi` - OpenAPI/REST testing

- **`validation/`** - Message validation modules
  - Separate modules for JSON, XML, YAML, text, binary validation
  - Includes Hamcrest and Groovy-based validators

- **`tools/`** - Additional tooling
  - Maven plugins, archetypes, Cucumber step definitions
  - JBang support for running Citrus without project setup

### Core Concepts

**TestAction**: The fundamental unit of execution in Citrus. Implements `execute(TestContext)`.

**TestActionRunner**: Interface for executing test actions. Two main styles:
- Standard runner: `runner.run(action)`
- Gherkin-style: `runner.given()`, `runner.when()`, `runner.then()`

**TestCaseRunner**: Extends TestActionRunner with start/stop lifecycle methods.

**Endpoints**: Configurable message endpoints for send/receive operations. Each protocol has its own endpoint implementation (HttpClient, KafkaEndpoint, JmsEndpoint, etc.).

**Message Validation**: Framework validates message body and headers. Supports different validators for XML, JSON, YAML, etc.

### Common Test Patterns

**JUnit Jupiter Integration Test** (most common):

```java
@CitrusSupport
public class MyFeatureIT implements TestActionSupport {

    @Test
    @CitrusTest
    void shouldTestFeature(@CitrusResource TestActionRunner runner) {
        runner.given(
            createVariable("id", "12345")
        );

        runner.when(
            http().client("myClient")
                .send()
                .post("/api/resource")
                .message()
                .body("{ \"id\": \"${id}\" }")
        );

        runner.then(
            http().client("myClient")
                .receive()
                .response(HttpStatus.OK)
        );
    }
}
```

**Key Annotations**:

- `@CitrusSupport` - Enables Citrus JUnit5 extension
- `@CitrusTest` - Marks a test method as a Citrus test
- `@CitrusResource` - Injects TestActionRunner, TestCaseRunner, or TestContext

**TestActionSupport Interface**: Provides access to test action builder methods like `echo()`, `createVariable()`, `http()`, `send()`, `receive()`, etc.

## Code Conventions

- Integration tests must end with `IT.java`
- Unit tests end with `Test.java`
- All Java code follows Apache 2.0 license header format
- Code quality is enforced via SonarCloud quality gates
- Test coverage is expected for new features

## Development Workflow

1. **Creating New Features**: Add tests first, ensure they fail, then implement
2. **Running Tests Locally**: Always run integration tests before pushing (`./mvnw verify`)
3. **Pull Requests**: Require linked issue, passing builds, and documentation updates
4. **Main Branch**: `main` (not `master`)
5. **CI/CD**: GitHub Actions runs on all PRs and pushes to main

## Documentation

- The user guide documentation can be found in `src/manual`

## Special Notes

- The framework supports both programmatic Java tests and declarative tests (XML, YAML)
- Tests can use JBang for quick prototyping without project setup
- Many modules have cross-dependencies - use `-am` (also-make) when building specific modules
- Integration tests may use TestContainers and require Docker to be running
- Some tests require external services (Kubernetes cluster for k8s tests, etc.)
