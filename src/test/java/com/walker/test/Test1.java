package com.walker.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.walker.activiti.App;
import com.walker.activiti.cmd.DeleteInstanceCmd;
import com.walker.activiti.entity.AddSign;
import com.walker.activiti.entity.TaskModel;
import com.walker.activiti.mapper.AddSignMapper;
import com.walker.activiti.service.AddSignService;
import com.walker.activiti.service.ProcessDiagramService;
import com.walker.activiti.utils.ActivityUtils;
import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class Test1 {
    private static final Logger logger = LoggerFactory.getLogger(Test1.class);

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;

    @Test
    public void testDeployModel() {
        repositoryService.createDeployment().addClasspathResource("processes/leave.bpmn20.xml").deploy();
    }

    @Test
    public void testDeleteOne() {
        managementService.executeCommand(new DeleteInstanceCmd(""));
    }

    @Test
    public void testDeleteAll() {
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionKey("leave").list();
        for (ProcessInstance processInstance : list) {
            managementService.executeCommand(new DeleteInstanceCmd(processInstance.getProcessInstanceId()));
        }
    }

    @Test
    public void testStartAndComplete() {
        runtimeService.startProcessInstanceByKey("leave");
        for (String assignee : Arrays.asList("zhangsan", "lisi", "wangwu")) {
            Task task = taskService.createTaskQuery().processDefinitionKey("leave")
                    .taskAssignee(assignee)
                    .singleResult();
            taskService.complete(task.getId());
        }
    }

    @Test
    public void addSignTest() {
        TaskEntity taskEntity = (TaskEntity) taskService.createTaskQuery()
                .taskAssignee("zhaoliu")
                .singleResult();
        logger.info("taskEntity: {}", taskEntity);

        String firstNodeId = "design_1";
        String lastNodeId = "hrApprove";
        List<TaskModel> taskModelList = Lists.newArrayList();

        TaskModel taskModel1 = ActivityUtils.buildTaskModel("design_1", "加签1", "${design_1Assignee}");
        TaskModel taskModel2 = ActivityUtils.buildTaskModel("design_2", "加签2", "wangwu");

        taskModelList.add(taskModel1);
        taskModelList.add(taskModel2);

        taskService.setVariable(taskEntity.getId(), "design_1Assignee", "lisi");
        AddSignService addSignService = new AddSignService();
        addSignService.addUserTask(taskEntity.getProcessDefinitionId(), taskEntity.getProcessInstanceId(),
                processEngine, taskModelList, firstNodeId, lastNodeId, true, true,
                taskEntity.getId(), taskModelList.get(0).getId());
    }

    private void loadToCache(String processInstanceId) {
        ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
        SqlSessionFactory sqlSessionFactory = processEngineConfiguration.getSqlSessionFactory();
        sqlSessionFactory.getConfiguration().addMapper(AddSignMapper.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        AddSignMapper mapper = sqlSession.getMapper(AddSignMapper.class);

        List<AddSign> addSigns = mapper.find(processInstanceId);

        // 将数据库中的加签节点加载到缓存中
        for (AddSign addSign : addSigns) {
            logger.info(addSign.getProcessInstanceId());
            Map map = (Map) JSON.parse(addSign.getPropertiesText());
            String firstId = (String) map.get("firstId");
            String lastId = (String) map.get("lastId");
            JSONArray jsonArray = (JSONArray) map.get("activityList");
            List<TaskModel> taskModels = JSONArray.parseArray(jsonArray.toJSONString(), TaskModel.class);

            AddSignService addSignService = new AddSignService();
            addSignService.addUserTask(addSign.getProcessDefinitionId(), addSign.getProcessInstanceId(),
                    processEngine, taskModels, firstId, lastId, false, false,
                    null, null);
        }
    }

    @Test
    public void testComplete() {
    loadToCache("96d5d857-ba6a-11ed-be61-0a0027000003");

//        Task task = taskService.createTaskQuery().processDefinitionKey("leave")
//                .taskAssignee("lisi")
//                .singleResult();
//        taskService.complete(task.getId());

        Task task = taskService.createTaskQuery().processDefinitionKey("leave")
                .taskAssignee("wangwu")
                .singleResult();
        taskService.complete(task.getId());
//
//
//        Task task = taskService.createTaskQuery().processDefinitionKey("leave")
//                .taskAssignee("zhaoliu")
//                .singleResult();
//        taskService.complete(task.getId());
    }

    @Test
    public void testImageGenerator() throws IOException {
        String processInstanceId = "96d5d857-ba6a-11ed-be61-0a0027000003";
        loadToCache(processInstanceId);

        ProcessDiagramService processDiagramService = new ProcessDiagramService(processEngine);
        processDiagramService.createActivitiImg(processInstanceId, "leave1", true);
    }

}
