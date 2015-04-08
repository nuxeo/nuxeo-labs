/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.TreeMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

/**
 * 
 */
@Operation(id = HTTPCall.ID, category = Constants.CAT_SERVICES, label = "HTTP Call", description = "")
public class HTTPCall {

    public static final String ID = "HTTP.Call";

    @Context
    protected OperationContext ctx;

    @Param(name = "method", required = true, widget = Constants.W_OPTION, values = {
            "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE" })
    String method;

    @Param(name = "url", required = true)
    protected String url;

    @Param(name = "headers", required = false)
    protected Properties headers;

    @Param(name = "headersAsJSON", required = false)
    protected String headersAsJSON;

    @Param(name = "body", required = false)
    protected String body;

    @OperationMethod
    public Blob run() throws IOException {

        HttpURLConnection http = null;
        String result = "";
        String restResult = "";
        String error = "";
        boolean isUnknownHost = false;

        try {

            URL theURL = new URL(url);

            http = (HttpURLConnection) theURL.openConnection();

            addHeaders(http, headers, headersAsJSON);

            method = method.toUpperCase();
            http.setRequestMethod(method);

            if (body != null && !body.isEmpty()) {

                http.setDoInput(true);
                http.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(
                        http.getOutputStream());
                writer.write(body);
                writer.flush();
            }

            InputStream is = http.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            StringBuffer sb = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();

            restResult = sb.toString();

        } catch (Exception e) {

            error = e.getMessage();
            if (e instanceof java.net.UnknownHostException) {
                isUnknownHost = true;
            }

        } finally {

            int status = 0;
            String statusMessage = "";

            if (isUnknownHost) { // can't use our http variable
                status = 0;
                statusMessage = "UnknownHostException";
            } else {
                // Still, other failures _before_ reaching the server may occur
                // => http.getResponseCode() and others woul throw an error
                try {
                    status = http.getResponseCode();
                    statusMessage = http.getResponseMessage();
                } catch (Exception e) {
                    statusMessage = "Error getting the status message itself";
                }

            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode resultObj = mapper.createObjectNode();
            resultObj.put("status", status);
            resultObj.put("statusMessage", statusMessage);
            resultObj.put("error", error);

            // Check if we received a JSON string, so we put the object directly
            // in "result"
            try {
                ObjectMapper resultAsObj = new ObjectMapper();
                JsonNode rootNode = resultAsObj.readTree(restResult);
                resultObj.put("result", rootNode);
            } catch (Exception e) {
                resultObj.put("result", restResult);
            }

            ObjectWriter ow = mapper.writer();// .withDefaultPrettyPrinter();
            result = ow.writeValueAsString(resultObj);

        }

        return new StringBlob(result, "text/plain", "UTF-8");
    }
    
    /**
     * Adds the headers to the HttpURLConnection object
     * 
     * @param inHttp
     * @param inProps. A list of key-value pairs
     * @param inJsonStr. A JSON objects as String, each property is a header to
     *            set
     * @throws JsonProcessingException
     * @throws IOException
     *
     * @since 7.2
     */
    public static void addHeaders(HttpURLConnection inHttp, Properties inProps,
            String inJsonStr) throws JsonProcessingException, IOException {

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
