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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * The tests expect environement variables to be set with the distant server and misc info to test.
 * <ul>
 * <li>NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL
 * <ul>
 * <li>The full url to use to access the resource to get.</li>
 * <li>Must be a Nuxeo server</li>
 * <li>And must have file:content. The downlaod test will just append the /@blob adapter</li>
 * <li>Example: https://someserver.cloud.nuxeo.com/nuxeo/api/v1/path/mydomain/myworkspace/mydocument</li>
 * <li>Example: https://someserver.cloud.nuxeo.com/nuxeo/api/v1/id/1234567890-abcdef-etc</li>
 * </ul></li>
 * <li>NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION<br>
 * The full authorizaiton header. For example, "Basic 1234567890ABcdefGhiJ123LiBBf"</li>
 * <li>NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD and NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD_VALUE<br>
 * The field (XPATH) to test and the expected value</li>
 * <li>NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME</li>
 * <li>NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE</li>
 * <li></li>
 * </ul>
 * Examples:<br>
 * NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL="https://my.nuxeo.server/nuxeo/api/v1/id/123456-abcdef"<br>
 * NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION="Basic 1234567890ABcdefGhiJ123LiBBf"<br>
 * NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD="dc:title"<br>
 * NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD_VALUE="The Title"<br>
 * NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME="myfile.jpg"<br>
 * NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE="image/jpeg"<br>
 *
 * @since 7.2
 */
@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, CoreFeature.class, EmbeddedAutomationServerFeature.class })
@Deploy({ "org.nuxeo.labs.operations" })
public class HTTPTest {

    protected static String NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL = null;

    protected static String NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION = null;

    protected static String NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD = null;

    protected static String NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD_VALUE = null;

    protected static String NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME = null;

    protected static String NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE = null;

    private static final Log log = LogFactory.getLog(HTTPTest.class);

    public static final String HYLAND_DOT_COM = "https://www.hyland.com/en";

    protected static boolean isHylandComAvailable = false;

    @Inject
    CoreSession session;

    @Inject
    AutomationService automationService;

    @Before
    public void setUp() throws Exception {

        URL url = new URL(HYLAND_DOT_COM);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);

        int responseCode = connection.getResponseCode();
        // Response code is in the range of 200 to 299 (inclusive) are considered successful HTTP responses.
        isHylandComAvailable = 200 <= responseCode && responseCode <= 299;

        NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL = System.getProperty("NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL");
        NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION = System.getProperty(
                "NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION");
        NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD = System.getProperty(
                "NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD");
        NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD_VALUE = System.getProperty(
                "NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD_VALUE");
        NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME = System.getProperty(
                "NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME");
        NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE = System.getProperty(
                "NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE");
    }

    @After
    public void cleanup() {

    }

    @Test
    public void testSimpleGet() throws Exception {

        Assume.assumeTrue(HYLAND_DOT_COM + " not available => no test", isHylandComAvailable);

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Accept", "application/json");

        chain.add(HTTPCall.ID).set("method", "GET").set("url", HYLAND_DOT_COM).set("headers", props);

        Blob result = (Blob) automationService.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").intValue();
        if (status == 200) {

            String rawHtml = rootNode.get("result").asText();
            assertTrue(rawHtml.indexOf("Hyland") > -1);

        } else {
            // This is not an error in the unit test
            String statusMsg = rootNode.get("statusMessage").textValue();
            String error = rootNode.get("error").textValue();
            System.out.println("PROBLEM REACHING " + HYLAND_DOT_COM + ", status: " + status + ", statusMessage: "
                    + statusMsg + ", error: " + error);
        }

    }

    @Test
    public void testRESTGet() throws Exception {

        Assume.assumeTrue("No remote server defined => no test",
                StringUtils.isNotBlank(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL));

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Authorization", NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION);
        props.put("Accept", "application/json");
        props.put("Content-Type", "application/json");
        props.put("properties", "*");

        chain.add(HTTPCall.ID)
             .set("method", "GET")
             .set("url", NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL)
             .set("headers", props);
        // No input

        Blob result = (Blob) automationService.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").intValue();
        if (status == 200) {

            JsonNode theDoc = rootNode.get("result");
            assertNotNull(theDoc);

            JsonNode properties = theDoc.get("properties");
            assertNotNull(properties);

            JsonNode field = properties.get(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD);
            assertNotNull(field);

            String fieldValue = field.asText();
            assertEquals(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_TEST_FIELD_VALUE, fieldValue);

        } else {

            String statusMsg = rootNode.get("statusMessage").textValue();
            String error = rootNode.get("error").textValue();
            System.out.println("PROBLEM REACHING " + NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL + ", status: " + status
                    + ", statusMessage: " + statusMsg + ", error: " + error);
        }

    }

    @Test
    public void testDownloadFile() throws Exception {

        Assume.assumeTrue("No remote server defined => no test",
                StringUtils.isNotBlank(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL));
        Assume.assumeTrue("No test value defined => no test",
                StringUtils.isNotBlank(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME));
        Assume.assumeTrue("No test value defined => no test",
                StringUtils.isNotBlank(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE));

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Authorization", NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_AUTHORIZATION);
        props.put("Accept", "*/*");
        String url = NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL + "/@blob/file:content";
        
        chain.add(HTTPDownloadFile.ID)
             .set("url", url)
             .set("headers", props);


        Blob result = (Blob) automationService.run(ctx, chain);
        
        String statusStr = (String) ctx.get("httpDownloadFileStatus");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(statusStr);
        int status = rootNode.get("status").intValue();
        if (status == 200) {
            assertNotNull(result);
            assertTrue(result instanceof FileBlob);

            assertEquals(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_FILENAME, result.getFilename());
            assertEquals(NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOWNLOAD_MIMETYPE, result.getMimeType());

            File f = result.getFile();
            result = null;
            f.delete();

        } else {

            String statusMsg = rootNode.get("statusMessage").textValue();
            String error = rootNode.get("error").textValue();
            System.out.println("PROBLEM REACHING " + NXLABS_TEST_HTTPCCAL_NUXEO_SERVER_DOC_URL + "/@blob, status: " + status
                    + ", statusMessage: " + statusMsg + ", error: " + error);
        }

    }

    @Ignore
    @Test
    public void testSendBlob() throws Exception {

        // TODO Assume.assumeTrue("No remote server defined => no test", StringUtils.isNotBlank(DISTANT_SERVER));

        File f = FileUtils.getResourceFileFromContext("Nuxeo-logo.png");
        FileBlob fileBlob = new FileBlob(f);

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);

        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Authorization", "Basic QWRtaW5pc3RyYXRvcjpOdXhlbzIwMTU=");
        props.put("Accept", "application/json");
        props.put("Content-Type", "application/json");

        // TODO 
        /*
        chain.add(HTTPCall.ID)
             .set("method", "POST")
             .set("url", URL_TEST_FILE_UPLOAD)
             .set("headers", props)
             .set("blobToSend", fileBlob);
        */
        
        Blob result = (Blob) automationService.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();
        assertNotNull(jsonResult);

        // If dam.cloud.nuxeo.com can't be reached, it's not an error
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").intValue();

        if (status == 201) {
            JsonNode theDoc = rootNode.get("result");
            JsonNode batchId = theDoc.get("batchId");
            assertNotNull(batchId);
        } else {

            String statusMsg = rootNode.get("statusMessage").textValue();
            String error = rootNode.get("error").textValue();
            // TODO
            //log.error("PROBLEM REACHING " + URL_TEST_FILE_UPLOAD + ", status: " + status + ", statusMessage: "
            //        + statusMsg + ", error: " + error);
        }

    }

}
