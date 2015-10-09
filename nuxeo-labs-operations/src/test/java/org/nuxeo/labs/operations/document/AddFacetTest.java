/**
 *
 */

package org.nuxeo.labs.operations.document;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.After;
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
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @author Thibaud Arguillere
 */

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, CoreFeature.class,
        EmbeddedAutomationServerFeature.class })
@Deploy({ "org.nuxeo.labs.operation", "org.nuxeo.labs.operation.test" })
public class AddFacetTest {

    @Inject
    CoreSession session;

    @Inject
    AutomationService service;

    // MyNewFacet is declared in OSGI-INF/add-facet-test-contrib.xml
    public static final String THE_FACET = "MyNewFacet";

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
    public void testAddFacet() throws InvalidChainException, OperationException, Exception {
        
        assertNotNull(theDoc);
        assertFalse("New doc should not have the facet.", theDoc.hasFacet(THE_FACET));        
        OperationContext ctx = new OperationContext(session);
        ctx.setInput(theDoc);
        OperationChain chain = new OperationChain("testAddFacet");
        chain.add(AddFacet.ID).set("facet", "MyNewFacet");
        DocumentModel resultDoc = (DocumentModel)service.run(ctx, chain);

        assertNotNull(resultDoc);
        assertTrue("The doc should now have the facet.", resultDoc.hasFacet(THE_FACET));        

    }

}
