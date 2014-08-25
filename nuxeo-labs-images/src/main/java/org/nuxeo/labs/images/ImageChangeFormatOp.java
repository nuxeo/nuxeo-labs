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
 *     thibaud
 */
package org.nuxeo.labs.images;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;

/**
 * With ImageMagick, changing the format of a picture is about changing the extension
 * of the file. For example:
 *      convert something.jpg somethingelse.png
 *
 * See comments in GenericImageMagickConverter for (a bit) more
 * explanations about converters and commandLines.
 *
 * @since 5.9.6
 */
@Operation(id=ImageChangeFormatOp.ID, category=Constants.CAT_CONVERSION, label="Image: Change Format", description="Convert the image to another format (The operation uses ImageMagick, valid format are png, jpg, jpeg, git, tiff, ...)<p>If <code>targetFileName</code> is not provided, the output blob filename will be the same as the picture to convert (with a new extension). Also, if <code>targetFileSuffix</code> is used, it will be added to the target file name <i>before</i> the extension (So, if the file name is 'mypict.jpg', the suffix is '-copy', and the format is 'png', the target file name will be 'mypict-copy.png')</p>")
public class ImageChangeFormatOp {

    public static final String ID = "ImageChangeFormat";

    @Param(name = "format",  required = true)
    protected String format;

    @Param(name = "targetFileName",  required = false)
    protected String targetFileName;

    @Param(name="targetFileNameSuffix", required = false)
    protected String targetFileNameSuffix = "";

    @OperationMethod(collector=BlobCollector.class)
    public Blob run(Blob inBlob) {

        targetFileName = ConversionUtils.updateTargetFileName(inBlob, targetFileName, targetFileNameSuffix);
        // Remove the extension before calling ImageMagick commandLine (which adds #{format})
        int dotIndex = targetFileName.lastIndexOf('.');
        if(dotIndex > 0) {
            targetFileName = targetFileName.substring(0, dotIndex);
        }

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("targetFilePath", targetFileName);
        params.put("format", format);

        // The "changeFormat" converter is defined in OSGI-INF/extensions/conversions-contrib.xml
        Blob result = ConversionUtils.convert("changeFormat", inBlob, params, targetFileName + "." + format);
        return result;
    }
  }