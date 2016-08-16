## Configuration

You have several options in customizing the Citrus project configuration. Citrus uses default settings that can be overwritten to some extend. As a framework Citrus internally works with the Spring IoC container. So Citrus will start a Spring application context and register several components as Spring beans. You can customize the behavior of these beans and you can add custom settings by setting system properties.

### Citrus Spring XML application context

Citrus starts a Spring application context and adds some default Spring bean components. By default Citrus will load some internal Spring Java config classes defining those bean components. At some point you might add some custom beans to that basic application context. This is why Citrus will search for custom Spring application context files in your project. These are automatically loaded.

By default Citrus looks for custom XML Spring application context files in this location: **classpath*:citrus-context.xml** . So you can add a file named **citrus-context.xml** to your project classpath and Citrus will load all Spring beans automatically.

The location of this file can be customized by setting a System property **citrus.spring.application.context** . So you can customize the XML Spring application context file location. The System property is settable with Maven surefire and failsafe plugin for instance or via Java before the Citrus framework gets loaded.

See the following sample XML configuration which is a normal Spring bean XML configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:citrus="http://www.citrusframework.org/schema/config"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.citrusframework.org/schema/config http://www.citrusframework.org/schema/config/citrus-config.xsd
    http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

  <citrus:schema-repository id="schemaRepository" />

</beans>
```

Now you can add some Spring beans and you can use the Citrus XML components such as **schema-repository** for adding custom beans and components to your Citrus project. Citrus provides several namespaces for custom Spring XML components. These are described in more detail in the respective chapters and sections in this reference guide.

**Tip**
You can also use import statements in this Spring application context in order to load other configuration files. So you are free to modularize your configuration in several files that get loaded by Citrus.

### Citrus Spring Java config

Using XML Spring application context configuration is the default behavior of Citrus. However some people might prefer pure Java code configuration. You can do that by adding a System property **citrus.spring.java.config** with a custom Spring Java config class as value.

```java
System.setProperty("citrus.spring.java.config", MyCustomConfig.class.getName())
```

Citrus will load the Spring bean configurations in **MyCustomConfig.class** as Java config then. See the following example for custom Spring Java configuration:

```java
import com.consol.citrus.TestCase;
import com.consol.citrus.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyCustomConfig {

    @Bean(name = "customTestListener")
    public TestListener customTestListener() {
        return new PlusMinusTestReporter();
    }

    private static class PlusMinusTestReporter extends AbstractTestListener implements TestReporter {

        /** Logger */
        private Logger log = LoggerFactory.getLogger(CustomBeanConfig.class);

        private StringBuilder testReport = new StringBuilder();

        @Override
        public void onTestSuccess(TestCase test) {
            testReport.append("+");
        }

        @Override
        public void onTestFailure(TestCase test, Throwable cause) {
            testReport.append("-");
        }

        @Override
        public void generateTestResults() {
            log.info(testReport.toString());
        }

        @Override
        public void clearTestResults() {
            testReport = new StringBuilder();
        }
    }
}
```

You can also mix XML and Java configuration so Citrus will load both configuration to the Spring bean application context on startup.

### Citrus application properties

The Citrus framework references some basic System properties that can be overwritten. The properties are loaded from Java System and are also settable via property file. Just add a property file named **citrus-application.properties** to your project classpath. This property file contains customized settings such as:

```xml
citrus.spring.application.context=classpath*:citrus-custom-context.xml
citrus.spring.java.config=com.consol.citrus.config.MyCustomConfig
citrus.file.encoding=UTF-8
citrus.xml.file.name.pattern=/**/*Test.xml,/**/*IT.xml
```

Citrus loads these application properties at startup. All properties are also settable with Java System properties. The location of the **citrus-application.properties** is customizable with the System property **citrus.application.config** .

```java
System.setProperty("citrus.application.config", "custom/path/to/citrus-application.properties")
```

At the moment you can use these properties for customization:

* citrus.spring.application.context: File location for Spring XML configurations
* citrus.spring.java.config: Class name for Spring Java config
* citrus.file.encoding: Default file encoding used in Citrus when reading and writing file content
* citrus.xml.file.name.pattern: File name patterns used for XML test file package scan


