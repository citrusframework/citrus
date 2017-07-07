---
layout: sample
title: Https sample
sample: sample-https
description: Shows how to use SSL connectivity as a client and server
categories: [samples]
permalink: /samples/https/
---

This sample demonstrates the usage of secure Http connections with client and server certificates. Http support is described in detail in [reference guide](http://www.citrusframework.org/reference/html/#http-rest)

Objectives
---------

In this sample project we want to configure both Http client and server to use secure connections with SSL. First we need a keystore that holds the
supported certificates. The sample uses the keystore in **src/test/resources/keys/citrus.jks**

We need a special Http client configuration:

{% highlight xml %}
<bean class="com.consol.citrus.samples.todolist.config.HttpClientSslConfig"/>

<citrus-http:client id="todoClient"
                    request-factory="sslRequestFactory"
                    request-url="https://localhost:8443"/>
{% endhighlight %}
    
The client component references a special request factory and uses the transport scheme **https** on port **8443**. The SSL request factory is defined in a
Java Spring configuration class simply because it is way more comfortable to do this in Java than in XML.

{% highlight java %}
@Configuration
public class HttpClientSslConfig {

    @Bean
    public HttpClient httpClient() {
        try {
            SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(new ClassPathResource("keys/citrus.jks").getFile(), "secret".toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslcontext, NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new BeanCreationException("Failed to create http client for ssl connection", e);
        }
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory sslRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }
}
{% endhighlight %}
        
As you can see we load the keystore file **keys/citrus.jks** in order to setup the http client ssl context. In the Citrus test case you can use the client component as usual for 
sending messages to the server.

{% highlight java %}
http()
    .client(todoClient)
    .send()
    .get("/todo")
    .accept("application/xml");
    
http()
    .client(todoClient)
    .receive()
    .response(HttpStatus.OK);    
{% endhighlight %}
        
On the server side the configuration looks like follows:

{% highlight xml %}
<citrus-http:server id="todoSslServer"
                    connector="sslConnector"
                    endpoint-adapter="staticResponseAdapter"
                    auto-start="true"/>

<bean id="sslConnector" class="org.eclipse.jetty.server.ServerConnector">
  <constructor-arg>
    <bean class="org.eclipse.jetty.server.Server"></bean>
  </constructor-arg>
  <constructor-arg>
    <list>
      <bean class="org.eclipse.jetty.server.SslConnectionFactory">
        <constructor-arg>
          <bean class="org.eclipse.jetty.util.ssl.SslContextFactory">
            <property name="keyStorePath" value="${project.basedir}/src/test/resources/keys/citrus.jks"/>
            <property name="keyStorePassword" value="secret"/>
          </bean>
        </constructor-arg>
        <constructor-arg value="http/1.1"/>
      </bean>
      <bean class="org.eclipse.jetty.server.HttpConnectionFactory">
        <constructor-arg>
          <bean class="org.eclipse.jetty.server.HttpConfiguration">
            <constructor-arg>
              <bean class="org.eclipse.jetty.server.HttpConfiguration">
                <property name="secureScheme" value="https"/>
                <property name="securePort" value="8443"/>
              </bean>
            </constructor-arg>
            <property name="customizers">
              <list>
                <bean class="org.eclipse.jetty.server.SecureRequestCustomizer"/>
              </list>
            </property>
          </bean>
        </constructor-arg>
      </bean>
    </list>
  </constructor-arg>
  <property name="port" value="8443" />
</bean>        
{% endhighlight %}
        
That is a lot of Spring bean configuration, but it works! The server component references a special **sslConnector** bean
that defines the certificates and on the secure port **8443**. Client now have to use the certificate in order to connect.
       
The server component has a static endpoint adapter always sending back a Http 200 Ok response when clients connect.

{% highlight xml %}
<citrus:static-response-adapter id="staticResponseAdapter">
  <citrus:payload>
    <![CDATA[
    <todo xmlns="http://citrusframework.org/samples/todolist">
      <id>100</id>
      <title>todoName</title>
      <description>todoDescription</description>
    </todo>
    ]]>
  </citrus:payload>
</citrus:static-response-adapter>    
{% endhighlight %}
                
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
    mvn clean install -Dembedded=true
    
This executes the complete Maven build lifecycle.

During the build you will see Citrus performing some integration tests.

Citrus test
---------

Execute all Citrus tests by calling

    mvn integration-test

You can also pick a single test by calling

    mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output in your terminal. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.