# Description
This plugin contains miscellaneous operations. It was better to group them in this generic "nuxeo-labs-operation" plug-in, instead of creating one dedicated plug-in/operation

# List of Operations 
* `Document > Add Complex Property From Json String` (id `AddComplexProperty`)
  * This operation can add new fields to a multivalued complex metadata. The `value` parameter is a String containing the JSON list of new values for the metadata given in `xpath`
* `Notification > AdvancedSendEmail` (id `AdvancedSendEmail`)
  * Send Email according parameters set.
  * EMail Address resolution is as following if you check `strict` : <ul>
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
  * Redirect to the a nuxeo URL passed as a parameter, for instance the parameter can be: /nuxeo/site/automation/doc
  * The url must be a URL in the current server can't redirect to another website for example)