/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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

package org.nuxeo.labs.operations.document;

import static org.junit.Assert.*;

import java.util.UUID;

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
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy({ "org.nuxeo.labs.operations", "org.nuxeo.labs.operations.test" })
public class DocumentExistsTest {

    @Inject
    CoreSession session;

    @Inject
    AutomationService service;

    protected String ID;

    protected String PATH;

    @Before
    public void initRepo() throws Exception {

        DocumentModel folder;
        DocumentModel doc;

        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        folder = session.createDocumentModel("/", "Folder", "Folder");
        folder.setPropertyValue("dc:title", "Folder");
        folder = session.createDocument(folder);
        session.save();

        doc = session.createDocumentModel("/Folder", "TheDoc", "File");
        doc.setPropertyValue("dc:title", "TheDoc");
        doc = session.createDocument(doc);
        session.save();

        ID = doc.getId();
        PATH = doc.getPathAsString();
    }

    @After
    public void cleanup() {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();
    }

    @Test
    public void testShouldExist_id() throws Exception {

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("idShouldExist");
        chain.add(DocumentExistsOp.ID).set("idOrPath", ID);
        boolean exists = (boolean) service.run(ctx, chain);

        assertTrue(exists);

    }

    @Test
    public void testShouldExist_path() throws Exception {

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("pathShouldExist");
        chain.add(DocumentExistsOp.ID).set("idOrPath", PATH);
        boolean exists = (boolean) service.run(ctx, chain);

        assertTrue(exists);

    }

    @Test
    public void testShouldNotExist() throws Exception {

        OperationContext ctx = new OperationContext(session);
        OperationChain chain = new OperationChain("shouldNotExist");
        chain.add(DocumentExistsOp.ID).set("idOrPath", UUID.randomUUID().toString());
        boolean exists = (boolean) service.run(ctx, chain);

        assertFalse(exists);

    }

}
