/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.ecm.platform.picture.api.PictureView;
import org.nuxeo.ecm.platform.picture.api.PictureViewImpl;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPicture;
import org.nuxeo.ecm.platform.picture.api.adapters.MultiviewPictureAdapter;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 */
@Operation(id = ImageCropInViewsOp.ID, category = Constants.CAT_CONVERSION, label = "ImageCropInViewsOp", description = "")
public class ImageCropInViewsOp {

    public static final String ID = "ImageCropInViewsOp";

    @Context
    protected CoreSession session;

    @Param(name = "title", required = false)
    protected String title = "";

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

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel inDoc) throws IOException {

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

        if (targetFileNameSuffix == null || targetFileNameSuffix.isEmpty()) {
            targetFileNameSuffix = "-crop" + top + "-" + left + "-" + width
                    + "x" + height;
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
        croppedBlob.setMimeType(pictureBlob.getMimeType());

        if (title == null || title.isEmpty()) {
            title = "Crop-" + top + "-" + left + "-" + width + "x" + height;
        }
        /*
         * MultiviewPicture mvp = inDoc.getAdapter(MultiviewPicture.class);
         * PictureView view = mvp.getView(title); if(view != null) {
         * mvp.removeView(title); }
         */

        ImagingService imagingService = Framework.getService(ImagingService.class);
        ImageInfo info = imagingService.getImageInfo(croppedBlob);
        PictureView view = new PictureViewImpl();
        view.setBlob(croppedBlob);
        view.setDescription(title);
        view.setFilename(croppedBlob.getFilename());
        view.setHeight((int) height);
        view.setImageInfo(info);
        view.setTitle(title);
        view.setWidth((int) width);
        // mvp.addView(view);

        List<Map<String, Object>> views = (List<Map<String, Object>>) inDoc.getPropertyValue("picture:views");
        if (views != null) {
            int max = views.size();
            int idxToDelete = -1;
            for (int i = 0; i < max; ++i) {
                Map<String, Object> map = views.get(i);
                if (map.get(PictureView.FIELD_TITLE).equals(title)) {
                    idxToDelete = i;
                    break;
                }
            }
            if (idxToDelete > -1) {
                views.remove(idxToDelete);
            }
        }
        views.add(myViewToMap(view));
        inDoc.setPropertyValue("picture:views", (Serializable) views);
        inDoc = session.saveDocument(inDoc);

        return inDoc;
    }

    // 2015-03-14: workaround a bug in MultiviewPictureAdapter#viewToMap.
    // Will be fixed in the platform very soon, but need this to work for like
    // in 2 days.
    protected Map<String, Object> myViewToMap(PictureView view) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(PictureView.FIELD_TITLE, view.getTitle());
        map.put(PictureView.FIELD_DESCRIPTION, view.getDescription());
        map.put(PictureView.FIELD_TAG, view.getTag());
        map.put(PictureView.FIELD_HEIGHT, view.getHeight());
        map.put(PictureView.FIELD_WIDTH, view.getWidth());
        map.put(PictureView.FIELD_FILENAME, view.getFilename());
        Object o = view.getBlob();
        Blob blob = null;
        if (o instanceof File) {
            blob = new FileBlob((File) o, "application/octet-stream");
        } /*
           * else if (o instanceof InputStream) { blob = new
           * InputStreamBlob((InputStream) o, "application/octet-stream"); }
           */else if (o instanceof Blob) {
            blob = (Blob) o;
        }
        if (blob != null) {
            map.put(PictureView.FIELD_CONTENT, blob);
        }

        map.put(PictureView.FIELD_INFO, view.getImageInfo().toMap());

        return map;
    }

}
