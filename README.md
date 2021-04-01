# Nuxeo Labs

Build Status: [![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=master/nuxeo-labs-master)](https://qa.nuxeo.org/jenkins/job/master/job/nuxeo-labs-master/)

Nuxeo Labs is a collection of features developed by the Nuxeo Solution Engineering team. These features are purpose-built generally in the context of creating prospect demos.  These features are often re-usable and, therefore, collected in this project.

Note that over time certain features may be removed from this project as a) they no longer work or b) they were integrated into Nuxeo platform.

Notice: We always build the `master` branch. For older version, check the misc. branches. For example, if you need the plugin for LTS2019 (10.10), switch to the 10.10 branch and build the plugin.

## What's Inside?

* [_nuxeo-labs-operations_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-operations)
    * Contains interesting operations for list management, also a redirect operation and an advanced email operation with an easier signature and cc/bcc/replyto configuration.
* [_nuxeo-labs-automation-helpers_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-automation-helpers)
    * Helpers for Automation and Automation Scripting
* [_nuxeo-labs-utils_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-utils-)
    * Somme utilities. Actually only one for now: An example of custom PageProvider handling an in-memory list of documents


## Build

Assuming [maven](http://maven.apache.org/) (3.6+) is installed on your system, after downloading the whole repository, execute the following:

```
cd /path/to/nuxeo-labs
mvn install
```

The MP is in `nuxeo-labs-mp/target`, named `nuxeo-labs-mp-{version}.zip`. It can be [installed from the Admin Center](http://doc.nuxeo.com/display/ADMINDOC/Installing+a+new+package+on+your+instance#InstallingaNewPackageonYourInstance-OfflineInstallation), or from the commandline using `nuxeoctl mp-install`.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
