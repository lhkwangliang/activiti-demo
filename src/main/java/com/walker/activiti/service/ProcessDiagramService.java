package com.walker.activiti.service;


import com.walker.activiti.cmd.GetProcessDefinitionCacheEntryCmd;
import lombok.AllArgsConstructor;
import org.activiti.bpmn.BPMNLayout;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class ProcessDiagramService {

    private ProcessEngine processEngine;

    public void createActivitiImg(String processInstanceId, String fileName, boolean useCache) throws IOException {
        InputStream inputStream = getProcessDiagram(processInstanceId, useCache);
        File file = new File("D://" + fileName + ".svg");
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file, false);//true表示在文件末尾追加
        byte[] bList = new byte[100];
        while (inputStream.read(bList) != -1) {
            fos.write(bList);
        }
        fos.close();
        inputStream.close();

    }

    /**
     * Get Process instance diagram
     */
    public InputStream getProcessDiagram(String processInstanceId, boolean useCache) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();

        // null check
        if (processInstance == null) {
            throw new RuntimeException("processInstance is null");
        }

        BpmnModel bpmnModel = null;
        if (useCache) {
            bpmnModel = getBpmnModelFromCache(processInstance.getProcessDefinitionId());
        } else {
            RepositoryService repositoryService = processEngine.getRepositoryService();
            bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        }

        if (bpmnModel != null && bpmnModel.getLocationMap().size() > 0) {
            ProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            // 生成流程图 已启动的task 高亮
            List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
            List<String> highLightedFlows = Collections.emptyList();
            return generator.generateDiagram(bpmnModel, highLightedActivities, highLightedFlows, "宋体", "宋体", "宋体");

            // 生成流程图 都不高亮
            // return generator.generateDiagram(bpmnModel, "宋体", "宋体", "宋体");
        }

        return null;
    }

    public BpmnModel getBpmnModelFromCache(String processDefinitionId) {
        ManagementService managementService = processEngine.getManagementService();
        ProcessDefinitionCacheEntry processDefinitionCacheEntry = managementService.executeCommand(new GetProcessDefinitionCacheEntryCmd(processDefinitionId));
        // 通过缓存获取
        Process process = processDefinitionCacheEntry.getProcess();

        BpmnModel bpmnModel = new BpmnModel();
        bpmnModel.addProcess(process);
        BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
        bpmnLayout.execute();

        return bpmnModel;
    }


}
