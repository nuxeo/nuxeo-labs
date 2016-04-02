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
@Operation(id = DocumentExistsOp.ID, category = Constants.CAT_DOCUMENT, label = "Document Exists", description = "Returns true/false.")
public class DocumentExistsOp {

    public static final String ID = "Document.Exists";

    @Context
    protected CoreSession session;

    @Param(name = "idOrPath", required = true)
    protected String idOrPath;

    @OperationMethod
    public boolean run() {

        DocumentRef ref;
        if (idOrPath.startsWith("/")) {
            ref = new PathRef(idOrPath);
        } else {
            ref = new IdRef(idOrPath);
        }

        return session.exists(ref);
    }

}
