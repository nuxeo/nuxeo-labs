/**
 *
 */

package org.nuxeo.labs.operations.document;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

/**
 * @author fvadon
 */
@Operation(id=UnpublishDocument.ID, category=Constants.CAT_DOCUMENT, label="Unpublish Document", description="Will remove all proxies of the input document, make sure proxies are not used for something else than publishing")
public class UnpublishDocument {

    public static final String ID = "UnpublishDocument";
    @Context
    protected CoreSession session;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) {
      DocumentModelList proxies = session.getProxies(input.getRef(), null);
      for(DocumentModel proxy: proxies){
          session.removeDocument(proxy.getRef());
      }
      session.save();
    return null;
    }

}
