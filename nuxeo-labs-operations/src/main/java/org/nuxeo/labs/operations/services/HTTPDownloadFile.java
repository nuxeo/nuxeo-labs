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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.node.ObjectNode;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;

/**
 *
 */
@Operation(id = HTTPDownloadFile.ID, category = Constants.CAT_BLOB, label = "HTTP Download File", description = "Download the file referenced by the url (using HTTP GET) and returns a blob")
public class HTTPDownloadFile {

    public static final String ID = "HTTPlabs.DownloadFile";

    public static final Log log = LogFactory.getLog(HTTPDownloadFile.class);

    protected static final int BUFFER_SIZE = 4096;

    protected static String tempFolderPath = null;

    protected static final String MUTEX = "HTTPDownloadFileLock";

    @Context
    protected OperationContext ctx;

    @Param(name = "url", required = true)
    protected String url;

    @Param(name = "headers", required = false)
    protected Properties headers;

    @Param(name = "headersAsJSON", required = false)
    protected String headersAsJSON;

    @OperationMethod
    public Blob run() throws IOException {

        Blob result = null;

        HttpURLConnection http = null;
        String error = "";
        String resultStatus = "";
        boolean isUnknownHost = false;

        try {
            URL theURL = new URL(url);

            http = (HttpURLConnection) theURL.openConnection();
            HTTPUtils.addHeaders(http, headers, headersAsJSON);

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {

                String fileName = "";
                String disposition = http.getHeaderField("Content-Disposition");
                String contentType = http.getContentType();
                String encoding = http.getContentEncoding();

                // Try to get a filename
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > -1) {
                        fileName = disposition.substring(index + 9);
                    }
                } else {
                    // extracts file name from URL
                    fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
                }
                if (StringUtils.isEmpty(fileName)) {
                    fileName = "DownloadedFile-" + java.util.UUID.randomUUID().toString();
                }

                File tempFile = new File(getTempFolderPath() + File.separator + java.util.UUID.randomUUID().toString());

                FileOutputStream outputStream = new FileOutputStream(tempFile);
                InputStream inputStream = http.getInputStream();
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.close();
                inputStream.close();

                result = new FileBlob(tempFile, contentType, encoding);
                result.setFilename(fileName);
            }

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
                // => http.getResponseCode() and others would throw an error
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

            ObjectWriter ow = mapper.writer();// .withDefaultPrettyPrinter();
            resultStatus = ow.writeValueAsString(resultObj);
        }

        ctx.put("httpDownloadFileStatus", resultStatus);

        return result;
    }

    protected String getTempFolderPath() throws IOException {

        if (tempFolderPath == null) {
            synchronized (MUTEX) {
                if (tempFolderPath == null) {
                    tempFolderPath = Files.createTempDirectory("HTTPDownloadFile").toString();
                }
            }
        }

        return tempFolderPath;
    }

}
