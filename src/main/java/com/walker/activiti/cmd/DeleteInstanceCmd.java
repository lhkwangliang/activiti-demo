package com.walker.activiti.cmd;

import lombok.AllArgsConstructor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

@AllArgsConstructor
public class DeleteInstanceCmd implements Command<Void> {

    private String processInstanceId;

    @Override
    public Void execute(CommandContext commandContext) {
        try {
            commandContext.getExecutionEntityManager().deleteProcessInstance(processInstanceId, "废弃", true);
        } catch (Exception ex) {
            commandContext.getHistoricProcessInstanceEntityManager().delete(processInstanceId);
            commandContext.getHistoricActivityInstanceEntityManager().deleteHistoricActivityInstancesByProcessInstanceId(processInstanceId);
            commandContext.getHistoricTaskInstanceEntityManager().deleteHistoricTaskInstancesByProcessInstanceId(processInstanceId);
            commandContext.getHistoricDetailEntityManager().deleteHistoricDetailsByProcessInstanceId(processInstanceId);
            commandContext.getHistoricIdentityLinkEntityManager().deleteHistoricIdentityLinksByProcInstance(processInstanceId);
            commandContext.getHistoricVariableInstanceEntityManager().deleteHistoricVariableInstanceByProcessInstanceId(processInstanceId);
        }
        return null;
    }
}
