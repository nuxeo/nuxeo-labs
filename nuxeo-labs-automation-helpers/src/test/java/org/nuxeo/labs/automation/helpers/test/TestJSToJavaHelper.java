package org.nuxeo.labs.automation.helpers.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.context.ContextHelper;
import org.nuxeo.ecm.automation.context.ContextService;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.labs.automation.helpers.JSToJava;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-labs-automation-helpers", "org.nuxeo.ecm.automation.scripting",
    "nuxeo-labs-automation-helpers-test:automation-scripting-contrib.xml" })
public class TestJSToJavaHelper {

    protected DocumentModel parentOfTestDocs;

    @Inject
    CoreSession coreSession;

    @Inject
    ContextService ctxService;

    @Inject
    AutomationService automationService;

    @Before
    public void setUp() {

        parentOfTestDocs = coreSession.createDocumentModel("/", "test-jshelper", "Folder");
        parentOfTestDocs.setPropertyValue("dc:title", "test-jshelper");
        parentOfTestDocs = coreSession.createDocument(parentOfTestDocs);
        parentOfTestDocs = coreSession.saveDocument(parentOfTestDocs);

        coreSession.save();

    }

    @After
    public void cleanup() {

        coreSession.removeDocument(parentOfTestDocs.getRef());
        coreSession.save();
    }

    @Test
    public void testHelperIsAvailable() throws Exception {

        Map<String, ContextHelper> contextHelperList = ctxService.getHelperFunctions();
        ContextHelper ch = contextHelperList.get("JSToJava");
        assertNotNull(ch);
        assertTrue(ch instanceof JSToJava);
    }

    @Test
    public void testInJS_jsArrayToJavaArrayList() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);

        try {
            // Create the 3 documents expected by the script
            DocumentModel doc;
            for(int i = 1; i < 4; ++i) {
                doc = coreSession.createDocumentModel(parentOfTestDocs.getPathAsString(), "jsArrayTest-" + i, "File");
                doc = coreSession.createDocument(doc);
            }
            coreSession.save();

            Object result = automationService.run(ctx, "TestHelpers.jsToJava_Array");
            assertNotNull(result);
            assertTrue(result instanceof ArrayList<?>);
            
            @SuppressWarnings("unchecked")
            ArrayList<DocumentModel> theArray = (ArrayList<DocumentModel>) result;
            assertEquals(3, theArray.size());

        } finally {
            
        }
    }

}
