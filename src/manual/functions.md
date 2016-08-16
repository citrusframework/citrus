## Functions

The test framework will offer several functions that are useful throughout the test execution. The functions will always return a string value that is ready for use as variable value or directly inside a text message.

A set of functions is usually combined to a function library. The library has a prefix that will identify the functions inside the test case. The default test framework function library uses a default prefix (citrus). You can write your own function library using your own prefix in order to extend the test framework functionality whenever you want.

The library is built in the Spring configuration and contains a set of functions that are of public use.

```xml
<citrus:function-library id="testLibrary" prefix="foo:">
          <citrus:function name="randomNumber"> class="com.consol.citrus.functions.RandomNumberFunction"/>
          <citrus:function name="randomString"> class="com.consol.citrus.functions.RandomStringFunction"/>
          <citrus:function name="customFunction"> ref="customFunctionBean"/>
          ...
</citrus:function-library>
```

As you can see the library defines one to many functions either referenced as normal Spring bean or by its implementing Java class name. Citrus constructs the library and you are able to use the functions in your test case with the leading library prefix just like this:

```xml
foo:randomNumber()
      foo:randomString()
      foo:customFunction()
```

**Tip**
You can add custom function implementations and custom function libraries. Just use a custom prefix for your library. The default Citrus function library uses the **citrus:** prefix.In the next chapters the default functions offered by the framework will be described in detail.

### citrus:concat()

The function will combine several string tokens to a single string value. This means that you can combine a static text value with a variable value for instance. A first example should clarify the usage:

```xml
<testcase name="concatFunctionTest">
    <variables>
        <variable name="date" value="citrus:currentDate(yyyy-MM-dd)" />
        <variable name="text" value="Hello Test Framework!" />
    </variables>
    <actions>
        <echo>
            <message>
                citrus:concat('Today is: ', ${date}, ' right!?')
            </message>
        </echo>
        <echo>
            <message>
                citrus:concat('Text is: ', ${text})
            </message>
        </echo>
    </actions>
</testcase>
```

Please do not forget to mark static text with single quote signs. There is no limitation for string tokens to be combined.

```
citrus:concat('Text1', 'Text2', 'Text3', ${text}, 'Text5', …, 'TextN') 
```

The function can be used wherever variables can be used. For instance when validating XML elements in the receive action.

```xml
<message>
    <validate path="//element/element" value="citrus:concat('Cx1x', ${generatedId})"/>
</message>
```

### citrus:substring()

The function will have three parameters.

1. String to work on
2. Starting index
3. End index (optional)

Let us have a look at a simple example for this function:

```xml
<echo>
    <message>
        citrus:substring('Hello Test Framework', 6)
    </message>
</echo>
<echo>
    <message>
        citrus:substring('Hello Test Framework', 0, 5)
    </message>
</echo>
```

Function output:

```
Test Framework 
Hello 
```

### citrus:stringLength()

The function will calculate the number of characters in a string representation and return the number.

```xml
<echo>
    <message>citrus:stringLength('Hello Test Framework')</message>
</echo>
```

Function output:

**20** 

### citrus:translate()

This function will replace regular expression matching values inside a string representation with a specified replacement string.

```xml
<echo>
    <message>
        citrus:translate('H.llo Test Fr.mework', '\.', 'a')
    </message>
</echo>
```

Note that the second parameter will be a regular expression. The third parameter will be a simple replacement string value.

Function output:

**Hello Test Framework** 

### citrus:substringBefore()

The function will search for the first occurrence of a specified string and will return the substring before that occurrence. Let us have a closer look in a simple example:

```xml
<echo>
    <message>
        citrus:substringBefore('Test/Framework', '/')
    </message>
</echo>
```

In the specific example the function will search for the ‘/’ character and return the string before that index.

Function output:

**Test** 

### citrus:substringAfter()

The function will search for the first occurrence of a specified string and will return the substring after that occurrence. Let us clarify this with a simple example:

