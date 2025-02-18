package com.qcloud.cmq.client.client;

import com.qcloud.cmq.client.common.ClientConfig;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MQClientManager {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(MQClientManager.class);
    private static final MQClientManager instance = new MQClientManager();
    private final AtomicInteger factoryIndexGenerator = new AtomicInteger();
    private final ConcurrentMap<String/* clientId */, MQClientInstance> factoryTable = new ConcurrentHashMap<String, MQClientInstance>();

    private MQClientManager() {
    }

    public static MQClientManager getInstance() {
        return instance;
    }

    public MQClientInstance getAndCreateMQClientInstance(final ClientConfig clientConfig,
                                                         List<CMQClientInterceptor> interceptors) {
        String clientId = clientConfig.buildMQClientId();
        MQClientInstance instance = this.factoryTable.get(clientId);
        if (null == instance) {
            instance = new MQClientInstance(clientConfig.cloneClientConfig(),
                    this.factoryIndexGenerator.getAndIncrement(), clientId, interceptors);
            MQClientInstance prev = this.factoryTable.putIfAbsent(clientId, instance);
            if (prev != null) {
                instance = prev;
                log.warn("Returned Previous MQClientInstance for clientId:[{}]", clientId);
            } else {
                log.info("Created new MQClientInstance for clientId:[{}]", clientId);
            }
        }

        return instance;
    }

    void removeClientFactory(final String clientId) {
        this.factoryTable.remove(clientId);
    }
}
