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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Thibaud Arguillere
 * 
 * @since 5.9.6
 * 
 *        This class avoids having the same code in several operations, that's
 *        all.
 * 
 */
public class ConversionUtils {

    public static final Log myLog = LogFactory.getLog(ConversionUtils.class);

    public static Blob convert(String inConverterName, Blob inBlob,
            Map<String, Serializable> inParams, String inTargetFileName) {
        try {

            ConversionService conversionService = Framework.getService(ConversionService.class);

            BlobHolder source = new SimpleBlobHolder(inBlob);
            BlobHolder result = conversionService.convert(inConverterName,
                    source, inParams);
            Blob convertedBlob = result.getBlob();
            if (inTargetFileName != null && !inTargetFileName.isEmpty()) {
                convertedBlob.setFilename(inTargetFileName);
            }
            return convertedBlob;

        } catch (Exception e) {
            myLog.error("Error during conversion", e);
        }

        return null;
    }

    public static Blob convert(String inConverterName, Blob inBlob,
            Map<String, Serializable> inParams) {

        return ConversionUtils.convert(inConverterName, inBlob, inParams, null);
    }

    /*
     * Centralize handling of the targetFileName (used in at least 3 operations
     * => less code in the operation itself)
     */
    public static String updateTargetFileName(Blob inBlob,
            String inTargetFileName, String inTargetFileSuffix) {

        String updatedName = "";

        if (inTargetFileName == null || inTargetFileName.isEmpty()) {
            updatedName = inBlob.getFilename();
        } else {
            updatedName = inTargetFileName;
        }

        if (inTargetFileSuffix != null && !inTargetFileSuffix.isEmpty()) {
            updatedName = ConversionUtils.addSuffixToFileName(updatedName,
                    inTargetFileSuffix);
        }

        return updatedName;
    }

    /*
     * Adds the suffix before the file extension, if any
     */
    public static String addSuffixToFileName(String inFileName, String inSuffix) {
        if (inFileName == null || inFileName.isEmpty() || inSuffix == null
                || inSuffix.isEmpty()) {
            return inFileName;
        }

        int dotIndex = inFileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return inFileName + inSuffix;
        }

        return inFileName.substring(0, dotIndex) + inSuffix
                + inFileName.substring(dotIndex);
    }
}
