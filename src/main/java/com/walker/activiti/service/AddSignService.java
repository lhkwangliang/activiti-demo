package com.walker.activiti.service;

import com.alibaba.fastjson.JSON;
import com.walker.activiti.cmd.GetProcessDefinitionCacheEntryCmd;
import com.walker.activiti.cmd.JumpCmd;
import com.walker.activiti.entity.AddSign;
import com.walker.activiti.entity.TaskModel;
import com.walker.activiti.entity.TmpActivityModel;
import com.walker.activiti.mapper.AddSignMapper;
import com.walker.activiti.utils.ActivityUtils;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.assertj.core.util.Lists;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class AddSignService {

    /**
     * @param procDefId     流程定义 ID
     * @param procInstId    流程实例 ID
     * @param processEngine 流程引擎
     * @param taskModelList 加签节点列表
     * @param firstNodeId   加签开始节点 ID
     * @param lastNodeId    加签结束节点 ID
     * @param persistence   是否持久化
     * @param onset         是否需要立即跳转
     * @param taskId        taskID
     * @param targetNodeId  跳转的目标节点
     */
    public void addUserTask(String procDefId, String procInstId, ProcessEngine processEngine, List<TaskModel> taskModelList,
                            String firstNodeId, String lastNodeId, boolean persistence, boolean onset, String taskId, String targetNodeId) {
        ManagementService managementService = processEngine.getManagementService();
        ProcessDefinitionCacheEntry processDefinitionCacheEntry = managementService.executeCommand(new GetProcessDefinitionCacheEntryCmd(procDefId));
        // 通过缓存获取
        Process process = processDefinitionCacheEntry.getProcess();
        // 批量生成任务, 循环遍历 TaskModel
        List<UserTask> userTaskList = Lists.newArrayList();
        taskModelList.forEach(taskModel -> {
            UserTask userTask = ActivityUtils.convertToUserTask(taskModel, processEngine);
            userTaskList.add(userTask);
            process.addFlowElement(userTask);
        });
        // 构造并添加连线
        for (int i = 0; i < userTaskList.size(); ++i) {
            UserTask userTask = userTaskList.get(i);
            SequenceFlow sequenceFlow = null;
            if (i == userTaskList.size() - 1) {
                // 如果是最后一个节点
                sequenceFlow = ActivityUtils.buildSequenceFlow(userTask.getId() + "-->" + lastNodeId,
                        userTask.getId() + "-->" + lastNodeId, userTask.getId(), lastNodeId);
                sequenceFlow.setTargetRef(lastNodeId);
                sequenceFlow.setSourceRef(userTask.getId());

                sequenceFlow.setTargetFlowElement(process.getFlowElement(lastNodeId));
                sequenceFlow.setSourceFlowElement(userTask);
            } else {
                // 如果不是最后一个
                sequenceFlow = ActivityUtils.buildSequenceFlow(userTask.getId() + "-->" + userTaskList.get(i + 1).getId(),
                        userTask.getId() + "-->" + userTaskList.get(i + 1).getId(),
                        userTask.getId(), userTaskList.get(i + 1).getId());
                sequenceFlow.setTargetRef(userTaskList.get(i + 1).getId());
                sequenceFlow.setSourceRef(userTask.getId());

                sequenceFlow.setTargetFlowElement(userTaskList.get(i + 1));
                sequenceFlow.setSourceFlowElement(userTask);
            }
            userTask.setOutgoingFlows(Arrays.asList(sequenceFlow));
            process.addFlowElement(sequenceFlow);
        }
        log.info("process: {}", process);
        // 更新缓存
        processDefinitionCacheEntry.setProcess(process);
        // 如果需要立即生效(直接跳转)
        if (onset) {
            managementService.executeCommand(new JumpCmd(taskId, targetNodeId));
        }
        // 如果需要持久化
        if (persistence) {
            persistenceToDB(procDefId, procInstId, firstNodeId, lastNodeId, taskModelList, processEngine);
        }
    }

    /**
     * 将加签的任务节点添加到数据库
     * @param procDefId
     * @param procInstId
     * @param firstNodeId
     * @param lastNodeId
     * @param taskModelList
     * @param processEngine
     */
    private void persistenceToDB(String procDefId, String procInstId, String firstNodeId, String lastNodeId, List<TaskModel> taskModelList, ProcessEngine processEngine) {
        ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        SqlSessionFactory sqlSessionFactory = processEngineConfiguration.getSqlSessionFactory();
        sqlSessionFactory.getConfiguration().addMapper(AddSignMapper.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        AddSignMapper mapper = sqlSession.getMapper(AddSignMapper.class);
        TmpActivityModel tmpActivityModel = new TmpActivityModel();
        tmpActivityModel.setFirstId(firstNodeId);
        tmpActivityModel.setLastId(lastNodeId);
        tmpActivityModel.setActivityList(taskModelList);
        StringBuilder stringBuilder = new StringBuilder();
        for (TaskModel taskModel : taskModelList) {
            stringBuilder.append(taskModel.getId() + ",");
        }
        tmpActivityModel.setActivityIds(stringBuilder.toString());

        AddSign addSign = new AddSign();
        addSign.setProcessDefinitionId(procDefId);
        addSign.setAssignee("admin");
        addSign.setActivityId("hrApprove");
        addSign.setProcessInstanceId(procInstId);
        addSign.setPropertiesText(JSON.toJSONString(tmpActivityModel));
        addSign.setState(0);
        addSign.setCreateTime(System.currentTimeMillis());
        int insert = mapper.insert(addSign);
        log.info("insert 结果: {}", insert);

        sqlSession.commit();
        sqlSession.close();
    }
}