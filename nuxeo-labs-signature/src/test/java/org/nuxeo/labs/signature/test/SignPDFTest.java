/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Wojciech Sulejman
 *     Florent Guillaume
 *     Michaël Vachette
 */
package org.nuxeo.labs.signature.test;

import com.google.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.directory.Session;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.ecm.platform.signature.api.sign.SignatureService;
import org.nuxeo.ecm.platform.signature.api.user.CUserService;
import org.nuxeo.ecm.platform.signature.core.sign.SignatureServiceImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.labs.signature.SignPDF;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import java.io.File;

import static org.junit.Assert.assertNotNull;

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(type = BackendType.H2, user = "Administrator", init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.directory",
        "org.nuxeo.ecm.directory.sql",
        "org.nuxeo.ecm.directory.types.contrib",
        "org.nuxeo.ecm.platform.usermanager.api",
        "org.nuxeo.ecm.platform.usermanager",
        "org.nuxeo.ecm.platform.signature.core", //
        "org.nuxeo.labs.signature",
        "org.nuxeo.labs.signature.test:OSGI-INF/directory-contrib.xml",
        "org.nuxeo.labs.signature.test:OSGI-INF/schema-contrib.xml",
        "org.nuxeo.labs.signature.test:OSGI-INF/cuser-contrib.xml",
        "org.nuxeo.labs.signature.test:OSGI-INF/root-contrib.xml"})
public class SignPDFTest {

    @Inject
    protected CUserService cUserService;

    @Inject
    protected SignatureService signatureService;

    @Inject
    protected UserManager userManager;

    @Inject
    protected DirectoryService directoryService;

    @Inject
    CoreSession session;

    @Inject
    AutomationService automationService;


    private static final String ORIGINAL_PDF = "pdf-tests/original.pdf";

    private static final String USER_KEY_PASSWORD = "abc";

    private static final String CERTIFICATE_DIRECTORY_NAME = "certificate";

    private static final String DEFAULT_USER_ID = "hsimpsons";

    private File origPdfFile;

    private DocumentModel user;


    /**
     * Signing Prerequisite: a user with a certificate needs to be present
     */
    @Before
    public void setUp() throws Exception {

        DocumentModel userModel = userManager.getBareUserModel();
        userModel.setProperty("user", "username", DEFAULT_USER_ID);
        userModel.setProperty("user", "firstName", "Homer");
        userModel.setProperty("user", "lastName", "Simpson");
        userModel.setProperty("user", "email", "hsimpson@springfield.com");
        userModel.setPathInfo("/", DEFAULT_USER_ID);
        user = userManager.createUser(userModel);

        DocumentModel certificate = cUserService.createCertificate(user,
                USER_KEY_PASSWORD);
        assertNotNull(certificate);

        origPdfFile = FileUtils.getResourceFileFromContext(ORIGINAL_PDF);
    }

    @After
    public void tearDown() throws Exception {

        // delete certificates associated with user ids
        Session sqlSession = directoryService.open(CERTIFICATE_DIRECTORY_NAME);
        sqlSession.deleteEntry(DEFAULT_USER_ID);
        sqlSession.close();

        // delete users
        userManager.deleteUser(DEFAULT_USER_ID);
    }

    @Test
    public void testSignPDF() throws Exception {

        SignatureServiceImpl ssi = (SignatureServiceImpl) signatureService;
        // first user signs
        FileBlob origBlob = new FileBlob(origPdfFile);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(origBlob);
        OperationChain chain = new OperationChain("signTest");
        chain.add(SignPDF.ID).
                set("password", USER_KEY_PASSWORD).
                set("reason", "TEST").
                set("username",DEFAULT_USER_ID);
        Blob signedBlob = (Blob) automationService.run(ctx,chain);
        assertNotNull(signedBlob);
    }
}