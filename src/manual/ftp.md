## FTP support

Citrus is able to start a little ftp server accepting incoming client requests. Also Citrus is able to call FTP commands as a client. The next sections deal with FTP connectivity.

**Note**
The FTP components in Citrus are kept in a separate Maven module. So you should add the module as Maven dependency to your project accordingly.

```xml
<dependency>
  <groupId>com.consol.citrus</groupId>
  <artifactId>citrus-ftp</artifactId>
  <version>2.6.2</version>
</dependency>
```

As Citrus provides a customized FTP configuration schema for the Spring application context configuration files we have to add name to the top level **beans** element. Simply include the ftp-config namespace in the configuration XML files as follows.

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:citrus="http://www.citrusframework.org/schema/config"
    xmlns:citrus-ftp="http://www.citrusframework.org/schema/ftp/config"
    xsi:schemaLocation="
    http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.citrusframework.org/schema/config
    http://www.citrusframework.org/schema/config/citrus-config.xsd
    http://www.citrusframework.org/schema/http/config
    http://www.citrusframework.org/schema/ftp/config/citrus-ftp-config.xsd">

      [...]

      </beans>
```

Now we are ready to use the customized Citrus FTP configuration elements with the citrus-ftp namespace prefix.

### FTP client

We want to use Citrus fo connect to dome FTP server as a client sending commands such as creating a directory or listing all files. Citrus offers a client component doing exactly this FTP client connection.

```xml
<citrus-ftp:client id="ftpClient"
      host="localhost"
      port="22222"
      username="admin"
      password="admin"
      timeout="10000"/>
```

The configuration above describes a Citrus ftp client connected to a ftp server with **ftp://localhost:22222** . For authentication username and password are defined as well as the global connection timeout. The client will automatically send username and password for proper authentication to the server when opening a new connection.

In a test case you are now able to use the client to push commands to the server.

```xml
<send endpoint="ftpClient" fork="true">
  <message>
    <data></data>
  </message>
  <header>
    <element name="citrus_ftp_command" value="PWD"/>
    <element name="citrus_ftp_arguments" value="test"/>
  </header>
</send>

<receive endpoint="ftpClient">
  <message type="plaintext">
    <data>PWD</data>
  </message>
  <header>
    <element name="citrus_ftp_command" value="PWD"/>
    <element name="citrus_ftp_arguments" value="test"/>
    <element name="citrus_ftp_reply_code" value="257"/>
    <element name="citrus_ftp_reply_string" value="@contains('is current directory')@"/>
  </header>
</receive>
```

As you can see most of the ftp communication parameters are specified as special header elements in the message. Citrus automatically converts those information to proper FTP commands and response messages.

### FTP server

Now that we are able to access FTP as a client we might also want to simulate the server side. Therefore Citrus offers a server component that is listening on a port for incoming FTP connections. The server has a default home directory on the local file system specified. But you can also define home directories per user. For now let us have a look at the server configuration component:

```xml
<citrus-ftp:server id="ftpServer">
      port="22222"
      auto-start="true"
      user-manager-properties="classpath:ftp.server.properties"/>
```

The ftp server configuration is quite simple. The server starts automatically and binds to a port. The user configuration is read from a **user-manager-property** file. Let us have a look at the content of this user management file:

```xml
# Password is "admin"
ftpserver.user.admin.userpassword=21232F297A57A5A743894A0E4A801FC3
ftpserver.user.admin.homedirectory=target/ftp/user/admin
ftpserver.user.admin.enableflag=true
ftpserver.user.admin.writepermission=true
ftpserver.user.admin.maxloginnumber=0
ftpserver.user.admin.maxloginperip=0
ftpserver.user.admin.idletime=0
ftpserver.user.admin.uploadrate=0
ftpserver.user.admin.downloadrate=0

ftpserver.user.anonymous.userpassword=
ftpserver.user.anonymous.homedirectory=target/ftp/user/anonymous
ftpserver.user.anonymous.enableflag=true
ftpserver.user.anonymous.writepermission=false
ftpserver.user.anonymous.maxloginnumber=20
ftpserver.user.anonymous.maxloginperip=2
ftpserver.user.anonymous.idletime=300
ftpserver.user.anonymous.uploadrate=4800
ftpserver.user.anonymous.downloadrate=4800
```

As you can see you are able to define as many user for the ftp server as you like. Username and password define the authentication on the server. In addition to that you have plenty of configuration possibilities per user. Citrus uses the Apache ftp server implementation. So for more details on configuration capabilities please consult the official Apache ftp server documentation.

Now we would like to use the server in a test case. Very easy you just have to define a receive message action within your test case that uses the server id as endpoint reference:

```xml
<echo>
  <message>Receive user login on FTP server</message>
</echo>

<receive endpoint="ftpServer">
  <message type="plaintext">
    <data>USER</data>
  </message>
  <header>
    <element name="citrus_ftp_command" value="USER"/>
    <element name="citrus_ftp_arguments" value="admin"/>
  </header>
</receive>

<send endpoint="ftpServer">
  <message type="plaintext">
    <data>OK</data>
  </message>
</send>

<echo>
  <message>Receive user password on FTP server</message>
</echo>

<receive endpoint="ftpServer">
  <message type="plaintext">
    <data>PASS</data>
  </message>
  <header>
    <element name="citrus_ftp_command" value="PASS"/>
    <element name="citrus_ftp_arguments" value="admin"/>
  </header>
</receive>

<send endpoint="ftpServer">
  <message type="plaintext"">
    <data>OK</data>
  </message>
</send>
```

The listing above shows two incoming commands representing a user login. We indicate with re send actions that we would link the server to respond with positive feedback and to accept the login. As we have a fully qualified ftp server running the client can also push files read directories and more. All incoming commands can be validated inside a test case.

