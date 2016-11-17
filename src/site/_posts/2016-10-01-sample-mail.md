---
layout: sample
title: Mail sample
sample: sample-mail
description: Shows mail server activities in Citrus
categories: [samples]
permalink: /samples/mail/
---

This sample demonstrates the usage of mail server activities in Citrus tests. You can also read about this in [reference guide](http://www.citrusframework.org/reference/html/mail.html).

Objectives
---------

The [todo-list](/samples/todo-app/) sample application sends out mail reports to users on demand.
Citrus is able to trigger the report via Http REST API. In this sample we send out some Http REST calls and
wait for the incoming mail in a single test.

First we need the mail server component in Citrus. Lets add this to the configuration:

{% highlight xml %}
<citrus-mail:server id="mailServer"
            port="2222"
            auto-accept="true"
            auto-start="true"/>
{% endhighlight %}
                
The mail server component needs a special XML namespace in the configuration root element.

{% highlight xml %}
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:citrus-mail="http://www.citrusframework.org/schema/mail/config"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.citrusframework.org/schema/mail/config http://www.citrusframework.org/schema/mail/config/citrus-mail-config.xsd">            
{% endhighlight %}
    
Now we can receive the mail in the test case.
 
{% highlight java %}
receive(mailServer)
    .payload(new ClassPathResource("templates/mail.xml"))
    .header(CitrusMailMessageHeaders.MAIL_SUBJECT, "ToDo report");

send(mailServer)
    .payload(new ClassPathResource("templates/mail-response.xml"));            
{% endhighlight %}
        
The mail content is loaded from external file resource. Here is the mail content that we expect to arrive in the test.

{% highlight xml %}
<mail-message xmlns="http://www.citrusframework.org/schema/mail/message">
  <from>todo-report@example.org</from>
  <to>users@example.org</to>
  <cc></cc>
  <bcc></bcc>
  <subject>ToDo report</subject>
  <body>
    <contentType>text/plain; charset=us-ascii</contentType>
    <content>There are '${entryCount}' todo entries!</content>
  </body>
</mail-message>
{% endhighlight %}
        
Citrus is able to convert mail messages to an internal XML representation. This way the content is more comfortable to
compare in validation. The mail response looks like this.

{% highlight xml %}
<mail-response xmlns="http://www.citrusframework.org/schema/mail/message">
  <code>250</code>
  <message>OK</message>
</mail-response>
{% endhighlight %}
    
In the sample the success code **250** is returned to the mail client marking that everything is ok. Here we also could place
some other code and message in order to simulate mail server problems.    
                
Run
---------

You can run the sample on your localhost in order to see Citrus in action. Read the instructions [how to run](/samples/run/) the sample.