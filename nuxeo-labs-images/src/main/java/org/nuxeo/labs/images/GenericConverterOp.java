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
 *     Frederic Vadon
 */
package org.nuxeo.labs.images;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author fvadon and aescaffre
 */
@Operation(id = GenericConverterOp.ID, category = Constants.CAT_CONVERSION, label = "GenericConverter", description = "")
public class GenericConverterOp {

    public static final String ID = "GenericConverter";

    public static final Log log = LogFactory.getLog(GenericConverterOp.class);

    @Param(name = "converterName")
    protected String converterName;

    @Param(name = "parameters", required = false)
    protected Properties parameters;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) {

        try {

            ConversionService conversionService = Framework.getService(ConversionService.class);

            BlobHolder source = new SimpleBlobHolder(input);

            Map<String, Serializable> serializableParameters = new HashMap<String, Serializable>();

            if (parameters != null) {
                Set<String> parameterNames = parameters.keySet();

                for (String parameterName : parameterNames) {
                    serializableParameters.put(parameterName,
                            parameters.get(parameterName));
                }
            }

            log.debug("Converter Being called:" + converterName);
            BlobHolder result = conversionService.convert(converterName,
                    source, serializableParameters);

            return result.getBlob();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Error during conversion", e);
        }

        return null;
    }

}
