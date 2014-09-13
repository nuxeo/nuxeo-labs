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
 *     Benjamin JALON <bjalon@nuxeo.com>
 */
package org.nuxeo.labs.operations.notification;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.Environment;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.operations.FetchContextDocument;
import org.nuxeo.ecm.automation.core.operations.document.SetDocumentBlob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.labs.operations.notification.AdvancedSendEmail;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:bjalon@nuxeo.com">Benjamin JALON</a>
 * @since 5.7
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy({ "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.platform.notification.core",
        "org.nuxeo.ecm.platform.notification.api",
        "org.nuxeo.ecm.platform.url.api", "org.nuxeo.ecm.platform.url.core",
        "org.nuxeo.labs.operation", "org.nuxeo.ecm.directory",
        "org.nuxeo.ecm.directory.sql", "org.nuxeo.ecm.directory.types.contrib",
        "org.nuxeo.ecm.platform.usermanager.api",
        "org.nuxeo.ecm.platform.usermanager",
        "org.nuxeo.labs.operation.test:OSGI-INF/doc-type-contrib.xml",
        "org.nuxeo.labs.operation.test:OSGI-INF/directory-config.xml",
        "org.nuxeo.labs.operation.test:OSGI-INF/userservice-config.xml" })
public class AdvancedSendEmailTest {

    private static final String MAIL_EXAMPLE = "<h3>Current doc: ${Document.path}</h3> title: ${Document['dc:title']}<p>Doc link: <a href=\"${docUrl}\">${Document.title}</a>";

    protected DocumentModel src;

    @Inject
    AutomationService service;

    @Inject
    CoreSession session;

    @Before
    public void init() throws Exception {
        session.removeChildren(session.getRootDocument().getRef());
        session.save();

        src = session.createDocumentModel("/", "src", "File");
        src.setPropertyValue("dc:title", "Source");
        src = session.createDocument(src);
        session.save();
        src = session.getDocument(src.getRef());

        InputStream in = AdvancedSendEmailTest.class.getClassLoader().getResourceAsStream(
                "example.mail.properties");
        File file = new File(Environment.getDefault().getConfig(),
                "mail.properties");
        file.getParentFile().mkdirs();
        FileUtils.copyToFile(in, file);
        in.close();

    }

    @Ignore("This test is disabled since the mail configuration may not work correctly on hudson. and anyway we need to check the received mail format.")
    @Test
    public void testSendMail() throws Exception {
        StringBlob blob = new StringBlob("my content");
        blob.setMimeType("text/plain");
        blob.setEncoding("UTF-8");
        blob.setFilename("thefile.txt");

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(src);
        OperationChain chain = new OperationChain("sendEMailTest");
        chain.add(FetchContextDocument.ID);
        chain.add(SetDocumentBlob.ID).set("file", blob);
        chain.add(AdvancedSendEmail.ID).from(fillAdvancedSendMailParameters());
        service.run(ctx, chain);

    }

    protected Map<String, Object> fillAdvancedSendMailParameters() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("from", "test@nuxeo.org");
        result.put("to", "Administrator");

        List<String> cc = Arrays.asList("bjalon@nuxeo.com", "Administrator",
                "members");
        result.put("cc", cc);
        List<String> bcc = Arrays.asList("administrators", "administrators");
        result.put("bcc", bcc);
        List<String> replyTo = Arrays.asList("administrators", "group:members");
        result.put("replyto", replyTo);
        result.put("subject", "test mail");
        result.put("asHTML", true);
        result.put("files", "file:content");
        result.put("message", MAIL_EXAMPLE);
        return result;
    }

}
