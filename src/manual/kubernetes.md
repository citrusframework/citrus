## Kubernetes support

[Kubernetes](http://kubernetes.io/) is one of the hottest management platforms for containerized applications these days. Kubernetes lets you deploy, scale and manage your containers on the platform so you get features like auto-scaling, self-healing, service discovery and load balancing.
Citrus provides interaction with the Kubernetes REST API so you can access the Kubernetes platform within a Citrus test case.

**Note**
The Kubernetes test components in Citrus are kept in a separate Maven module. If not already done so you have to include the module as Maven dependency to your project

```xml
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-kubernetes</artifactId>
  <version>2.7-SNAPSHOT</version>
</dependency>
```

Citrus provides a "citrus-kubernetes" configuration namespace and schema definition for Kubernetes related components and actions. Include this namespace into your Spring configuration in order to use the Citrus Kubernetes configuration elements. The namespace URI and schema location are added to the Spring configuration XML file as follows.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:citrus-k8s="http://www.citrusframework.org/schema/kubernetes/config"
       xsi:schemaLocation="
       http://www.springframework.org/schema/beans 
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.citrusframework.org/schema/kubernetes/config
       http://www.citrusframework.org/schema/kubernetes/config/citrus-kubernetes-config.xsd">
       
    [...]
    
</beans>
```

After that you are able to use customized Citrus XML elements in order to define the Spring beans.

### Kubernetes client

Citrus operates with the Kubernetes remote REST API in order to interact with the Kubernetes platform. The Kubernetes client is defined as Spring bean component in the configuration as follows:

```xml
<citrus-kubernetes:client id="myK8sClient"/>
```

The Kubernetes client is based on the [Fabric8 Java Kubernetes client](https://github.com/fabric8io/kubernetes-client) implementation. Following from that the component can be configured in various ways. 
By default the client reads the system properties as well as environment variables for default Kubernetes settings such as:

*  **kubernetes.master** / **KUBERNETES_MASTER**
*  **kubernetes.api.version** / **KUBERNETES_API_VERSION**
*  **kubernetes.trust.certificates** / **KUBERNETES_TRUST_CERTIFICATES**

If you set these properties in your environment the client component will automatically pick up the configuration settings. For a complete list of settings and
explanation of those please refer to the [Fabric8 client documentation](https://github.com/fabric8io/kubernetes-client).

In case these settings are not settable in your environment you can also use explicit settings in the Kubernetes client component:

```xml
<citrus-kubernetes:client id="myK8sClient"
              url="http://localhost:8843"
              version="v1"
              username="user"
              password="s!cr!t"
              namespace="user_namespace"
              message-converter="messageConverter"
              object-mapper="objectMapper"/>
```

Now Citrus is able to access the Kubernetes remote API for executing commands such as list-pods, watch-services and so on.

### Kubernetes commands

We have several Citrus test actions each representing a Kubernetes command. These actions can be part of a test case where you can manage Kubernetes pods inside the test. As a prerequisite we have to enable the Kubernetes specific test actions in our XML test as follows:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:k8s="http://www.citrusframework.org/schema/kubernetes/testcase"
        xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.citrusframework.org/schema/kubernetes/testcase
        http://www.citrusframework.org/schema/kubernetes/testcase/citrus-kubernetes-testcase.xsd">

    [...]

</beans>
```

We added a special kubernetes namespace with prefix **k8s:** so now we can start to add Kubernetes test actions to the test case:

**XML DSL** 

```xml
<testcase name="KubernetesCommandIT">
    <actions>
      <k8s:info client="myK8sClient">
        <k8s:expect>
          <k8s:result>{
            "result": {
              "clientVersion": "1.4.27",
              "apiVersion": "v1",
              "kind":"Info",
              "masterUrl": "${masterUrl}",
              "namespace": "test"
            }
          }</k8s:result>
        </k8s:expect>      
      </k8s:info>
      
      <k8s:list-pods>
        <k8s:expect>
          <k8s:result>{
            "result": {
              "apiVersion":"v1",
              "kind":"PodList",
              "metadata":"@ignore@",
              "items":[]
            }
          }</k8s:result>
          <k8s:element path="$.result.items.size()" value="0"/>
        </k8s:expect>        
      </k8s:list-pods>
    </actions>
</testcase>
```

In this very simple example we first ping the Kubernetes REST API to make sure we have connectivity up and running. The info command connects the REST API and returns a list of status information of the Kubernetes client. 
After that we get the list of available Kubernetes pods. As a tester we might be interested in validating the command results. So wen can specify an optional **k8s:result** which is usually in JSON data format. As usual we can use test 
variables here and ignore some values explicitly such as the **metadata** value.

Based on that we can execute several Kubernetes commands in a test case and validate the Json results:

Citrus supports the following Kubernetes API commands with respective test actions:

*  **k8s:info** 
*  **k8s:list-pods** 
*  **k8s:list-services** 
*  **k8s:list-namespaces** 
*  **k8s:list-events** 
*  **k8s:list-endpoints** 
*  **k8s:list-nodes** 
*  **k8s:list-replication-controllers**
*  **k8s:watch-pods** 
*  **k8s:watch-services** 
*  **k8s:watch-namespaces** 
*  **k8s:watch-nodes** 
*  **k8s:watch-replication-controllers** 

Up to now we have only used the Citrus XML DSL. Of course all Kubernetes commands are also available in Java DSL as the next example shows.

**Java DSL** 

```java
@CitrusTest
public void kubernetesTest() {
    kubernetes().info()
        .validateCommandResult(new CommandResultCallback<InfoResult>() {
            @Override
            public void doWithCommandResult(InfoResult version, TestContext context) {
                Assert.assertEquals(version.getApiVersion(), "v1");
            }
    });

    kubernetes().listPods()
                .withoutLabel("running")
                .label("app", "myApp");
}
```

The Java DSL Kubernetes commands provide an optional **CommandResultCallback** that is called with the unmarshalled command result object. In the example above the *InfoResult* model object is passed as argument to the callback. So the tester can access the command result and validate its properties with assertions.

By default Citrus tries to find a Kubernetes client component within the Citrus Spring application context. If not present Citrus will instantiate a default kubernetes client with all default settings. You can also explicitly set the kubernetes client instance when using the Java DSL Kubernetes command actions:

**Java DSL** 

```java
@Autowired
private KubernetesClient kubernetesClient;

@CitrusTest
public void kubernetesTest() {
    kubernetes().client(kubernetesClient).info()
        .validateCommandResult(new CommandResultCallback<InfoResult>() {
            @Override
            public void doWithCommandResult(InfoResult version, TestContext context) {
                Assert.assertEquals(version.getApiVersion(), "v1");
            }
    });

    kubernetes().client(kubernetesClient).listPods()
                    .withoutLabel("running")
                    .label("app", "myApp");
}
```

### Kubernetes messaging

We have seen how to access the Kubernetes remote REST API by using special Citrus test actions in out test. As an alternative we can also go back
to the generic send/receive actions in Citrus for accessing the Kubernetes API. We demonstrate this with a simple example:

**XML DSL** 

```xml
<testcase name="KubernetesSendReceiveIT">
    <actions>
      <send endpoint="k8sClient">
        <message>
          <data>
            { "command": "info" }
          </data>
        </message>
      </send>
  
      <receive endpoint="k8sClient">
        <message type="json">
          <data>{
            "command": "info",
            "result": {
                "clientVersion": "1.4.27",
                "apiVersion": "v1",
                "kind":"Info",
                "masterUrl": "${masterUrl}",
                "namespace": "test"
              }
            }</data>
        </message>
      </receive>
      
      <echo>
        <message>List all pods</message>
      </echo>

      <send endpoint="k8sClient">
        <message>
          <data>
            { "command": "list-pods" }
          </data>
        </message>
      </send>

      <receive endpoint="k8sClient">
        <message type="json">
          <data>{
            "command": "list-pods",
            "result": {
                  "apiVersion":"v1",
                  "kind":"PodList",
                  "metadata":"@ignore@",
                  "items":[]
              }
          }</data>
          <validate path="$.result.items.size()" value="0"/>
        </message>
      </receive>
    </actions>
</testcase>
```

As you can see we can use the send/receive actions to call Kubernetes API commands and receive the respective results in Json format, too.