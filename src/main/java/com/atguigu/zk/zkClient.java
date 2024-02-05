package com.atguigu.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class zkClient {
    ZooKeeper zkClient;
    @Before
    public void init() throws IOException {
        String connectString = "hadoop100:2181,hadoop101:2181,hadoop102:2181";
        int sessionTimeout = 200000000;
        zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                List<String> children = null;
                try {
                    children = zkClient.getChildren("/", true);
                    for (String child : children) {
                        System.out.println(child);
                    }
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        );
    }
    @Test
    public void create() throws InterruptedException, KeeperException {
        String nodeCreate = zkClient.create("/atguigu", "ss.avi".getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(nodeCreate);
    }
    @Test
    public void getChild() throws InterruptedException, KeeperException {
        List<String> children = zkClient.getChildren("/", true);
        for (String child : children) {
            System.out.println(child);
        }
        Thread.sleep(Long.MAX_VALUE);
    }
    @Test
    public void exist() throws InterruptedException, KeeperException {
        Stat exists = zkClient.exists("/atguigu", false);
        System.out.println(exists==null?"no":"yes");
    }
}
