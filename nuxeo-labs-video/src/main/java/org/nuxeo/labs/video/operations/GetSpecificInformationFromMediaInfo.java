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
 *     Frédéric Vadon
 */
package org.nuxeo.labs.video.operations;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.labs.video.mediainfo.MediaInfoHelper;

/**
 * @author fvadon
 */
@Operation(id = GetSpecificInformationFromMediaInfo.ID, category = Constants.CAT_BLOB, label = "Get Specific Information From Media Info", description = "returns a string corresponding to the info requested, the result is put in the context variable infoResult. Two keys are necessary, the first one is the category (General, Audio, Video), and the second is the specificInfo (Width, Format...),"
        + "a few examples : "
        + "- getSpecificMediaInfo(General,Format) could return : MPEG-4"
        + "- getSpecificMediaInfo(Video,Heigh) could return : 288 pixels"
        + ". Please be aware that the result of getSpecificMediaInfo is always the output String of mediainfo and may need extra parsing, for example for the Height the result could be 512 pixels and will not be 512.")
public class GetSpecificInformationFromMediaInfo {

    public static final String ID = "GetSpecificInformationFromMediaInfo";

    @Context
    protected OperationContext ctx;

    @Param(name = "category")
    protected String category;

    @Param(name = "specificInfo")
    protected String specificInfo;

    @Param(name = "resultContextVariable")
    protected String resultContextVariable;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) throws NuxeoException {
        if (input == null) {
            return null;
        }
        ctx.put(resultContextVariable, MediaInfoHelper.getSpecificMediaInfo(category, specificInfo, input));
        return input;
    }

}
