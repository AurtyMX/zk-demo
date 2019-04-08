package com.michelle.zkdemo;

import com.michelle.common.ZKScheduleTask;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;

/**
 * @author michelle.min
 */

public class ClusterNode {
    private String id;
    private String masterNode;
    private String path;
    private int index;
    private LeaderSelectorListener listener;
    private LeaderLatchListener leaderLatchListener;
    private ZKScheduleTask zkScheduleTask;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(String masterNode) {
        this.masterNode = masterNode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LeaderSelectorListener getListener() {
        return listener;
    }

    public void setListener(LeaderSelectorListener listener) {
        this.listener = listener;
    }

    public LeaderLatchListener getLeaderLatchListener() {
        return leaderLatchListener;
    }

    public void setLeaderLatchListener(LeaderLatchListener leaderLatchListener) {
        this.leaderLatchListener = leaderLatchListener;
    }

    public ZKScheduleTask getZkScheduleTask() {
        return zkScheduleTask;
    }

    public void setZkScheduleTask(ZKScheduleTask zkScheduleTask) {
        this.zkScheduleTask = zkScheduleTask;
    }
}
