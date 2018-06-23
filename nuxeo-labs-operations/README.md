# Description
This plugin contains miscellaneous operations. It was better to group them in this generic "nuxeo-labs-operation" plug-in, instead of creating one dedicated plug-in/operation

# List of Operations
* `Document > Add Complex Property From Json String` (id `AddComplexProperty`)
  * This operation can add new fields to a multivalued complex metadata. The `value` parameter is a String containing the JSON list of new values for the metadata given in `xpath`

* `Services > Vocabulary: Add Entry` (id `Vocabulary.AddEntry`)
  * Add a new entry to a vocabulary.
  * The operation only handles Nuxeo Vocabularies, which are specific types of Directories. It assumes the directory has the following fields `id`, `label`, `obsolete` and `ordering`. It it is a hierarchical vocabulary, it also assumes there is a `parent` field.
  * Parameters:
    * `name`: The name of the vocabulary, required
    * `id`: The id of the new entry, required
    * `label`: The label of of the new entry. If empty, the label is set to the id
    * `obsolete`: Integer/long, 0 or 1,  optional
    * `ordering`: Integer/long, optional
    * `parent`: The parent of of the new entry, in case the vocabulary is hierarchical (not use if it is not hierarchical)

* `Notification > AdvancedSendEmail` (id `AdvancedSendEmail`)
  * Send Email according parameters set.
  * EMail Address resolution is as following if you check `strict` :
    * If you set a string that starts with 'user:xxxx', will resolve email of username xxxx.
    * I you set a string that starts with 'group:xxxx, will resolve all users and add their email.
    * If none these 2, if the value contains an '@' it is handle as a full eMail address already formated.
    * If there is no match, then throw an exception.
  * If you uncheck the `strict box, you have the same behavior explainèd above, but the exception is not thrown and the plug-in handles the value as a username:
    * If no match then handles it as a group
    * and if no match again, then empty string
  * You can also put in values:
    * A NuxeoPrincipal
    * Or a list of NuxeoPrincipal
    * Or a list of Strings (resolution is the same as explain previously)
    * Or a list of mixed value
  * If `rollbackOnError` is checked, then error will be catch if one is thrown.
  * For the file value just put the `xpath` value of field that stores the file (`file:content`, `files:/files/0/content`, `myschema:myfield`, `myschema:/myfield/0/content`)

* `User Interface > Navigate To Url` (id: `NavigateToUrl`)
  * Redirects to the a nuxeo URL passed as a parameter, for instance the parameter can be: /nuxeo/site/automation/doc
  * The url must be a URL in the current server can't redirect to another website for example)

* `Services > HTTPlabs: Call` (id: `HTTP.Call`)
  * Sends a HTTP request, returns the result as a `StringBlob`
  * Parameters:
    * `method`: Required. The HTTP method to use: "GET", "POST", "PUT3, "DELETE", "OPTIONS" or "TRACE"
    * `url`: Required. The full URL to call, including any queryString, parameters, ... (must be already formated)
    * `headers`: A string, containing a list a `key=value`, separated with a newline, to setup the headers
    * `headersAsJSON`: A string containing a JSON object with the headers.
	* `body`: If not empty, `body` is sent along with the request
	* `blobToSend`: A Blob whose file is sent. *IMPORTANT*: You cannot have both a `body` and a `blobToSend`. The operation first checks `body`. If it is not empry, it is sent. If it is empty, the oprration checks `blobToSend` and sends it if not null
  * Returns a `StringBlob` whose data is a JSON string with the following fields:
    * `status`: The HTTP status code (200, 404, ...). 0 means an error occured during the call itself (before reaching the server)
    * `statusMessage`: The HTTP status message ("OK" for example)
    * `error`: The detailed error when reaching the server or parsing the result failed.
    * `result`: The raw data ans returned by the server.
  * Example of JavaScript Automation (_new since nuxeo 7.2_), getting a document from a distant nuxeo server:

```javascript
function run(input, params) {

  var resultStringBlob, headers, resultTxt, resultObj, msg, serverAndPort;

  try {

    headers = {
       "Authorization": "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y",
       "Accept": "application/json",
       "Content-Type": "application/json"
    };

    resultStringBlob = HTTPlabs.Call(input, {
      'method': "GET",
      'url': "http://YOUR_NUXEO_SERVER/nuxeo/api/v1//path//",
      'headersAsJSON': JSON.stringify(headers)
    });

    // If we are here, the call itself was succesful, lets get the results as an object
    resultTxt = resultStringBlob.getString();
    resultObj = JSON.parse(resultTxt);

    // Check the status. Here, we are expecting 200
    if(resultObj.status == 200) {
      Log(input, {
	'level': "warn",
	'message': "All good, the doc uid is: " + resultObj.result.uid,
      });

    } else {
      // The server returned an error
      msg = "Status: " + resultObj.status;
      msg += ", message: " + resultObj.statusMessage;
      msg += ", error: " + resultObj.error;
      Log(input, {
        'level': "warn",
         'message': "Some error occured: " + msg
      });
    }

  } catch(e) {
    // An error occured when reaching the server or parsing the result
    if(typeof resultStringBlob !== "undefined" && resultStringBlob != null) {
      resultTxt = resultStringBlob.getString();
      resultObj = JSON.parse(resultTxt);

      msg = "Status: " + resultObj.status;
      msg += ", message: " + resultObj.statusMessage;
      msg += ", error: " + resultObj.error;
    } else {
      msg = e;
    }
    Log(input, {
        'level': "warn",
         'message': "An error was catched during execution of the script: " + msg
    });
  }
}
```

* `Services > HTTPlabs: Download File` (id: `HTTP.DownloadFile`)
  * Sends a GET HTTP request, returns the result as a `FileBlob`
  * Parameters:
    * `url`: Required. The full URL to call, including any queryString, parameters, ... (must be already formated)
    * `headers`: A string, containing a list a `key=value`, separated with a newline, to setup the headers
    * `headersAsJSON`: A string containing a JSON object with the headers.
  * Returns a `FileBlob`
  * Example of JavaScript Automation (_new since nuxeo 7.2_), getting a file from a distant nuxeo server, saving the file to current document:

```
// Here, input is a File for example
function run(input, params) {
  var headers, blob;

  headers = {
    "Authorization": "Basic 34565pc3RyYXRvcjpBZG1pbmlzdHJhdG9y",
    "Accept": "*/*"
  };

  blob = HTTPlabs.DownloadFile(input, {
    'url': "your-server-url-to-the-file",
    'headersAsJSON': JSON.stringify(headers)
  });

  // Save the blob to the document
  input.setPropertyValue("file:content", blob);
  input = Document.Save(input, {});

  return input;
}
```

* `Document > Publish Rendition` (id: `Document.PublishRenditionOp`)
  * Publishes a rendition of the input document. See the Publicaiton Service for more details. To summarize:
    * Depending on the `renditionName`, the type of document created in the section will change (render a pdf => create a `File`,  render an image => create a `Picture`, ...)
    * In all cases, a version of the input document is created if needed
    *  The published document keeps a relation to its source document
  * Parameters:
    * `targetSectionRef`: ID or path (starting with "/") of the target section. **WARNING** Current user must have the permission to publish in this section
    * `renditionName`: The name of the rendition to use. Renditions are contributed using the RenditionService. Some default renditions exist ("pdf" for example)

* `Document > Document Exists` (id: `Document.Exists`)
  * Check if the document exists, returns a `boolean`
  * (No error is thrown if the document does not exist)
  * Parameters:
    * idOrPath: An ID or path (starting with "/") to test

* `Document > Get Tags` (id: `Document.GetTags`)
  * Accepts a Document/Documents
  * Returns all the tags of the document as a _sorted_ `List` of `String`. If the input is a list of documents, the duplicated tags (if any) are removed.
  * Parameters:
    * `currentUserOnly`: Optionnal If `true`, returns only the tags created by the current user. Default value is `false`.

    * `User Interface > Reset Content View` (id: `ResetContentView`)
      * Uses conentViewActions.reset() to reset a Content View.
      * Can be used to restore the default sort, for example.
      * Parameters:
        * `Content View Name`: The name of the Content View, required

* `Images Sheet Builder` (ID `ImagesSheet.Build`)
  * Receives a list of documents or a list of Blobs, outputs a blob, a JPEG Images Sheet. Different parameters allow to setup the output.
  * The operation uses ImageMagick `montage` command, so please refer to its documentation for details about the parameters. Typically, make sure to pass a correct format for hez `geometry` parameter for example`
  * `input`:
    * A list of Documents or a list of Blobs
    * When input is a list of Documents, then, for each document with the "Picture" facet, it uses a `PictureView` to generate the thumbnail in the sheet. By default, it uses the "Medium" view. If the view is not found, it uses the binary in "file:content".
    * If a document in the list does not have the "Picture" facet, or does not have a valid blob, the thumbnail is used
    * When input is a list o Blobs, it assumes they are images and will be handled by ImageMagick: Make sure you don't pass an incompatible blob.
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

* `Custom Images Sheet Builder` (ID `ImagesSheet.CustomBuild`)
  * Receives a list of documents or a list of Blobs, outputs a blob, a JPEG Images Sheet.
  * See "`Images Sheet Builder` (ID `ImagesSheet.Build`)" for a description of the input
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