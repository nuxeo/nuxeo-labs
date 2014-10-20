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
 *     Thomas Roger <troger@nuxeo.com>
 */

package org.nuxeo.labs.video.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandAvailability;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.Deploy;

import com.google.inject.Inject;

/**
 * @author <a href="mailto:fvadon@nuxeo.com">Fred Vadon</a>
 * @since 5.6
 */

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy({ "org.nuxeo.ecm.platform.commandline.executor", "org.nuxeo.labs.video" })
public class MediaInfoOperationsTest {

    @Inject
    CoreSession session;

    public static final Log log = LogFactory.getLog(MediaInfoOperationsTest.class);

    // http://www.elephantsdream.org/
    public static final String ELEPHANTS_DREAM = "elephantsdream-160-mpeg4-su-ac3.avi";

    public static final String SINGLE_INFO_LINE = "Codec ID/Info                            : Advanced Video Coding";

    public static final String SINGLE_INFO_KEY = "Codec ID/Info";

    public static final String SINGLE_INFO_VALUE = "Advanced Video Coding";

    private GetInfoFromMediaInfo getInfosOperation;

    private GetSpecificInformationFromMediaInfo getSpecificInfosOperation;

    private OperationContext ctx;

    @Before
    public void setUp() throws Exception {
        getInfosOperation = new GetInfoFromMediaInfo();
        ctx = new OperationContext(session);
        getInfosOperation.ctx = ctx;
        getInfosOperation.resultContextVariable = "resultVariable";

        getSpecificInfosOperation = new GetSpecificInformationFromMediaInfo();
        getSpecificInfosOperation.ctx = ctx;

    }

    protected static BlobHolder getBlobFromPath(String path) throws IOException {
        InputStream is = MediaInfoOperationsTest.class.getResourceAsStream("/"
                + path);
        assertNotNull(String.format("Failed to load resource: " + path), is);
        return new SimpleBlobHolder(
                StreamingBlob.createFromStream(is, path).persist());
    }

    // Checks the processing of the output String List from Media Info
    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetAllProcessedInfos() throws Exception {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);
        CommandAvailability ca = cles.getCommandAvailability("mediainfo-info");
        if (!ca.isAvailable()) {
            log.warn("mediainfo is not avalaible, skipping test");
            return;
        }
        BlobHolder in = getBlobFromPath(ELEPHANTS_DREAM);
        Blob resultBlob = getInfosOperation.run(in.getBlob());
        Map<String, Map<String, String>> testResult = ((Map<String, Map<String, String>>) ctx.get("resultVariable"));
        assertEquals(testResult.get("General").get("Format/Info"),
                "Audio Video Interleave");
        assertEquals(testResult.get("General").get("Format"), "AVI");
        assertEquals(testResult.get("General").get("Writing application"),
                "Lavf52.31.0");
        assertEquals(testResult.get("General").get("Writing application"),
                "Lavf52.31.0");
        assertEquals(testResult.get("Video").get("ID"), "0");
        assertEquals(testResult.get("Video").get("Writing library"),
                "Lavc52.20.0");
        assertEquals(
                testResult.get("Audio").get("Interleave, preload duration"),
                "24 ms");
        assertEquals(resultBlob, in.getBlob());
    }

    // Checks the processing of the output String List from Media Info

    @Test
    public void shouldGetSpecificMediaInfos() throws Exception {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);
        CommandAvailability ca = cles.getCommandAvailability("mediainfo-info");
        if (!ca.isAvailable()) {
            log.warn("mediainfo is not avalaible, skipping test");
            return;
        }
        BlobHolder in = getBlobFromPath(ELEPHANTS_DREAM);
        getSpecificInfosOperation.category = "General";
        getSpecificInfosOperation.specificInfo = "Format/Info";
        getSpecificInfosOperation.resultContextVariable = "theSpecificInfoResult";
        Blob resultBlob = getSpecificInfosOperation.run(in.getBlob());
        assertEquals(resultBlob, in.getBlob());
        String theSpecificResult = (String) ctx.get("theSpecificInfoResult");
        assertEquals(theSpecificResult, "Audio Video Interleave");

        getSpecificInfosOperation.category = "Audio";
        getSpecificInfosOperation.specificInfo = "Interleave, preload duration";
        resultBlob = getSpecificInfosOperation.run(in.getBlob());
        theSpecificResult = (String) ctx.get("theSpecificInfoResult");
        assertEquals(theSpecificResult, "24 ms");

    }

}
