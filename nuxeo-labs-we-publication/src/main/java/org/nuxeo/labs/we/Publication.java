/**
 * 
 */

package org.labs.we;

import java.util.HashMap;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.webengine.forms.FormData;
import org.nuxeo.ecm.webengine.model.Resource;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;


/**
 * The root entry for the WebEngine module. 
 * @author fvadon
 */
@Path("/publication")
@Produces("text/html;charset=UTF-8")
@WebObject(type="Publication")
public class Publication extends ModuleRoot {

	private static final Log log = LogFactory.getLog(Publication.class);

	@GET
	public Object doGet() {
		return getView("index");
	}

	@Path("section/{id}")
	public Resource getsectionContent(@PathParam("id") String id){
		return newObject("Section", id);

	}


	protected void initialize(Object... arg) {
		CoreSession session = null;
		DocumentModelList rootSections = null;
		DocumentModelList higherSections = null;
		DocumentModelList testme=null;
		HashMap latestPublicationsMap=new HashMap<DocumentModel, DocumentModelList>(); 
		session = ctx.getCoreSession();
		if (session != null) {

			String query = "SELECT * FROM Document WHERE ecm:currentLifeCycleState != 'deleted' and ecm:primaryType='SectionRoot'";
			try {
				rootSections = session.query(query);
				if(!rootSections.isEmpty())
				{
					Iterator<DocumentModel> rootSectionIterator = rootSections.iterator();

					query = "SELECT * FROM Document WHERE ecm:currentLifeCycleState != 'deleted' and ecm:primaryType='Section' AND (ecm:parentId='";
					query = query.concat(rootSectionIterator.next().getId());
					while (rootSectionIterator.hasNext()){ //in case there are several sectionRoots
						query = query.concat(" 'OR ecm:parentId='");
						query = query.concat(rootSectionIterator.next().getId());
					}
					query = query.concat("')");
					higherSections = session.query(query);
				}
				ctx.setProperty("higherSections", higherSections);
				latestPublicationsMap = getLatestPublicationMap(session, higherSections);
				ctx.setProperty("latestPublicationsMap", latestPublicationsMap);

				log.warn(higherSections.size());
			} catch (ClientException e) {
				log.error(e);
			}

		}
	}

	protected HashMap getLatestPublicationMap(CoreSession session, DocumentModelList higherSections) throws ClientException {
		HashMap<String, DocumentModelList> latestPublicationsMap=new HashMap<String, DocumentModelList>();
		if(!higherSections.isEmpty()){
			Iterator<DocumentModel> higherSectionsIterator=higherSections.iterator();
			String query;
			DocumentModelList sectionChildren;
			DocumentModel higherSection;
			while (higherSectionsIterator.hasNext()){ 
				higherSection=higherSectionsIterator.next();
				query = "select * from Document where ecm:primaryType!='Section' and ecm:path startswith '" + higherSection.getPathAsString() +"' order by dc:issued desc" ;
				sectionChildren=session.query(query,3);
				latestPublicationsMap.put(higherSection.getId(), sectionChildren);
			}

		}		
		return latestPublicationsMap;
	}

	String searchPattern;

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}
	
	@POST
	@Path("search")
	@Produces("text/html")
	public Object searchNotes() {

		FormData data = ctx.getForm();
		searchPattern = (String) data.get("searchPattern")[0];
		CoreSession session = ctx.getCoreSession();
		String query = "select * from Document where ecm:currentLifeCycleState != 'deleted' and ecm:primaryType!='Section' and ecm:isProxy=1 and ecm:fulltext='"
				+ searchPattern + "'";
		log.warn(query);
		DocumentModelList searchResult = null;
		try {
			searchResult = session.query(query);
		} catch (ClientException e) {
			log.error(e);
		}
		ctx.setProperty("searchResult", searchResult);
		return getView("search");

	}
	
    public String getDownloadURL(DocumentModel doc) throws Exception {
        if (doc == null) {
            return null;
        }

        String filename = (String) doc.getPropertyValue("file:filename");

        String downloadURL = getNuxeoContextPath() + "/";
        downloadURL += "nxbigfile" + "/";
        downloadURL += doc.getRepositoryName() + "/";
        downloadURL += doc.getRef().toString() + "/";
        downloadURL += "blobholder:0" + "/";
        downloadURL += filename;

        return downloadURL;
    }
    private String nuxeoContextPath;

    private String getNuxeoContextPath() {
        if (nuxeoContextPath == null) {
            nuxeoContextPath = Framework.getProperty("org.nuxeo.ecm.contextPath");
        }
        return nuxeoContextPath;
    }
	@GET
	@Path("about")
	@Produces("text/html")
	public Object getAbout() {
		return getView("about");

	}



}
