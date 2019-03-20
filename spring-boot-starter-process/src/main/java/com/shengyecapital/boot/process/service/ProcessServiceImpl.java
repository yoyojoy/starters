package com.shengyecapital.boot.process.service;

import com.shengyecapital.boot.process.exception.CustomProcessException;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service("activitiService")
public class ProcessServiceImpl implements ProcessService {

    @Value("${spring.profiles.active}")
    private String businessEnv;
    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;

    private String generateBusinessID(String businessID) {
        return String.format("%s:%s:%s", businessID, applicationName, businessEnv).toLowerCase();
    }

    @Override
    public void startProcess(String businessId, final String processDefinitionKey, String processStarter, Map<String, Object> variables) throws CustomProcessException {
        if (StringUtils.isBlank(businessId)) {
            throw new CustomProcessException("业务唯一标识businessId不能为空");
        }
        if (StringUtils.isBlank(processDefinitionKey)) {
            throw new CustomProcessException("流程定义KEY不能为空");
        }
        if (CollectionUtils.isEmpty(variables)) {
            variables = new HashMap<>();
        }
        identityService.setAuthenticatedUserId(processStarter);
        variables.put("applicant", processStarter);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, this.generateBusinessID(businessId), variables);
        if (processInstance == null) {
            throw new CustomProcessException("内部错误,发起申请流程失败");
        }
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).active().singleResult();
        if (task == null) {
            throw new CustomProcessException("内部错误,发起申请流程失败");
        }
        taskService.complete(task.getId(), variables);
    }

    @Override
    public String getTaskMode(String processDefinitionKey, String businessId, String modeKey) {
        if (StringUtils.isNotBlank(businessId)) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey)
                    .processInstanceBusinessKey(this.generateBusinessID(businessId)).singleResult();
            if (processInstance == null) {
                return null;
            }
            Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).active().singleResult();
            if (task == null) {
                return null;
            }
            Map<String, Object> map = taskService.getVariables(task.getId());
            String mode = map != null ? (String) map.get(modeKey) : "";
            if (StringUtils.isBlank(mode)) {
                return null;
            }
            return mode;
        }
        return null;
    }

    @Override
    public void dynamicJumpActivity(String processDefinitionKey, String businessId, String targetActivityId, Map<String, Object> variables) {
        //当前环节实例
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey).processInstanceBusinessKey(this.generateBusinessID(businessId))
                .unfinished().singleResult();
        if (currTask == null) {
            throw new CustomProcessException("当前任务不存在");
        }
        // 取得流程定义
        ProcessDefinitionEntity definition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(currTask.getProcessDefinitionId());
        // 取得本步活动
        ActivityImpl currActivity = ((ProcessDefinitionImpl) definition).findActivity(currTask.getTaskDefinitionKey());
        if (currActivity.getId().equalsIgnoreCase(targetActivityId)) {
            throw new CustomProcessException("当前已经是该环节");
        }
        //当前正在执行的流程实例Id
        Execution execution = runtimeService.createExecutionQuery().processDefinitionId(currTask.getProcessDefinitionId())
                .processInstanceBusinessKey(this.generateBusinessID(businessId)).singleResult();
        final String ex = execution.getId();
        ((RuntimeServiceImpl) runtimeService).getCommandExecutor().execute(new Command<Object>() {
            @Override
            public Object execute(CommandContext commandContext) {
                ExecutionEntity execution = commandContext.getExecutionEntityManager().findExecutionById(ex);
                execution.destroyScope("进行强制跳转操作");
                ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
                ActivityImpl findActivity = processDefinition.findActivity(targetActivityId);
                execution.executeActivity(findActivity);
                execution.setVariables(variables);
                return execution;
            }
        });
    }

    @Override
    public void pullTask(String userId, String processDefinitionKey, String taskDefineKey) {
        List<Task> list = this.selectAllTasks(processDefinitionKey, taskDefineKey);
        if (CollectionUtils.isEmpty(list)) {
            throw new CustomProcessException("任务已经被领完");
        }
        taskService.setAssignee(list.get(0).getId(), userId);
    }

    @Override
    public List<Task> selectAllTasks(String processDefinitionKey, String taskDefineKey) {
        List<Task> tasks = taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).taskDefinitionKey(taskDefineKey).active().list();
        if (CollectionUtils.isEmpty(tasks)) {
            return null;
        }
        Collections.sort(tasks, Comparator.comparing(Task::getCreateTime));
        return tasks;
    }

    @Override
    public void abandonTask(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            throw new CustomProcessException("流程任务ID不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new CustomProcessException("流程任务不存在");
        }
        taskService.unclaim(taskId);
    }

    @Override
    public String loadTaskIdByBusinessId(String businessId, String processDefinitionKey) {
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceBusinessKey(this.generateBusinessID(businessId))
                .unfinished().singleResult();
        if (currTask == null) {
            throw new CustomProcessException("当前任务不存在");
        }
        return currTask.getId();
    }

    @Override
    public String loadBusinessIdByTaskId(String taskId) {
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (currTask == null) {
            throw new CustomProcessException("流程任务不存在");
        }
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(currTask.getProcessInstanceId()).singleResult();
        if (processInstance == null) {
            throw new CustomProcessException("流程任务不存在");
        }
        String key = processInstance.getBusinessKey();
        return StringUtils.isNotBlank(key) ? key.split(":")[0] : null;
    }

    @Override
    public String lastStepAssigneeByBusinessId(String businessId, String processDefinitionKey) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(this.generateBusinessID(businessId), processDefinitionKey).singleResult();
        if (processInstance == null) {
            return null;
        }
        List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId()).activityType("userTask")
                .finished().orderByHistoricActivityInstanceEndTime().desc().list();
        return CollectionUtils.isEmpty(activityInstances) ? null : activityInstances.get(0).getAssignee();
    }

    @Override
    public void processTask(String processDefinitionKey, String businessId, String userId, Map<String, Object> variables) {
        this.processTask(processDefinitionKey, businessId, userId, null, variables);
    }

    @Override
    public void processTask(String processDefinitionKey, String businessId, String userId, String comment, Map<String, Object> variables) {
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceBusinessKey(this.generateBusinessID(businessId))
                .unfinished().singleResult();
        if (currTask == null) {
            throw new CustomProcessException("流程任务不存在");
        }
        Task task = taskService.createTaskQuery().taskId(currTask.getId()).active().singleResult();
        if (task == null) {
            throw new CustomProcessException("流程任务不存在");
        }
        if (task.getAssignee() == null) {
            taskService.claim(task.getId(), userId);
        }
        task.setAssignee(userId);
        taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
        taskService.complete(task.getId(), variables);
    }

    @Override
    public InputStream traceProcess(String processDefinitionKey, String businessId) {
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey).processInstanceBusinessKey(this.generateBusinessID(businessId))
                .unfinished().singleResult();
        if (currTask == null) {
            throw new CustomProcessException("任务不存在");
        }
        Execution execution = runtimeService.createExecutionQuery().processDefinitionId(currTask.getProcessDefinitionId())
                .processInstanceBusinessKey(this.generateBusinessID(businessId)).singleResult();
        if (execution == null) {
            throw new CustomProcessException("任务不存在");
        }
        Task task = taskService.createTaskQuery().executionId(execution.getId()).singleResult();
        ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        BpmnModel bpmnmodel = repositoryService.getBpmnModel(process.getProcessDefinitionId());
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(execution.getId());
        DefaultProcessDiagramGenerator gen = new DefaultProcessDiagramGenerator();
        // 获得历史活动记录实体（通过启动时间正序排序，不然有的线可能绘制不出来）
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().executionId(execution.getId())
                .orderByHistoricActivityInstanceStartTime().asc().list();
        // 计算活动线
        List<String> highLightedFlows = this.getHighLightedFlows(
                (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(process.getProcessDefinitionId()),
                historicActivityInstances);
        return gen.generateDiagram(bpmnmodel, "png", activeActivityIds, highLightedFlows, "宋体", "宋体", "宋体", null, 1.0);

    }

    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<>();
        for (int i = 0; i < historicActivityInstances.size(); i++) {
            ActivityImpl activityImpl = processDefinitionEntity.findActivity(historicActivityInstances.get(i).getActivityId());
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<>();
            if ((i + 1) >= historicActivityInstances.size()) {
                break;
            }
            ActivityImpl sameActivityImpl1 = processDefinitionEntity.findActivity(historicActivityInstances.get(i + 1).getActivityId());
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);
                if (activityImpl1.getStartTime().equals(activityImpl2.getStartTime())) {
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity.findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();
            for (PvmTransition pvmTransition : pvmTransitions) {
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    @Override
    public void jointProcess(String taskId, List<String> userIds) {
        Task taskEntity = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (taskEntity == null) {
            throw new CustomProcessException("任务不存在");
        }
        for (String userCode : userIds) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            TaskEntity task = (TaskEntity) taskService.newTask(uuid);
            task.setAssignee(userCode);
            task.setName(taskEntity.getName() + "-会签");
            task.setProcessDefinitionId(taskEntity.getProcessDefinitionId());
            task.setProcessInstanceId(taskEntity.getProcessInstanceId());
            task.setParentTaskId(taskId);
            task.setDescription("会签操作");
            taskService.saveTask(task);
        }
    }

    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        Task taskEntity = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (taskEntity == null) {
            throw new CustomProcessException("任务不存在");
        }
        return taskService.getVariables(taskId);
    }

    @Override
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        Task taskEntity = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (taskEntity == null) {
            throw new CustomProcessException("任务不存在");
        }
        taskService.setVariables(taskId, variables);
    }

    @Override
    public void suspendProcess(String processDefinitionKey, String businessId) {
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey).processInstanceBusinessKey(this.generateBusinessID(businessId))
                .unfinished().singleResult();
        if (currTask == null) {
            throw new CustomProcessException("任务不存在");
        }
        runtimeService.suspendProcessInstanceById(currTask.getProcessInstanceId());
    }

    @Override
    public void activeProcess(String processDefinitionKey, String businessId) {
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey).processInstanceBusinessKey(this.generateBusinessID(businessId))
                .unfinished().singleResult();
        if (currTask == null) {
            throw new CustomProcessException("任务不存在");
        }
        runtimeService.activateProcessInstanceById(currTask.getProcessInstanceId());
    }
}
