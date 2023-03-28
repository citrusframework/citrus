Citrus RMI module ![Logo][1]
==============

Welcome to Citrus RMI
---------

This project contains a module for connecting to Java RMI software components.
This includes RMI client and server components.

Visit our official website at 'http://www.citrusframework.org'
for more information and a detailed documentation.

Preconditions
---------

You need following software on your machine in order to use the
Citrus framework:

* Java 11+
  Installed JDK plus JAVA_HOME environment variable set
  up and pointing to your Java installation directory. Used to compile and build the Citrus code.

* Maven 3.6.3+
  Citrus projects will fit best with [Maven](https://maven.apache.org).
  However, it is not required to use Maven. You can also run tests using [Gradle](https://gradle.org/) for instance.

* Java IDE (optional)
  A Java IDE will help you to manage your Citrus project (e.g. creating
  and executing test cases). You can use the Java IDE that you like best like Eclipse or IntelliJ IDEA.

Usage
---------

RMI module provides client and server components. The components are part of a new Citrus configuration
namespace:

    xmlns:citrus-rmi="http://www.citrusframework.org/schema/rmi/config"

So we have to add this new namespace to the Spring application context configuration.

    <bean xmlns:citrus-rmi="http://www.citrusframework.org/schema/rmi/config"
        schema-location="http://www.citrusframework.org/schema/rmi/config http://www.citrusframework.org/schema/rmi/config/citrus-rmi-config.xsd">

Once this is done we can add the RMI Citrus components to the configuration. Client components connect to a service 
registry performing a lookup for some service binding name.

    <citrus-rmi:client id="rmiClient" 
                     host="localhost" 
                     port="1099"
                     binding="service/path/or/name"/>
                     
Send operations can reference this client for a remote service call.
                     
    send(rmiClient)
        .message(RmiMessage.invocation("someMethod")
                        .argument("Hello from RMI client")));
                        
The send operation above will call the **someMethod** method on the remote target. The remote target
must be registered as remote service in the registry. Optional method arguments can be passed to the method call.

The remote call result is the return object of the remote method call. This service result is passed to the client for
validation:

    receive(rmiClient)
        .message(RmiMessage.result("Should return this text");
    
The client will automatically validate the method result with given result object.
    
On the server side we define one to many remote interfaces and bind those in a registry.
    
    <citrus-rmi:server id="rmiServer"
                     host="localhost" 
                     port="1099"
                     binding="service/path/or/name"
                     interface="org.citrusframework.remote.SomeInterface"/>
                    
The server component connects to a registry for bindin the remote interface as service with given binding name. Clients 
can then do a lookup for calling the remote service methods. Each method call is handled within the server and can be validated
in a receive action.

    receive(rmiServer)
        .message(RmiMessage.invocation(SomeInterface.class, "someMethod")
                        .argument("Hello remote service");
                        
As usual the receive action makes sure that the service call is as expected by validating and comparing the invocation with expected
data. After that the server is able to return some result object to the calling client.

    send(rmiServer)
        .message(RmiMessage.result("OK"));
    
The server can also raise a RemoteException.

    send(rmiServer)
        .message(RmiMessage.result().exception("Something went wrong");
    
Citrus will automatically raise a new RemoteException with **Something went wrong** as failure message.

XML DSL support is also available for RMI components and actions:

    <send endpoint="rmiClient">
      <message>
        <payload>
            <service-invocation xmlns="http://www.citrusframework.org/schema/rmi/message">
              <remote>org.citrusframework.remote.SomeInterface</remote>
              <method>someMethod</method>
              <args>
                <arg value="Hello remote service!"/>
              </args>
            </service-invocation>
        </payload>
      </message>  
    </send>
  
    <receive endpoint="rmiClient">
      <message>
        <payload>
            <service-result xmlns="http://www.citrusframework.org/schema/rmi/message">
              <object type="java.lang.String" value="This is the server result!"/>
            </service-result>
        </payload>
      </message>
    </receive>
  
Citrus will automatically marshal the XML representation of the service invocation and result.  
   
Licensing
---------
  
Copyright 2006-2023 the original author or authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Team
---------

The Citrus Community

https://citrusframework.org

Information
---------

For more information on Citrus see [www.citrusframework.org][2], including
a complete [reference manual][3].

 [1]: https://citrusframework.org/images/brand_logo.png "Citrus"
 [2]: https://citrusframework.org
 [3]: https://citrusframework.org/reference/html/
