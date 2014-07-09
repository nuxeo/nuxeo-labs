package org.nuxeo.labs.images;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.platform.convert.plugins.PDF2ImageConverter;

public class GenericImageMagickConverter extends PDF2ImageConverter {
	public static final Log log = LogFactory
			.getLog(GenericImageMagickConverter.class);
	@Override
	protected Map<String, String> getCmdStringParameters(BlobHolder blobHolder,
			Map<String, Serializable> parameters) throws ConversionException {
		// TODO Auto-generated method stub
		Map<String, String> cmdStringParameters= super.getCmdStringParameters(blobHolder, parameters);
		
		Map<String, String> stringParameters = new HashMap<String, String>();
		Set<String> parameterNames = parameters.keySet();
		for (String parameterName : parameterNames) {
			//targetFilePath is computed by the method of the superType
			if (!parameterName.equals("targetFilePath"))
			stringParameters.put(parameterName,
					(String) parameters.get(parameterName));
		}
		cmdStringParameters.putAll(stringParameters);
		return cmdStringParameters;
	}
	
}
