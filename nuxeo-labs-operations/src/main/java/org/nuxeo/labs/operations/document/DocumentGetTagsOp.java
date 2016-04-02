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
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.operations.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;

/**
 * 
 * @since 8.1
 */
@Operation(id = DocumentGetTagsOp.ID, category = Constants.CAT_DOCUMENT, label = "Get tags", description = "Returns the tags of the document (sorted")
public class DocumentGetTagsOp {

    public static final String ID = "Document.GetTags";

    @Context
    protected CoreSession session;
    
    @Context
    protected TagService tagService;

    @Param(name = "currentUserOnly", required = false)
    protected boolean currentUserOnly = false;

    @OperationMethod
    public ArrayList<String> run(DocumentModel input) {
        
        String userName = null;
        
        if(currentUserOnly) {
            userName = getOriginatingUserOrCurrentUser(session);
        }
        
        List<Tag> tags = tagService.getDocumentTags(session, input.getId(), userName);
        ArrayList<String> result = new ArrayList<String>();
        for(Tag tag : tags) {
            result.add(tag.getLabel());
        }
        Collections.sort(result);
        
        return result;
        
    }
    
    @OperationMethod
    public ArrayList<String> run(DocumentModelList input) {
        
        String userName = null;
        
        if(currentUserOnly) {
            userName = getOriginatingUserOrCurrentUser(session);
        }
        
        List<Tag> tags = new ArrayList<Tag>();
        List<Tag> tempTags;
        for(DocumentModel doc : input) {
            tempTags = tagService.getDocumentTags(session, doc.getId(), userName);
            // Merge while removing duplicates
            tags.removeAll(tempTags);
            tags.addAll(tempTags);
        }
        
        ArrayList<String> result = new ArrayList<String>();
        for(Tag tag : tags) {
            result.add(tag.getLabel());
        }
        Collections.sort(result);
        
        return result;
        
    }
    
    protected String getOriginatingUserOrCurrentUser(CoreSession inSession) {
        String userName = null;
        NuxeoPrincipal nxPcipal = (NuxeoPrincipal) inSession.getPrincipal();
        if (nxPcipal != null && nxPcipal.getOriginatingUser() != null) {
            userName = nxPcipal.getOriginatingUser();
        } else {
            userName = inSession.getPrincipal().getName();
        }

        return userName;
    }

}
