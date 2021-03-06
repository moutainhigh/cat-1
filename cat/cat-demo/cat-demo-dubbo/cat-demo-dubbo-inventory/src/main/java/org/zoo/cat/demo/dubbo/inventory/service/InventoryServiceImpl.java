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

package org.zoo.cat.demo.dubbo.inventory.service;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zoo.cat.annotation.Cat;
import org.zoo.cat.common.exception.CatRuntimeException;
import org.zoo.cat.demo.dubbo.inventory.api.dto.InventoryDTO;
import org.zoo.cat.demo.dubbo.inventory.api.entity.InventoryDO;
import org.zoo.cat.demo.dubbo.inventory.api.service.InventoryService;
import org.zoo.cat.demo.dubbo.inventory.mapper.InventoryMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Inventory service.
 *
 * @author dzc
 */
@Service("inventoryService")
public class InventoryServiceImpl implements InventoryService {

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private static AtomicInteger tryCount = new AtomicInteger(0);

    private static AtomicInteger confirmCount = new AtomicInteger(0);

    private final InventoryMapper inventoryMapper;

    /**
     * Instantiates a new Inventory service.
     *
     * @param inventoryMapper the inventory mapper
     */
    @Autowired(required = false)
    public InventoryServiceImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }


    /**
     * 扣减库存操作
     * 这一个tcc接口
     *
     * @param inventoryDTO 库存DTO对象
     * @return true
     */
    @Override
    @Cat(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public Boolean decrease(InventoryDTO inventoryDTO) {
        inventoryMapper.decrease(inventoryDTO);
        final int i = tryCount.incrementAndGet();
        LOGGER.info("调用了inventory  try " + i + " 次");
        return true;
    }

    @Override
    @Cat(confirmMethod = "confirmInline", cancelMethod = "cancelInline")
    public List<InventoryDTO> testInLine() {
        System.out.println("test in line for rpc.......");
        return new ArrayList<>();
    }


    public List<InventoryDTO> confirmInline() {
        System.out.println("confirmInline in line for rpc.......");
        List<InventoryDTO> rs = Lists.newArrayList();
        InventoryDTO dto = new InventoryDTO();
        dto.setProductId("1111");
        dto.setCount(9);
        rs.add(dto);
        return rs;
    }


    public List<InventoryDTO> cancelInline() {
        System.out.println("cancelTest in line for rpc.......");
        return new ArrayList<>();
    }


    @Override
    public Boolean testDecrease(InventoryDTO inventoryDTO) {
        inventoryMapper.decrease(inventoryDTO);
        return true;
    }

    /**
     * 获取商品库存信息
     *
     * @param productId 商品id
     * @return InventoryDO
     */
    @Override
    public InventoryDO findByProductId(String productId) {
        return inventoryMapper.findByProductId(productId);
    }

    @Override
    @Cat(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    public String mockWithTryException(InventoryDTO inventoryDTO) {
        //这里是模拟异常所以就直接抛出异常了
        throw new CatRuntimeException("库存扣减异常！");
    }

    @Override
    @Cat(confirmMethod = "confirmMethod", cancelMethod = "cancelMethod")
    @Transactional(rollbackFor = Exception.class)
    public Boolean mockWithTryTimeout(InventoryDTO inventoryDTO) {
        try {
            //模拟延迟 当前线程暂停10秒
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int decrease = inventoryMapper.decrease(inventoryDTO);
        if (decrease != 1) {
            throw new CatRuntimeException("库存不足");
        }
        return true;
    }

    @Override
    @Cat(confirmMethod = "confirmMethodException", cancelMethod = "cancelMethod")
    @Transactional(rollbackFor = Exception.class)
    public String mockWithConfirmException(InventoryDTO inventoryDTO) {
        final int decrease = inventoryMapper.decrease(inventoryDTO);
        if (decrease != 1) {
            throw new CatRuntimeException("库存不足");
        }
        return "success";
    }

    @Override
    @Cat(confirmMethod = "confirmMethodTimeout", cancelMethod = "cancelMethod")
    @Transactional(rollbackFor = Exception.class)
    public Boolean mockWithConfirmTimeout(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存确认方法mockWithConfirmTimeout===========");
        final int decrease = inventoryMapper.decrease(inventoryDTO);
        if (decrease != 1) {
            throw new CatRuntimeException("库存不足");
        }
        return true;
    }

    /**
     * Confirm method timeout boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmMethodTimeout(InventoryDTO inventoryDTO) {
        try {
            //模拟延迟 当前线程暂停11秒
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("==========调用扣减库存确认方法===========");
        inventoryMapper.decrease(inventoryDTO);
        return true;
    }

    /**
     * Confirm method exception boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmMethodException(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存确认方法===========");
        final int decrease = inventoryMapper.decrease(inventoryDTO);
        if (decrease != 1) {
            throw new CatRuntimeException("库存不足");
        }
        return true;
    }

    /**
     * Confirm method boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    public Boolean confirmMethod(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存confirm方法===========");
        inventoryMapper.confirm(inventoryDTO);
        final int i = confirmCount.incrementAndGet();
        LOGGER.info("调用了inventory confirm " + i + " 次");
        return true;
    }

    /**
     * Cancel method boolean.
     *
     * @param inventoryDTO the inventory dto
     * @return the boolean
     */
    public Boolean cancelMethod(InventoryDTO inventoryDTO) {
        LOGGER.info("==========调用扣减库存取消方法===========");
        inventoryMapper.cancel(inventoryDTO);
        return true;
    }

}
