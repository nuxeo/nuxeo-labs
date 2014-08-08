/**
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
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
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
    public Blob run(Blob blob) throws Exception {
        SignatureService service = Framework.getLocalService(SignatureService.class);
        DocumentModel user = umgr.getUserModel(username);
        return service.signPDF(blob,user,password,reason);
    }

}
