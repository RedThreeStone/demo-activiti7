package it.lei.demoactiviti7;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author uu
 * @date 2019/11/2 19:51
 * @desciption TODO
 */
public class ApiTest extends DemoActiviti7ApplicationTests {

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    private ProcessEngine processEngine;

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

    @Before
    public void pre(){
        System.out.println("开始测试");
    }
    @After
    public void post(){
        System.out.println("测试结束");
    }

    /**
     * 创建流程
     */
    @Test
    public void createProcessTest(){
        repositoryService.createDeployment()
                //一般部署一个流程需要指定bpmn和png两种类型的文件
                .addClasspathResource("yfzProcess.bpmn")
                .addClasspathResource("yfzProcess.png")
                .name("yfz审核流程部署")
                .deploy();
    }

    /**
     * 查询流程定义相关信息
     */
    @Test
    public void queryProcessInfo(){
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("yfzProcess")
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
                .processDefinitionKey("yfzProcess")
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
                .processDefinitionKey("yfzProcess")
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
                .processDefinitionKey("yfzProcess").singleResult();
        //获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("yfzProcess").singleResult();
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
                .processDefinitionKey("yfzProcess").singleResult();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(yfzProcess.getProcessInstanceId())
                .orderByHistoricActivityInstanceStartTime().asc().list();
        list.forEach(item ->{
            System.out.println(item.getActivityName());
            System.out.println(item.getTime());
            System.out.println("----------------------------------------------");
        });
    }

}
