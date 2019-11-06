package it.lei.demoactiviti7;

import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author uu
 * @date 2019/11/2 19:51
 * @desciption TODO
 */
public class CandidateTest extends DemoActiviti7ApplicationTests {

    public static final String YFZKEY="yfzProcess";
    public static final String busKey= "lpzId";
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
     */
    @Test
    @Transactional
    @Rollback(value = false)
    public void createProcessInstanceTest(){
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("candidate.bpmn")
                .name("候选人业务测试").deploy();
        System.out.println("创建流程完成:"+deploy.getName());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(YFZKEY,busKey);
        System.out.println("创建流程实例成功:"+processInstance.getBusinessKey());

    }

    /**
     * 办结任务
     */
    @Test
    @Transactional
    @Rollback(value = false)
    public void completeTask(){
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY)
                .processInstanceBusinessKey(busKey)
                .taskAssignee("张三").singleResult();
        taskService.complete(task.getId());
    }

    /**
     * 候选人拾取任务
     */
    @Test
    @Transactional
    @Rollback(value = false)
    public void candidateClaimTask(){
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY)
                .processInstanceBusinessKey(busKey).singleResult();
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
        identityLinksForTask.forEach(item->{
            String userId = item.getUserId();
            //如果候选人包含了李四才拉去任务
            if(userId.equals("李四")){
                taskService.claim(task.getId(),"李四");
                System.out.println("拉取任务完成");
            }
        });

    }
    /**
     * 当办理人不想做了,归还任务,由其他候选人继续拉去任务
     */
    @Test
    @Transactional
    @Rollback(value = false)
    public void giveBackTask(){
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY)
                .processInstanceBusinessKey(busKey)
                .taskAssignee("李四").singleResult();
        if(task!=null){
            taskService.setAssignee(task.getId(),null);
            System.out.println("归还任务完成");
        }
    }

    /**
     * 办理人将任务交接给其他人,甚至可以不是候选人
     */
    @Test
    @Transactional
    @Rollback(value = false)
    public void handoverTask(){
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY)
                .processInstanceBusinessKey(busKey)
                .taskAssignee("李四").singleResult();
        if(task!=null){
            taskService.setAssignee(task.getId(),"李彤彤");
        }
    }

    /**
     * 查询属于自己的候选人任务,被别人拉取的任务依然能查到
     */
    @Test
    @Transactional
    @Rollback(value = false)
    public void queryMyCandidateTask(){
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(YFZKEY)
                .processInstanceBusinessKey(busKey).singleResult();

        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
        identityLinksForTask.forEach(item->{
            String userId = item.getUserId();
            if(userId.equals("王五")&& (task.getAssignee()==null)){
                System.out.println("查到任务了");
            }
        });
    }
    @Test
    @Transactional
    @Rollback(value = false)
    public void queryCandidateTasks(){
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("王五").list();
        tasks.forEach(item->{
            System.out.println(item);
        });

    }

}
