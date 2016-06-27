---
layout: post
title: Arquillian & Citrus in combination - Part 2
short-title: Arquillian & Citrus II
author: Christoph Deppisch
github: christophd
categories: [blog]
---

Some time has passed since [part one](/news/2015/06/29/arquillian-extension-part-1) of this blog post series and we have made some improvements on the Citrus Arquillian extension.
So we can look forward to this post as we move on with a more complex test scenario where we include some Citrus mail server within our test. In part one we have already combined both frameworks <a href="http://arquillian.org/" title="Arquillian" target="_blank">Arquillian</a>
and <a href="http://www.citrusframework.org" title="Citrus framework" target="_blank">Citrus</a> with a basic example.

Let me recap first what we have so far. Citrus is part of the Arquillian test execution and we are able to call our employee REST JEE service which is deployed in an embedded Wildfly application server.
Arquillian takes care on the test deployment and provides resources such as the endpoint URL to call. In our first test we called the employee service adding new employees and getting the complete list of all employees via REST.

Now we want to extend the employee service so that each new employee gets a welcome email message. Therefore we extend the employee JEE EJB with a mail session bean that is able to send email messages.

{% highlight java %}
@Singleton
public class EmployeeRepository {

   private final Employees employees;

   @EJB
   private MailSessionBean mailSessionBean;

   public EmployeeRepository() {
       employees = new Employees();
   }

   public void addEmployee(Employee e) {
       employees.getEmployees().add(e);

       if (e.getEmail() != null && e.getEmail().length() > 0) {
         mailSessionBean.sendMail(e.getEmail(), "Welcome new employee",
           String.format("We welcome you '%s' to our company - now get to work!", e.getName()));
       }
   }

   public Employees getEmployees() {
       return employees;
   }
}
{% endhighlight %}

The employee service calls a mail session bean implementation when the email field is set on the new employee. The mail message is sent to the employee mail address as recipient and is a basic text message with subject and text body part.
Lets have a closer look at the mail session bean implementation that uses Java mail API for creating sending out the mail mime message.

{% highlight java %}
@Singleton
public class MailSessionBean {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(MailSessionBean.class);

    private int port = 2222;
    private String host = "localhost";
    private String from = "employee-registry@example.com";
    private String username = "employee-registry@example.com";
    private String password = "secretpw";

    public void sendMail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", true);

        Authenticator authenticator = new Authenticator() {
            private PasswordAuthentication pa = new PasswordAuthentication(username, password);
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return pa;
            }
        };

        Session session = Session.getInstance(props, authenticator);
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(subject);
            message.setSentDate(new Date());
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send welcome mail!", e);
        }
    }
}
{% endhighlight %}

Now in our test scenario we need a valid SMTP mail server on host __localhost__ and port __2222__. Fortunately Citrus is able to provide such a mail server so we add the __citrus-mail__ Maven module to our test project.

{% highlight xml %}
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-mail</artifactId>
  <version>2.3</version>
</dependency>
{% endhighlight %}

Now we are ready to add the mail server as a Citrus component. Citrus is working with Spring so we need to add the mail server to the Spring application context. We add a new property to the Arquillian descriptor so the Citrus extension will
load our new configuration:

{% highlight xml %}
<extension qualifier="citrus">
  <property name="citrusVersion">2.3</property>
  <property name="autoPackage">true</property>
  <property name="suiteName">citrus-arquillian-suite</property>
  <property name="configurationClass">com.consol.citrus.samples.javaee.config.CitrusConfig</property>
</extension>
{% endhighlight %}

The configuration class is a Spring Java configuration class in our project that adds the new mail server as Spring bean to the application context.

{% highlight java %}
@Configuration
public class CitrusConfig extends CitrusBaseConfig {

    private MailServer mailServer;

    @Bean
    public MailServer mailServer() {
        if (mailServer == null) {
            mailServer = new MailServer();
            mailServer.setPort(2222);
            mailServer.setAutoStart(true);
        }

        return mailServer;
    }
}
{% endhighlight %}

The configuration class must extend the Citrus base configuration class. This basic class adds all needed Spring beans for Citrus such as message validators, functions, validation matchers and so on. Important is our new mail server that uses the port
__2222__ and automatically starts with the Citrus application context. This is everything we need to do in order to add the mail server component to the Citrus runtime. We can add other Citrus endpoint components such as JMS endpoints, SOAP WebService servers
and Camel endpoints here the same way. For now we are finished with the configuration and we can reference the mail server in our test case.

