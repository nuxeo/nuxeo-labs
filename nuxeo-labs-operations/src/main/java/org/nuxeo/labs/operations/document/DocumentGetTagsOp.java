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

import java.util.Set;
import java.util.TreeSet;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
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
    public Set<String> run(DocumentModel input) {
        
        Set<String> result = tagService.getTags(session, input.getId());        
        return result;
        
    }
    
    @OperationMethod
    public Set<String> run(DocumentModelList input) {
        
        Set<String> tags = new TreeSet<>();
        Set<String> tempTags;
        for(DocumentModel doc : input) {
            tempTags = tagService.getTags(session, doc.getId());
            // Merge while removing duplicates
            tags.addAll(tempTags);
        }
        
        return tags;
        
    }

}
