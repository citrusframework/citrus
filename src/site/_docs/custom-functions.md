---
layout: docs
title: Custom functions
permalink: /docs/custom-functions/
---

This tutorial tries to explain how to write and include your own functions in Citrus. First of all let us keep in mind 
what functions do. Functions can ease up your life when working with dynamic values and test variables. For instance if 
you need the current date in a test case you may use the Citrus function currentDate() in your test case.

{% highlight xml %}  
<variable name="date" value="citrus:currentDate()"/>
<variable name="dateCustomFormat" value="citrus:currentDate('yyyy-mm-dd')"/>
{% endhighlight %}  

Citrus offers a lot of functions to be used out of the box (function prefix: citrus). See the reference documentation 
for a detailed function overview. However you might need to write your own functions in order to implement a specific 
logic in your test. We will deal with an id generating function in this tutorial. Maybe your project needs special 
identifiers in a special format and you are tired of statically defining the ids in all your tests. Therefore a function 
shall generate the id strings in the special format:

    Lx0x123456789xM

So the generated identifiers must have a static prefix "Lx0x" and a static ending "xM". In between there is a number of 
9 digits that should be randomly generated. The function should be accessible from every test case with the function name 
*"generateId"*. No function parameters are needed so far. So much for the requirements. No let us start implementing this 
function in Java.

All functions in Citrus need to implement the common function interface:

{% highlight java %}  
public interface Function {
    public String execute(List<String> parameterList, TestContext context);
}
{% endhighlight %}  

The function interface defines an execute method that will return a String value. Lets see how we can implement our id generating function.

{% highlight java %}  
package com.mycompany.citrus.extension.functions;
 
import java.util.Collections;
import java.util.List;
 
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.core.RandomNumberFunction;
 
public class IdGeneratingFunction implements Function {
    public String execute(List<String> parameterList, TestContext context) {
        StringBuffer idBuffer = new StringBuffer();
        
        idBuffer.append("Lx0x");
        idBuffer.append(new RandomNumberFunction().execute(Collections.singletonList("9")));
        idBuffer.append("xM");
        
        return idBuffer.toString();
    }
}
{% endhighlight %}
  
Our function implementation is very simple. We use a StringBuffer to build the resulting identifyer string with its 
static prefix ("Lx0x") and suffix ("xM"). For the generated random number of 9 digits we use the existing random number 
function of Citrus. The function receives the number length of 9 as a singelton parameter list and will do all the number 
generating work for us.

That's it! We return the identifyer string as a result of our function and we are done!

Now we need to publish the function to Citrus so that test cases can call the function. Every function in Citrus belongs 
to a function library. The library holds one or more functions and provides the prefix to identify the functions inside 
the test case. Let us define a custom function library in the Spring configuration to see how it is done:  

{% highlight xml %}  
<citrus:function-library id="myCustomFunctionLibrary" prefix="mcfl:">
            <citrus:function name="generateId" class="com.mycompany.citrus.extension.functions.IdGeneratingFunction"/>
</citrus:function-library>
{% endhighlight %}  

Once you have placed the function library into the Spring configuration Citrus is ready to use it inside the test cases. 
The library defines a custom function prefix (in our example "mcfl" for "my custom function library"). Usually this prefix 
would reflect the name of your project or your companies name. The library holds one or more functions as members in a 
simple map. Every function needs to have a name and the implementing Java object. Here we use our previously coded 
IdGeneratingFunction in the package *"com.mycompany.citrus.extension.functions"*.

Inside a test case we have to use the function prefix as well as the defined function name to access the new function:

{% highlight xml %}  
<variable name="correlationId" value="mcfl:generateId()"/>
{% endhighlight %}
  
Now you can add as many functions as you want to the new function library. You can extend Citrus with own functionalities
fitting the special needs of your project. In the following we will add another function that will use parameters.
  
## Writing functions with parameters

The common function interface says that a function gets a list of parameters while executing. This parameters are 
automatically converted into a Java list implementation so you can easily handle the parameters passed to your function.

Let us implement another function called "greeting". The function will return a greeting phrase when executed. The 
function looks for a parameter specifying the language. Let us see the detailed implementation:

{% highlight java %}  
public class GreetingFunction implements Function {
 
    public String execute(List<String> parameterList, TestContext context) {
        if(parameterList.isEmpty()) {
            throw new InvalidFunctionUsageException("Function parameters must not be empty. Specify language as parameter (US, GERMAN, SPANISH)!");
        }
        
        if(parameterList.size() > 1) {
            throw new InvalidFunctionUsageException("Function does not support multiple parameters");
        }
        
        String language = parameterList.get(0).toUpperCase();
        
        if(language.equals("US")) {
            return "Welcome";
        } else if(language.equals("GERMAN")) {
            return "Willkommen";
        } else if(language.equals("SPANISH")) {
            return "Bienvenido";
        } else {
            throw new UnsupportedOperationException("Unsupported language: '" + language + "'");
        }
    }
}
{% endhighlight %}

The function is again very simple. After the parameter list is checked for possible usage errors the function returns 
the correct greeting phrase according to the passed language. I bet fantastic function was just missing in your project, 
right? However lets add it to the function library and use it in our test. This time we use a Spring bean reference when 
including the new function in our library:

{% highlight xml %}  
<citrus:function-library id="myCustomFunctionLibrary" prefix="mcfl:">
            <citrus:function name="generateId" class="com.mycompany.citrus.extension.functions.IdGeneratingFunction"/>
            <citrus:function name="greeting" ref="greetingFunction"/>
</citrus:function-library>
 
<bean id="greetingFunction" class="com.mycompany.citrus.extension.functions.GreetingFunction"/>
{% endhighlight %}  

Another member entry for the function library and the function is ready for usage in our test cases:

{% highlight xml %}  
<testcase name="greetingTest">
    <variables>
        <variable name="user" value="Mickey Mouse"/>
    </variables>
    <actions>
        <echo>
            <message>mcfl:greeting('GERMAN') ${user}!</message>
        </echo>
        <echo>
            <message>mcfl:greeting('US') ${user}!</message>
        </echo>
        <echo>
            <message>mcfl:greeting('SPANISH') ${user}!</message>
        </echo>
    </actions>
</testcase>
{% endhighlight %}  

Note that functions can be used wherever you use variables. Inside echo messages for example as shown in our test example. 
Citrus will parse the function string and execute the function with the respective parameter list.

The output of the test proves that the function is working as expected:

    echo Willkommen MickeyMouse!
    echo Welcome MickeyMouse!
    echo Bienvenido MickeyMouse!
    
That's it for the writing functions tutorial. I think it is quite easy to include own logic into a test case using custom 
function implementations. We are glad to here about your functions and maybe we can include your function into the Citrus 
function library so all Citrus users can take the benefit.    
