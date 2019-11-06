package it.lei.demoactiviti7;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author uu
 * @date 2019/11/2 19:51
 * @desciption TODO
 */
public class ApiTest extends DemoActiviti7ApplicationTests {

    public static final String YFZKEY="yfzProcess";
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    private ProcessEngine processEngine;

    @BeforeEach
    public  void before(){
        System.out.println("测试开始了");
    }

    @AfterEach
    public  void after(){
        System.out.println("测试结束了");
    }

    /**
     * 是activiti的资源管理类，提供了管理和控制流程发布包和流程定义的操作。使用工作流建模工具设计的业务流程图需要使用此service将流程定义文件的内容部署到计算机。
     * 除了部署流程定义以外还可以：
     * 查询引擎中的发布包和流程定义。
     * 暂停或激活发布包，对应全部和特定流程定义。 暂停意味着它们不能再执行任何操作了，激活是对应的反向操作。
     * 获得多种资源，像是包含在发布包里的文件， 或引擎自动生成的流程图。
     * 获得流程定义的pojo版本， 可以用来通过java解析流程，而不必通过xml。
     * 4.4.4 RuntimeService
     */
    @Autowired
    private RepositoryService repositoryService;

    /**
     * 它是activiti的流程运行管理类。可以从这个服务类中获取很多关于流程执行相关的信息
     */
    @Autowired
    private RuntimeService runtimeService;
    @Autowired

    /**
     * 是activiti的任务管理类。可以从这个类中获取任务的信息。
     */
    private TaskService taskService;
    /**
     * 是activiti的历史管理类，可以查询历史信息，执行流程时，引擎会保存很多数据（根据配置），
     * 比如流程实例启动时间，任务的参与者， 完成任务的时间，每个流程实例的执行路径，等等。
     * 这个服务主要通过查询功能来获得这些数据。
     */
    @Autowired
    private HistoryService historyService;

    /**
     * 是activiti的引擎管理类，提供了对 Activiti 流程引擎的管理和维护功能，
     * 这些功能不在工作流驱动的应用程序中使用，主要用于 Activiti 系统的日常维护。
     */
    @Autowired
    private ManagementService managementService;



    /**
     * 创建流程
     * 流程创建以后会在ACT_GE_BYTEARRAY 通用的流程定义和流程资源表 存入bpmn和png的信息
     * 在ACT_RE_DEPLOYMENT存入部署信息
     * 在ACT_GE_PROPERTY存入系统信息
     * 在实际测试过程中发现eul表达式赋值貌似只能赋值一次 第二次不成功 还是用基本类型去设定流程变量吧 对象的有点玩不转
     * @// TODO: 2019/11/5 以对象的形式来设置流程变量
     */
    @Test
    public void createProcessTest(){
        Deployment deployment = repositoryService.createDeployment()
                //一般部署一个流程需要指定bpmn和png两种类型的文件
                .addClasspathResource("YfzProcess.bpmn")
          //      .addClasspathResource("YfzProcess.png")
                .name("yfz审核流程部署")
                .deploy();
        String id = deployment.getId();
        System.out.println("id:"+id);
        String key = deployment.getKey();
        System.out.println("key:"+key);
        String name = deployment.getName();
        System.out.println("name:"+name);
    }

