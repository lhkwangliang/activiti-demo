package com.walker.activiti.entity;

import lombok.Data;

@Data
public class AddSign {

    private long id;
    private String processDefinitionId; // 流程定义 id
    private String assignee;            // 加签用户
    private String activityId;          // 节点 id
    private String processInstanceId;   // 流程实例 id
    private String propertiesText;      // 参数(复合字段)
    private int state;                  // 状态 0-可用, 1-不可用
    private long createTime;            // 创建时间

}