{% highlight java %}
@RunWith(Arquillian.class)
@RunAsClient
public class EmployeeMailTest {

    @CitrusFramework
    private Citrus citrusFramework;

    @ArquillianResource
    private URL baseUri;

    private String serviceUri;

    @CitrusEndpoint
    private MailServer mailServer;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(
                RegistryApplication.class, MailSessionBean.class, EmployeeResource.class,
                Employees.class, Employee.class, EmployeeRepository.class);
    }

    @Before
    public void setUp() throws Exception {
        serviceUri = new URL(baseUri, "registry/employee").toExternalForm();
    }

    @Test
    @CitrusTest
    public void testPostWithWelcomeEmail(@CitrusResource TestDesigner citrus) {
        citrus.send(serviceUri)
            .fork(true)
            .message(new HttpMessage("name=Rajesh&age=20&email=rajesh@example.com")
                    .method(HttpMethod.POST)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        citrus.receive(mailServer)
            .payload("<mail-message xmlns=\"http://www.citrusframework.org/schema/mail/message\">" +
              "<from>employee-registry@example.com</from>" +
              "<to>rajesh@example.com</to>" +
              "<cc></cc>" +
              "<bcc></bcc>" +
              "<subject>Welcome new employee</subject>" +
              "<body>" +
                "<contentType>text/plain; charset=us-ascii</contentType>" +
                "<content>We welcome you 'Rajesh' to our company - now get to work!</content>" +
              "</body>" +
            "</mail-message>")
            .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "Welcome new employee")
            .header(CitrusMailMessageHeaders.MAIL_FROM, "employee-registry@example.com")
            .header(CitrusMailMessageHeaders.MAIL_TO, "rajesh@example.com");

        citrus.receive(serviceUri)
            .message(new HttpMessage()
                .statusCode(HttpStatus.NO_CONTENT));

        citrus.send(serviceUri)
            .fork(true)
            .message(new HttpMessage()
                .method(HttpMethod.GET)
                .accept(MediaType.APPLICATION_XML));

        citrus.receive(serviceUri)
            .message(new HttpMessage("<employees>" +
                    "<employee>" +
                        "<age>20</age>" +
                        "<name>Rajesh</name>" +
                        "<email>rajesh@example.com</email>" +
                    "</employee>" +
                "</employees>")
                .statusCode(HttpStatus.OK));

        citrusFramework.run(citrus.getTestCase());
    }

}
{% endhighlight %}

The Arquillian test case is basically the same as before in our first part example. The test now uses a __@CitrusEndpoint__ annotated mail server component. The Arquillian Citrus extension will automatically inject the
mail server Spring bean that we have added to the Citrus configuration before. In case multiple components of the same type are available in the configuration you can use the __@CitrusEndpoint__ annotation with a Spring bean name
like __@CitrusEndpoint(name="myMailServer")__. We receive the mail message right after the test has called the service interface with the new employee __Rajesh__ and a valid email field __rajesh@example.com__. The mail server Citrus component
is ready to be used in a __receive__ test action. Citrus waits for the mail message to arrive and performs message validation with an expected mail message. Citrus automatically converts the mail mime message to a XML message representation.
We can expect the mail message content much better using the XML syntax as we can use the powerful XML validation with XPath, ignore and validation matcher support.

In addition to that Citrus adds some special header values for explicit validation in the __receive__ action. That completes the mail server test case. Citrus automatically start a SMTP server that receives the mail message. The mail is not sent to
a real recipient we just want to validate the mail message content in our test. The mail server did automatically accept the authentication in default mode. We could also switch to advanced mode where we can also validate the authentication steps. For now we keep
it simple and are happy to receive the mail message. Please note that we can mix the receive actions of different interfaces in Citrus very easy. Citrus acts both as client and server on our REST and mail interfaces in the test.

This should close the book for this post. You can review the example code on <a href="https://github.com/christophd/citrus-samples/tree/master/javaee" title="Sample code" target="_blank">christophd@github</a>. In a next post I will show you how we can execute
the test as part of the test deployment on the application server. Then we have direct access to container managed resources such as JMS connection factories and queues. Once again, stay tuned!