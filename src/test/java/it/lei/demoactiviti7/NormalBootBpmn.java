package it.lei.demoactiviti7;

import it.lei.demoactiviti7.component.SecurityUtil;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.builders.StartProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.CompleteTaskPayloadBuilder;
import org.activiti.api.task.model.builders.GetTasksPayloadBuilder;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author uu
 * @date 2019/11/7 22:27
 * @desciption TODO
 */

public class NormalBootBpmn extends DemoActiviti7ApplicationTests{

    public static final String processKey ="bootprocess";

    /**
     * 一下三个用户信息都是在springSecurityConfig里面定义在内存中的
     */
    public static final String jlname="salaboy";

    public static final String shgroup="activitiTeam";

    public static final String qrgroup="otherTeam";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 查看所有的流程定义
     */
    @Test
    public void getProcessDefine(){
        securityUtil.logInAs(jlname);
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0, 5));
        processDefinitionPage.getContent().forEach(item->{
            System.out.println(item);
        });

    }
    @Test
    public void getProcessInstance(){
        securityUtil.logInAs(jlname);
        processRuntime.start(new StartProcessPayloadBuilder().withProcessDefinitionKey(processKey).build());
        System.out.println("启动流程成功");
    }
    @Transactional
    @Rollback(value = false)
    @Test
    public void completeTask(){
        securityUtil.logInAs(jlname);
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), new GetTasksPayloadBuilder().withAssignee(jlname).build());
        tasks.getContent().forEach(item->{
            taskRuntime.complete(new CompleteTaskPayloadBuilder().withTaskId(item.getId()).build());
        });
    }
    @Transactional
    @Rollback(value = false)
    @Test
    public void claimTask(){
        securityUtil.logInAs(jlname);
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 10), new GetTasksPayloadBuilder().withGroup(shgroup).build());
        tasks.getContent().forEach(item->{
            taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(item.getId()).build());
        });
    }
}
