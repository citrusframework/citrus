---
layout: docs
title: Maven plugin
permalink: /docs/maven-plugin/
---

The Citrus Maven plugin offers several tools and handy tasks for generating test cases and reports.

## System Requirements

The following specifies the minimum requirements to run this Maven plugin:

| Library | Version |
|:-------:|:-------:|
| Maven | 3.0 |
| JDK | 1.7 |
| Memory | No minimum requirement. |
| Disk Space | No minimum requirement. |

## Usage

You should specify the version in your project's plugin configuration:

{% highlight xml %}  
<project>
  ...
  <build>
    <!-- To define the plugin version in your parent POM -->
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>com.consol.citrus.mvn</groupId>
          <artifactId>citrus-maven-plugin</artifactId>
          <version>${project.version}</version>
        </plugin>
        ...
      </plugins>
    </pluginManagement>
    <!-- To use the plugin goals in your POM or parent POM -->
    <plugins>
      <plugin>
        <groupId>com.consol.citrus.mvn</groupId>
        <artifactId>citrus-maven-plugin</artifactId>
        <configuration>
          ...
        </configuration>
      </plugin>
      ...
    </plugins>
  </build>
  ...
</project>
{% endhighlight %}  

## Goals

| Goal | Description |
|:-------:|:-------:|
| citrus:create-excel-doc | Creates test documentation in MS Excel listing all available tests with meta information (name, author, description, date, ...) |
| citrus:create-html-doc | Creates test overview documentation in HTML. The web page contains a list of all available tests with meta information. |
| citrus:create-test | Creates new Citrus test cases with empty XML test file and executable Java class. Mojo offers an interactive mode, where the plugin prompts for parameters during execution. In non-interactive mode the parameters are given as command line arguments. Also supports automatic message XML payload generation for XSD and WSDL schemas. |
| citrus:help | Display help information on citrus-maven-plugin. Call **mvn citrus:help -Ddetail=true -Dgoal=<goal-name>** to display parameter details. |

## citrus:create-excel-doc

### Full name

    com.consol.citrus.mvn:citrus-maven-plugin:${project.version}:create-excel-doc

### Description

Creates test documentation in MS Excel listing all available tests with meta information (name, author, description, date, ...) .

### Optional Parameters

| Name | Type | Since | Description |
|:-------:|:-------:|:-------:|:-------:|
| author | String | - | Author name that goes into Excel meta information. Default value is: Citrus Testframework. |
| company | String | - | Name of company that goes into Excel meta information. Default value is: Unknown. |
| customHeaders | String | - | Customized column headers as comma separated value string (e.g. "Nr;Name;Author;Status;TestCase;Date"). |
| interactiveMode | boolean | - | Whether to use interactive mode where user is prompted for parameter input during execution. Default value is: true. |
| outputFile | String | - | Name of output file (.xsl file extension is added automatically and can be left out). Defaults to "CitrusTests". Default value is: CitrusTests. |
| pageTitle | String | - | Page title displayed on top of the sheet. Default value is: Citrus Test Documentation. |
| testDirectory | String | - | Mojo looks in this directory for test files that are included in this report. Defaults to "src/it/tests" Default value is: src/it/tests. |

### Parameter Details

**author:**

Author name that goes into Excel meta information.

- **Type:** java.lang.String
- **Required:** No
- **Default:** Citrus Testframework

**company:**

Name of company that goes into Excel meta information.

- **Type:** java.lang.String
- **Required:** No
- **Default:** Unknown

**customHeaders:**

Customized column headers as comma separated value string (e.g. "Nr;Name;Author;Status;TestCase;Date").

- **Type:** java.lang.String
- **Required:** No

**interactiveMode:**

Whether to use interactive mode where user is prompted for parameter input during execution.

- **Type:** boolean
- **Required:** No
- **Expression:** ${interactiveMode}
- **Default:** true

**outputFile:**

Name of output file (.xsl file extension is added automatically and can be left out). Defaults to "CitrusTests".

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${outputFile}
- **Default:** CitrusTests

**pageTitle:**

Page title displayed on top of the sheet.

- **Type:** java.lang.String
- **Required:** No
- **Default:** Citrus Test Documentation

**testDirectory:**

Mojo looks in this directory for test files that are included in this report. Defaults to "src/it/tests"

- **Type:** java.lang.String
- **Required:** No
- **Default:** src/it/tests

## citrus:create-html-doc

### Full name

    com.consol.citrus.mvn:citrus-maven-plugin:${project.version}:create-html-doc

### Description

Creates test overview documentation in HTML. The web page contains a list of all available tests with meta information.

### Optional Parameters

