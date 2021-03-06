/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zoo.cat.springcloud.loadbalancer;

import com.google.common.collect.Maps;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import org.zoo.cat.common.bean.context.CatTransactionContext;
import org.zoo.cat.common.enums.CatActionEnum;
import org.zoo.cat.core.concurrent.threadlocal.CatTransactionContextLocal;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The type Cat zone aware LoadBalancer.
 *
 * @author dzc
 */
public class CatZoneAwareLoadBalancer extends ZoneAvoidanceRule {

    private static final Map<String, Server> SERVER_MAP = Maps.newConcurrentMap();

    public CatZoneAwareLoadBalancer() {
    }

    @Override
    public Server choose(final Object key) {
        List<Server> serverList = getLoadBalancer().getAllServers();
        if (null == serverList || serverList.isEmpty() || serverList.size() == 1) {
            return super.choose(key);
        }
        final Server server = super.choose(key);

        final CatTransactionContext catTransactionContext = CatTransactionContextLocal.getInstance().get();

        if (Objects.isNull(catTransactionContext)) {
            return server;
        }

        final String transId = catTransactionContext.getTransId();
        //if try
        if (catTransactionContext.getAction() == CatActionEnum.TRYING.getCode() || catTransactionContext.getAction() == CatActionEnum.NOTICEING.getCode()) {
            SERVER_MAP.put(transId, server);
            return server;
        }

        final Server oldServer = SERVER_MAP.get(transId);

        SERVER_MAP.remove(transId);

        if (Objects.nonNull(oldServer)) {
            for (Server s : serverList) {
                if (Objects.equals(s, oldServer)) {
                    return oldServer;
                }
            }
        }
        return server;
    }

}
