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

package org.zoo.cat.common.bean.entity;

import com.google.common.collect.Lists;
import lombok.Data;

import org.zoo.cat.common.enums.CatActionEnum;
import org.zoo.cat.common.enums.CatRoleEnum;
import org.zoo.cat.common.utils.IdWorkerUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * The CatTransaction.
 *
 * @author dzc
 */
@Data
public class CatTransaction implements Serializable {

    private static final long serialVersionUID = -6792063780987394917L;

    /**
     *  id.
     */
    private Long id;
    
    /**
     * appName.
     */
    private String appName;
    
    /**
     * transaction id.
     */
    private String transId;
    
    /**
     * trans_type.
     */
    private String transType;

    /**
     * nodeTransId Can be empty, node transaction id.
     */
    private String nodeTransId;

    /**
     * transaction status.
     * {@linkplain CatActionEnum}
     */
    private int status;

    /**
     * transaction role .
     * {@linkplain CatRoleEnum}
     */
    private int role;
    
    /**
     * retryMax.
     */
    private volatile int retryMax;

    /**
     * retriedCount.
     */
    private volatile int retriedCount;
    
    /**
     * timeOut.
     */
    private volatile int timeoutMills;

    /**
     * createTime.
     */
    private Date createTime;

    /**
     * lastTime.
     */
    private Date lastTime;

    /**
     * version number mysql optimistic lock control.
     */
    private Integer version = 1;

    /**
     * pattern.
     */
    private Integer pattern;

    /**
     * Call interface name.
     */
    private String targetClass;

    /**
     * Call interface method name.
     */
    private String targetMethod;

    /**
     * confirm Method.
     */
    private String confirmMethod;

    /**
     * cancel Method.
     */
    private String cancelMethod;

    /**
     * A collection of methods that participate in coordination.
     */
    private List<CatParticipant> catParticipants;

    public CatTransaction() {
        this.transId = IdWorkerUtils.getInstance().createUUID();
        this.createTime = new Date();
        this.lastTime = new Date();
        catParticipants = Lists.newCopyOnWriteArrayList();

    }

    public CatTransaction(final String transId) {
        this.transId = transId;
        this.createTime = new Date();
        this.lastTime = new Date();
        catParticipants = Lists.newCopyOnWriteArrayList();
    }

    /**
     * registerParticipant.
     *
     * @param catParticipant {@linkplain CatParticipant}
     */
    public void registerParticipant(final CatParticipant catParticipant) {
        catParticipants.add(catParticipant);
    }

}
