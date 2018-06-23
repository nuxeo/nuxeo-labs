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

package org.nuxeo.labs.operations.images;

import java.io.IOException;
import java.util.Map.Entry;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;

/**
 * Receives a list of documents, outputs a blob, a JPEG Images Sheet. Uses a contributed custom commandLine where you are responsible of the parameters, passed as key=value text ist.
 *
 * @since 8.2
 */
@Operation(id = ImagesSheetBuilderCustomOp.ID, category = Constants.CAT_CONVERSION, label = "Custom Images Sheet Builder", description = "Build an image sheet from the input documents, using a custom contributed command line and its parameters. Use the thumbnail of documents that do not have the Picture facet. Outputs the resulting image (always a jpeg). See ImageMagick montage command line for details about the parameters")
public class ImagesSheetBuilderCustomOp {

    public static final String ID = "ImagesSheet.CustomBuild";

    @Param(name = "commandLine", required = true)
    protected String commandLine;

    @Param(name = "parameters", required = false)
    protected Properties parameters;

    @Param(name = "imageViewToUse", required = false, values = { ImagesSheetBuilder.DEFAULT_VIEW })
    protected String imageViewToUse;

    @Param(name = "useDocTitle", required = false, values = { "false" })
    protected boolean useDocTitle = false;

    @OperationMethod
    public Blob run(DocumentModelList input) throws NuxeoException, IOException, CommandNotAvailable {

        return run(input, null);
    }

    @OperationMethod
    public Blob run(BlobList input) throws NuxeoException, IOException, CommandNotAvailable {

        return run(null, input);
    }

    protected Blob run(DocumentModelList docs, BlobList blobs) throws NuxeoException, IOException, CommandNotAvailable {

        ImagesSheetBuilder isb = null;

        if (docs != null) {
            isb = new ImagesSheetBuilder(docs);
        } else if (blobs != null) {
            isb = new ImagesSheetBuilder(blobs);
        } else {
            return null;
        }

        isb.setCommand(commandLine)
           .setView(imageViewToUse)
           .setUseDocTitle(useDocTitle);

        CmdParameters moreParams = null;
        if(parameters != null && parameters.size() > 0) {
            moreParams = new CmdParameters();
            for(Entry<String, String> oneParam : parameters.entrySet()) {
                moreParams.addNamedParameter(oneParam.getKey(), oneParam.getValue());
            }
        }
        Blob result = isb.build(moreParams);

        return result;
    }

}
