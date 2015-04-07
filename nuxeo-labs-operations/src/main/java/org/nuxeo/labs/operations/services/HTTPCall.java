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
            "GET", "POST" })
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

            RESTUtils.addHeaders(http, headers, headersAsJSON);

            method = method.toUpperCase();
            http.setRequestMethod(method);
            if (method.equals("POST")) {

                http.setRequestMethod("POST");
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
            result = "{";
            if (isUnknownHost) { // can't use our http variable
                result += "\"status\": 0";
                result += ", \"statusMessage\": \"UnknownHostException\"";
            } else {
                result += "\"status\": " + http.getResponseCode();
                result += ", \"statusMessage\": "
                        + RESTUtils.doubleQuoteString(http.getResponseMessage());
            }
            result += ", \"result\": " + RESTUtils.formatForJSON(restResult);
            result += ", \"error\": " + RESTUtils.doubleQuoteString(error);
            result += "}";
        }

        return new StringBlob(result, "text/plain", "UTF-8");
    }

}
