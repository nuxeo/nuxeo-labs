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
package org.nuxeo.labs.automation.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.context.ContextHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;

/**
 * A set of Automation functions to handle files on disk (we are server side in all cases).
 * <p>
 * The original purpose of these helpers was to be able to quickly create/handle demo data from JavaScript Automation,
 * just creating a few hundred of documents and avoiding creating a Java plug-in, the marketplace package, installing in
 * the test server, etc.
 * <p>
 * <b>IMPORTANT WARNING ABOUT SECURITY</b><br/>
 * The helpers, by essence, run server side of course. And some helpers here can create/write/delete files and/or
 * folders => <b>MAKE SURE YOU DON't ALLOW EXTERNAL CALLS TO ACCESS FILES/FOLDERS OF YOUR SERVER</b><br/>
 * A helper cannot be called directly by itself, it must be used inside an operation, inside an Automation Chain.<br/>
 * So: please BE VERY CAREFUL, and hard code your values, and/or make sure the paths cannot be get/set from a REST call.
 * Typical example of very, very wrong way of using these helpers would be a chain that accepts a "path" parameter, and
 * call FileUtils.deleteFile() with this path for example. Don't do that.
 * <p>
 * 
 * @since 7.4
 */
public class FileUtils implements ContextHelper {

    /**
     * Load the whole content of the file and return the corresponding blob
     * 
     * @param inPath
     * @return
     * @throws IOException
     * @since 7.4
     */
    public Blob readFileToBlob(String inPath) throws IOException {

        if (StringUtils.isBlank(inPath)) {
            throw new IllegalArgumentException("The file path parameter cannot be empty or null");
        }

        File theFile = new File(inPath);
        return Blobs.createBlob(theFile);
    }

    /**
     * Load the whole content of the file and return it as String. Caller is responsible for:
     * <ul>
     * <li>Making sure the file is a text file</li>
     * <li>There is enough memory to load the whole file in a String</li>
     * </ul>
     * 
     * @param inPath
     * @return
     * @throws IOException
     * @since 7.4
     */
    public String readFileToText(String inPath) throws IOException {

        if (StringUtils.isBlank(inPath)) {
            throw new IllegalArgumentException("The file path parameter cannot be empty or null");
        }

        Blob b = readFileToBlob(inPath);
        return b.getString();
    }

    /**
     * Create a file on disk at <code>inPath</code>
     * 
     * @param inPath
     * @param inOverwrite
     * @return
     * @throws IOException
     * @since 7.4
     */
    public File createFile(String inPath, boolean inOverwrite) throws IOException {
        File f = new File(inPath);

        if (inOverwrite && f.exists()) {
            f.delete();
        }

        if (f.createNewFile()) {
            return f;
        } else {
            return null;
        }
    }

    public File createFile(String inPath) throws IOException {
        return createFile(inPath, false);
    }

    /**
     * Appends the string to the file, creates the file if it does not exist.
     * 
     * @param inFile
     * @param inWhat
     * @return
     * @throws IOException
     * @since 7.4
     */
    public File appendToFile(File inFile, String inWhat) throws IOException {

        org.apache.commons.io.FileUtils.writeStringToFile(inFile, inWhat, Charset.defaultCharset(), true);
        return inFile;
    }

    /**
     * SECURITY WARNING: Using this API should be called with hard coded value. Do not allow parameter passed from a
     * REST call for example.
     * 
     * @param inPath
     * @since 7.4
     */
    public void deleteFile(String inPath) {
        if (StringUtils.isNotBlank(inPath)) {
            deleteFile(new File(inPath));
        }
    }

    /**
     * SECURITY WARNING: Using this API should be called with hard coded value. Do not allow parameter passed from a
     * REST call for example.
     * 
     * @param inPath
     * @since 7.4
     */
    public void deleteFile(File inFile) {

        if (inFile != null && inFile.exists()) {
            inFile.delete();
        }
    }

    /**
     * Stores the blob to the given path.
     * <p>
     * Again, current user (nuxeo server user) must have enough rights in the directory.
     * 
     * @param inBlob
     * @param inPath
     * @throws IOException
     * @since 7.4
     */
    public void saveBlob(Blob inBlob, String inPath) throws IOException {

        File dest = new File(inPath);
        inBlob.transferTo(dest);
    }

    /**
     * Returns an array of the full paths of all the non-hidden/non folder elements found at inFolderPath
     * 
     * @param inFolderPath
     * @return
     * @since 7.4
     */
    public ArrayList<String> getFiles(String inFolderPath) {

        File folder = new File(inFolderPath);
        ArrayList<String> files = new ArrayList<String>();
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile() && !f.isHidden()) {
                    files.add(f.getAbsolutePath());
                }
            }
        }

        return files;
    }

    /**
     * Returns an array of the full paths of all the non-hidden folder elements found at inFolderPath
     * 
     * @param inFolderPath
     * @return
     * @since 7.4
     */
    public ArrayList<String> getFolders(String inFolderPath) {

        File folder = new File(inFolderPath);
        ArrayList<String> files = new ArrayList<String>();
        if (folder.exists() && folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isDirectory() && !f.isHidden()) {
                    files.add(f.getAbsolutePath());
                }
            }
        }

        return files;
    }

    /**
     * Creates a new folder in inContainerPath, returns true if the folder was created, false otherwise.
     * 
     * @param inContainerPath
     * @param inFolderName
     * @return
     * @since 7.4
     */
    public boolean createFolder(String inContainerPath, String inFolderName) {

        boolean result = false;
        File folder = new File(inContainerPath);
        if (folder.exists() && folder.isDirectory()) {
            File newFolder = new File(inContainerPath, inFolderName);
            result = newFolder.mkdir();
        }

        return result;
    }

}
