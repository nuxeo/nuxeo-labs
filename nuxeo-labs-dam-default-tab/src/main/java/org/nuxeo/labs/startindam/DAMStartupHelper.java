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
 *     Fred Vadon
 */

package org.nuxeo.labs.startindam;

import static org.jboss.seam.ScopeType.SESSION;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.dam.seam.DamSearchActions;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;
import org.nuxeo.ecm.platform.ui.web.api.WebActions;
import org.nuxeo.ecm.webapp.helpers.StartupHelper;

@Name("startupHelper")
@Scope(SESSION)
@Install(precedence = Install.DEPLOYMENT)
public class DAMStartupHelper extends StartupHelper {

    private static final long serialVersionUID = 3248232383219879845L;

    private static final Log log = LogFactory.getLog(StartupHelper.class);

    protected static final String DAM_TAB = WebActions.MAIN_TABS_CATEGORY + ":"
            + "dam";

    protected static final String ASSETS_VIEW = "assets";

    @In(create = true)
    protected transient DamSearchActions damSearchActions;

    @In(create = true)
    protected ContentViewActions contentViewActions;

    @Override
    /**
     * Initializes the context with the principal id, and tries to connect to
     * the default server if any then: - if the server is empty, create a new
     * domain with title 'domainTitle' - Initialize and show the dam view.
     * <p>
     * If several servers are available, let the user choose.
     * 
     * @return the view id of the contextually computed startup page
     */
    @Begin(id = "#{conversationIdGenerator.nextMainConversationId}", join = true)
    public String initDomainAndFindStartupPage(String domainTitle, String viewId) {
        try {
            // delegate server initialized to the default helper
            String result = initServerAndFindStartupPage();

            if (SERVERS_VIEW.equals(result)) {
                return result;
            }
            // Set the tab
            webActions.setCurrentTabIds(DAM_TAB);
            // Initialize the page provider to be able to select the first doc
            contentViewActions.getContentViewWithProvider(damSearchActions.getCurrentContentViewName());
            damSearchActions.updateCurrentDocument();
            // Show the view
            return ASSETS_VIEW;

        } catch (ClientException e) {
            // avoid pages.xml contribution to catch exceptions silently
            // hiding the cause of the problem to developers
            // TODO: remove this catch clause if we find a way not to make it
            // fail silently
            log.error(
                    "error while initializing the Seam context with a CoreSession instance: "
                            + e.getMessage(), e);
            return null;
        }
    }
}