```xml
<echo>
    <message>
        citrus:substringAfter('Test/Framework', '/')
    </message>
</echo>
```

Similar to the substringBefore function the ‘/’ character is found in the string. But now the remaining string is returned by the function meaning the substring after this character index.

Function output:

**Framework** 

### citrus:round()

This is a simple mathematic function that will round decimal numbers representations to their nearest non decimal number.

```xml
<echo>
    <message>citrus:round('3.14')</message>
</echo>
```

Function output:

**3** 

### citrus:floor()

This function will round down decimal number values.

```xml
<echo>
    <message>citrus:floor('3.14')</message>
</echo>
```

Function output:

**3.0** 

### citrus:ceiling()

Similar to floor function, but now the function will round up the decimal number values.

```xml
<echo>
    <message>citrus:ceiling('3.14')</message>
</echo>
```

Function output:

**4.0** 

### citrus:randomNumber()

The random number function will provide you the opportunity to generate random number strings containing positive number letters. There is a singular Boolean parameter for that function describing whether the generated number should have exactly the amount of digits. Default value for this padding flag will be true.

Next example will show the function usage:

```xml
<variables>
    <variable name="rndNumber1" value="citrus:randomNumber(10)"/>
    <variable name="rndNumber2" value="citrus:randomNumber(10, true)"/>
    <variable name="rndNumber2" value="citrus:randomNumber(10, false)"/>
    <variable name="rndNumber3" value="citrus:randomNumber(3, false)"/>
</variables>
```

Function output:

```
8954638765 
5003485980 
6387650 
65 
```

### citrus:randomString()

This function will generate a random string representation with a defined length. A second parameter for this function will define the case of the generated letters (UPPERCASE, LOWERCASE, MIXED). The last parameter allows also digit characters in the generated string. By default digit charaters are not allowed.

```xml
<variables>
    <variable name="rndString0" value="${citrus:randomString(10)}"/>
    <variable name="rndString1" value="citrus:randomString(10)"/>
    <variable name="rndString2" value="citrus:randomString(10, UPPERCASE)"/>
    <variable name="rndString3" value="citrus:randomString(10, LOWERCASE)"/>
    <variable name="rndString4" value="citrus:randomString(10, MIXED)"/>
    <variable name="rndString4" value="citrus:randomString(10, MIXED, true)"/>
</variables>
```

Function output:

```
HrGHOdfAer 
AgSSwedetG 
JSDFUTTRKU 
dtkhirtsuz 
Vt567JkA32 
```

### citrus:randomEnumValue()

This function returns one of its supplied arguments. Furthermore you can specify a custom function with a configured list of values (the enumeration). The function will randomly return an entry when called without arguments. This promotes code reuse and facilitates refactoring.

In the next sample the function is used to set a httpStatusCode variable to one of the given HTTP status codes (200, 401, 500)

```xml
<variable name="httpStatusCode" value="citrus:randomEnumValue('200', '401', '500')" />
```

As mentioned before you can define a custom function for your very specific needs in order to easily manage a list of predefined values like this:

```xml
<citrus:function-library id="myCustomFunctionLibrary" prefix="custom:">
    <citrus-function name="randomHttpStatusCode" ref="randomHttpStatusCodeFunction"/>
</citrus:function-library>

<bean id="randomHttpStatusCodeFunction" class="com.consol.citrus.functions.core.RandomEnumValueFunction">
  <property name="values">
    <list>
      <value>200</value>
      <value>500</value>
      <value>401</value>
    </list>
  </property>
</bean>
```

We have added a custom function library with a custom function definition. The custom function "randomHttpStatusCode" randomly chooses an HTTP status code each time it is called. Inside the test you can use the function like this:

```xml
<variable name="httpStatusCode" value="custom:randomHttpStatusCode()" />
```

### citrus:currentDate()

This function will definitely help you when accessing the current date. Some examples will show the usage in detail:

