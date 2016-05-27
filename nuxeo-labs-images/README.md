# nuxeo-labs-images
A project to manage Image manipulation through automation or action(s).

This package contains:
* [Converters](#converters)
  * A generic converter using ImageMagick
  * Specific opêrations, which use this converter: Watermark, Crop, ...
* [Operation](#operations) to build an Images Sheet from a list of documents
* [A "Crop" Toolbar Button](#crop-toolbar-button), which displays a dialog letting the user to crop the picture embedded in the current document.

## Using the plugin

* **General Information**

  As of today, each picture operation uses **ImageMagick** to perform its task. Which means ImageMagick must be installed on the server. That said, it is likely already installed, since it is used by nuxeo (thumbnail generation for example.)

* **Converters**
  * The project creates new operations that can be declared in Nuxeo Studio, so they are available in the list of operations in the Automation Chains.
  * To declare the operations in Studio, just add their JSON definition in the "Automation Operations" registry (see [this documentation](http://doc.nuxeo.com/display/Studio/Referencing+an+Externally+Defined+Operation)).
* **Crop Toolbar Button**
  * Automatically displayed when the current document has an image
  * If you don't want to display this button, you can just add an XML extension in you project (see below)



## Converters

All the operations are istalled in the `Conversion` topic. Every conversion operation uses the `GenericConverter`. This `GenericConverter`itself can be easily extended via XML declarations, so you can add your own converters using ImageMagick without the need to write a Java plug-in. All other operaitons are mainly helpers, to make it easier to watermark, crop, or convert an image.

Also, each operation is documented: In Studio, when you select the operation, it gives details about its parameters.

* **Common parameters**. Some operations use the `targetFileName` and `targetFileNameSuffix` parameters. Here is how to use them:
  * If `targetFileName` is not provided, the output blob filename will be the same as input blob (possibly with another extension - in case of format change for example).
  * f `targetFileSuffix` is used, it will be added to the target file name, *before* the extension. So, for example, if the destination file name is `mypict.jpg` and the suffix is `-copy`, the target file name will be `mypict-copy.jpg`.

* **`Conversion > Image: Change Format`** (operation ID `ImageChangeFormat`)

  Receives a `blob` (picture binary) as input, converts it to another format (`format` parameter), and returns the converted `blob`. See [ImageMagick documentation](http://www.imagemagick.org/script/formats.php) to check the available formats

* **`Conversion > Image: Crop`** (operation ID `ImageCrop`)
  * Receives a `blob` (picture binary) as input, crops it using the parameters, returns the cropped `blob`
  * Parameters:
    * `top`, `left`, `widht`, `height` define the position and size of the crop
      * *Notice*: If `width` or `height` is 0, nothing is done, the input blob is returned unchanged
    * `pictureWidth` and `pictureHeight` are the size (in pixels) of the picture used for cropping. The coordinates of the crop will be scaled to fit the original picture size. So for example,
      * If...
        * The original picture is 4000x2000,
        * The picture used for cropping (the input blob) is 1000x500 (4 time smaller),
        * And the crop is 10, 10, 200, 200
      * ... then the final crop on the original picture will be 40, 40, 800, 800, so there will be no changes for the user.

* **`Conversion > Image: Crop and Save in Document`** (operation ID `ImageCropInDocument`)
  * Receives a Document as input. Gets it's main file (`file:content`) and crops it (see `ImageCrop`)
  * The cropped image becomes the new main file of the Document.
  * The optionnal `incrementVersion` parameter makes it possible to increment the version of the document *before* replacing the main file.
  
* **`Conversion > Image: Watermark with Text`** (operation ID `ImageWatermarkWithText`)
  * Receives a `blob` (picture binary) as input, adds the watermark using the parameters, returns the watermarked `blob`.
  * There are many parameters, all optionnal but the `textValue`:
    * `textValue`
    * `gravity`: Default value `SouthWest`
    * `textColor`: Default value `Red`
    * `textSize`: Default value `24`
    * `strokeColor`: Default value `#A84100`
    * `strokeWidth`: Default value `1`
    * `textRotation`: Default value `0`
    * `xOffset`: Default value `0`
    * `yOffset`: Default value `0`

* **`Conversion > GenericConverter`**
  * As its name states, this converter is generic. Which means that you must tell it what converter to use, and which parameters to inject
  * The parameters are:
    * `converterName`: The exact name (case sensitive) as the one you declared in Studio or of one of the converters provoided by this plug-in
    * `parameters`: The parameters of the command line to pass to the operation (the command line can be found in the declaration of the converters)
  * Have a look at `/main+resources/OSGI-INF/extensions/conversions.xml`. Basically, you must:
    1. Declare a `converter` using a `commandLine`
    2. Declare this `CommandLine`, where you use parameters that will be passed to the command line
  * So in Studio, you can declare a [new XML Extension](http://doc.nuxeo.com/display/NXDOC/Contributing+to+an+Extension+Using+Nuxeo+Studio) and fill it with your converter and your command line. Important point is to use the `org.nuxeo.labs.images.GenericImageMagickConverter` in your converter:
  ```
<!-- Declare the "MyConverter" converter which uses the "MyCommandLine" command line -->
<extension target="org.nuxeo.ecm.core.convert.service.ConversionServiceImpl"
  point="converter">
  <converter name="MyConverter" class="org.nuxeo.labs.images.GenericImageMagickConverter">
    <parameters>
      <parameter name="CommandLineName">MyCommandLine</parameter>
    </parameters>
  </converter>
</extension>
<!-- Declare the MyCommandLine command line -->
<extension target="org.nuxeo.ecm.platform.commandline.executor.service.CommandLineExecutorComponent"
  point="command">
  <command name="MyCommandLine" enabled="true">
    <!-- The Command to use. Here, we use "convert", form ImageMagick -->
    <commandLine>convert</commandLine>
    <!-- The parameters to pass to the command line.
         Here, we use the conversion, from jpg to png for example
         The expected parameter is the format -->
    <parameterString>#{sourceFilePath} #{targetFilePath}.#{theFormat}</parameterString>
    <!-- This one is good habit. So server.log will let you know you have a problem
         if ImageMagick is not avilable -->
    <installationDirective>You need to install ImageMagick.</installationDirective>
  </command>
</extension>
  ```
    * Now, you can use the `Conversion > GenericConverter` operation with the following parameters:
      * `converterName`: `MyConverter`
      * `parameters`: `theFormat=png`
    * **IMPORTANT**: The names of the `sourceFilePath` and `targetFilePath` must not be modified, these names are hard-coded in the converter.
        * `sourceFilePath` is provided by the converter (using the input blob), so you don't have to handle it. Don't use it in the parameters
        * `targetFilePath` is misnamed. It actually should be "targetFileName, because it must be filled with the name of the resulting file, including the extension.
        

## Operations

* `Images Sheet from Documents` (ID `ImagesSheet.Build`)
  * Receives a list of documents, outputs a blob, a JPEG Images Sheet. Different parameters allow to setup the output.
  * The operation uses ImageMagick `montage` command, so please refer to its documentation for details about the parameters. Typically, make sure to pass a correct format for hez `geometry` parameter for example`
  * `input`:
    * A list of Documents
    * For each document with the "Picture" facet, it uses a `PictureView` to generate the thumbnail in the sheet. By default, it uses the "Medium" view. If the view is not found, it uses the binary in "file:content".
    * If a document in the list does not have the "Picture" facet, or does not have a valid blob, the thumbnail is used
  * `output`: A `Blob`, holding a JPEG image
  * **Parameters**
    * `tile`: Number of thumbs/row or columns. defauit value is "0" (ImageMagick organizes the sheet). If you want 4 thumbs per row, just pass "4"
      * **WARNING** The `montage` command may return several images. For example, if you ask for 5 thumbnails per row and colums and you have 100 images, it will output 4 images (5 x 5 = 25 thumbnails per image), and this will lead to an error. Make sure to return only one image
    * `label`: A pattern as expected by ImageMagick. Typically, you would pass "%f" (default value)n with sers the label of each thumb to the filename. Notice that is `useDocTitle` is `true`, it is the Document’s title that is used. If `label` is set to `"NO_LABEL"` then the lables are not displayed at all
    * `backgroundColor`
      * Default value: "white"
    * `fillColor`: Color used to draw the labels.
      * Default value: "black"
    * `font`
      * Default value:s "Helvetica"
    * `fontSize`
      * Default value: 12
    * `define`: **important parameter**
      * When ImageMagick builds the sheet, it keeps all the images in memory. If the list has a lot of big images, it will likely fail
      * This parameters sets the maximum dimension of an image
      * It is highly recommended to use this parameter
      * Default value is "jpeg:size=150x150>"
    * `geometry`: The dimensions of each thumb
      * Default value: "150x150>+20+20", so a rectangle of 150 pixels, with a margin of 20 pixels in each direction. The ">" character tells ImageMagick to not enlarge the image if it is already smaller than the rectangle.
    * `imageViewToUse`: The `PictureView`to use. "Medium" by default. If the plug-in does not find a blob for this view, it uses the binary stored in "file:content".
    * `useDocTitle`: If `true`, instead of using the filename, the labels will display the title of the Nuxeo Document. Default value is `false`
    * `command`: A commadLine you contributed.
      * Maybe the default command does not fit all your needs, so you can contribute you own.
      * **Important** see the "IM-montage" command line contributed in "conversions.xml" two parameters are required, _must be_ the 2 last parameters and _must be_ exactly `@#{listFilePath} #{targetFilePath}` (notice the `@`). Other parameters can be used, just remember they are case sensitive.

* `Custom Images Sheet from Documents` (ID `ImagesSheet.CustomBuild`)
  * Receives a list of documents, outputs a blob, a JPEG Images Sheet.
  * Uses a custom commandLine contribution (you probably declared it in the "XML Extensions" part of Studio) and the parameters to use. See, in this plug-in, the "IM-montage" in resourse/OSGI-INF/extenions/converters.xml
  * **Important**: Two parameters are required:
    * _must be_ the 2 last parameters
    * and _must be_ exactly `@#{listFilePath} #{targetFilePath}` (notice the `@`).
    * Do not use them in the list of parameters you pass to the operation
  * **Parameters**
    * `commandLine`: The name of the command line you contributed in an XML extension
    * `parameters`: A `key=value` list in a text (each key-value pair on its single line)
      * The key must be exactly the same as the parameter you iused in the definition of the commed line
      * For example, if, in your command, you have the expression `#{font}` and `#{fontSize}`, your `parameters` would look like:
      
      ```
      font=Arial
      fontSize=14
      . . . other parameters . . .
      ```
    * `imageViewToUse`: The `PictureView`to use. "Medium" by default. If the plug-in does not find a blob for this view, it uses the binary stored in "file:content".
    * `useDocTitle`: If `true`, instead of using the filename, the labels will display the title of the Nuxeo Document. Default value is `false`
    * **WARNING**: See the warning about the `tile` parameter for the `Images Sheet from Documents` operation.
    

## Crop Toolbar Button

This button displays a "Crop" button in the toolbar for documents having the `Picture` facet. A dialog lets the user to crop the picture using a selection rectangle and the cropped image replaces the existing one.

*Note*: The action declares a `filter` so it will not be displayed if the user does not have *write* access to the document, or if the document cannot be modified (it is a verison for example).

If you don't want to display this button, you can disable it by adding the following [XML Extension](http://doc.nuxeo.com/display/NXDOC/Contributing+to+an+Extension+Using+Nuxeo+Studio):

```
<require>org.nuxeo.ecm.platform.actions</require>
<extension target="org.nuxeo.ecm.platform.actions.ActionService"
    point="actions">
  <action id="cropImageAction" enabled="false" />
</extension>
```


## Third Party Tools Used
 * **Jcrop**<br/>
  Jcrop (http://deepliquid.com/content/Jcrop.html) is used in the User Interface for cropping an image

## Notes
This project needs ImageMagick to work

Only part oif this project is unit-tested, please use with care

