package com.walker.activiti.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskModel implements Serializable {

    private String id;
    private String name;
    private String assignee;    // 处理人
    private int type = 1;       // 任务类型, 1-任务节点
}