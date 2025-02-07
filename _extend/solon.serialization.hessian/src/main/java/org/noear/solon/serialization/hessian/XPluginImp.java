package org.noear.solon.serialization.hessian;

import org.noear.solon.SolonApp;
import org.noear.solon.core.Bridge;
import org.noear.solon.core.Plugin;
import org.noear.solon.core.handle.RenderManager;

public class XPluginImp implements Plugin {
    public static boolean output_meta = false;

    @Override
    public void start(SolonApp app) {
        output_meta = app.cfg().getInt("solon.output.meta", 0) > 0;

        HessianRender render = new HessianRender();

        //XRenderManager.register(render);
        RenderManager.mapping("@hessian",render);
        Bridge.actionExecutorAdd(new HessianActionExecutor());
    }
}
