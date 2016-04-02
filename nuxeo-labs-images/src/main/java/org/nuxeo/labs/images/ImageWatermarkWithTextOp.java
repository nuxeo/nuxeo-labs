/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 * Adds the text as watermark to the input blob, outputs the watermarked blob.
 * Default values are: gravity SouthWest textColor Red textSize 24 strokeColor
 * #A84100 strokeWidth 1 textRotation 0 xOffset 0 yOffset 0. If targetFileName
 * is not provided, the output blob filename will be the same as the picture to
 * crop. Also, if targetFileSuffix is used, it will be added to the target file
 * name before the extension (So, if the file name is "mypict.jpg" and the
 * suffix is "-copy", the target file name will be "mypict-copy.jpg")
 * 
 * See comments in GenericImageMagickConverter for (a bit) more explanations
 * about converters and commandLines.
 * 
 * @since 5.9.6
 */
@Operation(id = ImageWatermarkWithTextOp.ID, category = Constants.CAT_CONVERSION, label = "Image: Watermark with Text", description = "Adds the text as watermark to the input blob, outputs the watermarked blob.<br/>Default values are: <code>gravity SouthWest</code>, <code>textColor Red</code>, <code>textSize 24</code>, <code>strokeColor #A84100</code>, <code>strokeWidth 1</code>, <code>textRotation 0</code>, <code>xOffset 0</code>, <code>yOffset 0</code>.<p>If <code>targetFileName</code> is not provided, the output blob filename will be the same as the picture to watermark. Also, if <code>targetFileSuffix</code> is used, it will be added to the target file name <i>before</i> the extension (So, if the file name is mypict.jpg and the suffix is -copy, the target file name will be mypict-copy.jpg)</p>")
public class ImageWatermarkWithTextOp {

    public static final String ID = "ImageWatermarkWithText";

    @Param(name = "textValue", required = true)
    protected String textValue = "";

    @Param(name = "gravity", required = false, widget = Constants.W_OPTION, values = {
            "SouthWest", "NorthWest", "North", "NorthEast", "West", "Center",
            "East", "SouthWest", "South", "SouthEast" })
    protected String gravity = "SouthWest";

    @Param(name = "textColor", required = false)
    protected String textColor = "Red";

    @Param(name = "textSize", required = false)
    protected String textSize = "24";

    @Param(name = "strokeColor", required = false)
    protected String strokeColor = "black";

    @Param(name = "strokeWidth", required = false)
    protected String strokeWidth = "1";

    @Param(name = "textRotation", required = false)
    protected String textRotation = "0";

    @Param(name = "xOffset", required = false)
    protected String xOffset = "0";

    @Param(name = "yOffset", required = false)
    protected String yOffset = "0";

    @Param(name = "targetFileName", required = false)
    protected String targetFileName;

    @Param(name = "targetFileNameSuffix", required = false)
    protected String targetFileNameSuffix = "";

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) {

        targetFileName = ConversionUtils.updateTargetFileName(inBlob,
                targetFileName, targetFileNameSuffix);

        Map<String, Serializable> params = new HashMap<String, Serializable>();

        params.put("textValue", textValue);
        params.put("targetFilePath", targetFileName);
        params.put("gravity", gravity);
        params.put("textColor", textColor);
        params.put("textSize", textSize);
        params.put("strokeColor", strokeColor);
        params.put("strokeWidth", strokeWidth);
        params.put("textRotation", textRotation);
        params.put("xOffset", xOffset);
        params.put("yOffset", yOffset);

        // The "textWatermarking" converter is defined in
        // OSGI-INF/extensions/conversions-contrib.xml
        Blob result = ConversionUtils.convert("textWatermarking", inBlob,
                params, targetFileName);
        return result;
    }
}
