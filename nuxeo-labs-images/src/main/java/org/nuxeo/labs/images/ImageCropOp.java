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
 *     Thibaud Arguillere
 */

package org.nuxeo.labs.images;


import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

/**
 * Crops the picture: Generates a new picture whose size will be width/height,
 * origin of the crop is top/left.  If width or height is 0, nothing is done,
 * original blob is returned unchanged.
 * pictureWidth/pictureHeight is the size of the picture used for cropping.
 * The coordinates of the crop will be scaled to fit the original picture size.
 * So, if original picture is 4000x2000, the picture used for cropping is
 * 1000x500 (4 time smaller), and the crop is 10, 10, 200, 200, the final crop
 * on the original picture will be 40, 40, 800, 800, so there will be no
 * changes for the use: The final crop will look the same as what he/she
 * selected in the UI (but bigger)
 *
 * If targetFileName is not provided, the output blob filename will be the same
 * name as the picture to crop. Also, if targetFileSuffix is used, it will be
 * added to the target file name before the extension (So, if the file name
 * is "mypict.jpg" and the suffix is "-copy", the target file name will be
 * "mypict-copy.jpg")
 *
 * If width is 0 or height is 0, the original picture is returned.
 *
 * See comments in GenericImageMagickConverter for (a bit) more
 * explanations about converters and commandLines.
 *
 * @since 5.9.6
 *
 */
@Operation(id=ImageCropOp.ID, category=Constants.CAT_CONVERSION, label="Image: Crop", description="Crops the picture: Generates a new picture whose size will be <code>width</code>/<code>height</code>, origin of the crop is <code>top</code>/<code>left</code>.  If <code>width</code> or <code>height</code> is 0, nothing is done, original blob is returned unchanged<p><code>pictureWidth</code>x<code>pictureHeight</code> is the size of the picture used for cropping. The coordinates of the crop will be scaled to fit the original picture size. So, if original picture is 4000x2000, the picture used for cropping is 1000x500 (4 time smaller), and the crop is 10, 10, 200, 200, the final crop on the original picture will be 40, 40, 800, 800, so there will be no changes for the user.</p><p>If <code>targetFileName</code> is not provided, the output blob filename will be the same name as the picture to crop. Also, if <code>targetFileSuffix</code> is used, it will be added to the target file name <i>before</i> the extension (So, if the file name is mypict.jpg and the suffix is -copy, the target file name will be mypict-copy.jpg)</p><p>If <code>width</code> is 0 or <code>height</code> is 0, the original picture is returned.</p>")
public class ImageCropOp {

    public static final String ID = "ImageCrop";

    public static final Log log = LogFactory
            .getLog(ImageCropOp.class);

    @Param(name="top", required = false)
    protected long top = 0;

    @Param(name="left", required = false)
    protected long left = 0;

    @Param(name="width", required = false)
    protected long width = 0;

    @Param(name="height", required = false)
    protected long height = 0;

    @Param(name="pictureWidth", required = false)
    protected long pictureWidth = 0;

    @Param(name="pictureHeight", required = false)
    protected long pictureHeight = 0;

    @Param(name="targetFileName", required = false)
    protected String targetFileName = "";

    @Param(name="targetFileNameSuffix", required = false)
    protected String targetFileNameSuffix = "";

    @OperationMethod(collector=BlobCollector.class)
    public Blob run(Blob inBlob) {

        if(width ==0 || height == 0) {
            return inBlob;
        }

        // Scale the crop
        if(pictureWidth > 0 && pictureHeight > 0) {
            ImageInfo imgInfo = Framework.getService(ImagingService.class).getImageInfo(inBlob);

            int w = imgInfo.getWidth();
            int h = imgInfo.getHeight();

            double coef = 0.0;
            if(w != (int) pictureWidth) {
                coef = (double) w / (double) pictureWidth;
                left *= coef;
                width *= coef;
            }
            if(h != (int) pictureHeight) {
                coef = (double) h / (double) pictureHeight;
                top *= coef;
                height *= coef;
            }
        }

        targetFileName = ConversionUtils.updateTargetFileName(inBlob, targetFileName, targetFileNameSuffix);

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("targetFilePath", targetFileName);
        params.put("top", "" + top);
        params.put("left", "" + left);
        params.put("width", "" + width);
        params.put("height", "" + height);

        // The "imageCropping" converter is defined in OSGI-INF/extensions/conversions-contrib.xml
        Blob result = ConversionUtils.convert("imageCropping", inBlob, params, targetFileName);
        return result;
    }
}
