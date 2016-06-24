---
layout: post
title: Validate Excel files in Citrus
short-title: Validate Excel files
author: Christoph Deppisch
github: christophd
categories: [blog]
---

Lately I had to deal with Excel files as REST Http service response. I came up with a pretty clever validation mechanism in Citrus that I would like to share with you. You can apply the Excel validator to your Citrus project, too. It is not very complicated as you will see in this post.

First of all lets have a closer look at the response message we get from REST Http service.

{% highlight xml %}
HTTP/1.1 200 OK
Content-Language: de-DE
Content-Type: application/vnd.ms-excel;charset=windows-1252
Pragma: private
Cache-Control: private, must-revalidate
Content-Disposition: attachment; filename="ReportData.xls"
Transfer-Encoding: chunked
Server: Jetty(7.2.2.v20101205)

-- binary content --
{% endhighlight %}

The response message states a Content-Type header set to _application/vnd.ms-excel_ which indicates the Excel content to the browser. Furthermore the Content-Disposition header informs the browser that he should open the save as dialog to the user rather than displaying the content. Finally the Excel file is added as binary content with charset _windows-1252_. 

So now we would like to receive this message in our Citrus test also being able to validate the Excel file content as well as the important header entries. This is how we can do it:

First of all we introduce a custom message validator which handles the binary Excel message content. We intend to create an Excel workbook object that we can pass into the Citrus test. The tester is then able to write Groovy validation code accessing the Excel workbook object. Heres the code for the custom Excel message validator:

{% highlight java %}
public class ExcelMessageValidator extends GroovyScriptMessageValidator {

    /**
     * Default constructor using a customized groovy script template 
     * with POI workbook support.
     */
    public ExcelMessageValidator() {
        super(new ClassPathResource("de/buk/aso/test/validation/excel-script-validation.groovy"));
    }
    
    @Override
    public boolean supportsMessageType(String messageType) {
        return messageType.equalsIgnoreCase("ms-excel");
    }

}
{% endhighlight %}

The message validator is quite simple isn't it. We extend GroovyScriptMessageValidator as we would like to write groovy validation code inside the test. The validator uses a custom script template (_excel-script-validation.groovy_) and supports message type _ms-excel_. Keep that in mind as we will use this later in our test case. For now we add this message validator to the Citrus Spring application context (_citrus-context.xml_).

{% highlight xml %}
<bean id="excelMessageValidator" class="com.consol.citrus.validation.ExcelMessageValidator"/>
{% endhighlight %}

Not much has happened though in this validator's Java code. The magic is done inside the Groovy script template _excel-script-validation.groovy_ that we have added to the classpath in our project. Lets have a look at this file:

{% highlight java %}
import java.io.ByteArrayInputStream
import java.io.IOException
import com.consol.citrus.*
import com.consol.citrus.message.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.exceptions.CitrusRuntimeException
import com.consol.citrus.validation.script.GroovyScriptMessageValidator.ValidationScriptExecutor
import org.apache.poi.hssf.usermodel.HSSFWorkbook

public class ExcelValidationScript implements ValidationScriptExecutor {
    public void validate(Message receivedMessage, TestContext context) {
        Map<String, Object> headers = receivedMessage.copyHeaders()
        String payload = receivedMessage.getPayload(String.class)
        
        HSSFWorkbook workbook;

        try {
            workbook = new HSSFWorkbook(new ByteArrayInputStream(payload.getBytes()))
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to create excel workbook", e)
        }
        
        @SCRIPTBODY@
    }
}
{% endhighlight %}

Ah, now we are getting more precisely! We introduce a new HSSFWorkbook (see also [http://poi.apache.org/](http://poi.apache.org/)) with the message payload that we have just received. Please do not mind the Java-like Groovy programming style - I want to keep things easy for Java programmers in this post. However we now have the workbook object ready for validation code in our test which automatically comes in where _@SCRIPTBODY@_ placeholder is located. So before we continue to look at the test example we shortly add apache poi jar dependency to our project Maven pom.

{% highlight xml %}
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>3.7</version>
</dependency>
{% endhighlight %}

Finally let's write the Citrus test with custom Excel file validation code in Groovy:

{% highlight xml %}
<testcase name="ExcelValidationITest">
  <actions>
     <send endpoint="httpRestEndpoint">
        <message><data></data></message>
           <header>
              <element name="citrus_endpoint_uri" 
                  value="http://localhost:8080/rest-api/report/excel"/>
              <element name="citrus_http_method" value="GET"/>
              <element name="Content-Type" value="text/html"/>
              <element name="Accept" value="application/vnd.ms-excel"/>
          </header>
      </send>
            
      <receive endpoint="httpRestEndpoint">
          <message type="ms-excel">
            <validate>
              <script type="groovy">
               // Header validation
               assert headers["citrus_http_status_code"].toString() == '200'
               assert headers["citrus_http_reason_phrase"] == 'OK'
               assert headers["Content-Type"].toString() == 
                    'application/vnd.ms-excel;charset=windows-1252'
               assert headers["Content-Disposition"] == 
                    'attachment; filename="ReportData.xls"'

               // Excel workbook validation
               assert workbook.getSheetAt(0).getSheetName() == "Report Data"
             </script>
           </validate>
         </message>
     </receive>
  </actions>
</testcase>
{% endhighlight %}

We can access the Excel workbook object like a charm. Also the important message headers are checked for expected values. Do not forget to define the message type _ms-excel_ in the receive action. This ensures that the validation mechanism in Citrus uses our custom Excel message validator for preparing the apache poi workbook object in advance.

That's it! We are now able to validate Excel files in Citrus! The same thing can easily be done with other MS office formats (doc), too. If I have time the next days I will zip that project code and add it as download for you. Have fun with it!