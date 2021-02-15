package org.nuxeo.labs.operations.blob;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.blob.BlobInfo;
import org.nuxeo.ecm.core.blob.BlobManager;
import org.nuxeo.ecm.core.blob.BlobProvider;

import java.io.IOException;

@Operation(id = GetManagedBlobOp.ID, category = Constants.CAT_BLOB, label = "Get Managed Blob", description = "Returns a managed blob corresponding to the provided key")
public class GetManagedBlobOp {

    public static final String ID = "GetManagedBlob";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Context
    protected BlobManager blobManager;

    @Param(name = "providerName")
    protected String providerName;

    @Param(name = "key")
    protected String key;

    @Param(name = "mimetype")
    protected String mimetype;

    @Param(name = "filename")
    protected String filename;

    @Param(name = "length")
    protected long length;

    @OperationMethod
    public Blob run() throws IOException {
        BlobProvider myProvider = blobManager.getBlobProvider(providerName);
        BlobInfo blobInfo = new BlobInfo();
        blobInfo.key = key;
        blobInfo.mimeType = mimetype;
        blobInfo.filename = filename;
        blobInfo.length = length;
        return myProvider.readBlob(blobInfo);
    }
}