package org.nuxeo.labs.operations.usermanagement;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.labs.operations")
public class TestCreateUserOp {

    public static final String USERNAME = "testuser";
    public static final String USERNAME2 = "testuser2";
    public static final String PASSWORD = "yo";
    public static final String FIRSTNAME = "mika";
    public static final String MEMBERS = "members";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected UserManager userManager;

    @Test
    public void testCreate() throws OperationException, LoginException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("username", USERNAME);
        params.put("password", PASSWORD);
        Properties properties = new Properties();
        properties.put("firstName", FIRSTNAME);
        params.put("properties",properties);
        params.put("groups",new StringList(new String[]{MEMBERS}));
        automationService.run(ctx, CreateOrUpdateUserOp.ID, params);
        NuxeoPrincipal principal = userManager.getPrincipal(USERNAME);
        Assert.assertNotNull(principal);
        Assert.assertEquals(FIRSTNAME,principal.getFirstName());
        Assert.assertTrue(principal.getGroups().size()>0);
        Assert.assertEquals(MEMBERS,principal.getGroups().get(0));
    }

    @Test
    public void testUpdate() throws OperationException, LoginException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("username", USERNAME2);
        params.put("password", PASSWORD);
        automationService.run(ctx, CreateOrUpdateUserOp.ID, params);
        NuxeoPrincipal principal = userManager.getPrincipal(USERNAME2);
        Assert.assertNotNull(principal);

        Properties properties = new Properties();
        properties.put("firstName", FIRSTNAME);
        params.put("properties",properties);
        params.put("groups",new StringList(new String[]{MEMBERS}));
        automationService.run(ctx, CreateOrUpdateUserOp.ID, params);

        principal = userManager.getPrincipal(USERNAME2);
        Assert.assertNotNull(principal);
        Assert.assertEquals(FIRSTNAME,principal.getFirstName());
        Assert.assertTrue(principal.getGroups().size()>0);
        Assert.assertEquals(MEMBERS,principal.getGroups().get(0));
    }

}
