package com.shengyecapital.boot.process.service;

import com.shengyecapital.boot.process.exception.CustomProcessException;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface ProcessService {


	/**
	 * 发起流程
	 * @param businessId			业务唯一标识ID,由调用方确定唯一性
	 * @param processDefinitionKey	流程定义KEY,对应到****.bpmn文件中的流程定义ID值
	 * @param processStarter		流程发起者
	 * @param variables				设定的流程运运行参数,key-value格式
	 * @return						返回值 [流程实例ID]
	 * @throws CustomProcessException
	 */
	void startProcess(String businessId, final String processDefinitionKey, String processStarter, Map<String, Object> variables) throws CustomProcessException;

	/**
	 * 获取业务部门岗操作模式
	 * @param processDefinitionKey  流程定义KEY
	 * @param businessId  业务唯一标识ID
	 * @param modeKey 标识KEY
	 * @return
	 */
	String getTaskMode(String processDefinitionKey, String businessId, String modeKey);

	/**
	 * 动态跳转到流程中的某个环节
	 * @param processDefinitionKey 	流程定义KEY
	 * @param businessId       		业务唯一标识ID
	 * @param targetActivityId 		跳转至的流程环节定义ID
	 * @param variables 			需要的参数集合{key: value}
	 */
	void dynamicJumpActivity(String processDefinitionKey, String businessId, String targetActivityId, Map<String, Object> variables);

	/**
	 * 任务池中拉取一个任务
	 * @param userId                任务认领人ID
	 * @param processDefinitionKey  流程定义KEY
	 * @param taskDefineKey			流程环节定义KEY(对应流程图中的taskDefineKey定义)
	 */
	void pullTask(String userId, String processDefinitionKey, String taskDefineKey);

	/**
	 * 查询某流程中的某个环节未处理的任务集合
	 * @param processDefinitionKey
	 * @param taskDefineKey
	 * @return
	 */
	List<Task> selectAllTasks(String processDefinitionKey, String taskDefineKey);

	/**
	 * 将某流程任务丢弃至任务池中
	 * @param taskId
	 */
	void abandonTask(String taskId);

	/**
	 * 根据流程任务ID获取业务唯一标识ID
	 * @param businessId
	 * @param processDefinitionKey
	 * @return
	 */
	String loadTaskIdByBusinessId(String businessId, String processDefinitionKey);

	/**
	 * 通过流程任务ID查询业务唯一标识ID
	 * @param taskId
	 * @return
	 */
	String loadBusinessIdByTaskId(String taskId);

	/**
	 * 获取某个流程上一环节(已完成的环节)的处理人
	 * @param businessId
	 * @param processDefinitionKey
	 * @return
	 */
	String lastStepAssigneeByBusinessId(String businessId, String processDefinitionKey);

	/**
	 * 流程任务处理
	 * @param processDefinitionKey	流程定义KEY
	 * @param businessId	业务唯一标识ID
	 * @param userId		流程任务处理人ID
	 * @param variables		处理流程环节所需要的参数集合{key: value}
	 */
	void processTask(String processDefinitionKey, String businessId, String userId, Map<String, Object> variables);

	/**
	 * 流程任务处理
	 * @param processDefinitionKey	流程定义KEY
	 * @param businessId	业务唯一标识ID
	 * @param userId		流程任务处理人ID
	 * @param comment		流程处理说明
	 * @param variables		处理流程环节所需要的参数集合{key: value}
	 */
	void processTask(String processDefinitionKey, String businessId, String userId, String comment, Map<String, Object> variables);

	/**
	 * 流程运行时图
	 * @param processDefinitionKey
	 * @param businessId
	 * @return
	 */
	InputStream traceProcess(String processDefinitionKey, String businessId);

	/**
	 * 会签操作
	 * @param taskId 	流程任务ID
	 * @param userIds	会签用户ID集合
	 */
	void jointProcess(String taskId, List<String> userIds);

	/**
	 * 获取流程任务的参数集合{key: value}
	 * @param taskId
	 * @return
	 */
	Map<String, Object> getTaskVariables(String taskId);

	/**
	 * 设置流程任务的参数集合{key: value}
	 * @param taskId
	 * @param variables
	 * @return
	 */
	void setTaskVariables(String taskId, Map<String, Object> variables);

	/**
	 * 挂起某个流程
	 * @param processDefinitionKey
	 * @param businessId
	 */
	void suspendProcess(String processDefinitionKey, String businessId);

	/**
	 * 激活某个流程
	 * @param processDefinitionKey
	 * @param businessId
	 */
	void activeProcess(String processDefinitionKey, String businessId);
}
