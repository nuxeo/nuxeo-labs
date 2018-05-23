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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
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
 * NOTICE: Using dam.cloud.nuxeo.com as distant test server. If it's not
 * available, we don't consider it's an error.
 *
 * @since 7.2
 */
@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, CoreFeature.class,
        EmbeddedAutomationServerFeature.class })
@Deploy({ "org.nuxeo.labs.operations" })
public class HTTPTest {

    private static final Log log = LogFactory.getLog(HTTPTest.class);

    // Maybe we should _not_ use dam.cloud.nuxeo.com. At least, PLEASE DO NOT ADD
    // DOCUMENTS THERE, we're using it for demos
    // 2017-07-11: dam.cloud?nuxeo.com ius not running anymore. Deactivating the test until we find another server
    // (using Assume.assumeTrue testing this value)
    protected static final String DISTANT_SERVER = null;//"http://dam.cloud.nuxeo.com/nuxeo";

    protected static final String DISTANT_SERVER_REST_PATTERN = DISTANT_SERVER
            + "/api/v1";

    protected static final String URL_TEST_GET = DISTANT_SERVER_REST_PATTERN
            + "/path//";

    protected static final String URL_TEST_FILE_UPLOAD = DISTANT_SERVER + "/api/v1/upload/";

    protected static final String DISTANT_PICTURE_DOC_ID = "ef825f50-c12e-4e13-b3b7-31f95cc500fe";

    @Inject
    CoreSession session;

    @Inject
    AutomationService service;

    @Before
    public void setUp() {

    }

    @After
    public void cleanup() {

    }

    @Test
    public void testRESTGet() throws Exception {

    	Assume.assumeTrue("No remote server defined => no test", StringUtils.isNotBlank(DISTANT_SERVER));

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        assertNotNull(ctx);

        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Authorization", "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y");
        props.put("Accept", "application/json");
        props.put("Content-Type", "application/json");

        chain.add(HTTPCall.ID).set("method", "GET").set("url", URL_TEST_GET).set(
                "headers", props);
        // No input

        Blob result = (Blob) service.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();// ((StringBlob)result).getString();
        // If dam.cloud.nuxeo.com can't be reached, it's not an error
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").intValue();
        if (status == 200) {

            JsonNode theDoc = rootNode.get("result");
            String str;

            str = theDoc.get("type").textValue();
            assertEquals("Root", str);

        } else {

            String statusMsg = rootNode.get("statusMessage").textValue();
            String error = rootNode.get("error").textValue();
            log.error("PROBLEM REACHING " + URL_TEST_GET + ", status: "
                    + status + ", statusMessage: " + statusMsg + ", error: "
                    + error);
        }

    }

    @Test
    public void testDownloadFile() throws Exception {

    	Assume.assumeTrue("No remote server defined => no test", StringUtils.isNotBlank(DISTANT_SERVER));

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        assertNotNull(ctx);

        chain = new OperationChain("testChain");

        // check we have our Picture available
        Properties props = new Properties();
        props.put("Authorization", "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y");
        props.put("Accept", "application/json");
        props.put("Content-Type", "application/json");

        String url = DISTANT_SERVER_REST_PATTERN + "/id/"
                + DISTANT_PICTURE_DOC_ID;
        chain.add(HTTPCall.ID).set("method", "GET").set("url", url).set(
                "headers", props);

        Blob result = (Blob) service.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").intValue();
        if (status == 200) {
            JsonNode theDoc = rootNode.get("result");
            String str;

            str = theDoc.get("type").textValue();
            assertEquals("Picture", str);

            // OK, now, download it
            // http://dam.cloud.nuxeo.com/nuxeo/nxpicsfile/default/ef825f50-c12e-4e13-b3b7-31f95cc500fe/Medium:content/Thu%20Mar%2019%2008%3A58%3A31%20UTC%202015
            url = DISTANT_SERVER + "/nxpicsfile/default/"
                    + DISTANT_PICTURE_DOC_ID + "/Medium:content/whatever";
            chain = new OperationChain("testChain2");
            props = new Properties();
            props.put("Authorization",
                    "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y");
            props.put("Accept", "*/*");

            chain.add(HTTPDownloadFile.ID).set("url", url).set("headers", props);
            result = (Blob) service.run(ctx, chain);
            assertTrue(result instanceof FileBlob);

            assertEquals("Medium_wallpaper-nuxeo-X-noir-1600.jpg",
                    result.getFilename());
            assertEquals("image/jpeg", result.getMimeType());

            File f = result.getFile();
            result = null;
            f.delete();

        }

    }

    @Test
    public void testSendBlob() throws Exception {

    	Assume.assumeTrue("No remote server defined => no test", StringUtils.isNotBlank(DISTANT_SERVER));

        File f = FileUtils.getResourceFileFromContext("Nuxeo-logo.png");
        FileBlob fileBlob = new FileBlob(f);

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        assertNotNull(ctx);

        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Authorization", "Basic QWRtaW5pc3RyYXRvcjpOdXhlbzIwMTU=");
        props.put("Accept", "application/json");
        props.put("Content-Type", "application/json");

        chain.add(HTTPCall.ID).set("method", "POST").set("url", URL_TEST_FILE_UPLOAD).set(
                "headers", props).set("blobToSend", fileBlob);

        // No input

        Blob result = (Blob) service.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();
        assertNotNull(jsonResult);

        // If dam.cloud.nuxeo.com can't be reached, it's not an error
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").intValue();

        if(status == 201) {
            JsonNode theDoc = rootNode.get("result");
            JsonNode batchId = theDoc.get("batchId");
            assertNotNull(batchId);
        } else {

            String statusMsg = rootNode.get("statusMessage").textValue();
            String error = rootNode.get("error").textValue();
            log.error("PROBLEM REACHING " + URL_TEST_FILE_UPLOAD + ", status: "
                    + status + ", statusMessage: " + statusMsg + ", error: "
                    + error);
        }

    }

}
