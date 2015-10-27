# nuxeo-labs-google-publisher
This plugin enables you to publish on Google Calendar and Google Tasks via nuxeo automation operations.


## Requirements

You must install `nuxeo-liveconnect` (available in the Marketplace) in order to use these features.

## Build

Assuming [maven](http://maven.apache.org/) (3.2.1) is installed on your system, after downloading the whole repository, execute the following:
```
mvn install
```

## Deploy

Upload the package marketplace generated under the folder nuxeo-google-publisher-mp/target/ into your local packages of the nuxeo platform and install it.

## Nuxeo Studio

Import the automation the 2 operations in your project registry. You will find a new entry under the automation tools:

**Notification>Publish on Google Calendar**

This operation expects multiple parameters:
- userEmailAddress: the user e-mail to whom an event will be created
- summary: the title of the event
- location: the location of the event
- description: the description of the event
- startDate: the start date of the event (currently only full days events are handled)
- endDate: the end date of the event
- attendeeEmailAddress: e-mail address of the attendee (currently only one attendee is handled)

**Notification>Publish on Google Task**

This operation expects multiple parameters:
- userEmailAddress: the user e-mail to whom an event will be created
- title: the title of the task
- note: the note of the task
- dueDate: the due date of the task

## Configuration in the Nuxeo platform

We use the OAuth2 authentification mechanism in order to do the publication on behalf of a user. In order to do that you need to have a google calendar api account with a client ID and Client Secret token. If you don't have one you have to create it here: https://console.developers.google.com/

Then you need to add some modification under Administrator>Cloud Services>Service Providers>googledrive
- Edit the Client ID and the Client Secret. Note that this is actually a workaround, the ideal solution would be to have a dedicated service provider named googlecalendar.

- Edit the scope part by adding "email,https://www.google.com/calendar/feeds/,https://www.googleapis.com/auth/tasks" in order that the user authorizes the offline access to his calendar and his tasks.

## Usage

Before launching the operation of publication, make sure that you go through the offline access authorization process, in order to have a valid token for the api. To do so, you need to go to Home>Cloud Services and Connect to Google Drive then accept the offline access to the user's calendar and his tasks.
That's it! Now you can run your publication operation, and you should be able to see that an event/task has been published on the calendar/tasks of the user.

## Important Note

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com/en/products/ep) and packaged applications for [document management](http://www.nuxeo.com/en/products/document-management), [digital asset management](http://www.nuxeo.com/en/products/dam) and [case management](http://www.nuxeo.com/en/products/case-management). Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information at <http://www.nuxeo.com/>
