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
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.video.mediainfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandAvailability;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.labs.video.mediainfo.MediaInfoHelper;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

/**
 * @author <a href="mailto:fvadon@nuxeo.com">Fred Vadon</a>
 * @since 5.6
 */

public class MediaInfoTest extends NXRuntimeTestCase {

    public static final Log log = LogFactory.getLog(MediaInfoTest.class);

    // http://www.elephantsdream.org/
    public static final String ELEPHANTS_DREAM = "elephantsdream-160-mpeg4-su-ac3.avi";

    public static final String SINGLE_INFO_LINE = "Codec ID/Info                            : Advanced Video Coding";

    public static final String SINGLE_INFO_KEY = "Codec ID/Info";

    public static final String SINGLE_INFO_VALUE = "Advanced Video Coding";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.labs.video");
        deployBundle("org.nuxeo.ecm.platform.commandline.executor");
    }

    protected static BlobHolder getBlobFromPath(String path) throws IOException {
        InputStream is = MediaInfoTest.class.getResourceAsStream("/" + path);
        assertNotNull(String.format("Failed to load resource: " + path), is);
        
        return new SimpleBlobHolder( new FileBlob(is) );
    }

    // Checks the parsing single outout lines.
    @Test
    public void testgetSingleInfoKey() throws Exception {
        assertEquals(SINGLE_INFO_KEY,
                MediaInfoHelper.getSingleInfoKey(SINGLE_INFO_LINE));
    }

    // Checks the parsing single outout lines.
    @Test
    public void testgetSingleInfoValue() throws Exception {
        assertEquals(SINGLE_INFO_VALUE,
                MediaInfoHelper.getSingleInfoValue(SINGLE_INFO_LINE));
    }

    // Checks the processing of the output String List from Media Info
    @Test
    public void testprocessMediaInfo() throws Exception {
        List<String> rawInfoMedia;
        rawInfoMedia = new ArrayList<String>();
        rawInfoMedia.add("General");
        rawInfoMedia.add("Complete name                            : test.mp4");
        rawInfoMedia.add("Format                                   : MPEG-4");
        rawInfoMedia.add("");
        rawInfoMedia.add("Video");
        rawInfoMedia.add("ID                                       : 1");
        rawInfoMedia.add("Format                                   : AVC");
        rawInfoMedia.add("Format/Info                              : Advanced Video Codec");
        rawInfoMedia.add("Format profile                           : Baseline@L2.1");
        rawInfoMedia.add("Height                                   : 288 pixels");
        rawInfoMedia.add("Width                                    : 512 pixels");
        rawInfoMedia.add("");
        rawInfoMedia.add("Audio");
        rawInfoMedia.add("ID                                       : 0");
        rawInfoMedia.add("Format                                   : MPEG Audio");
        rawInfoMedia.add("Format profile                           : Layer 2");
        rawInfoMedia.add("Delay relative to video                  : 83ms");
        rawInfoMedia.add("");
        rawInfoMedia.add("");

        Map<String, Map<String, String>> testResult = MediaInfoHelper.processMediaInfo(rawInfoMedia);
        assertEquals(testResult.get("General").get("Complete name"), "test.mp4");
        assertEquals(testResult.get("General").get("Format"), "MPEG-4");
        assertEquals(testResult.get("Video").get("Width"), "512 pixels");
        assertEquals(testResult.get("Audio").get("Delay relative to video"),
                "83ms");

    }

    // Checks that media info get results and that the result can be parsed.
    // Skipped if media info is not available.
    @Test
    public void testMediainfoInfo() throws Exception {
        CommandLineExecutorService cles = Framework.getLocalService(CommandLineExecutorService.class);
        assertNotNull(cles);
        CommandAvailability ca = cles.getCommandAvailability("mediainfo-info");
        if (!ca.isAvailable()) {
            log.warn("mediainfo is not available, skipping test");
            return;
        }
        BlobHolder in = getBlobFromPath(ELEPHANTS_DREAM);
        List<String> rawResult;
        rawResult = MediaInfoHelper.getRawMediaInfo(in.getBlob());
        assertNotNull(rawResult);
        Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
        result = MediaInfoHelper.getProcessedMediaInfo(in.getBlob());
        assertEquals(result.get("General").get("Format/Info"),
                "Audio Video Interleave");
        assertEquals(result.get("General").get("Format"), "AVI");
        assertEquals(result.get("General").get("Writing application"),
                "Lavf52.31.0");
        assertEquals(result.get("General").get("Writing application"),
                "Lavf52.31.0");
        assertEquals(result.get("Video").get("ID"), "0");
        assertEquals(result.get("Video").get("Writing library"), "Lavc52.20.0");
        assertEquals(result.get("Audio").get("Interleave, preload duration"),
                "24 ms");

        String oneSpecificInformation = MediaInfoHelper.getSpecificMediaInfo(
                "Video", "Writing library", in.getBlob());
        assertEquals(oneSpecificInformation, "Lavc52.20.0");

    }

}
