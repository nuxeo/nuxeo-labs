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
import org.nuxeo.ecm.core.api.DocumentModelList;

@Operation(id=CopySchema.ID, category=Constants.CAT_DOCUMENT, label="Copy Schema", description="Copy all the info in the schema of the source to the input document.")
public class CopySchema {

    public static final String ID = "Document.CopySchema";

    @Context
    protected OperationContext context;

    @Context
    protected CoreSession session;

    @Param(name = "source", required = false)
    protected DocumentModel source;

    @Param(name = "schema", required = true)
    protected String schema;

    @OperationMethod
         public DocumentModel run(DocumentModel docToUpdate) {

        if (source==null) {
            source = (DocumentModel) context.get("request");
        }

        DataModel model = source.getDataModel(schema);
        if (model!=null) {

            DataModel targetDM = docToUpdate.getDataModel(schema);
            if (targetDM!=null) {
                // explicitly set values so that the dirty flags are set !
                targetDM.setMap(model.getMap());
            }
        }

        return docToUpdate;
    }


    @OperationMethod
    public DocumentModelList run(DocumentModelList docs) {
        if (source==null) source = (DocumentModel) context.get("request");
        DataModel model = source.getDataModel(schema);
        if (model!=null) {
            for (DocumentModel doc : docs) {
                DataModel targetDM = doc.getDataModel(schema);
                if (targetDM != null) {
                    // explicitly set values so that the dirty flags are set !
                    targetDM.setMap(model.getMap());
                }
            }
        }
        return docs;
    }
    
}