    /**
     * 启动流程实例
     * 使用uel表达式动态分配任务所属人，测试taskListener的功能
     */
    @Test
    public void getProcessInstance(){
        Map<String, Object> hashMap = new HashMap<>();
        YfzProcessParam yfzProcessParam = new YfzProcessParam();
        yfzProcessParam.setJl("李四");
        yfzProcessParam.setGljsh("haha");
        hashMap.put("yfzProcessParam",yfzProcessParam);
        //key值为bpmn文件的id  设置流程key值 业务表id  以及流程变量指定办理人
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(YFZKEY,"lpzId",hashMap);
        System.out.println("businessKey:"+processInstance.getBusinessKey());
        System.out.println("deploymentid:"+processInstance.getDeploymentId());
        System.out.println("实例id:"+processInstance.getId());
        System.out.println("流程定义id:"+processInstance.getProcessDefinitionId());
        System.out.println("当前获得活动id:"+processInstance.getActivityId());
    }
    /**
     * 查询当前用户的任务列表
     */
    @Test
    public void queryTask(){
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY)
                .taskAssignee("张三").list();
        for (Task task : tasks){
            System.out.println(task.getProcessInstanceId());
            System.out.println(task.getId());
            System.out.println(task.getAssignee());
            System.out.println(task.getName());
        }
    }

    /**
     * 办结任务
     * 办结前加入该任务环节该人是否有任务能办结
     */
    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void finishTask() throws JsonProcessingException {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee("李四")

//                .processDefinitionKey(YFZKEY)//流程key
//                .processInstanceId("")//流程实例id
                //  .taskId("41a7426c-fe2b-11e9-9695-0c9d921ac95e")//环节任务id
                //.taskName("")//环节名
                .list();
        if(tasks==null){
            throw  new RuntimeException("未获取到该经办人下的任务");
        }
        //这边如果要动态的设定下一个经办人，则之前不能设定过
//        通过execution设置流程变量
//        Execution execution = runtimeService.createExecutionQuery().processDefinitionKey(YFZKEY).singleResult();
//        runtimeService.setVariables(execution.getId(),hashMap);
        //通过任务id来设置流程变量
        //taskService.setVariables(tasks.get(0).getId(),hashMap);
        //先取后写 顺便打印日志
        JsonNode jsonNode = (JsonNode) taskService.getVariable(tasks.get(0).getId(), "yfzProcessParam");
        ObjectMapper objectMapper = new ObjectMapper();
        YfzProcessParam yfzProcessParam = objectMapper.readValue(jsonNode.toString(), YfzProcessParam.class);
        System.out.println(yfzProcessParam);
        yfzProcessParam.setGljsh("又改了一下");
        taskService.setVariable(tasks.get(0).getId(),"yfzProcessParam",yfzProcessParam);
//        taskService.complete(tasks.get(0).getId(),map);  没有测试出影响流程的详细中的效果
        taskService.complete(tasks.get(0).getId());

    }

    @Test
    @Transactional(rollbackFor = Exception.class)
    @Rollback(value = false)
    public void setVar(){
        Map<String, Object> map = new HashMap<>();
        YfzProcessParam yfzProcessParam = new YfzProcessParam();
        yfzProcessParam.setJl("dwas");
        map.put("yfzProcessParam",yfzProcessParam);
        queryParams();
       taskService.setVariable("57b97b61-003d-11ea-aab6-40a5ef4a0f30","yfzProcessParam",yfzProcessParam);
    }
    @Test
    public void queryParams(){

        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY).list();
        list.forEach(item->{
            Map<String, Object> variables = taskService.getVariables(item.getId());
            System.out.println(variables);
        });
    }
    /**
     * 查询流程定义相关信息
     */
    @Test
    public void queryProcessInfo(){
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(YFZKEY)
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        for(ProcessDefinition processDefinition:processDefinitions){
            int version = processDefinition.getVersion();
            System.out.println("version:"+version);
            String description = processDefinition.getDescription();
            System.out.println("description:"+description);
            String resourceName = processDefinition.getResourceName();
            System.out.println("resourceName:"+resourceName);
        }
    }

    /**
     * 获取流程的所有任务节点信息
     */
    @Test
    public void findAllTaskNodeName(){
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(YFZKEY)
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        for(ProcessDefinition processDefinition:processDefinitions) {
            String id = processDefinition.getId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(id);
            Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
            flowElements.stream().filter(item->item instanceof UserTask).forEach(item->{
                String name = item.getName();
                System.out.println(name);
            });
        }
    }

    /**
     * 删除流程信息
     */
    @Test
    public void deleteProcess(){
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(YFZKEY)
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        for(ProcessDefinition processDefinition:processDefinitions) {
            String deploymentId = processDefinition.getDeploymentId();
            repositoryService.deleteDeployment(deploymentId,false);
        }
    }

    /**
     * 获取部署流程时的资源文件
     */
    @Test
    public void getAllResource() throws IOException {
        //获取流程部署信息
        Deployment deployment = repositoryService.createDeploymentQuery()
                .processDefinitionKey(YFZKEY).singleResult();
        //获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(YFZKEY).singleResult();
        //获取文件流
        InputStream bpmn = repositoryService.getResourceAsStream(deployment.getId(),processDefinition.getResourceName());
        InputStream png = repositoryService.getResourceAsStream(deployment.getId(),processDefinition.getDiagramResourceName());
        //拷贝文件
        FileUtils.copyInputStreamToFile(bpmn,new File("C:\\Users\\huangl\\Desktop\\新建文件夹\\bpmn.bpmn"));
        FileUtils.copyInputStreamToFile(png,new File("C:\\Users\\huangl\\Desktop\\新建文件夹\\png.png"));
        bpmn.close();
        png.close();
    }

    /**
     * 获取流程的历史记录
     * 问题 带activity 以及带Native 和不带的有什么区别
     */
    @Test
    public void findTaskHistory(){
        ProcessInstance yfzProcess = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(YFZKEY).singleResult();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(yfzProcess.getProcessInstanceId())
                .orderByHistoricActivityInstanceStartTime().asc().list();
        list.forEach(item ->{
            System.out.println(item.getActivityName());
            System.out.println(item.getTime());
            System.out.println("----------------------------------------------");
        });
    }

    /**
     * 挂起(激活)该流程定义下的所有流程
     */
    @Test
    public void suspendedProcessDefinition(){
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(YFZKEY).singleResult();
        //如果是暂停就激活,如果是激活状态就暂停
        if(processDefinition.isSuspended()){
            repositoryService.activateProcessDefinitionById(processDefinition.getId());
        }else {
            repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        }
    }

    /**
     * 暂停单条流程实例
     */
    @Test
    public void suspendedSingleProcessInstance(){
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey(YFZKEY).singleResult();
        if(processInstance.isSuspended()){
            //唤醒流程实例
            runtimeService.activateProcessInstanceById(processInstance.getProcessInstanceId());
        }else {
            //暂停流程实例
            runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId());
        }

    }
}
