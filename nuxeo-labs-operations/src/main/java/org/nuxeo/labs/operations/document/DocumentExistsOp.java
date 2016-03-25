/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Thibaud Arguillere
 */

package org.nuxeo.labs.operations.document;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;

/**
 * Check if the document exists, returns a boolean.
 * 
 * @since 8.1
 */
@Operation(id=DocumentExistsOp.ID, category=Constants.CAT_DOCUMENT, label="Document Exists", description="Returns true/false.")
public class DocumentExistsOp {

    public static final String ID = "Document.Exists";
    
    @Context
    protected CoreSession session;

    @Param(name = "idOrPath", required = true)
    protected String idOrPath;

    @OperationMethod
    public boolean run() {
        
        DocumentRef ref;
        if(idOrPath.startsWith("/")) {
            ref = new PathRef(idOrPath);
        } else {
            ref = new IdRef(idOrPath);
        }
        
        return session.exists(ref);
    }    

}
