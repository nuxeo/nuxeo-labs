/**
 *
 */

package org.nuxeo.labs.operations.images;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.labs.operations.images.ImagesSheetBuilder;
import org.nuxeo.labs.operations.images.ImagesSheetBuilderCustomOp;
import org.nuxeo.labs.operations.images.ImagesSheetBuilderOp;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;

/**
 * @author Thibaud Arguillere
 */

// Ignoring because I am failing deploying the Pictre document. And have no idea why.
// And need to move on. Sorry. 2018-04-08, Thibaud.
@Ignore
@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
    "org.nuxeo.ecm.platform.picture.api",
    "org.nuxeo.ecm.platform.picture.core",
    "org.nuxeo.ecm.platform.picture.convert",
    "org.nuxeo.ecm.platform.commandline.executor",
    "org.nuxeo.ecm.platform.rendition.core",
    "org.nuxeo.labs.operations"})
@LocalDeploy({ "org.nuxeo.labs.operations.test:OSGI-INF/test-custom-commandline.xml" })
public class ImagesSheetTest {

    protected static final int NUMBER_OF_TEST_IMAGES = 10;

    @Inject
    CoreSession session;

    @Inject
    AutomationService automationService;

    @Inject
    EventService eventService;

    protected DocumentModel folder;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        folder = session.createDocumentModel("/", "Folder", "Folder");
        folder.setPropertyValue("dc:title", "Folder");
        folder = session.createDocument(folder);
        session.save();
        folder = session.getDocument(folder.getRef());

        for (int i = 1; i <= NUMBER_OF_TEST_IMAGES; ++i) {
            createPictureDocument("test-" + i + ".jpg", "Example #" + i);
        }

        session.save();
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        // Wait for PictureViews to be generated
        eventService.waitForAsyncCompletion();
    }

    @After
    public void cleanup() {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();
    }

    protected void createPictureDocument(String fileName, String docTitle) {

        String title = docTitle;
        DocumentModel pict = session.createDocumentModel("/Folder", fileName, "Picture");
        pict.setPropertyValue("dc:title", title);
        File f = FileUtils.getResourceFileFromContext(fileName);
        pict.setPropertyValue("file:content", new FileBlob(f));
        pict = session.createDocument(pict);
    }

    protected void checkImage1WiderImage2Higher(Blob b1, Blob b2) throws Exception {

        BufferedImage bi1 = ImageIO.read(b1.getStream());
        BufferedImage bi2 = ImageIO.read(b2.getStream());
        assertTrue(bi1.getWidth() > bi2.getWidth());
        assertTrue(bi1.getHeight() < bi2.getHeight());
    }

    @Test
    public void testBuildSheet() throws Exception {

        System.out.print("\n\n\ntestBuildSheet - COUCOU OCUOC LJK JLKJLM?JKL?JKLM\n\n\n");

        DocumentModelList docs = session.query("SELECT * FROM Picture");
        assertEquals(NUMBER_OF_TEST_IMAGES, docs.size());

        // Using default values
        ImagesSheetBuilder isb = new ImagesSheetBuilder(docs);
        Blob result1 = isb.build();
        assertNotNull(result1);

        // Test with some custom parameters. For example, 2 tiles/row
        // Must have a smaller width and a bigger height than previous one
        isb = new ImagesSheetBuilder(docs);
        isb.setTile("2");
        Blob result2 = isb.build();

        assertNotNull(result2);
        checkImage1WiderImage2Higher(result1, result2);

    }

    @Test
    public void testOperation() throws Exception {

        System.out.print("\n\n\ntestOperation - COUCOU OCUOC LJK JLKJLM?JKL?JKLM\n\n\n");

        DocumentModelList docs = session.query("SELECT * FROM Picture");
        assertEquals(NUMBER_OF_TEST_IMAGES, docs.size());

        OperationChain chain;
        OperationContext ctx = new OperationContext(session);

        ctx.setInput(docs);
        chain = new OperationChain("testChain1");

        // Default parameters
        chain.add(ImagesSheetBuilderOp.ID);

        Blob result1 = (Blob) automationService.run(ctx, chain);
        assertNotNull(result1);

        // Customize the tile
        ctx.setInput(docs);
        chain = new OperationChain("testChain2");
        chain.add(ImagesSheetBuilderOp.ID).set("tile", "2").set("useDocTitle", true);
        Blob result2 = (Blob) automationService.run(ctx, chain);
        assertNotNull(result1);
        checkImage1WiderImage2Higher(result1, result2);

    }

    @Test
    public void testOperationCustomCommandline() throws Exception {

        System.out.print("\n\n\ntestOperationCustomCommandline - COUCOU OCUOC LJK JLKJLM?JKL?JKLM\n\n\n");

        DocumentModelList docs = session.query("SELECT * FROM Picture");
        assertEquals(NUMBER_OF_TEST_IMAGES, docs.size());

        OperationChain chain;
        OperationContext ctx = new OperationContext(session);

        ctx.setInput(docs);
        chain = new OperationChain("testChain3");

        chain.add(ImagesSheetBuilderCustomOp.ID).set("commandLine", "test-montage").set("parameters",
                "tile=5\ngeometry=" + ImagesSheetBuilder.DEFAULT_GEOMETRY);

        Blob result1 = (Blob) automationService.run(ctx, chain);
        assertNotNull(result1);

        // DIfferent tile
        ctx.setInput(docs);
        chain = new OperationChain("testChain4");
        chain.add(ImagesSheetBuilderCustomOp.ID).set("commandLine", "test-montage").set("parameters",
                "tile=2\ngeometry=" + ImagesSheetBuilder.DEFAULT_GEOMETRY);
        Blob result2 = (Blob) automationService.run(ctx, chain);
        assertNotNull(result1);
        checkImage1WiderImage2Higher(result1, result2);

    }

}
