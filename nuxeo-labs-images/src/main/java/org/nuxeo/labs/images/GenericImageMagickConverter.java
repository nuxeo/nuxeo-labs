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
 *     Alain Escaffre
 */
package org.nuxeo.labs.images;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.platform.convert.plugins.PDF2ImageConverter;

/**
 * In OSGI-INF/extensions/conversions-contrib.xml, you can find: - The
 * contribution to the ConversionService, using the
 * org.nuxeo.labs.images.GenericImageMagickConverter class - And the
 * contribution to the CommandLineExecutorComponent
 * 
 * So, to summarize, we have: - Converters with unique names ("overlaying",
 * "changeFormat", ...) - Which are bound to commandLine ("imageOverlay",
 * "changeFormat", ...) - At runtime, the parameters are packed in a map and
 * passed to the the command line executor
 * 
 * IMPORTANT: This means the parameters must match what the command line
 * expects. For example, for "overlay", a parameter named "textSize" must exist
 * in the context, since the "convert" commandLine uses it in its
 * parameterString<> tag: . . . -pointsize #{textSize} . . .
 * 
 */
public class GenericImageMagickConverter extends PDF2ImageConverter {
    public static final Log log = LogFactory.getLog(GenericImageMagickConverter.class);

    @Override
    protected Map<String, String> getCmdStringParameters(BlobHolder blobHolder,
            Map<String, Serializable> parameters) throws ConversionException {
        // TODO Auto-generated method stub
        Map<String, String> cmdStringParameters = super.getCmdStringParameters(
                blobHolder, parameters);

        Map<String, String> stringParameters = new HashMap<String, String>();
        Set<String> parameterNames = parameters.keySet();
        for (String parameterName : parameterNames) {
            // targetFilePath is computed by the method of the superType
            if (!parameterName.equals("targetFilePath")) {
                stringParameters.put(parameterName,
                        (String) parameters.get(parameterName));
            }
        }
        cmdStringParameters.putAll(stringParameters);
        return cmdStringParameters;
    }

}
