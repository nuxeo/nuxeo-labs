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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
@Operation(id = RESTGetOp.ID, category = Constants.CAT_SERVICES, label = "REST: GET", description = "Calls the url (with the headers). Returns a JSON string with 3 fields: status (200, 404, ...), statusMessage and result, which is plain text (will be ok for text, JSON, ..., but will not work with binaries). Fills the restGetStatus and restGetStatusMessage context variables")
public class RESTGetOp {

    public static final String ID = "REST.Get";

    private static final Log log = LogFactory.getLog(RESTGetOp.class);

    @Context
    protected OperationContext ctx;

    @Param(name = "url", required = true)
    protected String url;

    @Param(name = "headers", required = true)
    protected Properties headers;

    @OperationMethod
    public String run() throws IOException {

        URL theURL = new URL(url);
        HttpURLConnection http = null;

        http = (HttpURLConnection) theURL.openConnection();

        http.setAllowUserInteraction(false);
        for (String oneHeader : headers.keySet()) {
            http.setRequestProperty(oneHeader, headers.get(oneHeader));
        }

        InputStream is = http.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        StringBuffer sb = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            sb.append(inputLine);
        }
        in.close();

        String result = "{\"status\": " + http.getResponseCode();
        result += ", \"statusMessage\": "
                + RESTUtils.doubleQuoteString(http.getResponseMessage());
        result += ", \"result\": " + RESTUtils.formatForJSON(sb.toString())
                + "}";
        return result;

    }
}
