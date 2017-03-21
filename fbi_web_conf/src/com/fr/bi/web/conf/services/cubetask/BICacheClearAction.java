package com.fr.bi.web.conf.services.cubetask;

import com.finebi.cube.data.disk.BICubeDiskPrimitiveDiscovery;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.json.JSONObject;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by kary on 2016/9/12.
 */
public class BICacheClearAction extends AbstractBIConfigureAction {
    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        clearCache();
        WebUtils.printAsJSON(res, new JSONObject().put("result", "success"));
    }

    @Override
    public String getCMD() {
        return "cache_clear";
    }

    private void clearCache() {
        BILoggerFactory.getLogger().info("start clear readers");
        BICubeDiskPrimitiveDiscovery.getInstance().forceRelease();
        BICubeDiskPrimitiveDiscovery.getInstance().finishRelease();
        BILoggerFactory.getLogger().info("readers clear finished");
    }
}
