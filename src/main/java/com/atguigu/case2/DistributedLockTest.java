package com.atguigu.case2;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class DistributedLockTest {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        DistributedLock lock1 = new DistributedLock();
        DistributedLock lock2 = new DistributedLock();
        new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    lock1.zklock();;
                    System.out.println("线程1启动获取到锁");
                    Thread.sleep(5000);
                    lock1.unZklock();
                    System.out.println("线程1释放锁");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    lock2.zklock();
                    System.out.println("线程2启动获取到锁");
                    Thread.sleep(5000);
                    lock2.unZklock();
                    System.out.println("线程2释放锁");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
