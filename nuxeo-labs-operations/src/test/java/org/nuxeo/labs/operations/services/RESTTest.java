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

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

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
@Deploy({ "org.nuxeo.labs.operation" })
public class RESTTest {

    private static final Log log = LogFactory.getLog(RESTTest.class);

    protected static final String URL_TEST_GET = "http://dam.cloud.nuxeo.com/nuxeo/api/v1/path//";

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

        OperationContext ctx;
        OperationChain chain;

        ctx = new OperationContext(session);
        assertNotNull(ctx);

        chain = new OperationChain("testChain");

        Properties props = new Properties();
        props.put("Authorization", "Basic QWRtaW5pc3RyYXRvcjpBZG1pbmlzdHJhdG9y");
        props.put("Accept", "application/json");
        props.put("Content-Type", "application/json");

        chain.add(RESTGetOp.ID).set("url", URL_TEST_GET).set("headers", props);
        // No input
        
        Blob result = (Blob) service.run(ctx, chain);
        assertTrue(result instanceof StringBlob);

        String jsonResult = result.getString();// ((StringBlob)result).getString();
        // If dam.cloud.nuxeo.com can't be reached, it's not an error
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResult);
        int status = rootNode.get("status").getIntValue();
        if (status == 200) {

            JsonNode theDoc = rootNode.get("result");
            String str;

            str = theDoc.get("type").getTextValue();
            assertEquals("Root", str);

        } else {

            String statusMsg = rootNode.get("statusMessage").getTextValue();
            String error = rootNode.get("error").getTextValue();
            log.error("PROBLEM REACHING " + URL_TEST_GET + ", status: " + status
                    + ", statusMessage: " + statusMsg + ", error: " + error);
        }

    }

}
