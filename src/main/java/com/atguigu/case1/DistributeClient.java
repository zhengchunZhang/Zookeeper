package com.atguigu.case1;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DistributeClient {
    String connectString = "hadoop100:2181,hadoop101:2181,hadoop102:2181";
    int sessionTimeout = 20000000;
    private ZooKeeper zooKeeper;
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        DistributeClient client = new DistributeClient();
        client.getConnect();
        client.getServerList();
        client.business();
    }

    private void getServerList() throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren("/servers", true);
        ArrayList<String> servers = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {

            byte[] data = zooKeeper.getData("/servers/" + children.get(i), false, null);
            servers.add(new String(data));
        }
        System.out.println(servers);
    }

    private void business() throws InterruptedException {
        Thread.sleep(Long.MAX_VALUE);
    }

    private void regist(String hostname) throws InterruptedException, KeeperException {
        String s = zooKeeper.create("/servers/"+hostname, hostname.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(hostname + "isonline");
    }

    private void getConnect() throws IOException {
        zooKeeper = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                try {
                    getServerList();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
