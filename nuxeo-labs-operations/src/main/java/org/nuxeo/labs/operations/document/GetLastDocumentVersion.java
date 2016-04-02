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
 *     thibaud
 */

package org.nuxeo.labs.operations.document;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.versioning.VersioningService;

/**
 * 
 */
@Operation(id = GetLastDocumentVersion.ID, category = Constants.CAT_DOCUMENT, label = "Get Last version", description = "Returns the last version of the document. If there is no version and create is true, creates a version and returns it, else returns null. Important: If a version must be created, the document is saved.")
public class GetLastDocumentVersion {

    public static final String ID = "Document.GetLastVersion";

    @Context
    protected CoreSession session;

    @Param(name = "createIfNeeded", required = false)
    protected boolean createIfNeeded = false;

    @Param(name = "increment", required = false, widget = Constants.W_OPTION, values = { "Minor", "Major" })
    protected String snapshot = "Minor";

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) {

        IdRef idRef = new IdRef(input.getId());
        DocumentModel lastVersion = session.getLastDocumentVersion(idRef);

        if (lastVersion == null && input.hasFacet(FacetNames.VERSIONABLE) && createIfNeeded) {
            VersioningOption vo;
            if ("Minor".equalsIgnoreCase(snapshot)) {
                vo = VersioningOption.MINOR;
            } else if ("Major".equalsIgnoreCase(snapshot)) {
                vo = VersioningOption.MAJOR;
            } else {
                vo = null;
            }
            if (vo != null) {
                input.putContextData(VersioningService.VERSIONING_OPTION, vo);
                input = session.saveDocument(input);
                lastVersion = session.getLastDocumentVersion(idRef);
            }
        }

        return lastVersion;
    }

}
