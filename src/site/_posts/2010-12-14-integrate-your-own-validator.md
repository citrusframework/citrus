---
layout: post
title: Integrate your own validator into your Citrus project
short-title: Integrate your own validator
author: Yvonne Sunke
github:  
categories: [blog]
---

Citrus includes a lot of convenient features which are only waiting for you to discover and use them. The other day I needed to validate a SoapAttachment. 
As you probably already know, a SoapAttachment is referenced by a href property in an Include tag like this: 

{% highlight xml %}
&lt;Include href="..." xmlns="http://www.w3.org/2004/08/xop/include"/&gt; 
{% endhighlight %}

Validation is quite easy when you're still mock testing your application because you have full control over what your mock response will look like.

So just add the following bean to your citrus-context.xml:

{% highlight xml %}
<bean id="soapAttachmentValidator">
  <property name="ignoreAllWhitespaces" value="true"/>
</bean>
{% endhighlight %}

And use it in your test file like so:

{% highlight xml %}
<send endpoint="facadeMessageSender">
  <message>
    <data>
      <![CDATA[...-your request-...]]>
    </data>
  </message>
</send>
<ws:receive endpoint="facadeResponseHandler">
  <message schema-validation="false">
    <data>
      <![CDATA[
        <test:MyTestRes xmlns:mail="http://www.testdomain.com/foo/bar/test">
          <test:MyStructure>
            <Include href="@ignore@" xmlns="http://www.w3.org/2004/08/xop/include"/>
          </test:MyStructure>
        </test:MyTestRes>
      ]]>
    </data>
    <ignore path="//test:MyTestRes/test:MyStructure/*/@href" />
  </message>
  <extract>
    <message variable="contentId" path="///test:MyTestRes/test:MyStructure/*/@href"/>
  </extract>
  <!-- Extract cid: (unfortunately the @ is masked by %40 and has to be replaced too) -->
  <ws:attachment content-id="citrus:concat(citrus:substringBefore(citrus:substringAfter('${contentId}', 'cid:'), '%40www.testdomain.com'), '@www.testdomain.com')" 
                 content-type="TEXT/CALENDAR; 
                 charset=us-ascii" 
                 validator="soapAttachmentValidator">
    <ws:data>
      <![CDATA[...-the expected response-...]]>
    </ws:data>
  </ws:attachment>
</ws:receive>
{% endhighlight %}

Things become a little more complicated as soon as you start to connect to real backend systems for integration testing. Especially when timestamps are involved. In my case the SoapAttachment contains a calendar 
invitation in vCal format which is basically a simple text based key/value format. I have to ignore the value for the key <code>LAST-MODIFIED</code> because it would be impossible to predict this timestamp and 
it's not really relevant either. So what I really need is a way to compare the expected structure with the actual result line by line, a possibility to completely ignore a line and a way to only ignore the value 
but still compare the keys of some lines.

To solve this, the Citrus framework offers the possibility to define your own validators and include them into your test cases. Simply add a Java class which extends AbstractSoapAttachmentValidator to your test project and 
implement the validateAttachmentContent method. This will be your new validator and could look somewhat like the simple example I'll add to the end of this blog entry.

Go back to your citrus-context.xml and add your newly created validator bean:

{% highlight xml %}
<bean id="lineByLineSoapAttachmentValidator" class="com.testproject.itest.validation.LineByLineWithIgnoreSoapAttachmentValidator"/>
{% endhighlight %}

Now you can use the validator in your test files:

{% highlight xml %}
<ws:attachment content-id="citrus:concat(citrus:substringBefore(citrus:substringAfter('${contentId}', 'cid:'), '%40www.testdomain.com'),  '@www.testdomain.com')" content-type="TEXT/CALENDAR; charset=us-ascii" validator="lineByLineSoapAttachmentValidator">
  <ws:data>
    <![CDATA[
BEGIN:VCALENDAR
PRODID:-//Company//Test Project//DE
VERSION:2.0
METHOD:REQUEST
BEGIN:VEVENT
ORGANIZER:mailto:${user2.mailaddress}
ATTENDEE:mailto:${user3.mailaddress}
CATEGORIES:Test Category
DESCRIPTION:Event Description
DTEND:20110101T120000Z
DTSTART:20110101T100000Z
LAST-MODIFIED:@ignore@
LOCATION:A cool location
PRIORITY:1
SUMMARY:The summary of my test event @ignore@
@ignore@
UID:${randomNum}
END:VEVENT
END:VCALENDAR
    ]]>
  </ws:data>
</ws:attachment>
{% endhighlight %}

So here's the promised simple example for your lineByLineSoapAttachmentValidator:

{% highlight java %}
public class LineByLineWithIgnoreSoapAttachmentValidator extends AbstractSoapAttachmentValidator {

  private static final String IGNORE = "@ignore@";
  private static final String LINE_DELIMITERS_R_N = "[\\r\\n]+";
  
  @Override
  protected void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
    try {
      String control = controlAttachment.getContent().trim();
      String received = receivedAttachment.getContent().trim();
      String[] controlLines = control.split(LINE_DELIMITERS_R_N);
      String[] receivedLines = received.split(LINE_DELIMITERS_R_N);
      
      // Check number of properties
      if (controlLines.length != receivedLines.length) {
        throw new CitrusRuntimeException("Number of lines are not equal. Expected: " + controlLines.length + " Received: " + receivedLines.length);
      }
      
      // Now compare the properties
      for (int i = 0; i &lt; controlLines.length; i++) {
        String controlLine = controlLines[i].trim();
        String receivedLine = receivedLines[i].trim();
        
        if (StringUtils.endsWithIgnoreCase(controlLine, IGNORE)) {
          compareBeginningOfLines(controlLine, receivedLine);
          // value shall be ignored, so just continue;
          continue;
        }
        
        if (StringUtils.startsWithIgnoreCase(controlLine, IGNORE)) {
          compareEndingOfLines(controlLine, receivedLine);
          // value shall be ignored, so just continue;
          continue;
        }
        
        if (!controlLine.equalsIgnoreCase(receivedLine)) {
          throw new CitrusRuntimeException("Lines are not equal.\n\nExpected: " + controlLine + "\n\nReceived: " + receivedLine);
        }
      }
    } catch (Exception e) {
      throw new CitrusRuntimeException("Validation failed!", e);
    }
  }
  
  private void compareBeginningOfLines(String controlLine, String receivedLine) {
    // check the beginning of the line
    String begin = controlLine.substring(0, controlLine.length() - 8);
    
    if (StringUtils.hasText(begin) &amp;&amp; !StringUtils.startsWithIgnoreCase(receivedLine, begin)) {
      throw new CitrusRuntimeException("Beginning of lines not equal.\n\nExpected: " + controlLine + "\n\nReceived: " + receivedLine);
    }
  }
  
  private void compareEndingOfLines(String controlLine, String receivedLine) {
    // check the beginning of the line
    String end = controlLine.substring(8);
    
    if (StringUtils.hasText(end) &amp;&amp; !StringUtils.endsWithIgnoreCase(receivedLine, end)) {
      throw new CitrusRuntimeException("Ending of lines not equal.\n\nExpected: " + controlLine + "\n\nReceived: " + receivedLine);
    }
  }
}
{% endhighlight %}

Have fun using Citrus!

yvonne