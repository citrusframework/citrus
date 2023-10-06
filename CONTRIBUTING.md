# Contributing to Citrus ![Logo][1]


:tada::lemon::tada: First, thank you for taking the time to contribute! :tada::lemon::tada:

This document contains a set of guidelines for contributing to the Citrus framework ecosystem.
The intention of these guidelines is to make it easier to work together. However, these guidelines are just
suggestions and not set in stone. Use them to the best of your knowledge and feel free to propose changes in a
pull request. 

_Credit: This Contribution guidelines are based on the
[Atom contribution guidelines](https://github.com/atom/atom/blob/master/CONTRIBUTING.md)_

> Note: In order to have your contributions included in the official Citrus code repository you have to agree to release
your changes under the same licensing agreement that Citrus uses at that time. Please also note that the Citrus team
might change the licensing of Citrus some time in future.

#### Table Of Contents

* [I just have a question](#i-just-have-a-question)
* [How Can I Contribute?](#how-can-i-contribute)
  * [Reporting Bugs](#reporting-bugs)
  * [Suggesting Changes](#suggesting-changes)
  * [Contributions](#contributions)
  * [Pull Requests](#pull-requests)
  * [Types of changes](#types-of-changes)
  * [Definition of ready](#definition-of-ready)
  * [Definition of done](#definition-of-done)

## I just have a question

Before you open an issue concerning your question, please make sure that none of the following steps already lead to an answer.

* Searching for a similar [question on github](https://github.com/citrusframework/citrus/issues?utf8=%E2%9C%93&q=is%3Aissue+label%3A%22Type%3A+Question%22).
* Searching for an answer on [stackoverflow](https://stackoverflow.com/questions/tagged/citrus-framework).
* Having a look into the [latest Citrus documentation](http://citrusframework.org/citrus/reference/html/index.html).

If your question is still unanswered, please use our
[question template](https://github.com/citrusframework/citrus/issues/new?template=question.md)
to create a new question on github or ask a new question on [stackoverflow](https://stackoverflow.com/questions/ask)
tagged with `citrus-framework`.

## How can I contribute?

### Reporting bugs

This section guides you through submitting a bug report for Citrus. Following these guidelines helps maintainers and the
community understand your report :pencil:, reproduce the behavior :computer: :computer:, and find related reports 
:mag_right:.

Before creating bug reports, please check [this list](#before-submitting-a-bug-report) as you might find out that you 
don't need to create one. When you are creating a bug report, please fill out 
[the required template](https://github.com/citrusframework/citrus/issues/new?template=bug_report.md) and ensure, that
the [definition of ready](#definition-of-ready) is fulfilled. The information it asks for helps us resolve issues faster.
Please ensure that the bug is reproducible with the latest version of Citrus.

> **Note:** If you find a **Closed** issue that seems like it is the same thing that you're experiencing, open a new 
issue and include a link to the original issue in the body of your new one.

#### Before submitting a bug report

* Please check the [latest Citrus documentation](http://citrusframework.org/citrus/reference/html/index.html) concerning
  the feature you are using, to ensure that Citrus does not work as designed. 
* [Search for issues](https://github.com/citrusframework/citrus/issues?utf8=%E2%9C%93&q=is%3Aissue+label%3A%22Type%3A+Bug%22+)
  to see if the problem has already been reported. If it has **and the issue is still open**, add a comment to the
  existing issue instead of opening a new one.

### Suggesting changes

This section guides you through submitting enhancement suggestions, feature requests or maintenance tasks for Citrus.
Following these guidelines helps maintainers and the community understand your suggestion :pencil: and find related
suggestions :mag_right:. We distinguish between different [different types of changes](#types-of-changes):
Maintenance, Enhancements, Features and Bugs. This section only concerns the first three of these. If you want to learn
about how to report bugs, [this](#reporting-bugs) might help you.

Before creating a suggestion, please check Check if there's already an [issue](https://github.com/citrusframework/citrus/issues)
addressing your suggestion. When you are creating an enhancement/feature suggestion, please fill in
[the template](https://github.com/citrusframework/citrus/issues/new?template=feature_request.md) and ensure, that the 
[definition of ready](#definition-of-ready) is fulfilled.

### Contributions
Please feel free to begin with any issue you would like to work on. It might be helpful to get some orientation by having
a look at our [types of changes](#types-of-changes) and the estimated requirements linked to them.  

>**Important Note**: If you'd like to contribute a bug fix, please ensure to branch from `v2.7-bugfix` instead of master.

We also highly appreciate contributions to milestones and we love to work closely with the community.
So if you'd like to work on an issue that is scheduled for a release, please make sure to stay in close contact with the
maintainers and make sure that it's possible for you to propose a [pull request](#pull-requests) before the planned
release date of the milestone, if specified.  

#### Preconditions

You need following software on your machine in order to start developing:

* Java 17+
  Installed JDK plus JAVA_HOME environment variable set
  up and pointing to your Java installation directory. Used to compile and build the Citrus code.

* Maven 3.9.5+
  Citrus projects will fit best with [Maven](https://maven.apache.org).
  However, it is not required to use Maven. You can also run tests using [Gradle](https://gradle.org/) for instance.

* Java IDE (optional)
  A Java IDE will help you to manage your Citrus project (e.g. creating
  and executing test cases). You can use the Java IDE that you like best like Eclipse or IntelliJ IDEA.

#### Unsure where to begin contributing to Citrus?
You can start by looking for issues labeled with `good first issue` which should only require a few lines of code, and
a test or two. 

### Pull Requests
Pull requests are required for every contribution to Citrus including contributions from maintainers and administrators.
Before you propose a pull request, please ensure, that the [definition of done](#definition-of-done) is fulfilled.
Please ensure that every pull request is linked to an issue. If you open a pull request without an existing issue, please 
open an issue as well, to point out the requirements for the change. That helps us to separate the functional discussion 
(issue) from the technical discussion (pull-request).

This helps us to reach several goals:
* Maintain the high quality standards of Citrus
* Make changes transparent to the community
* Discuss changes to ensure that the best possible solution will make it into the next release
* Enable a sustainable system for Citrus maintainers to review contributions

>**Important Note**: If you'd like to propose a pull request for a bug fix, please ensure that your base branch is
`v2.7-bugfix` instead of master and that the target branch for the pull request is `v2.7-bugfix` as well .

The pull request workflow is as follows: 
* Every pull request will be built via [GitHub Actions](https://github.com/citrusframework/citrus/actions/workflows/build.yml). It's mandatory that
  the build of the pull request is successful before the review begins. 
* A maintainer will [review](#review-criteria) your changes and provide feedback to you.
* If the pull request is project internal, it is required that the 
  [quality gate](https://sonarcloud.io/dashboard?id=citrus) is fulfilled.
  
#### Review criteria
The review of proposed code will focus on some important criteria for this project.

* The code should follow common style and design principles as well as our [sonarcloud rule set](https://sonarcloud.io/dashboard?id=citrus)
* The code should have a suitable test coverage
* The changes should be documented

### Types of changes
We distinguish between different types of changes which indicate different level of complexity, scope and required
knowledge about the Citrus framework.
 
* **Maintenance**  
  Tasks required to keep the framework up to date. E.g. updating dependencies. Tasks may vary in size from smaller to
  larger changes. Most of the tasks just require a little knowledge about the framework as the correctness of the 
  change is verified by automated unit and integration tests.
* **Enhancements**  
  Smaller or medium size improvements of the functionality of Citrus. Those tasks are completable in a limited amount of
  time with a little or average knowledge about the Citrus internals.
* **Features**  
  Medium or large additions to the framework. E.g. the integration of a new technology. Those changes require a medium
  or large amount of time and a deep knowledge of the framework.
* **Bug**  
  Parts of the framework that don't work as specified in the 
  [Citrus documentation](http://citrusframework.org/citrus/reference/html/index.html). Scope and required knowledge are
  dependent of the reported bug.

### Definition of ready
The definition of ready (DOR) specifies the entry-criteria for an issue that is currently in in the backlog to be moved
to the *ready* state so that it's possible to work on it. If you open a new issue, it would be great if you could ensure
that the following criteria are met. This helps us to understand your issue and allows to start developing right away
without any further research/reconstruction efforts.

* The corresponding issue template for [bugs](https://github.com/citrusframework/citrus/issues/new?template=bug_report.md)
  or [features](https://github.com/citrusframework/citrus/issues/new?template=feature_request.md) has been filled out completely
* Bigger changes have been transformed to epics and were broken down into smaller issues
* The context of the issue is understood by at least one maintainer
  * Therefore, it's important to provide as much context as possible
* Acceptance criteria are given
  * In case of a bug: There is a test to pass
  * In case of a feature/enhancement: There is at least one acceptance criteria derived from the user story

### Definition of done
The definition of done (DOD) specifies the exit-criteria for an issue that is currently in progress to be passed for
review. If you're working on an issue which you would like to pass for review, it would be great, if you could ensure
that all the following criteria are met. This helps us to review your changes as effective as possible.

* All requirements of the linked issue have been fulfilled
* Automated tests for the acceptance criteria have been created and passed
* Automated unit and integration tests have been created and passed
* The documentation has been updated

 [1]: https://citrusframework.org/img/brand-logo.png
