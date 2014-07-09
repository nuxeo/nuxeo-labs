/**
 * 
 */

package org.labs.we;

import java.util.HashMap;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.webengine.model.Resource;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;


/**
 * The root entry for the WebEngine module. 
 * @author fvadon
 */

@Produces("text/html;charset=UTF-8")
@WebObject(type="Section")
public class Section extends Publication {

	private static final Log log = LogFactory.getLog(Section.class);

	@GET
	public Object doGet() {
		return getView("index");
	}

	@Path("section/{id}")
	public Resource getsectionContent(@PathParam("id") String id){
		return newObject("Section", id);

	}


	protected void initialize(Object... arg) {
		String id = (String) arg[0];
		CoreSession session = null;
		DocumentModel currentSection;
		DocumentModelList childSections = null;
		DocumentModelList childrenContent;
		session = ctx.getCoreSession();
		HashMap latestPublicationsMap=new HashMap<DocumentModel, DocumentModelList>(); 
		if (session != null) {

			try {
				currentSection = session.query("SELECT * FROM Document WHERE ecm:uuid='"+id+"'").get(0);
				childSections = session.query("SELECT * FROM Section WHERE  ecm:currentLifeCycleState != 'deleted' and ecm:parentId='"+currentSection.getId()+"'");
				childrenContent = session.query("SELECT * FROM Document WHERE  ecm:primaryType!= 'Section' and ecm:currentLifeCycleState != 'deleted' and ecm:parentId='"+currentSection.getId()+"'");
				ctx.setProperty("childSections", childSections);
				ctx.setProperty("currentSection", currentSection);
				ctx.setProperty("childrenContent", childrenContent);
				latestPublicationsMap = getLatestPublicationMap(session, childSections);
				ctx.setProperty("latestPublicationsMap", latestPublicationsMap);

				log.warn(childSections.size());
			} catch (ClientException e) {
				log.error(e);
			}

		}
	}
	



}
