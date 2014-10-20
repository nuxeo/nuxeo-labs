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
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

/**
 * Display "Crop image" UI in a fancybox. The bean sets up information about the
 * picture and its size, to make it fit in the fancybox, while using a
 * small/medium image (void using the original multi-mega bytes image).
 * Possibly, the final width/height will be bigger than the image used, so the
 * browser will scale it.
 * 
 * When the user clicks "Crop" button, the crop itself is managed by the
 * JavaScript not by this Bean (see
 * resources/web/nuxeo.war/scripts/nuxeo-labs-image-cropper.js)
 * 
 * @since 5.9.6
 */
@Name("imageCropHelper")
@Scope(ScopeType.EVENT)
public class ImageCropHelperBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int kMAX_WIDTH = 1000;

    private static final int kMAX_HEIGHT = 600;

    private static final int kMIN_WIDTH = 300;

    private static final int kMIN_HEIGHT = 200;

    private static final Log log = LogFactory.getLog(ImageCropHelperBean.class);

    @In(create = true, required = false)
    protected transient CoreSession documentManager;

    @In(create = true)
    protected NavigationContext navigationContext;

    @In(create = true, required = false)
    protected transient FacesMessages facesMessages;

    @In(create = true, required = false)
    protected NuxeoPrincipal currentNuxeoPrincipal;

    protected DocumentModel currentDocument = null;

    protected long imageWidth = 0;

    protected long imageHeight = 0;

    protected String imageViewName = "";

    protected long originalImageWidth = 0;

    protected long originalImageHeight = 0;

    @Create
    public void initialize() throws ClientException {
        try {
            currentDocument = navigationContext.getCurrentDocument();

            ArrayList<Map<String, Object>> views = (ArrayList<Map<String, Object>>) currentDocument.getProperty(
                    "picture", "views");

            imageWidth = 0;
            imageHeight = 0;

            if (views != null) {
                // Here, we should find the most appropriate view to
                // use, depending on the width/height.
                for (Map<String, Object> oneView : views) {
                    long viewW = 0, viewH = 0;
                    String viewName;

                    String title = (String) oneView.get("title");

                    viewName = title + ":content";
                    viewW = (long) oneView.get("width");
                    viewH = (long) oneView.get("height");
                    if (title.equalsIgnoreCase("Original")) {
                        originalImageWidth = (long) oneView.get("width");
                        originalImageHeight = (long) oneView.get("height");
                    }

                    if (viewW < kMAX_WIDTH) {
                        if (viewW > imageWidth) {
                            imageWidth = viewW;
                            imageHeight = viewH;
                            imageViewName = viewName;
                        }
                    }
                }

                // Either we have no views or none of them are < kMAX_WIDTH,
                // which
                // is very, very unlikely. Result will be weird anyway.
                if (imageWidth == 0) {
                    // We do have a problem
                    log.warn("No  picture view found which is less than "
                            + kMAX_WIDTH + "x" + kMAX_HEIGHT);
                }
            }

            _updateDimensions();

        } catch (ClientException e) {
            log.error(e);
        }
    }

    /*
     * Check width/height and update them so that: - If they are smaller than
     * kMAX_WIDTH/kMAX_HEIGHT, and original width/height are greater, it means
     * we can resize the picture to kMAX_WIDTH/kMAX_HEIGHT. The picture used
     * will then be scaled, but it will be more comfortable for the user.
     * 
     * - If they are greater than kMAX_WIDTH/kMAX_HEIGHT, then picture is
     * resized to the max. values.
     */
    private void _updateDimensions() {
        double coef = 0.0;

        if (imageWidth == 0 || imageHeight == 0) {
            return;
        }

        coef = ((double) kMAX_WIDTH / (double) imageWidth);
        imageWidth = kMAX_WIDTH;
        imageHeight *= coef;

        if (imageHeight > kMAX_HEIGHT) {
            coef = ((double) kMAX_HEIGHT / (double) imageHeight);
            imageHeight = kMAX_HEIGHT;
            imageWidth *= coef;
        }
    }

    public String getimageViewName() {
        return imageViewName;
    }

    public long getImageWidth() {
        return imageWidth;
    }

    public long getImageHeight() {
        return imageHeight;
    }

    public long getImageBoxWidth() {
        return imageWidth < kMIN_WIDTH ? kMIN_WIDTH : imageWidth;
    }

    public long getImageBoxHeight() {
        return imageHeight < kMIN_HEIGHT ? kMIN_HEIGHT : imageHeight;
    }

    public long getOriginalImageWidth() {
        return originalImageWidth;
    }

    public long getOriginalImageHeight() {
        return originalImageHeight;
    }

    public long getMaxWidth() {
        return kMAX_WIDTH;
    }

    public long getMaxHeigth() {
        return kMAX_HEIGHT;
    }

}
