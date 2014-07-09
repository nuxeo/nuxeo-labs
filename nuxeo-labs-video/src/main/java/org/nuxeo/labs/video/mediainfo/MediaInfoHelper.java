/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Frederic Vadon
 */
package org.nuxeo.labs.video.mediainfo;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.runtime.api.Framework;

public class MediaInfoHelper {

	public static final String MEDIAINFO_INFO_COMMAND_LINE = "mediainfo-info";


	// Utility class.
	private MediaInfoHelper() {
	}	
	
	// Calls media info on the input video and returns the output processed in a map of maps (following mediaInfo output pattern).
	public static Map<String, Map<String,String>> getProcessedMediaInfo(Blob video) throws ClientException {
		return processMediaInfo(getRawMediaInfo(video));
	}
	
	/* Get one specific information on the input blob video using mediaInfo.
	 * a call to getSpecificMediaInfo("Video","Width",MyVideo) will get something like 512 pixels
	 */
	
	public static String getSpecificMediaInfo(String key1, String key2, Blob video) throws ClientException {
		return getProcessedMediaInfo(video).get(key1).get(key2);
	}

	// Get the String List from mediainfo without any processing
	public static List<String> getRawMediaInfo(Blob video) throws ClientException {
		if (video == null) {
			return null;
		}

		File file = null;
		try {
			CommandLineExecutorService cleService = Framework.getLocalService(CommandLineExecutorService.class);

			file = File.createTempFile("mediainfoInfo",
					"." + FilenameUtils.getExtension(video.getFilename()));
			video.transferTo(file);

			CmdParameters params = new CmdParameters();
			params.addNamedParameter("inFilePath", file.getAbsolutePath());

			// read the duration with a first command to adjust the best rate:
			ExecResult result = cleService.execCommand(
					MEDIAINFO_INFO_COMMAND_LINE, params);
			return result.getOutput();
		} catch (Exception e) {
			throw ClientException.wrap(e);
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	/* Processes the raw String List from media info and returns a map of maps.
	 * Result of media info is a list of String that follows a specific pattern: 
	 * The result of mediainfo looks like :
	 * General
	 * Format    : AVI
	 * Codec ID  : ISOM
	 * Video
	 * FORMAT/INFO  : AVC
	 * Codexc ID    : avc1
	 * 
	 * This method returns a map of maps that follow this pattern for example General is the first Key and references
	 * a map with Format as a key and AVI as a value.
	 *   
	 */
	public static Map<String, Map<String,String>> processMediaInfo(List<String> input) {

		Map<String, Map<String,String>> output = new HashMap<String, Map<String,String>>();
		int nextIndex=input.indexOf("");
		List<String> remainingList=input;
		List<String> subList;

		while (nextIndex!=-1 && !remainingList.get(0).equals("") ) {
			subList=remainingList.subList(1, nextIndex);
			output.put(remainingList.get(0), processSubList(subList));
			if (nextIndex==remainingList.size()-1)
				break;
			remainingList=remainingList.subList(nextIndex+1, remainingList.size());
			nextIndex=remainingList.indexOf("");
		}
		return output;
	}
	
	// Returns a map from a String List containing a key and a value in each line separated by a ":"
	protected static Map<String, String> processSubList(List<String> subList){
		Map<String,String> subMap = new HashMap<String, String>();
		Iterator<String> subListIterator = subList.iterator();
		String singleInfoLine;
		while (subListIterator.hasNext()) {
			singleInfoLine=subListIterator.next();
			subMap.put(getSingleInfoKey(singleInfoLine), getSingleInfoValue(singleInfoLine)); 			
		}
		return subMap;
	}

	// get the first part of a single entry line from the output String list  : before ":". Removes useless white spaces
	protected static String getSingleInfoKey(String input){
		return input.substring(0, input.indexOf(":")).trim();
	}

	// get the second part of a single entry line from the output String list  : after ":" Removes useless white spaces
	protected static String getSingleInfoValue(String input){
		return input.substring(input.indexOf(":")+1).trim();
	}	








}
