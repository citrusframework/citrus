## Selenium support

[Selenium](http://www.seleniumhq.org/) is a very popular tool for testing user interfaces with browser automation. Citrus is able to integrate with the Selenium Java API in order to execute Selenium commands.

**Note**
The Selenium test components in Citrus are kept in a separate Maven module. If not already done so you have to include the module as Maven dependency to your project

```xml
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-selenium</artifactId>
  <version>2.7-SNAPSHOT</version>
</dependency>
```

Citrus provides a "citrus-selenium" configuration namespace and schema definition for Selenium related components and actions. Include this namespace into your Spring configuration in order to use the Citrus Selenium configuration elements. The namespace URI and schema location are added to the Spring configuration XML file as follows.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:citrus-selenium="http://www.citrusframework.org/schema/selenium/config"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans 
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.citrusframework.org/schema/selenium/config
       http://www.citrusframework.org/schema/selenium/config/citrus-selenium-config.xsd">
       
    [...]
    
</beans>
```

After that you are able to use customized Citrus XML elements in order to define the Spring beans.

### Selenium browser

Selenium uses browser automation in order to simulate the user interact with web applications. You can configure the Selenium browser and web driver as Spring bean.

```xml
<citrus-selenium:browser id="seleniumBrowser"
                type="firefox"
                start-page="http://citrusframework.org"/>
```

The Selenium browser component supports different browser types for the commonly used browsers out in the wild.
 
* **htmlunit**
* **firefox**
* **safari**
* **chrome**
* **googlechrome**
* **internet explorer**  
* **custom**

Html unit is the default browser type and represents a headless browser that executed without displaying the graphical user interface. In case you need a totally different browser or
you need to customize the Selenium web driver you can use the *browserType="custom"* in combination with a web driver reference:

```xml
<citrus-selenium:browser id="mySeleniumBrowser"
                type="custom"
                web-driver="operaWebDriver"/>
                
<bean id="operaWebDriver" class="org.openqa.selenium.opera.OperaDriver"/>
```

Now Citrus is using the customized Selenium web driver implementation.

**Note**
When using Firefox as browser you may also want to set the optional properties **firefox-profile** and **version**.

```xml
<citrus-selenium:browser id="mySeleniumBrowser"
                type="firefox"
                firefox-profile="firefoxProfile"
                version="FIREFOX_38"
                start-page="http://citrusframework.org"/>
                
<bean id="firefoxProfile" class="org.openqa.selenium.firefox.FirefoxProfile"/>
```

Now Citrus is able to execute Selenium operations as a user.

### Selenium actions

We have several Citrus test actions each representing a Selenium command. These actions can be part of a Citrus test case. As a prerequisite we have to enable the Selenium specific test actions in our XML test as follows:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:selenium="http://www.citrusframework.org/schema/selenium/testcase"
        xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.citrusframework.org/schema/selenium/testcase
        http://www.citrusframework.org/schema/selenium/testcase/citrus-selenium-testcase.xsd">

    [...]

</beans>
```

We added a special selenium namespace with prefix **selenium:** so now we can start to add Selenium test actions to the test case:

**XML DSL** 

```xml
<testcase name="SeleniumCommandIT">
    <actions>
      <selenium:start browser="webBrowser"/>
      
      <selenium:navigate page="http://localhost:8080"/>
              
      <selenium:find>
        <selenium:element tag-name="h1" text="Welcome!">
          <selenium:styles>
            <selenium:style name="font-size" value="20pt"/>
          </selenium:styles>
        </selenium:element>
      </selenium:find>
      
      <selenium:click>
        <selenium:element id="ok-button"/>
      </selenium:click>  
    </actions>
</testcase>
```

In this very simple example we first start the Selenium browser instance. After that we can continue to use Selenium commands without browser attribute explicitly set. Citrus
knows which browser instance is currently active and will automatically use this opened browser instance. Next in this example we find some element on the displayed page by its
tag-name and text. We also validate the element style *font-size* to meet the expected value *20pt* in this step.

In addition to that the example performs a click operation on the element with the id *ok-button*. Selenium supports element find operations on different properties:

* **id** finds element based on the *id* attribute
* **name** finds element based on the *name* attribute
* **tag-name** finds element based on the *tag name*
* **class-name** finds element based on the css *class name*
* **link-text** finds link element based on the *link-text*
* **xpath** finds element based on XPath evaluation in the DOM

Based on that we can execute several Selenium commands in a test case and validate the results such as web elements.

Citrus supports the following Selenium commands with respective test actions:

*  **selenium:start** Start the browser instance 
*  **selenium:find** Finds element on current page and validates element properties 
*  **selenium:click** Performs click operation on element
*  **selenium:navigate** Navigates to new page url (including history back, forward and refresh) 
*  **selenium:set-input** Finds input element and sets value 
*  **selenium:check-input** Finds checkbox element and sets/unsets value 
*  **selenium:dropdown-select** Finds dropdown element and selects single or multiple value/s 
*  **selenium:page** Instantiate page object with dependency injection and execute page action with verification
*  **selenium:open** Open new window 
*  **selenium:close** Close window by given name 
*  **selenium:switch** Switch focus to window with given name 
*  **selenium:wait-until** Wait for element to be *hidden* or *visible* 
*  **selenium:alert** Access current alert dialog (with action *access* or *dismiss*) 
*  **selenium:screenshot** Makes screenshot of current page 
*  **selenium:store-file** Store file to temporary browser directory 
*  **selenium:get-stored-file** Gets stored file from temporary browser directory
*  **selenium:javascript** Execute Javascript code in browser 
*  **selenium:clear-cache** Clear browser cache and all cookies 
*  **selenium:stop** Stops the browser instance 

Up to now we have only used the Citrus XML DSL. Of course all Selenium commands are also available in Java DSL as the next example shows.

**Java DSL** 

```java
@Autowired
private SeleniumBrowser seleniumBrowser;

@CitrusTest
public void seleniumTest() {
    selenium().start(seleniumBrowser);
    
    selenium().navigate("http://localhost:8080");

    selenium().find().element(By.id("header"));
                .tagName("h1")
                .enabled(true)
                .displayed(true)
                .text("Welcome!")
                .style("font-size", "20pt");

    selenium().click().element(By.linkText("Click Me!"));
}
```

Now lets have a closer look at the different Selenium test actions supported in Citrus.

### Start/stop browser

You can start and stop the browser instance with a test action. This instantiates a new browser window and prepares everything for interacting with the web
interface.

**XML DSL** 

```xml
<selenium:start browser="seleniumBrowser"/>

<!-- Do something in browser -->

<selenium:stop browser="seleniumBrowser"/>
```

**Java DSL** 

```java
selenium().start(seleniumBrowser);

// do something in browser

selenium().stop(seleniumBrowser);
```

After starting a browser instance Citrus will automatically use this very same browser instance in all further Selenium actions. This mechanism is based on a test variable (**selenium_browser**) that
is automatically set. All other test actions are able to load the current browser instance by reading this test variable before execution. In case you need to explicitly use
a different browser instance than the active instance you can add the **browser** attribute to all Selenium test actions.

**Note**

It is a good idea to start and stop the browser instance before each test case. This makes sure that tests are also executable in single run and it always sets up a new browser instance so tests
will not influence each other.

### Find

The find element test action searches for an element on the current page. The element is specified by one of the following settings:

* **id** finds element based on the *id* attribute
* **name** finds element based on the *name* attribute
* **tag-name** finds element based on the *tag name*
* **class-name** finds element based on the css *class name*
* **link-text** finds link element based on the *link-text*
* **xpath** finds element based on XPath evaluation in the DOM

The find element action will automatically fail in case there is no such element on the current page. In case the element is found you can add additional attributes and properties
for further element validation:

**XML DSL** 

```xml
<selenium:find>
  <selenium:element tag-name="h1" text="Welcome!">
    <selenium:styles>
      <selenium:style name="font-size" value="20pt"/>
    </selenium:styles>
  </selenium:element>
</selenium:find>

<selenium:find>
  <selenium:element id="ok-button" text="Ok" enabled="true" displayed="true">
    <selenium:attributes>
      <selenium:attribute name="type" value="submit"/>
    </selenium:attributes>
  </selenium:element>
</selenium:find>
```

**Java DSL** 

```java
selenium().find().element(By.tagName("h1"))
        .text("Welcome!")
        .style("font-size", "20pt");

selenium().find().element(By.id("ok-button"))
        .tagName("button")
        .enabled(true)
        .displayed(true)
        .text("Ok")
        .style("color", "red")
        .attribute("type", "submit");
```

The example above finds the **h1** element by its tag name and validates the text and css style attributes. Secondly the **ok-button** is validated with expected
enabled, displayed, text, style and attribute values. The elements must be present on the current page and all expected element properties have to match. Otherwise the test action and the test case
is failing with validation errors.

### Click

The action performs a click operation on the element.

**XML DSL** 

```xml
<selenium:click>
  <selenium:element link-text="Click Me!"/>
</selenium:click>
```

**Java DSL** 

```java
selenium().click().element(By.linkText("Click Me!"));
```

### Form input actions

The following actions are used to access form input elements such as text fields, checkboxes and dropdown lists.

**XML DSL** 

```xml
<selenium:set-input value="Citrus">
  <selenium:element name="username"/>
</selenium:set-input>

<selenium:check-input checked="true">
  <selenium:element xpath="//input[@type='checkbox']"/>
</selenium:check-input>

<selenium:dropdown-select option="happy">
  <selenium:element id="user-mood"/>
</selenium:dropdown-select>
```

**Java DSL** 

```java
selenium().setInput("Citrus").element(By.name("username"));
selenium().checkInput(true).element(By.xpath("//input[@type='checkbox']"));

selenium().select("happy").element(By.id("user-mood"));
```

The actions above select dropdown options and set user input on text fields and checkboxes. As usual the form elements are selected by some properties such as
ids, names or xpath expressions.

### Window actions

Selenium is able to manage multiple windows. So you can open, close and swich active windows in a Citrus test. 

**XML DSL** 

```xml
<selenium:open-window name="my_window"/>
<selenium:switch-window name="my_window"/>
<selenium:close-window name="my_window"/>
```

**Java DSL** 

```java
selenium().open().window("my_window");
selenium().focus().window("my_window");
selenium().close().window("my_window");
```

When a new window is opened Selenium creates a window handle for us. This window handle is saved as test variable using a given window name. So after opening the window you can access the
window by its name in further actions. All upcoming Selenium actions will take place in this new active window. Of course the test actions will fail as soon as the window with that given 
name is missing. Citrus uses default window names that are automatically used as test variables:

* **selenium_active_window** the active window handle
* **selenium_last_window** the last window handle when switched to other window

### Make screenshot

You can execute this action in case you want to take a screenshot of the current page. This action only works with browsers that actually display the user interface. The action will not have any effect 
when executed with Html unit web driver in headless mode.

**XML DSL** 

```xml
<selenium:screenshot/>

<selenium:screenshot output-dir="target"/>
```

**Java DSL** 

```java
selenium().screenhsot();

selenium().screenhsot("target");
```

The test action has an optional parameter *output-dir* which represents the output directory where the screenshot is saved to.
