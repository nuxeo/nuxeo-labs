/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     
 */

package org.nuxeo.labs.signature;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.signature.api.exception.SignException;
import org.nuxeo.ecm.platform.signature.api.sign.SignatureService;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author mvachette
 */
@Operation(id= SignPDF.ID, category=Constants.CAT_CONVERSION, label="Sign PDF", description="Applies a digital signature to the input PDF")
public class SignPDF {

    public static final String ID = "SignPDF";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected UserManager umgr;

    @Param(name = "username", required = true)
    protected String username;

    @Param(name = "password", required = true)
    protected String password;

    @Param(name = "reason", required = true)
    protected String reason;

    @OperationMethod
    public Blob run(Blob blob) throws ClientException, SignException {
        SignatureService service = Framework.getLocalService(SignatureService.class);
        DocumentModel user = umgr.getUserModel(username);
        return service.signPDF(blob,user,password,reason);
    }

}
