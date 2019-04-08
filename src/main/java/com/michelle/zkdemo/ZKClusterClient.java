package com.michelle.zkdemo;

import com.michelle.common.ZKScheduleTask;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.TimeUnit;

/**
 * @author michelle.min
 */


/**
 * 使用zookeeper实现主从选举
 */
public class ZKClusterClient {
    private static final String appPath = "/qa_java_michelle_test";
    private static final String nodes = "nodes";
    private static final String master = "masterNode";
    private static final String leaderlatchNode = "leadship";
    private static final String servierId = "node_";
    private CuratorFramework client;
    private ClusterNode clusterNode;

    public static void testClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        try (CuratorFramework clientTest = CuratorFrameworkFactory.builder().connectionTimeoutMs(30000).sessionTimeoutMs(30000).connectString(ZkConfig.CONNECTION_STRING).canBeReadOnly(false).retryPolicy(retryPolicy).build();) {
            clientTest.start();
            Stat stat = clientTest.checkExists().forPath(appPath);
            if (stat == null) {
                clientTest.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(appPath, appPath.getBytes());
            }
            new EnsurePath(appPath).ensure(clientTest.getZookeeperClient());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initNode() throws Exception {
        Stat statRoot = client.checkExists().forPath(appPath);
        if (statRoot == null) {
            client.create().forPath(appPath);
        }
        Stat statNodes = client.checkExists().forPath(appPath + "/" + nodes);
        if (statNodes == null) {
            client.create().forPath(appPath + "/" + nodes);
        }
        Stat statMaster = client.checkExists().forPath(appPath + "/" + master);
        if (statMaster == null) {
            client.create().forPath(appPath + "/" + master);
        }
        Stat leadership = client.checkExists().forPath(appPath + "/" + leaderlatchNode);
        if (leadership == null) {
            client.create().forPath(appPath + "/" + leaderlatchNode);
        }
    }

    public void initClient() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(ZkConfig.CONNECTION_STRING)
                .defaultData(appPath.getBytes())
                .retryPolicy(retryPolicy)
                .canBeReadOnly(false)
                .build();
        client.start();
    }

    public void registerServer(ClusterNode clusterNode) throws Exception {
        if (clusterNode.getId() == null) {
            clusterNode.setId(servierId + clusterNode.getIndex());
            clusterNode.setPath(appPath + "/" + nodes + "/" + clusterNode.getId());
        }
        Stat statClusterNode = client.checkExists().forPath(clusterNode.getPath());
        if (statClusterNode == null) {
            client.create().forPath(clusterNode.getPath());
        }
    }

    /**
     * zookeeper有两种选主：LeaderSelector和LeaderLatch
     * LeaderSelector：支持任务执行完成后自动重新竞选，即LeaderSelectorListener中的回调方法执行完成后，重新进行选主
     * LeaderLatch：选出来的来的maser将长期保持master状态，在master节点挂掉后，会马上在集群中选出新的master节点
     *
     * @param clusterNode
     * @throws Exception
     */
    public void selectMasterLeaderSelector(ClusterNode clusterNode) throws Exception {
        LeaderSelector leaderSelector = new LeaderSelector(client, appPath + "/" + nodes, clusterNode.getListener());
        leaderSelector.autoRequeue();  // not required, but this is behavior that you will probably expect
        leaderSelector.start();
    }

    public void selectMasterLeaderLatch(ClusterNode clusterNode) throws Exception {
        LeaderLatch leaderLatch = new LeaderLatch(client, appPath + "/" + leaderlatchNode, clusterNode.getId());
//        LeaderLatch leaderLatch = new LeaderLatch(client, appPath + "/" + leaderlatchNode, clusterNode.getId(), LeaderLatch.CloseMode.NOTIFY_LEADER); 如果leaderLatch.close被调用，leader被释放，则监听器会被通知，默认不通知
        leaderLatch.addListener(clusterNode.getLeaderLatchListener());
        leaderLatch.start();
        TimeUnit.SECONDS.sleep(5);
//        leaderLatch.close();  不能调用close　否则会释放已经选举的leader
    }

    public static void main(String[] args) throws Exception {
        ZKClusterClient zkClusterClient = new ZKClusterClient();
        zkClusterClient.schedule(1);
        ZKClusterClient zkClusterClient2 = new ZKClusterClient();
        zkClusterClient2.schedule(2);
        TimeUnit.SECONDS.sleep(10);
        zkClusterClient.closeClient();
        TimeUnit.SECONDS.sleep(30);
        zkClusterClient2.closeClient();
    }

    public void closeClient() {
        System.out.println("close node is:" + clusterNode.getId());
        client.close();
        clusterNode.getZkScheduleTask().cloase();
    }

    public void schedule(int index) throws Exception {
        this.initClient();
        this.initNode();
        ClusterNode node1 = new ClusterNode();
        clusterNode = node1;
        node1.setIndex(index);
        node1.setListener(new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println("I am a master node!My id is:" + node1.getId());
            }
        });
        node1.setLeaderLatchListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                System.out.println("I am a master node!My id is:" + node1.getId());
                ZKScheduleTask zkScheduleTask = new ZKScheduleTask(index);
                node1.setZkScheduleTask(zkScheduleTask);
            }

            @Override
            public void notLeader() {
                System.out.println("I am  not a master node!My id is:" + node1.getId());
            }
        });
        this.registerServer(node1);
//        zkClient.selectMasterLeaderSelector(node1);
//        zkClient.selectMasterLeaderSelector(node2);
        this.selectMasterLeaderLatch(node1);
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {  //使用ConnectionStateListener来处理客户端与zookeeper连接丢失的情况
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.LOST) {
                    System.out.println("connection lost...");
                    node1.getZkScheduleTask().cloase();
                }
            }
        });
    }


}
