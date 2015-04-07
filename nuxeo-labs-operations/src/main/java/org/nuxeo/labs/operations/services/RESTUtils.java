/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
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
 * Centralizing utilities used in the GET, POST, ... operations
 *
 * @since 7.2
 */
public class RESTUtils {
    
    public static String formatForJSON(String inStr) {

        String result = inStr;
        if(result == null || result.isEmpty()) {
            result = "\"\"";
        } else {
            int l = result.length();
            if(l == 1) {
                result = doubleQuoteString(result);
            } else {
                char firstChar = result.charAt(0);
                char lastChar = result.charAt(l - 1);
                if(firstChar == '[' && lastChar == ']') {
                    // It's an array => no quotes
                } else if(firstChar == '{' && lastChar == '}') {
                    // It's an object => no quotes
                } else if(firstChar == '"' && lastChar == '"') {
                 // Already quoted?
                }else {
                    result = doubleQuoteString(result);
                }
            }
        }
        
        return result;
    }

    public static String doubleQuoteString(String inStr) {

        return "\"" + inStr + "\"";
        
    }
    
    /**
     * Adds the headers to the HttpURLConnection object
     * 
     * @param inHttp
     * @param inProps. A list of key-value pairs
     * @param inJsonStr. A JSON objects as String, each property is a header to set
     * @throws JsonProcessingException
     * @throws IOException
     *
     * @since 7.2
     */
    public static void addHeaders(HttpURLConnection inHttp, Properties inProps, String inJsonStr) throws JsonProcessingException, IOException {
        
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
                inHttp.setRequestProperty(oneHeader,
                        rootNode.get(oneHeader).getTextValue());
            }
        }
    }
}
