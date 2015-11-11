/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
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
@Operation(id=GetLastDocumentVersion.ID, category=Constants.CAT_DOCUMENT, label="Get Last version", description="Returns the last version of the document. If there is no version and create is true, creates a version and returns it, else returns null. Important: If a version must be created, the document is saved.")
public class GetLastDocumentVersion {

    public static final String ID = "Document.GetLastVersion";
    
    @Context
    protected CoreSession session;

    @Param(name = "createIfNeeded", required = false)
    protected boolean createIfNeeded = false;

    @Param(name = "increment", required = false, widget = Constants.W_OPTION, values = { "Minor", "Major" })
    protected String snapshot = "Minor";

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) {
        
        IdRef idRef = new IdRef(input.getId());
        DocumentModel lastVersion = session.getLastDocumentVersion(idRef);
        
        if(lastVersion == null && input.hasFacet(FacetNames.VERSIONABLE) && createIfNeeded) {
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
