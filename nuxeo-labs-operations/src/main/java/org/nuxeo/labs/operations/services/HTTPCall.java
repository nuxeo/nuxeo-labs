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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 */
@Operation(id = HTTPCall.ID, category = Constants.CAT_SERVICES, label = "HTTP Call", description = "")
public class HTTPCall {

    public static final String ID = "HTTPlabs.Call";

    @Context
    protected OperationContext ctx;

    @Param(name = "method", required = true, widget = Constants.W_OPTION, values = { "GET", "POST", "PUT", "DELETE",
            "HEAD", "OPTIONS", "TRACE" })
    String method;

    @Param(name = "url", required = true)
    protected String url;

    @Param(name = "headers", required = false)
    protected Properties headers;

    @Param(name = "headersAsJSON", required = false)
    protected String headersAsJSON;

    @Param(name = "body", required = false)
    protected String body;

    @Param(name = "blobToSend", required = false)
    protected Blob blobToSend = null;

    @OperationMethod
    public Blob run() throws IOException {

        HttpURLConnection http = null;
        String result = "";
        String restResult = "";
        String error = "";
        boolean isUnknownHost = false;

        InputStream in = null;
        OutputStream out = null;

        try {

            URL theURL = new URL(url);

            http = (HttpURLConnection) theURL.openConnection();

            HTTPUtils.addHeaders(http, headers, headersAsJSON);

            method = method.toUpperCase();
            http.setRequestMethod(method);

            if (body != null && !body.isEmpty()) {

                http.setDoInput(true);
                http.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
                writer.write(body);
                writer.flush();
            } else if (blobToSend != null) {

                http.setDoInput(true);
                http.setDoOutput(true);

                in = blobToSend.getStream();
                out = http.getOutputStream();

                IOUtils.copy(in, out);
                out.flush();
            }

            InputStream is = http.getInputStream();
            BufferedReader bf = new BufferedReader(new InputStreamReader(is));

            StringBuffer sb = new StringBuffer();
            String inputLine;
            while ((inputLine = bf.readLine()) != null) {
                sb.append(inputLine);
            }
            bf.close();

            restResult = sb.toString();

        } catch (Exception e) {

            error = e.getMessage();
            if (e instanceof java.net.UnknownHostException) {
                isUnknownHost = true;
            }

        } finally {

            // Cleanup
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }

            // Return result
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
                resultObj.set("result", rootNode);
            } catch (Exception e) {
                resultObj.put("result", restResult);
            }

            ObjectWriter ow = mapper.writer();// .withDefaultPrettyPrinter();
            result = ow.writeValueAsString(resultObj);

        }

        return new StringBlob(result, "text/plain", "UTF-8");
    }

}
