package com.walker.activiti.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TmpActivityModel implements Serializable {
    private String activityIds;     // 加签的节点id, 多个的话逗号分隔
    private String firstId;
    private String lastId;
    private List<TaskModel> activityList;
}