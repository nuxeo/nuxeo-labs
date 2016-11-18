package org.nuxeo.labs.operations.usermanagement;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.labs.operation")
public class TestCreateGroupOp {

    public static final String GROUPNAME = "groupname";
    public static final String GROUPNAME2 = "groupname2";
    public static final String GROUPLABEL = "grouplabel";
    public static final String GROUPLABEL2 = "grouplabel2";

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Inject
    protected UserManager userManager;

    @Test
    public void testCreate() throws OperationException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("groupname", GROUPNAME);
        params.put("grouplabel", GROUPLABEL);
        automationService.run(ctx, CreateOrUpdateGroupOp.ID, params);
        NuxeoGroup group = userManager.getGroup(GROUPNAME);
        Assert.assertNotNull(group);
        Assert.assertEquals(group.getLabel(),GROUPLABEL);
    }

    @Test
    public void testUpdate() throws OperationException {
        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        params.put("groupname", GROUPNAME2);
        params.put("grouplabel", GROUPLABEL);
        automationService.run(ctx, CreateOrUpdateGroupOp.ID, params);
        NuxeoGroup group = userManager.getGroup(GROUPNAME2);
        Assert.assertNotNull(group);
        Assert.assertEquals(group.getLabel(),GROUPLABEL);

        params.put("grouplabel", GROUPLABEL2);
        automationService.run(ctx, CreateOrUpdateGroupOp.ID, params);
        group = userManager.getGroup(GROUPNAME2);
        Assert.assertNotNull(group);
        Assert.assertEquals(group.getLabel(),GROUPLABEL2);
    }

}
