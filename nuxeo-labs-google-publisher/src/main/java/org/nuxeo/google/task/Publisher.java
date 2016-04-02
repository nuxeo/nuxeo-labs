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

package org.nuxeo.google.task;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;

/**
 * @author Michael Gena
 */
@Operation(id=Publisher.ID, category=Constants.CAT_NOTIFICATION, label="Publish on Google Tasks", description="This operation lets you publish a task to a Google Tasks. The <code>userEmailAddress</code> is the e-mail address of the user who authorized an offline access to their tasks.")
public class Publisher {

    public static final String ID = "GoogleTaskPublisher";
    
    @Context
    protected OperationContext context;
    
    @Param(name = "userEmailAddress", required = true)
    String userEmailAddress = "";

    @Param(name = "title", required = true)
    String title = "";
    
    @Param(name = "notes", required = false)
    String notes = "";
    
    @Param(name = "dueDate", required = true)
    GregorianCalendar dueDate = new GregorianCalendar();
   
    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) {
		JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
		HttpTransport httpTransport;
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			
			OAuth2ServiceProvider serviceProvider = Framework.getLocalService(OAuth2ServiceProviderRegistry.class).getProvider("googledrive");
	        Credential storedCredential = serviceProvider.loadCredential(userEmailAddress);
			
			Credential credential = new GoogleCredential.Builder()
				    .setClientSecrets(serviceProvider.getClientId(), serviceProvider.getClientSecret())
				    .setJsonFactory(JSON_FACTORY).setTransport(httpTransport).build()
				    .setRefreshToken(storedCredential.getRefreshToken()).setAccessToken(storedCredential.getAccessToken());

	    	// Initialize Tasks service with valid OAuth credentials	    	
			Tasks service = new Tasks.Builder(httpTransport, JSON_FACTORY, credential).build();
			
			// Create a Task and publish it
			DateTime dueDateTime = new DateTime(dueDate.getTime());
    		
			Task task = new Task()
					.setTitle(title)
					.setNotes(notes)
					.setDue(dueDateTime);
			
    		task = service.tasks().insert("@default", task).execute();	    	
		} catch (GeneralSecurityException | IOException e) {
			 throw new NuxeoException(e);
		}		    	
      return input; 
    }  
 
}