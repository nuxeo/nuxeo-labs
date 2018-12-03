package org.nuxeo.labs.automation.helpers.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
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
//import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.labs.automation.helpers.FileUtils;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "nuxeo-labs-automation-helpers", "org.nuxeo.ecm.automation.scripting",
    "nuxeo-labs-automation-helpers-test:automation-scripting-contrib.xml" })
public class TestFileUtilsHelper {

    protected DocumentModel parentOfTestDocs;

    protected static final String PDF_RELATIVE_PATH = "lorem-ipsum.pdf";

    protected static File PDF_FILE;

    protected static long PDF_FILE_SIZE;

    protected static final String TEXT_RELATIVE_PATH = "sample.txt";

    protected static File TEXT_FILE;

    protected static final String MISC_FOLDER_RELATIVE_PATH = "misc";

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

        PDF_FILE = org.nuxeo.common.utils.FileUtils.getResourceFileFromContext(PDF_RELATIVE_PATH);
        PDF_FILE_SIZE = PDF_FILE.length();

        TEXT_FILE = org.nuxeo.common.utils.FileUtils.getResourceFileFromContext(TEXT_RELATIVE_PATH);

    }

    @After
    public void cleanup() {

        coreSession.removeDocument(parentOfTestDocs.getRef());
        coreSession.save();
    }

    @Test
    public void testHelperIsAvailable() throws Exception {

        Map<String, ContextHelper> contextHelperList = ctxService.getHelperFunctions();
        ContextHelper ch = contextHelperList.get("FileUtils");
        assertNotNull(ch);
        assertTrue(ch instanceof FileUtils);
    }

    @Test
    public void testCreateFileAppendReadThenDelete() throws Exception {

        Map<String, ContextHelper> contextHelperList = ctxService.getHelperFunctions();
        FileUtils fileUtils = (FileUtils) contextHelperList.get("FileUtils");

        String tmp = System.getProperty("java.io.tmpdir");

        String destPath = tmp + java.util.UUID.randomUUID().toString() + ".txt";

        // Test createFile()
        File f = fileUtils.createFile(destPath);
        assertNotNull(f);

        // Test appendToFile()
        for (int i = 0; i < 10; ++i) {
            f = fileUtils.appendToFile(f, "" + i);
            assertNotNull(f);
        }

        // Test readFileToText()
        String str = fileUtils.readFileToText(destPath);
        assertEquals("0123456789", str);

        // Test deleteFile()
        fileUtils.deleteFile(destPath);
        assertFalse(f.exists());

    }

    @Test
    public void testSaveBlob() throws Exception {

        String name = PDF_FILE.getName();

        // Create the File document with its pdf
        DocumentModel doc = coreSession.createDocumentModel(parentOfTestDocs.getPathAsString(), name, "File");
        doc.setPropertyValue("dc:title", name);
        doc.setPropertyValue("file:content", new FileBlob(PDF_FILE));
        doc = coreSession.createDocument(doc);
        coreSession.save();

        Map<String, ContextHelper> contextHelperList = ctxService.getHelperFunctions();
        FileUtils fileUtils = (FileUtils) contextHelperList.get("FileUtils");

        String tmp = System.getProperty("java.io.tmpdir");
        String destPath = tmp + java.util.UUID.randomUUID().toString() + "-" + name;

        Blob b = (Blob) doc.getPropertyValue("file:content");
        fileUtils.saveBlob(b, destPath);

        File destFile = new File(destPath);
        assertTrue(destFile.exists());
        long destSize = destFile.length();
        assertEquals(PDF_FILE_SIZE, destSize);

        destFile.delete();

    }

    @Test
    public void testInJS_readFileToText() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);
        Map<String, Object> params = new HashMap<>();
        params.put("path", TEXT_FILE.getAbsolutePath());

        Object result = automationService.run(ctx, "TestHelpers.readFileToText", params);
        assertNotNull(result);
        assertEquals("Hello World!", result.toString());
    }

    @Test
    public void testInJS_readFileToBlob() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);
        Map<String, Object> params = new HashMap<>();
        params.put("path", TEXT_FILE.getAbsolutePath());

        Object result = automationService.run(ctx, "TestHelpers.readFileToBlob", params);
        assertNotNull(result);
        assertTrue(result instanceof Blob);
    }

    @Test
    public void testInJS_createWriteDelete() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);
        Map<String, Object> params = new HashMap<>();

        String tmp = System.getProperty("java.io.tmpdir");
        String destPath = tmp + java.util.UUID.randomUUID().toString() + ".txt";
        params.put("path", destPath);

        String toAppend = "1234567890\n1234567890";
        params.put("toAppend", toAppend);

        Object result = automationService.run(ctx, "TestHelpers.createWriteDelete", params);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals(toAppend, result.toString());

        File f = new File(destPath);
        assertTrue(!f.exists());

    }

    @Test
    public void testInJS_geFiles() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);
        Map<String, Object> params = new HashMap<>();

        File miscFolder = org.nuxeo.common.utils.FileUtils.getResourceFileFromContext(MISC_FOLDER_RELATIVE_PATH);
        params.put("path", miscFolder.getAbsolutePath());

        Object result = automationService.run(ctx, "TestHelpers.geFiles", params);
        assertNotNull(result);
        assertTrue(result instanceof String);
        // NOTE: If you change the content of the "misc" folder, you must change this test too
        String resultStr = (String) result;
        assertTrue(resultStr.indexOf("sample_1.txt\n") > -1);
        assertTrue(resultStr.indexOf("sample_2.txt\n") > -1);
        assertTrue(resultStr.indexOf("sample_3.txt\n") > -1);

    }

    @Test
    public void testInJS_geFolders() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);
        Map<String, Object> params = new HashMap<>();

        File miscFolder = org.nuxeo.common.utils.FileUtils.getResourceFileFromContext(MISC_FOLDER_RELATIVE_PATH);
        params.put("path", miscFolder.getAbsolutePath());

        Object result = automationService.run(ctx, "TestHelpers.geFolders", params);
        assertNotNull(result);
        assertTrue(result instanceof String);
        // NOTE: If you change the content of the "misc" folder, you must change this test too
        String resultStr = (String) result;
        assertTrue(resultStr.indexOf("a folder") > -1);

    }

    @Test
    public void testInJS_createFolder() throws Exception {

        OperationContext ctx = new OperationContext(coreSession);
        Map<String, Object> params = new HashMap<>();

        String tmp = System.getProperty("java.io.tmpdir");
        String destName = java.util.UUID.randomUUID().toString() + "-TEST";
        params.put("path", tmp);
        params.put("name", destName);
        File f = new File(tmp, destName);

        try {

            Object result = automationService.run(ctx, "TestHelpers.createFolder", params);
            assertNotNull(result);
            assertTrue(result instanceof Boolean);
            Boolean resultB = (Boolean) result;
            assertTrue(resultB);
            assertTrue(f.exists());

        } finally {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

}
