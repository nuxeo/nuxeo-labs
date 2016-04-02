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
package org.nuxeo.labs.operations.services;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.automation.core.util.Properties;

/**
 * @since 7.2
 */
public class HTTPUtils {

    /**
     * Adds the headers to the HttpURLConnection object
     * 
     * @param inHttp
     * @param inProps. A list of key-value pairs
     * @param inJsonStr. A JSON objects as String, each property is a header to set
     * @throws JsonProcessingException
     * @throws IOException
     * @since 7.2
     */
    public static void addHeaders(HttpURLConnection inHttp, Properties inProps, String inJsonStr)
            throws JsonProcessingException, IOException {

        if (inProps != null) {
            for (String oneHeader : inProps.keySet()) {
                inHttp.setRequestProperty(oneHeader, inProps.get(oneHeader));
            }
        }

        if (StringUtils.isNotBlank(inJsonStr)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inJsonStr);
            Iterator<String> it = rootNode.getFieldNames();
            while (it.hasNext()) {
                String oneHeader = it.next();
                inHttp.setRequestProperty(oneHeader, rootNode.get(oneHeader).getTextValue());
            }
        }
    }

}
