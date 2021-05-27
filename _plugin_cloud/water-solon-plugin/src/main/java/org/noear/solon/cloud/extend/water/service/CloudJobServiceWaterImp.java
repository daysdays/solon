package org.noear.solon.cloud.extend.water.service;

import org.noear.solon.Solon;
import org.noear.solon.cloud.model.HandlerEntity;
import org.noear.solon.cloud.service.CloudJobService;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.util.PrintUtil;
import org.noear.solon.logging.utils.TagsMDC;
import org.noear.water.WaterClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author noear
 */
public class CloudJobServiceWaterImp implements CloudJobService {
    public static final CloudJobServiceWaterImp instance = new CloudJobServiceWaterImp();

    public Map<String, HandlerEntity> jobMap = new LinkedHashMap<>();

    public HandlerEntity get(String name) {
        return jobMap.get(name);
    }

    public void push() {
        if(jobMap.size() == 0){
            return;
        }

        Map<String, String> jobs = new LinkedHashMap<>();
        jobMap.forEach((k, v) -> {
            jobs.put(v.getName(), v.getDescription());
        });

        try {
            WaterClient.job.register(Solon.cfg().appGroup(), Solon.cfg().appName(), jobs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean register(String name, String description, Handler handler) {
        jobMap.put(name, new HandlerEntity(name, description, handler));
        TagsMDC.tag0("CloudJob");
        PrintUtil.warn("CloudJob", "Handler registered name:" + name + ", class:" + handler.getClass().getName());
        TagsMDC.tag0("");
        return true;
    }

    @Override
    public boolean isRegistered(String name) {
        return jobMap.containsKey(name);
    }
}