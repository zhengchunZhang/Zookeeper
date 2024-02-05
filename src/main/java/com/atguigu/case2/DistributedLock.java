package com.atguigu.case2;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DistributedLock {
    String connectString = "hadoop100:2181,hadoop101:2181,hadoop102:2181";
    int sessionTimeout = 2000;
    private final ZooKeeper zooKeeper;

    private CountDownLatch connectLatch  = new CountDownLatch(1);

    private CountDownLatch waitLatch  = new CountDownLatch(1);
    private String waitPath;
    private String currentMode;

    public DistributedLock() throws IOException, InterruptedException, KeeperException {
        //获取连接
         zooKeeper = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                //监听到前一个节点已经下线
                //connectLatch 如果连上需要释放
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    connectLatch.countDown();
                }
                //waitLatch 需要释放
                if(watchedEvent.getType() == Event.EventType.NodeDeleted&&watchedEvent.getPath().equals(waitPath)){
                    waitLatch.countDown();
                }
            }
        });
         //等待zk正常连接后，才往下走
         connectLatch.await();
        //判断根节点/locks是否存在
        Stat stat = zooKeeper.exists("/locks", false);
        if(stat == null) {
            //创建根节点
            zooKeeper.create("/locks","locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        }
    }
    //加锁
    public void zklock(){
        //创建对应的临时的带序号的节点
        try {
            currentMode = zooKeeper.create("/locks/" + "seq-", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            //判断创建的节点是否是最小的节点，如果是获取到锁；不是，监听前一个序号最小的节点
            List<String> children = zooKeeper.getChildren("/locks", false);
            //如果children只有一个值，那就直接获取锁；如果有多个节点，需要判断谁最小
            if(children.size() == 1) {
                return;
            } else {
                //排序
                Collections.sort(children);
                //获取当前节点名称
                String thisNode = currentMode.substring("/locks/".length());
                //通过thisNode获取在children集合中的位置
                int index = children.indexOf(thisNode);
                if(index == -1) {
                    System.out.println("数据异常");
                }else if(index == 0) {
                    //就一个节点数据，就可以获取锁了
                    return;
                }else {
                    //需要监听 前一个节点变化
                    waitPath = "/locks/"+children.get(index-1);
                    zooKeeper.getData(waitPath,true,null);
                    //等待监听
                    waitLatch.await();
                    //接收到之后
                    return;
                }
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void unZklock(){
        //删除节点
        try {
            zooKeeper.delete(currentMode,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
