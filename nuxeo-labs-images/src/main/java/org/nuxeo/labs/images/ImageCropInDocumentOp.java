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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPicture;

/**
 * Crops the image embedded in a document: - Uses the main file ("file:content")
 * - Uses the crop coordinates passed as parameters - pictureWidth/pictureHeight
 * are the size of the picture used to build the crop coordinates. - Scale them
 * to fit the original image size if pictureWidth/pictureHeight are not 0. If
 * not passed, the operation assumes the original "file:content" image size is
 * used - If required, a version of the document is first created - Then, the
 * cropped picture is stored in "file:content", replacing the previous one. -
 * Last, the document is possibly saved
 * 
 * See comments in GenericImageMagickConverter for (a bit) more explanations
 * about converters and commandLines.
 * 
 * @since 7.1
 */
@Operation(id = ImageCropInDocumentOp.ID, category = Constants.CAT_CONVERSION, label = "Image: Crop and Save in Document", description = "Crops the picture: Generates a new picture whose size will be <code>width</code>/<code>height</code>, origin of the crop is <code>top</code>/<code>left</code>. If <code>width</code> or <code>height</code> is 0, nothing is done (no version, no replacement)<br/>If <code>incrementVersion</code> is not set to 'None', a version of the document is first created, then the crop replaces the picture. If the document is not versionable, the crop just replaces current picture.<p><code>pictureWidth</code>x<code>pictureHeight</code> is the size of the picture used for cropping. The coordinates of the crop will be scaled to fit the original picture. So, if original picture is 4000x2000, the picture used for cropping is 1000x500 (4 time smaller), and the crop is 10, 10, 200, 200, the final crop on the original picture will be 40, 40, 800, 800, so there will be no changes for the user.</p><p>If <code>targetFileName</code> is not provided, the output blob filename will be the same name as the picture to crop. Also, if <code>targetFileSuffix</code> is used, it will be added to the target file name <i>before</i> the extension (So, if the file name is 'mypict.jpg' and the suffix is '-copy', the target file name will be 'mypict-copy.jpg')</p><p>If <code>width</code> is 0 or <code>height</code> is 0, the original picture is returned.</p>")
public class ImageCropInDocumentOp {

    public static final String ID = "ImageCropInDocument";

    public static final Log log = LogFactory.getLog(ImageCropInDocumentOp.class);

    @Context
    protected CoreSession session;

    @Param(name = "top", required = false)
    protected long top = 0;

    @Param(name = "left", required = false)
    protected long left = 0;

    @Param(name = "width", required = false)
    protected long width = 0;

    @Param(name = "height", required = false)
    protected long height = 0;

    @Param(name = "pictureWidth", required = false)
    protected long pictureWidth = 0;

    @Param(name = "pictureHeight", required = false)
    protected long pictureHeight = 0;

    @Param(name = "targetFileName", required = false)
    protected String targetFileName = "";

    @Param(name = "targetFileNameSuffix", required = false)
    protected String targetFileNameSuffix = "";

    @Param(name = "incrementVersion", required = false, widget = Constants.W_OPTION, values = {
            "None", "Minor", "Major" })
    protected String incrementVersion = "None";

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel inDoc) throws ClientException {

        // Possibly, nothing to do.
        if (width == 0 || height == 0) {
            return inDoc;
        }

        if (!inDoc.hasFacet("Picture")) {
            throw new ClientException(
                    String.format(
                            "The document (id:'%s') with title '%s' doesn't have the 'Picture' facet",
                            inDoc.getId(), inDoc.getTitle()));
        }

        // Version the document
        if (!incrementVersion.equalsIgnoreCase("none")
                && inDoc.hasFacet(FacetNames.VERSIONABLE)) {
            VersioningOption vo;
            if (incrementVersion.equalsIgnoreCase("minor")) {
                vo = VersioningOption.MINOR;
            } else {
                vo = VersioningOption.MAJOR;
            }
            inDoc.putContextData(VersioningService.VERSIONING_OPTION, vo);
            inDoc = DocumentHelper.saveDocument(session, inDoc);
        }

        Blob pictureBlob = (Blob) inDoc.getPropertyValue("file:content");

        // Scale the crop
        if (pictureWidth > 0 && pictureHeight > 0) {
            double coef = 0.0;
            int w = ((Long) inDoc.getPropertyValue("picture:info/width")).intValue();
            int h = ((Long) inDoc.getPropertyValue("picture:info/height")).intValue();

            if (w != (int) pictureWidth) {
                coef = (double) w / (double) pictureWidth;
                left *= coef;
                width *= coef;
            }
            if (h != (int) pictureHeight) {
                coef = (double) h / (double) pictureHeight;
                top *= coef;
                height *= coef;
            }
        }

        targetFileName = ConversionUtils.updateTargetFileName(pictureBlob,
                targetFileName, targetFileNameSuffix);

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("targetFilePath", targetFileName);
        params.put("top", "" + top);
        params.put("left", "" + left);
        params.put("width", "" + width);
        params.put("height", "" + height);

        // The "imageCropping" converter is defined in
        // OSGI-INF/extensions/conversions-contrib.xml
        Blob croppedBlob = ConversionUtils.convert("imageCropping",
                pictureBlob, params, targetFileName);

        DocumentHelper.addBlob(inDoc.getProperty("file:content"), croppedBlob);
        inDoc = session.saveDocument(inDoc);

        return inDoc;
    }
}
