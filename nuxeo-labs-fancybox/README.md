# Description
This plugin provides helpers which make it super simple to display a form in a fancybox and pass the values to an automation chain.

# Using the plugin 
Displaying a form in a fancybox is as simple as adding a new action button. Check the provided sample. 

The value entered in the form by the user are available as a context variable in the automation chain: Context["data"]
This variable is an instance of the DocumentModel interface.

This project is not unit-tested, please use with care

#Known limitations
If the automation chain bound to the fancybox contains a "Download File" operation, the fancybox won't be closed automatically at the end of the process   
