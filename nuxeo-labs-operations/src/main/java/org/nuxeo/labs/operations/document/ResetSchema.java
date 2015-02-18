package org.nuxeo.labs.operations.document;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DataModel;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.util.Map;

@Operation(id= ResetSchema.ID, category=Constants.CAT_DOCUMENT, label="Reset Schema", description="Reset all values")
public class ResetSchema {

    public static final String ID = "Document.ResetSchema";

    @Context
    protected OperationContext context;

    @Context
    protected CoreSession session;

    @Param(name = "schema", required = true)
    protected String schema;

    @OperationMethod
    public DocumentModel run(DocumentModel doc) {

        DataModel dm = doc.getDataModel(schema);
        
        if (dm==null) return doc;
        
        Map<String,Object> map = dm.getMap();
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            entry.setValue(null);
        }
        dm.setMap(map);
        
        return doc;
    }
}
