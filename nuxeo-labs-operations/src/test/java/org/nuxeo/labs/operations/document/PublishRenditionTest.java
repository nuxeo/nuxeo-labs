/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.operations.document;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
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
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import com.google.inject.Inject;

/**
 * @since TODO
 */
@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class, AutomationFeature.class })
@Deploy({ "org.nuxeo.ecm.core.convert.api", "org.nuxeo.ecm.core.convert", "org.nuxeo.ecm.core.convert.plugins",
        "org.nuxeo.ecm.platform.convert", "org.nuxeo.ecm.platform.query.api", "org.nuxeo.ecm.platform.rendition.api",
        "org.nuxeo.ecm.platform.rendition.core", "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.platform.versioning.api", "org.nuxeo.ecm.platform.versioning", "org.nuxeo.ecm.relations",
        "org.nuxeo.ecm.relations.jena", "org.nuxeo.ecm.platform.publisher.core.contrib",
        "org.nuxeo.ecm.platform.publisher.core", "org.nuxeo.ecm.platform.publisher.task",
        "org.nuxeo.ecm.platform.task.core", "org.nuxeo.ecm.platform.task.testing",
        "org.nuxeo.ecm.platform.rendition.publisher", "org.nuxeo.ecm.actions", "org.nuxeo.labs.operations" })
@LocalDeploy({ "org.nuxeo.ecm.platform.rendition.publisher:OSGI-INF/relations-default-jena-contrib.xml"/*
                                                                                                        * ,
                                                                                                        * "org.nuxeo.labs.operations.test:OSGI-INF/directory-config.xml"
                                                                                                        */ })
@RepositoryConfig(init = DefaultRepositoryInit.class)
public class PublishRenditionTest {

    // Because we used @RepositoryConfig(init = DefaultRepositoryInit.class), we
    // know we have the "correct" structure
    public static final String SECTIONS_ROOT_PATH = "/default-domain/sections";

    @Inject
    protected CoreSession coreSession;

    @Inject
    protected AutomationService automationService;

    protected DocumentModel doc;

    protected DocumentModel sectionDest;

    @Before
    public void initRepo() throws Exception {

        DocumentModel folder, sectionRoot;

        folder = coreSession.createDocumentModel("/", "Folder", "Folder");
        folder.setPropertyValue("dc:title", "Folder");
        folder = coreSession.createDocument(folder);

        File f = FileUtils.getResourceFileFromContext("lorem-ipsum.txt");
        FileBlob fileBlob = new FileBlob(f);
        MimetypeRegistry mimetypeRegistry = Framework.getLocalService(MimetypeRegistry.class);
        String mimeType = mimetypeRegistry.getMimetypeFromFile(f);
        fileBlob.setMimeType(mimeType);

        doc = coreSession.createDocumentModel("/Folder", "TheDoc", "File");
        doc.setPropertyValue("dc:title", "TheDoc");
        doc.setPropertyValue("file:content", fileBlob);
        doc = coreSession.createDocument(doc);

        sectionRoot = coreSession.getDocument(new PathRef(SECTIONS_ROOT_PATH));

        sectionDest = coreSession.createDocumentModel(sectionRoot.getPathAsString(), "Publication", "Section");
        sectionDest.setPropertyValue("dc:title", "TheSection");
        sectionDest = coreSession.createDocument(sectionDest);

        coreSession.save();
    }

    @Test
    public void shouldPublishPDFRendition() throws Exception {

        assertNotNull(doc);
        assertNotNull(sectionDest);

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(doc);
        OperationChain chain = new OperationChain("testPublishRendition");
        chain.add(PublishRenditionOp.ID).set("targetSectionRef", sectionDest.getPathAsString()).set("renditionName",
                "pdf");

        DocumentModel publishedDoc = (DocumentModel) automationService.run(ctx, chain);

        assertNotNull(publishedDoc);

        BlobHolder bh = publishedDoc.getAdapter(BlobHolder.class);
        Blob renditionBlob = bh.getBlob();
        assertEquals("application/pdf", renditionBlob.getMimeType());
    }

}