| Name | Type | Since | Description |
|:-------:|:-------:|:-------:|:-------:|
| columns | String | - | Number of columns in test overview table Default value is: 1. |
| interactiveMode | boolean | - | Whether to use interactive mode where user is prompted for parameter input during execution. Default value is: true. |
| logo | String | - | Company or project logo displayed on top of page. Defaults to "logo.png" Default value is: logo.png. |
| outputFile | String | - | Name of output file (.html file extension is added automatically and can be left out). Defaults to "CitrusTests" Default value is: CitrusTests. |
| overviewTitle | String | - | The overview title displayed at the top of the test overview Default value is: Overview. |
| pageTitle | String | - | Page title displayed at the top of the page Default value is: Citrus Test Documentation. |
| testDirectory | String | - | All test files in this directory are included into the report. Defaults to "src/it/tests" Default value is: src/it/tests. |

### Parameter Details

**columns:**

Number of columns in test overview table

- **Type:** java.lang.String
- **Required:** No
- **Default:** 1

**interactiveMode:**

Whether to use interactive mode where user is prompted for parameter input during execution.

- **Type:** boolean
- **Required:** No
- **Expression:** ${interactiveMode}
- **Default:** true

**logo:**

Company or project logo displayed on top of page. Defaults to "logo.png"

- **Type:** java.lang.String
- **Required:** No
- **Default:** logo.png

**outputFile:**

Name of output file (.html file extension is added automatically and can be left out). Defaults to "CitrusTests"

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${outputFile}
- **Default:** CitrusTests

**overviewTitle:**

The overview title displayed at the top of the test overview

- **Type:** java.lang.String
- **Required:** No
- **Default:** Overview

**pageTitle:**

Page title displayed at the top of the page

- **Type:** java.lang.String
- **Required:** No
- **Default:** Citrus Test Documentation

**testDirectory:**

All test files in this directory are included into the report. Defaults to "src/it/tests"

- **Type:** java.lang.String
- **Required:** No
- **Default:** src/it/tests

## citrus:create-test

### Full name

    com.consol.citrus.mvn:citrus-maven-plugin:${project.version}:create-test

### Description

Creates new Citrus test cases with empty XML test file and executable Java class. Mojo offers an interactive mode, where 
the plugin prompts for parameters during execution. In non-interactive mode the parameters are given as command line arguments.

### Optional Parameters

| Name | Type | Since | Description |
|:-------:|:-------:|:-------:|:-------:|
| author | String | - | The test author Default value is: Unknown. |
| description | String | - | Describes the test case and its actions Default value is: TODO: Description. |
| framework | String | - | Which unit test framework to use for test execution (default: testng; options: testng, junit) Default value is: testng. |
| interactiveMode | boolean | - | Whether to run this command in interactive mode. Defaults to "true". Default value is: true. |
| name | String | - | The name of the test case (must start with upper case letter). | 
| targetPackage | String | - | Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus" Default value is: com.consol.citrus. |
| xsd | String | - | Path of the xsd from which the sample request and response are get from |
| xsdRequestElem | String | - | Name of the xsd-element used to create the xml-sample-request |
| xsdResponseElem | String | - | Name of the xsd-element used to create the xml-sample-response |
| wsdl | String | - | The path to the wsdl from which the suite is generated. |
| nameSuffix | String | - | The name-suffix of all test cases. Default value is: _IT. |

### Parameter Details

**author:**

The test author

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${author}
- **Default:** Unknown

**description:**

Describes the test case and its actions

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${description}
- **Default:** TODO: Description

**framework:**

Which unit test framework to use for test execution (default: testng; options: testng, junit)

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${framework}
- **Default:** testng

**interactiveMode:**

Whether to run this command in interactive mode. Defaults to "true".

- **Type:** boolean
- **Required:** No
- **Expression:** ${interactiveMode}
- **Default:** true

**name:**

The name of the test case (must start with upper case letter).

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${name}

**targetPackage:**

Which package (folder structure) is assigned to this test. Defaults to "com.consol.citrus"

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${targetPackage}
- **Default:** com.consol.citrus

**xsd:**

Path of the xsd from which the sample request and response are get from

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${xsd}

**xsdRequestElem:**

Name of the xsd-element used to create the xml-sample-request

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${xsdRequestElem}

**xsdResponseElem:**

Name of the xsd-element used to create the xml-sample-response

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${xsdResponseElem}

**wsdl:**

The path to the wsdl from which the suite is generated.

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${wsdl}

**nameSuffix:**

The name-suffix of all test cases.

- **Type:** java.lang.String
- **Required:** No
- **Expression:** ${nameSuffix}
- **Default:** _IT