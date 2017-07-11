# Description
This plugin contains miscellaneous utilities.

# List of Utilities

## StringList PageProvider
This PageProvider will return a DocumentModelList ordered in the same order as a StringList field.

### Usage
To use it, copy the definition in a Studio XML Extension, and set the `xpath` parameter to the StringList field you want to use. For example, here we named the page provider "pp_mystringlistfield" and used the `myshcema:mystringlistfield` field:

```
<extension target="org.nuxeo.ecm.platform.query.api.PageProviderService"
    point="providers">

    <genericPageProvider name="pp_mystringlistfield" class="org.nuxeo.labs.utils.StringListPageProvider">
      <property name="coreSession">#{documentManager}</property>
      <property name="currentDoc">#{currentDocument}</property>
        <!-- Put the xpath of your String Multivalued field here -->
        <!--  Create as many  -->
        <property name="xpath">myschema:mystringlistfield </property>
    </genericPageProvider>
</extension>
```

### Usage in a Content View
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