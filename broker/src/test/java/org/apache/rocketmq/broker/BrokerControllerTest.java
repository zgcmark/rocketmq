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

package org.apache.rocketmq.broker;

import java.io.File;

import com.sun.corba.se.pept.broker.Broker;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.remoting.netty.NettyClientConfig;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.apache.rocketmq.store.config.FlushDiskType;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BrokerControllerTest {

    @Test
    public void testBrokerRestart() throws Exception {
        BrokerController brokerController = new BrokerController(
            new BrokerConfig(),
            new NettyServerConfig(),
            new NettyClientConfig(),
            new MessageStoreConfig());
        assertThat(brokerController.initialize());
        brokerController.start();
        brokerController.shutdown();
    }

    @After
    public void destroy() {
        UtilAll.deleteFile(new File(new MessageStoreConfig().getStorePathRootDir()));
    }


    public static void main(String[] args) throws Exception {
        NettyServerConfig nettyServerConfig = new NettyServerConfig();

        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        nettyServerConfig.setListenPort(9011);
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setBrokerName("test-01");
        //这个需要注意的一个点是,broker不是只需要注册到一个nameServer，
        // 而是需要注册到所有的nameServer上的,因为nameServer集群的本身是无状态的，服务的节点之间是没有消息进行同步的
        brokerConfig.setNamesrvAddr("127.0.0.1:9876");

        //配置消息存储服务配置
        MessageStoreConfig messageStoreConfig = new MessageStoreConfig();
        //现在还不太清楚具体有什么作用，为什么这个需要用字符串存储 2019-11-11
        messageStoreConfig.setDeleteWhen("04");
        //同上
        messageStoreConfig.setFileReservedTime(48);
        //异步刷盘
        messageStoreConfig.setFlushDiskType(FlushDiskType.ASYNC_FLUSH);
        //是否允许副本
        messageStoreConfig.setDuplicationEnable(false);
        BrokerController brokerController = new BrokerController(brokerConfig,nettyServerConfig,nettyClientConfig,messageStoreConfig);
        brokerController.initialize();
        brokerController.start();

        Thread.sleep(DateUtils.MILLIS_PER_DAY);
    }
}
