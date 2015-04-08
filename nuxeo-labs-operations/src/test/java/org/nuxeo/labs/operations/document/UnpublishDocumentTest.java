/**
 *
 */

package org.nuxeo.labs.operations.document;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.InvalidChainException;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.operations.FetchContextDocument;
import org.nuxeo.ecm.automation.core.operations.document.PublishDocument;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author fvadon
 */

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, CoreFeature.class,
        EmbeddedAutomationServerFeature.class })
@Deploy({ "org.nuxeo.labs.operation" })
public class UnpublishDocumentTest {

    @Inject
    CoreSession session;

    @Inject
    AutomationService service;

    protected DocumentModel folder;
    protected DocumentModel section;
    protected DocumentModel fileToPublish;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        folder = session.createDocumentModel("/", "Folder", "Folder");
        folder.setPropertyValue("dc:title", "Folder");
        folder = session.createDocument(folder);
        session.save();
        folder = session.getDocument(folder.getRef());

        section = session.createDocumentModel("/", "Section", "Section");
        section.setPropertyValue("dc:title", "Section");
        section = session.createDocument(section);
        session.save();
        section = session.getDocument(section.getRef());

        fileToPublish = session.createDocumentModel("/Folder", "FileToPublish", "File");
        fileToPublish.setPropertyValue("dc:title", "File");
        fileToPublish = session.createDocument(fileToPublish);
        session.save();
        fileToPublish = session.getDocument(fileToPublish.getRef());
    }

    @Test
    public void testUnpublishDocument() throws InvalidChainException, OperationException, Exception {
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(fileToPublish);
        OperationChain chain = new OperationChain("testpublish");
        chain.add(FetchContextDocument.ID);
        chain.add(PublishDocument.ID).set("target",section.getId());
        DocumentModel publishedDoc = (DocumentModel)service.run(ctx, chain);

        Assert.assertEquals("Section", session.getDocument(publishedDoc.getParentRef()).getTitle());
        Assert.assertEquals(1, session.getChildren(section.getRef()).size());


        OperationChain unpublishChain = new OperationChain("testunpublish");
        unpublishChain.add(FetchContextDocument.ID);
        unpublishChain.add(UnpublishDocument.ID);
        service.run(ctx, unpublishChain);

        Assert.assertEquals(0, session.getChildren(section.getRef()).size());



    }

}
