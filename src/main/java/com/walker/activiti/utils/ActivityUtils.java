package com.walker.activiti.utils;

import com.walker.activiti.entity.TaskModel;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;

public class ActivityUtils {
    public static UserTask convertToUserTask(TaskModel taskModel, ProcessEngine processEngine) {
        UserTask userTask = new UserTask();
        userTask.setId(taskModel.getId());
        userTask.setName(taskModel.getName());
        userTask.setAssignee(taskModel.getAssignee());
        userTask.setBehavior(new UserTaskActivityBehavior(userTask));
        return userTask;
    }

    public static SequenceFlow buildSequenceFlow(String id, String name, String sourceId, String targetId) {
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setId(id);
        sequenceFlow.setName(name);
        sequenceFlow.setSourceRef(sourceId);
        sequenceFlow.setTargetRef(targetId);
        return sequenceFlow;
    }

    public static TaskModel buildTaskModel(String id, String name, String assignee) {
        TaskModel taskModel = new TaskModel();
        taskModel.setId(id);
        taskModel.setName(name);
        taskModel.setAssignee(assignee);
        return taskModel;
    }
}
