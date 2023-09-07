package org.citrusframework.reporter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class ReporterConfig {

    @Bean(name = "citrusTestReporters")
    public TestReportersFactory testReporters() {
        return new TestReportersFactory();
    }
}
