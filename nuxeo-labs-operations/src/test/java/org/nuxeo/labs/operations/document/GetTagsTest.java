/**
 *
 */

package org.nuxeo.labs.operations.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author Thibaud Arguillere
 */

@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class, AutomationFeature.class })
@Deploy({ "org.nuxeo.ecm.platform.tag.api", "org.nuxeo.ecm.platform.tag", "org.nuxeo.labs.operations" })
public class GetTagsTest {

    @Inject
    CoreSession session;

    @Inject
    AutomationService service;

    @Inject
    TagService tagService;

    protected DocumentModel folder;

    protected DocumentModel theDoc;

    @Before
    public void initRepo() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        folder = session.createDocumentModel("/", "Folder", "Folder");
        folder.setPropertyValue("dc:title", "Folder");
        folder = session.createDocument(folder);
        session.save();
        folder = session.getDocument(folder.getRef());

        theDoc = session.createDocumentModel("/Folder", "TheDoc", "File");
        theDoc.setPropertyValue("dc:title", "TheDoc");
        theDoc = session.createDocument(theDoc);
        session.save();
        theDoc = session.getDocument(theDoc.getRef());
    }

    @After
    public void cleanup() {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();
    }

    @Test
    public void testGetTags() throws Exception {

        final int COUNT_TAGS = 5;

        for(int i = 1; i <= COUNT_TAGS; ++i) {
            tagService.tag(session, theDoc.getId(), "tag" + i);
        }

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(theDoc);
        OperationChain chain = new OperationChain("testGetTags");
        chain.add(DocumentGetTagsOp.ID);

        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) service.run(ctx, chain);

        assertNotNull(tags);
        assertEquals(COUNT_TAGS, tags.size());
        // The list is sorted. So we must have "tag1", "tag2", "tag3", ...
        for(int i = 1; i <= COUNT_TAGS; ++i) {
            assertTrue(tags.contains("tag" + i));
        }

    }

    @Test
    public void testGetTags_DocList() throws Exception {

        final int COUNT_TAGS = 5;

        for(int i = 1; i <= COUNT_TAGS; ++i) {
            tagService.tag(session, theDoc.getId(), "tag" + i);
        }

        DocumentModelListImpl list = new DocumentModelListImpl();
        list.add(theDoc);

        DocumentModel doc = session.createDocumentModel("/Folder", "TheDoc2", "File");
        doc.setPropertyValue("dc:title", "TheDoc2");
        doc = session.createDocument(doc);
        tagService.tag(session, doc.getId(), "tag2");
        tagService.tag(session, doc.getId(), "tag20");
        list.add(doc);

        doc = session.createDocumentModel("/Folder", "TheDoc3", "File");
        doc.setPropertyValue("dc:title", "TheDoc2");
        doc = session.createDocument(doc);
        tagService.tag(session, doc.getId(), "tag3");
        tagService.tag(session, doc.getId(), "tag30");
        list.add(doc);

        session.save();

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(list);
        OperationChain chain = new OperationChain("testGetTags");
        chain.add(DocumentGetTagsOp.ID);

        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) service.run(ctx, chain);

        assertNotNull(tags);
        // Must have the 5 original tags + tag20 and tag30
        assertEquals(COUNT_TAGS + 2, tags.size());
        for(int i = 1; i <= COUNT_TAGS; ++i) {
            assertTrue(tags.contains("tag" + i));
        }
        assertTrue(tags.contains("tag20"));
        assertTrue(tags.contains("tag30"));

    }

}