```xml
<echo><message>citrus:currentDate()</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd'T'hh:mm:ss')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '+1y')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '+1M')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '+1d')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '+1h')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '+1m')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '+1s')</message></echo>
<echo><message>citrus:currentDate('yyyy-MM-dd HH:mm:ss', '-1y')</message></echo>
```

Note that the currentDate function provides two parameters. First parameter describes the date format string. The second will define a date offset string containing year, month, days, hours, minutes or seconds that will be added or subtracted to or from the actual date value.

Function output:

```
01.09.2009 
2009-09-01 
2009-09-01 12:00:00 
2009-09-01T12:00:00 
```

### citrus:upperCase()

This function converts any string to upper case letters.

```xml
<echo>
    <message>citrus:upperCase('Hello Test Framework')</message>
</echo>
```

Function output:

**HELLO TEST FRAMEWORK** 

### citrus:lowerCase()

This function converts any string to lower case letters.

```xml
<echo>
    <message>citrus:lowerCase('Hello Test Framework')</message>
</echo>
```

Function output:

**hello test framework** 

### citrus:average()

The function will sum up all specified number values and divide the result through the number of values.

```xml
<variable name="avg" value="citrus:average('3', '4', '5')"/>
```

avg = **4.0** 

### citrus:minimum()

This function returns the minimum value in a set of number values.

```xml
<variable name="min" value="citrus:minimum('3', '4', '5')"/>
```

min = **3.0** 

### citrus:maximum()

This function returns the maximum value in a set of number values.

```xml
<variable name="max" value="citrus:maximum('3', '4', '5')"/>
```

max = **5.0** 

### citrus:sum()

The function will sum up all number values. The number values can also be negative.

```xml
<variable name="sum" value="citrus:sum('3', '4', '5')"/>
```

sum = **12.0** 

### citrus:absolute()

The function will return the absolute number value.

```xml
<variable name="abs" value="citrus:absolute('-3')"/>
```

abs = **3.0** 

### citrus:mapValue()

This function implementation maps string keys to string values. This is very helpful when the used key is randomly chosen at runtime and the corresponding value is not defined during the design time.

The following function library defines a custom function for mapping HTTP status codes to the corresponding messages:

```xml
<citrus:function-library id="myCustomFunctionLibrary" prefix="custom:">
      <citrus-function name="getHttpStatusMessage" ref="getHttpStatusMessageFunction"/>
</citrus:function-library>

<bean id="getHttpStatusMessageFunction" class="com.consol.citrus.functions.core.MapValueFunction">
  <property name="values">
    <map>
      <entry key="200" value="OK" />
      <entry key="401" value="Unauthorized" />
      <entry key="500" value="Internal Server Error" />
    </map>
  </property>
</bean>
```

In this example the function sets the variable httpStatusMessage to the 'Internal Server Error' string dynamically at runtime. The test only knows the HTTP status code and does not care about spelling and message locales.

```xml
<variable name="httpStatusCodeMessage" value="custom:getHttpStatusMessage('500')" />
```

### citrus:randomUUID()

The function will generate a random Java UUID.

```xml
<variable name="uuid" value="citrus:randomUUID()"/>
```

uuid = **98fbd7b0-832e-4b85-b9d2-e0113ee88356** 

### citrus:encodeBase64()

The function will encode a string to binary data using base64 hexadecimal encoding.

```xml
<variable name="encoded" value="citrus:encodeBase64('Hallo Testframework')"/>
```

encoded = **VGVzdCBGcmFtZXdvcms=** 

### citrus:decodeBase64()

The function will decode binary data to a character sequence using base64 hexadecimal decoding.

```xml
<variable name="decoded" value="citrus:decodeBase64('VGVzdCBGcmFtZXdvcms=')"/>
```

decoded = **Hallo Testframework** 

### citrus:escapeXml()

