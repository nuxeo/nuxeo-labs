# Nuxeo Labs
===

The Nuxeo Solution Architects team is constantly producing prototype solutions for our current and future customers. From time to time customers have a problem that is not solved by the core Nuxeo platform, so we build a custom solution to solve it. [Nuxeo Labs](https://github.com/nuxeo/nuxeo-labs) contains those custom solutions we feel may enjoy usage by a wider audience.

## Build

Assuming [maven](http://maven.apache.org/) (3.2.1) is installed on your system, after downloading the whole repository, execute the following:

```
cd /path/to/nuxeo-labs
mvn install
```

The MP is in `nuxeo-labs-mp/target`, named `nuxeo-labs-mp-{version}.zip`. It can be [installed from the Admin Center](http://doc.nuxeo.com/display/ADMINDOC/Installing+a+new+package+on+your+instance#InstallingaNewPackageonYourInstance-OfflineInstallation), or from the commandline using `nuxeoctl mp-install`.

We also plan to eventually make it available from the "Package from Nuxeo Marketplace" tab.

## Important Note

**These features are not part of the supported Production platform.**

Nuxeo does not support usage of this code as part of any Nuxeo Connect Subscription. This is a moving project (no API maintenance, no depreciation process, etc.). If any of these solutions are found to be useful for the Nuxeo Platform, they will be move into the next version of Nuxeo Platform.

These solutions are provided as examples and inspiration.

## What's Inside?

* [_nuxeo-labs-fancybox_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-fancybox)
    * In Studio define a layout, a callback Automation Chain and an XML Extension. You now have a [Fancybox](http://fancybox.net/) dialog, and in the automation chain, you get the values enetered by the user.
* [_nuxeo-labs-images_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-images)
  * Operations including convert, watermark, crop, etc. for images.
  * Crop toolbar button with UI.
* [_nuxeo-labs-operations_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-operations)
    * Contains interesting operations for list management and an advanced email operation with an easier signature and cc/bcc/replyto configuration.
* [_nuxeo-labs-signature_](https://github.com/nuxeo/nuxeo-labs/tree/master/nuxeo-labs-signature)
    * Provides an Automation operation that applies a digital signature to a PDF file.
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
  * [_resources_](https://github.com/nuxeo/nuxeo-labs/tree/master/resources)
      * Free-to-use resources for your Studio project (icons, background image, etc.)

## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information at <http://www.nuxeo.com/>
