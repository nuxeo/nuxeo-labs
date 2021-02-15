# nuxeo-labs-automation-helpers

This plug-in adds [Scripting Automation Helpers](https://doc.nuxeo.com/x/RIB4AQ). Useful (hopefully) functions that can be used in your Automation Chains. They have been written and tested with Automation _Scripting_ (hence, JavaScript) in mind.

Because they are custom helpers, you will not have autocompletion in Nuxeo Studio, this is normal. You must type the whole ID ot the helper (`FileUtils.saveBlob` for example), case sentitive.

**Security Concerns**: These are _helpers_, they can't be called directly from a REST call for example. Still, you should pay a lot of attention to avoid publishing an Automation Chain which, for example, accepts a dynamic parameter used as a full path server side. This is very bad practice and major security issue.

## Helpers

### FileUtils
A set of helpers to handle files on disk (server side, we are in Automation): Create, load, write, delete. In all cases, you must make sure you have enough right to access the file system server side (helpers are ran server-side, as a "nuxeo" user on Linux, typically, for example).

The original purpose of the `FileUtils` helpers was to be able to quickly create/handle demo data from JavaScript Automation, just creating a few hundred of documents and avoiding creating a Java plug-in, the marketplace package, installing in the test server, etc.

#### VERY IMPORTANT WARNINGS ABOUT SECURITY
* _The helpers_, by essence, _run server side_ of course. And some helpers here can create/write/delete files and/or folders<br/>=> **MAKE SURE YOU DON't ALLOW EXTERNAL CALLS TO ACCESS FILES/FOLDERS OF YOUR SERVER**.
* A helper cannot be called directly by itself, it must be used inside an operation, inside an Automation Chain.
* So: please **BE VERY CAREFUL, and hard code your values, and/or make sure the paths cannot be get/set from a REST call**.

A typical example of very, very wrong way of using these helpers would be a chain that accepts a "path" parameter, and call FileUtils.deleteFile() with this path for example. Just don't do that please.

Here is the list of `FileUtils` helpers:

* `FileUtils.readFileToBlob(path)`
  * Loads the whole content of the file and return the corresponding blob
* `FileUtils.readFileToText(path)`
  * Loads the whole content of the file and returns it as String
  * Caller is responsible for making sure it can be read as a String( don't try with a JPEG, you'll have a corrupt string with NULL chars in it)
* `FileUtils.createFile(path, overwrite)`
  * Creates an empty file at `path`
  * Returns an object (a `File` actually) that can be used with other helpers
  * If a file already exists:
    * If `overwrite` is `false`, the helper returns `null` (and no error is thrown)
    * Else, the existing file is overriden
* `FileUtils.createFile(path)`
  * A shortcut for  `FileUtils.createFile(path, false);`
* `FileUtils.appendToFile(aFile, what)`
  * Appends the string to the file.
  * `aFile` is not a string (full path for example), it is a `File` object, as returned by `FileUtils.createFile(path, overwrite)`.
* `FileUtils.deleteFile(path)`
  * Deletes the file located at full path `path`.
* `FileUtils.deleteFile(aFile)`
  * Deletes `aFile`.
  * Warning: `aFile` is not a string (full path for example), it is a `File` object, as returned by `createFie()` or `appendToFile()` for example.
* `FileUtils.getFiles(path)`
  * Returns an array of the full paths of all the non-hidden/non folder elements found at `path`
  * (first level, non recursive)
* `FileUtils.getFolders(path)`
  * Returns an array of the full paths of all the non-hidden folder elements found at `path`
  * (first level, non recursive)
* `FileUtils.createFolder(where, name)`
  * Creates a new folder at `where`, named `name`
  * Returns true if the folder was created, false otherwise
* `FileUtils.saveBlob(Blob blob, path)`
  * Writes the blob on diak
  * Example of use: Writing a blog extracted for a document.
  
```
var doc, blob;
// Getting a Document by its path
doc = Repository.GetDocument(null, {"value": "/default-domain/mydoc.png"});
// Get the blob
blob = doc["file:fontent"];
// Save it
FileUtils.saveBlob(blob, "/tmp/something/" + blob.getFilename());
```


# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


# Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)


# About Nuxeo

[Nuxeo](www.nuxeo.com), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.
