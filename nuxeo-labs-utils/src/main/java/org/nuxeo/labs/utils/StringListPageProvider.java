/*
 * (C) Copyright 2016 Nuxeo (http://nuxeo.com/) and others.
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
package org.nuxeo.labs.utils;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.collections.api.CollectionConstants;
import org.nuxeo.ecm.collections.api.CollectionManager;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.query.api.AbstractPageProvider;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.api.PageSelections;
import org.nuxeo.runtime.api.Framework;

/**
 * Return an ordered list of DocumentModel, based on a StringList field.
 * <p>
 * The xpath is a parameter of the PahgeProvider (see
 * nuxeo-labs-utils-stringlist_pageprovider.xml)
 * <p>
 * If this parameter is empty and the document is a Collection, the page
 * provider uses the field of the collection
 * <p>
 * If you to use this page provider with different field, you must contribute as
 * many extension moints and just change the name of the page provider and the
 * xpath parameter (each one using this
 * org.nuxeo.labs.utils.StringListPagePaovider class)
 * <p>
 * A warning, but common sense warning: If will probably not scale with lists of
 * touhsands, hundred thousands, millions of elements.
 *
 * @since 8.10
 */
public class StringListPageProvider extends AbstractPageProvider<DocumentModel> implements PageProvider<DocumentModel> {

	private static final long serialVersionUID = 1L;

	public static final String CORE_SESSION_PROPERTY = "coreSession";

	public static final String CURRENT_DOCUMENT_PROPERTY = "currentDoc";

	public static final String XPATH_PROPERTY = "xpath";

	protected static final Log log = LogFactory.getLog(StringListPageProvider.class);

	protected CoreSession getCoreSession() {
		Object session = getProperties().get(CORE_SESSION_PROPERTY);
		if (session != null && session instanceof CoreSession) {
			return (CoreSession) session;
		}
		return null;
	}

	protected DocumentModel getCurrentDocument() {
		Object docObj = getProperties().get(CURRENT_DOCUMENT_PROPERTY);
		if (docObj != null && docObj instanceof DocumentModel) {
			return (DocumentModel) docObj;
		}
		return null;
	}

	protected String getXPath() {
		Object docObj = getProperties().get(XPATH_PROPERTY);
		if (docObj != null && docObj instanceof String) {
			return (String) docObj;
		}
		return null;
	}

	@Override
	public PageSelections<DocumentModel> getCurrentSelectPage() {
		return super.getCurrentSelectPage();
	}

	@Override
	public List<DocumentModel> getCurrentPage() {

		DocumentModelList docs = new DocumentModelListImpl();

		CoreSession session = getCoreSession();
		if (session == null) {
			log.error("No core session available in the context of this PageProvider");
			return docs;
		}

		DocumentModel doc = getCurrentDocument();
		if (doc == null) {
			log.error("No current document available in the context of this PageProvider");
			return docs;
		}

		String xpath = getXPath();
		if (StringUtils.isBlank(xpath)) {
			if (Framework.getService(CollectionManager.class).isCollection(doc)) {
				xpath = CollectionConstants.COLLECTION_DOCUMENT_IDS_PROPERTY_NAME;
			} else {
				log.error("No xpath provided for this PageProvider, and current document is not a collection");
				return docs;
			}
		}

		doc.refresh();

		String[] uuids = (String[]) doc.getPropertyValue(xpath);
		if (uuids != null && uuids.length > 0) {
			DocumentModel subDoc;
			for (String uid : uuids) {
				subDoc = session.getDocument(new IdRef(uid));
				docs.add(subDoc);
			}
		}

		return docs;

	}

}
