# Contributing to Citrus ![Logo][1]


:tada::lemon::tada: First off all, thanks for taking the time to contribute! :tada::lemon::tada:

This document contains a set of guidelines for contributing to the Citrus framework ecosystem.
The intention of these guidelines is to make it easier to work together. Nevertheless these guidelines are just
suggestions and not set in stone. Use them to the best of your knowledge and feel free to propose changes in a
pull request. 

_Credit: This Contribution guidelines are based on the
[Atom contribution guidelines](https://github.com/atom/atom/blob/master/CONTRIBUTING.md)_

#### Table Of Contents

[I just have a question](#i-just-have-a-question)
[How Can I Contribute?](#how-can-i-contribute)
  * [Reporting Bugs](#reporting-bugs)
  * [Suggesting Changes](#suggesting-changes)
  * [Contributions](#contributions)
  * [Pull Requests](#pull-requests)
  * [Types of change](#types-of-change)
  * [Definition of done](#definition-of-done)

## I just have a question

Before you open an issue concerning your question, please make sure that none of the following steps led to an answer.

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
[the required template](https://github.com/citrusframework/citrus/issues/new?template=bug_report.md).The information it
asks for helps us resolve issues faster. Please ensure that the bug is reproducible with the latest version of Citrus.

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
suggestions :mag_right:. We distinguish between different [types of change](#types-of-change):
Maintenance, Enhancements, Features and Bugs. This section only concerns the first three of these.

Before creating a suggestion, please check Check if there's already an [issue](https://github.com/citrusframework/citrus/issues) addressing your suggestion.
When you are creating an enhancement suggestion, please
fill in [the template](https://github.com/citrusframework/citrus/issues/new?template=feature_request.md).

### Contributions
Please feel free to begin with any issue you would like to work on. It might be helpful to get some orientation by having
a look at our [types of change](#types-of-change) and the estimated requirements linked to them.  

>**Important Note**: If you'd like to contribute a bug fix, please ensure to branch from `v2.7-bugfix` instead of master.

We also always high appreciate contributions to milestones and we love to work closely with the community.
So if you'd like to work on an issue that is scheduled for a release, please make sure to stay in close contact with the
maintainers and make sure that it's possible for you to propose a [pull request](#pull-requests) before the planned
release date of the milestone, if specified.  

#### Unsure where to begin contributing to Citrus?
You can start by looking for issues labeled with `good first issue` which should only require a few lines of code, and
a test or two. 

### Pull Requests
Pull requests are required for every contribution to Citrus including contributions from maintainers and administrators.
Before you propose a pull request, please ensure, that the [definition of done](#definition-of-done) is fulfilled.
Please ensure that every pull request is linked to an issue. If you open a pull request without an existing issue, please 
open an issue as well to point out the requirements for the change. That helps us to separate the functional discussion
from the technical discussion of the pull request.

This helps us to reach several goals:
* Maintain the high quality standards of Citrus
* Make changes transparent to the community
* Discuss changes to ensure that the best possible solution will make it into the next release
* Enable a sustainable system for Citrus maintainers to review contributions

>**Important Note**: If you'd like to propose a pull request for a bug fix, please ensure that your base branch is
`v2.7-bugfix` instead of master and that the target branch for the pull request is `v2.7-bugfix` as well .

The pull request workflow is as follows: 
* Every pull request will be built via [Travis CI](https://travis-ci.org/citrusframework/citrus). It's mandatory that
  the build of the pull request is successful before the review begins. 
* A maintainer will [review](#review-criteria) your changes and provide feedback to you.
* If the pull request is project internal, it is required that the 
  [quality gate](https://sonarcloud.io/dashboard?id=com.consol.citrus%3Acitrus) is fulfilled.
  
#### Review criteria
The review of proposed code will focus on some important criteria for this project.

* The code should follow common style and design principles
* The code should have a suitable test coverage
* The changes should be documented

### Types of change
We distinguish between different types of change which indicate different level of complexity, scope and required
knowledge about the Citrus framework.
 
* Maintenance  
  Tasks required to keep the framework up to date. E.g. updating dependencies or Java version. Depending on the tasks, 
  the changes may vary from smaller to larger size with a according time required. Most of the tasks just require a
  little knowledge about the framework as the correctness of the change is verified by automated unit- and
  integration tests.
* Enhancements  
  Smaller or medium size improvements of the functionality of Citrus. Those tasks are completable in a limited amount of
  time with a little or average knowledge about the Citrus internals.
* Features  
  Medium or large additions to the framework. E.g. the integration of a new technology. Those changes require a medium
  or large amount of time and a deep knowledge of the framework.
* Bug  
  Parts of the framework that don't work as specified in the 
  [Citrus documentation](http://citrusframework.org/citrus/reference/html/index.html). Scope and required knowledge are
  dependent of the reported bug.

### Definition of done
TODO!

 [1]: http://www.citrusframework.org/img/brand-logo.png