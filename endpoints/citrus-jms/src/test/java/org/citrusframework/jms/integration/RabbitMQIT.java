package org.citrusframework.jms.integration;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import jakarta.jms.ConnectionFactory;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.jms.endpoint.JmsEndpoint;
import org.citrusframework.jms.endpoint.JmsEndpoints;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testng.annotations.Test;

@Test
@Import({RabbitMQIT.JmsConfiguration.class})
public class RabbitMQIT extends TestNGCitrusSpringSupport {

    @CitrusEndpoint
    private JmsEndpoint personsQueueCreate;

    @Autowired
    private CitrusSpringConfig citrusSpringConfig;

    @CitrusTest
    public void testPost(@CitrusResource TestCaseRunner test) {
        test.variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        test.variable("todoDescription", "Description: ${todoName}");

        // Use the endpoint configured without a specific destination, set headers in the send action
        test.$(SendMessageAction.Builder.send()
                        .endpoint(personsQueueCreate)
//                .fork(false)
                        .message()
//                        .header(MP.MIDSA_PARAM_PERSON_ID.s(), "46b85fef-02a0-4f3f-bdf1-aaaa")
//                        .header(MP.MIDSA_PARAM_BIOMETRIC_INCLUDED.s(), "false")
//                        .header(MP.MIDSA_PARAM_MAX_RECENT_MOVEMENTS.s(), 1)
                        .body("{ \"title\": \"${todoName}\", \"description\": \"${todoDescription}\" }")
        );

        test.$(ReceiveMessageAction.Builder.receive()
                .endpoint(personsQueueCreate)
                .message()
                .body("\"Message received\""));
    }

    @TestConfiguration
    static class JmsConfiguration {

        @Bean
        public ConnectionFactory connectionFactory() {
            RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
            connectionFactory.setUsername("xxxxx");
            connectionFactory.setPassword("xxxxx");
            connectionFactory.setVirtualHost("xxxxxx");
            connectionFactory.setHost("localhost");
            connectionFactory.setPort(5672); // Replace with appropriate port
            return connectionFactory;
        }


        @Bean
        public RMQDestination jmsDestination() {
            RMQDestination jmsDestination = new RMQDestination();
            jmsDestination.setAmqp(true);
            return jmsDestination;
        }


        @Bean
        public JmsEndpoint personsQueueCreate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory,
                                              RMQDestination jmsDestination) {
            jmsDestination.setDestinationName("midsa-service-border-guard.persons.get");
            jmsDestination.setAmqpExchangeName("midsa.persons.get");
            jmsDestination.setAmqpRoutingKey("");
            jmsDestination.setAmqpQueueName("midsa-service-border-guard.persons.get");

            // Equivalent to:
            //  return JmsEndpointCatalog.jms()
            //             .synchronous()

            return JmsEndpoints.jms().synchronous()
                    .connectionFactory(connectionFactory)
                    .destination(jmsDestination)
                    .build();
        }
    }
}
