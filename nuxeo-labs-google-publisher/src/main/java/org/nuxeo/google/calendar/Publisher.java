/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Gena
 */
/**
 * 
 */

package org.nuxeo.google.calendar;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProvider;
import org.nuxeo.ecm.platform.oauth2.providers.OAuth2ServiceProviderRegistry;
import org.nuxeo.runtime.api.Framework;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * @author Michael Gena
 */
@Operation(id=Publisher.ID, category=Constants.CAT_NOTIFICATION, label="Publish on Google Calendar", description="This operation lets you publish an event to a Google Calendar. The <code>userEmailAddress</code> is the e-mail address of the user who authorized an offline access to his calendar.")
public class Publisher {

    public static final String ID = "GoogleCalendarPublisher";
    
    @Context
    protected OperationContext context;
    
    @Param(name = "userEmailAddress", required = true)
    String userEmailAddress = "";

    @Param(name = "summary", required = true)
    String summary = "";
    
    @Param(name = "location", required = false)
    String location = "";
    
    @Param(name = "description", required = false)
    String description = "";
    
    @Param(name = "startDate", required = true)
    GregorianCalendar startDate = new GregorianCalendar();
    
    @Param(name = "endDate", required = true)
    GregorianCalendar endDate = new GregorianCalendar();
    
    @Param(name = "attendeeEmailAddress", required = false)
    String attendeeEmailAddress = "";

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) {
		JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
		HttpTransport httpTransport;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			//Credential credential = new GoogleCredential().setAccessToken(getAccessToken(userEmailAddress));
			
			OAuth2ServiceProvider serviceProvider = Framework.getService(OAuth2ServiceProviderRegistry.class).getProvider("googledrive");
	        Credential storedCredential = serviceProvider.loadCredential(userEmailAddress);
			
			Credential credential = new GoogleCredential.Builder()
				    .setClientSecrets(serviceProvider.getClientId(), serviceProvider.getClientSecret())
				    .setJsonFactory(JSON_FACTORY).setTransport(httpTransport).build()
				    .setRefreshToken(storedCredential.getRefreshToken()).setAccessToken(storedCredential.getAccessToken());

	    	// Initialize Calendar service with valid OAuth credentials	    	
			Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential).build();
	    	// Create an Event and publish it
	    	Event event = new Event()
	    		    .setSummary(summary)
	    		    .setLocation(location)
	    		    .setDescription(description);
	    	
    		DateTime startDateTime = new DateTime(sdf.format(startDate.getTime()));
    		EventDateTime start = new EventDateTime().setDate(startDateTime);
    		event.setStart(start);
    		
       		//weird bug fix
    		endDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
    		
    		DateTime endDateTime = new DateTime(sdf.format(endDate.getTime()));
    		EventDateTime end = new EventDateTime().setDate(endDateTime);
    		event.setEnd(end);

    		if(attendeeEmailAddress != null && !"".equals(attendeeEmailAddress)){
	    		EventAttendee[] attendees = new EventAttendee[] {
	    		    new EventAttendee().setEmail(attendeeEmailAddress)
	    		};
	    		event.setAttendees(Arrays.asList(attendees));
    		}
    		String calendarId = "primary";
    		event = service.events().insert(calendarId, event).execute();	    	
		} catch (GeneralSecurityException | IOException e) {
			 throw new NuxeoException(e);
		}		    	
      return input; 
    }  
    
    protected String getAccessToken(String user) {
        OAuth2ServiceProvider serviceProvider = Framework.getService(OAuth2ServiceProviderRegistry.class).getProvider("googledrive");
        Credential credential = serviceProvider.loadCredential(user);
        if (credential != null) {
            String accessToken = credential.getAccessToken();
            if (accessToken != null) {
                return accessToken;
            }
        }
        return null;
    }
 
}