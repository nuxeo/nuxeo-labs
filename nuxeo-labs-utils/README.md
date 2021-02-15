# Description
This plugin contains miscellaneous utilities.

# List of Utilities

## StringList PageProvider
This PageProvider will return a `DocumentModelList` ordered in the same order as a StringList field.

### Usage
To use it, copy the definition in a Studio XML Extension, and set the `xpath` parameter to the StringList field you want to use. For example, here we named the page provider "pp_mystringlistfield" and used the `mysschema:myStringListField` field:

```
<extension target="org.nuxeo.ecm.platform.query.api.PageProviderService"
    point="providers">

    <genericPageProvider name="pp_mystringlistfield" class="org.nuxeo.labs.utils.StringListPageProvider">
      <property name="coreSession">#{documentManager}</property>
      <property name="currentDoc">#{currentDocument}</property>
        <!-- Put the xpath of your String Multivalued field here -->
        <!--  Create as many  -->
        <property name="xpath">myschema:myStringListField </property>
    </genericPageProvider>
</extension>
```

### Usage in a Content View

**WARNING: This topic is for JSF UI only**

Now, if you want to use it in a content view, how would you do it? This cannot be done in the "Content-Views" part of Studio, but is easy to do:

1. Create your content view in Studio as usual, just ignore the NXQL filer, it will not be used anyway. Say you named it "MyContentView"
2. Add an XML extension that overrides it and use the page provider:

In this example, we used a table layouts for the result ("AssetListing_NoCheckboxes"), you will adapt to your content-view

```
<extension point="contentViews" target="org.nuxeo.ecm.platform.ui.web.ContentViewService">
  <contentView name="MyContentView">
    <!-- Overriding the page provider to use the custom one -->
    <genericPageProvider name="pp_mystringlistfield" class="org.nuxeo.labs.utils.StringListPageProvider">
      <property name="coreSession">#{documentManager}</property>
      <property name="currentDoc">#{currentDocument}</property>
        <property name="xpath">myschema:mystringlistfield </property>
    </genericPageProvider>
    
    <!-- Now, the result layouts. Thumbnails first in this example -->
    <resultLayouts>
      <layout name="document_listing_thumbnail" title="document_thumbnail_listing" translateTitle="true" iconPath="/icons/document_listing_icon_2_columns_icon.png" showSpreadsheet="true" showEditRows="true"/>
      <layout name="AssetListing_NoCheckboxes" title="document_listing" translateTitle="true" iconPath="/icons/document_listing_icon.png" showCSVExport="true" showEditColumns="true" showSpreadsheet="true"/>
    </resultLayouts>
  </contentView>
</extension>
```

### Need to Use it with Different Fields?
To use different page providers with different fields, create as many XML extension and just change the name of the provider and the xpath.

# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


# About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.