If you want to deal with escaped XML in your test case you may want to use this function. It automatically escapes all XML special characters.

```xml
<echo>
    <message>
        <![CDATA[
            citrus:escapeXml('<Message>Hallo Test Framework</Message>')                        
        ]]>
    </message>
</echo>
```

**&lt;Message&gt;Hallo Test Framework&lt;/Message&gt;** 

### citrus:cdataSection()

Usually we use CDATA sections to define message payload data inside a testcase. We might run into problems when the payload itself contains CDATA sections as nested CDATA sections are prohibited by XML nature. In this case the next function ships very usefull.

```xml
<variable name="cdata" value="citrus:cdataSection('payload')"/>
```

cdata = **<![CDATA[payload]]>** 

### citrus:digestAuthHeader()

Digest authentication is a commonly used security algorithm, especially in Http communication and SOAP WebServices. Citrus offers a function to generate a digest authentication principle used in the Http header section of a message.

```xml
<variable name="digest" 
  value="citrus:digestAuthHeader('username', 'password', 'authRealm', 'acegi', 
                            'POST', 'http://127.0.0.1:8080', 'citrus', 'md5')"/>
```

A possible digest authentication header value looks like this:

```xml
<Digest username=foo,realm=arealm,nonce=MTMzNT,
uri=http://127.0.0.1:8080,response=51f98c,opaque=b29a30,algorithm=md5>
```

You can use these digest headers in messages sent by Citrus like this:

```xml
<header>
  <element name="citrus_http_Authorization" 
    value="vflig:digestAuthHeader('${username}','${password}','${authRealm}',
                            '${nonceKey}','POST','${uri}','${opaque}','${algorithm}')"/>
</header>  
        
```

This will set a Http Authorization header with the respective digest in the request message. So your test is ready for client digest authentication.

### citrus:localHostAddress()

Test cases may use the local host address for some reason (e.g. used as authentication principle). As the tests may run on different machines at the same time we can not use static host addresses. The provided function localHostAddress() reads the local host name dynamically at runtime.

```xml
<variable name="address" value="citrus:localHostAddress()"/>
```

A possible value is either the host name as used in DNS entry or an IP address value:

address = **<192.168.2.100>** 

### citrus:changeDate()

This function works with date values and manipulates those at runtime by adding or removing a date value offset. You can manipulate several date fields such as: year, month, day, hour, minute or second.

Let us clarify this with a simple example for this function:

```xml
<echo>
    <message>citrus:changeDate('01.01.2000', '+1y+1M+1d')</message>
</echo>
<echo>
    <message>citrus:changeDate(citrus:currentDate(), '-1M')</message>
</echo>
```

Function output:

```
02.02.2001 
13.04.2013 
```

As you can see the change date function works on static date values or dynamic variable values or functions like **citrus:currentDate()** . By default the change date function requires a date format such as the current date function ('dd.MM.yyyy'). You can also define a custom date format:

```xml
<echo>
    <message>citrus:changeDate('2000-01-10', '-1M-1d', 'yyyy-MM-dd')</message>
</echo>
```

Function output:

```
1999-12-09 
```

With this you are able to manipulate all date values of static or dynamic nature at test runtime.

### citrus:readFile()

The **readFile** function reads a file resource from given file path and loads the complete file content as function result. The file path can be a system file path as well as a classpath file resource. The file path can have test variables as part of the path or file name. In addition to that the file content can also have test variable values and other functions.

Let's see this function in action:

```xml
<echo>
    <message>citrus:readFile('classpath:some/path/to/file.txt')</message>
</echo>
<echo>
    <message>citrus:readFile(${filePath})</message>
</echo>
```

The function reads the file content and places the content at the position where the function has been called. This means that you can also use this function as part of Strings and message payloads for instance. This is a very powerful way to extract large message parts to separate file resources. Just add the **readFile** function somewhere to the message content and Citrus will load the extra file content and place it right into the message payload for you.

