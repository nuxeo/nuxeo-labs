# Nuxeo Labs

Nuxeo Labs is a collection of features developed by the Nuxeo Solution Engineering team. These features are purpose-built generally in the context of creating prospect demos.  These features are often re-usable and, therefore, collected in this project.

Note that over time certain features may be removed from this project as a) they no longer work or b) they were integrated into Nuxeo proper.

## Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

## Build

Assuming [maven](http://maven.apache.org/) (3.2.1) is installed on your system, after downloading the whole repository, execute the following:

```
cd /path/to/nuxeo-labs
mvn install
```

Build Status: [![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=master/nuxeo-labs-master)](https://qa.nuxeo.org/jenkins/job/master/job/nuxeo-labs-master/)

The MP is in `nuxeo-labs-mp/target`, named `nuxeo-labs-mp-{version}.zip`. It can be [installed from the Admin Center](http://doc.nuxeo.com/display/ADMINDOC/Installing+a+new+package+on+your+instance#InstallingaNewPackageonYourInstance-OfflineInstallation), or from the commandline using `nuxeoctl mp-install`.

We also plan to eventually make it available from the "Package from Nuxeo Marketplace" tab.

## What's Inside?

* [_nuxeo-labs-fancybox_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-fancybox)
    * In Studio define a layout, a callback Automation Chain and an XML Extension. You now have a [Fancybox](http://fancybox.net/) dialog, and in the automation chain, you get the values entered by the user.
    * **WARNING: This is a feature for the JSF UI**
* [_nuxeo-labs-google-publisher_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-google-publisher)
      * This plugin enables you to publish on Google Calendar and Tasks through a nuxeo automation operation.
* [_nuxeo-labs-images_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-images)
  * Operations including convert, watermark, crop, etc. for images.
  * Crop toolbar button with UI.
* [_nuxeo-labs-operations_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-operations)
    * Contains interesting operations for list management, also a redirect operation and an advanced email operation with an easier signature and cc/bcc/replyto configuration.
* [_nuxeo-labs-automation-helpers_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-automation-helpers)
    * Helpers for Automation and Automation Scripting
* [_nuxeo-labs-template-rendition-publisher_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-template-rendition-publisher)
    * Provides an Automation operation that enables publishing a template rendition. You can use this operation with the `nuxeo-template-rendering` plug-in.
* [_nuxeo-labs-video_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-video)
    * Helper class (using `MediaInfoHelper`) to call `mediainfo -i` on video blobs and parse the result.
      * With operations you can use in your chains
* [_nuxeo-labs-we-publication_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-we-publication)
    * Displays the `Sections` as a cool webengine application
* Others:
  * [_nuxeo-labs-dam-default-tab_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-dam-default-tab)
    *  Overrides the default startup helper to select the DAM view when a user logs in to the application
        *  *NOTE*: It is not included in the Marketplace Package and its .jar must be installed manually (typically in the `plugins` folder of `nxserver`).
    *  **WARNING: This is a feature for the JSF UI**
  * [_resources_](https://github.com/nuxeo/nuxeo-labs/tree/master/resources)
      * Free-to-use resources for your Studio project (icons, background image, etc.)
  * [nuxeo-labs-widgets](https://github.com/nuxeo-sandbox/nuxeo-labs-studio)
  	* Widget templates to be used in the platform; moved to [https://github.com/nuxeo-sandbox/nuxeo-labs-studio](https://github.com/nuxeo-sandbox/nuxeo-labs-studio)
  	* **WARNING: Most, if not all, widgets areto be used within the JSF UI**

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).
