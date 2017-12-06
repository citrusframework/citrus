---
layout: sample
title: SOAP SSL sample
sample: sample-soap-ssl
description: Shows SOAP secure web service support
categories: [samples]
permalink: /samples/soap-ssl/
---

This sample uses SOAP web services in combination with SSL secure connectivity on both client and server. You can read more about the 
Citrus SOAP features in [reference guide](http://www.citrusframework.org/reference/html/#soap)

Objectives
---------

In this sample project we want to configure both SOAP WebService client and server to use secure connections with SSL. First we need a 
keystore that holds the supported certificates. The sample uses the keystore in **src/test/resources/keys/citrus.jks**

We need a special Soap client configuration:

{% highlight java %}
@Bean
public WebServiceClient todoClient() {
    return CitrusEndpoints.soap()
                        .client()
                        .defaultUri(String.format("https://localhost:%s/services/ws/todolist", securePort))
                        .messageSender(sslRequestMessageSender())
                        .build();
}
{% endhighlight %}
    
The client component references a special request message sender and uses the transport scheme **https** on port **8443**. The SSL request message sender is defined in a
Java Spring configuration class simply because it is way more comfortable to do this in Java than in XML.
    
{% highlight java %}
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
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                .build();
    } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
        throw new BeanCreationException("Failed to create http client for ssl connection", e);
    }
}

@Bean
public HttpComponentsMessageSender sslRequestMessageSender() {
    return new HttpComponentsMessageSender(httpClient());
}
{% endhighlight %}
        
**Note**
We have to add the **HttpComponentsMessageSender.RemoveSoapHeadersInterceptor()** as interceptor to the http client. This prevents that content length headers get set several times which
is not allowed.

As you can see we load the keystore file **keys/citrus.jks** in order to setup the http client ssl context. In the Citrus test case you can use the client component as usual for 
sending messages to the server.

{% highlight java %}
soap()
    .client(todoClient)
    .send()
    .soapAction("addTodoEntry")
    .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"));

soap()
    .client(todoClient)
    .receive()
    .payload(new ClassPathResource("templates/addTodoEntryResponse.xml"));    
{% endhighlight %}
        
On the server side the configuration looks like follows:
        
{% highlight java %}
@Bean
public WebServiceServer todoSslServer() {
    return CitrusEndpoints.soap()
            .server()
            .connector(sslConnector())
            .timeout(5000)
            .autoStart(true)
            .build();
}

@Bean
public ServerConnector sslConnector() {
    ServerConnector connector = new ServerConnector(new Server(),
            new SslConnectionFactory(sslContextFactory(), "http/1.1"),
            new HttpConnectionFactory(httpConfiguration()));
    connector.setPort(securePort);
    return connector;
}

private HttpConfiguration httpConfiguration() {
    HttpConfiguration parent = new HttpConfiguration();
    parent.setSecureScheme("https");
    parent.setSecurePort(securePort);
    HttpConfiguration configuration = new HttpConfiguration(parent);
    configuration.setCustomizers(Collections.singletonList(new SecureRequestCustomizer()));
    return configuration;
}

private SslContextFactory sslContextFactory() {
    SslContextFactory contextFactory = new SslContextFactory();
    contextFactory.setKeyStorePath(sslKeyStorePath);
    contextFactory.setKeyStorePassword("secret");
    return contextFactory;
}        
{% endhighlight %}

That is a lot of Spring bean configuration, but it works! The server component references a special **sslConnector** bean
that defines the certificates and on the secure port **8443**. Client now have to use the certificate in order to connect.
       
In the test case we can receive the requests and provide proper response messages as usual.

{% highlight java %}
soap()
    .server(todoServer)
    .receive()
    .payload(new ClassPathResource("templates/addTodoEntryRequest.xml"));

soap()
    .server(todoServer)
    .send()
    .payload(new ClassPathResource("templates/addTodoEntryResponse.xml"));    
{% endhighlight %}
                
Run
---------

The sample application uses Maven as build tool. So you can compile, package and test the
sample with Maven.
 
     mvn clean install
    
This executes the complete Maven build lifecycle. During the build you will see Citrus performing some integration tests.

Execute all Citrus tests by calling

     mvn integration-test

You can also pick a single test by calling

     mvn integration-test -Ptest=TodoListIT

You should see Citrus performing several tests with lots of debugging output. 
And of course green tests at the very end of the build.

Of course you can also start the Citrus tests from your favorite IDE.
Just start the Citrus test using the TestNG IDE integration in IntelliJ, Eclipse or Netbeans.