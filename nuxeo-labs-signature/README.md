# Description
This plugin provides an automation operation which applies a digital signature to an input pdf file

# Using the plugin 
- Install the Digital signature plugin on your nuxeo instance:
    https://connect.nuxeo.com/nuxeo/site/marketplace/package/nuxeo-signature
    or get the sources at https://github.com/nuxeo/nuxeo-signature

- Configure the root certificate and create user certificates (see http://doc.nuxeo.com/x/8431)

- Have a look a the unit test to see how to use the SignPDF operation directly in code

- If you are a Studio user, first import the operation signature in your project

- The operation takes three mandatory parameters:
    - the username to use for the signature
    - the user certificate password
    - a comment 

#Known limitations
N/A