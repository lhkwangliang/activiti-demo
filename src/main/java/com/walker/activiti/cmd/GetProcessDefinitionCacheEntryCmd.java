package com.walker.activiti.cmd;

import lombok.AllArgsConstructor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;

@AllArgsConstructor
public class GetProcessDefinitionCacheEntryCmd implements Command<ProcessDefinitionCacheEntry> {

    private String processDefinitionId;

    @Override
    public ProcessDefinitionCacheEntry execute(CommandContext commandContext) {
        DeploymentManager deploymentManager = commandContext.getProcessEngineConfiguration().getDeploymentManager();
        return deploymentManager.getProcessDefinitionCache().get(processDefinitionId);
    }
}
