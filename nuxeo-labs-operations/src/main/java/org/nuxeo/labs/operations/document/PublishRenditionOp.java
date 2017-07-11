/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.labs.operations.document;

import java.util.Collections;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.rendition.publisher.RenditionPublicationFactory;

/**
 *
 * @since 8.10
 */
@Operation(id = PublishRenditionOp.ID, category = Constants.CAT_DOCUMENT, label = "Document Publish Rendition", description = "Publishes the rendition to the target Section, returns the published document. Current user must have enoiugh right to publish in this section.")
public class PublishRenditionOp {

	public static final String ID = "Document.PublishRenditionOp";

	@Context
	protected CoreSession coreSession;

	@Context
	protected PublisherService publisherService;

	// ID or Path
	@Param(name = "targetSectionRef", required = true)
	protected String targetSectionRef;

	@Param(name = "renditionName", required = true)
	protected String renditionName;

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(DocumentModel inDoc) {

		// Important: So far, 8.10, 9.x... there is only _one_ publication tree
		String defaultTreeName = publisherService.getAvailablePublicationTree().get(0);
		PublicationTree tree = publisherService.getPublicationTree(defaultTreeName, coreSession, null);

		String targetPath;
		if (targetSectionRef.startsWith("/")) {
			targetPath = targetSectionRef;
		} else {
			DocumentModel sectionDoc = coreSession.getDocument(new IdRef(targetSectionRef));
			targetPath = sectionDoc.getPathAsString();
		}

		PublicationNode targetNode = tree.getNodeByPath(targetPath);

		SimpleCorePublishedDocument publishedDocument = (SimpleCorePublishedDocument) tree.publish(inDoc, targetNode,
				Collections.singletonMap(RenditionPublicationFactory.RENDITION_NAME_PARAMETER_KEY, renditionName));

		DocumentModel proxy = publishedDocument.getProxy();
		return proxy;
	}

}
