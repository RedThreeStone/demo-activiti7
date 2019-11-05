package it.lei.demoactiviti7;

import lombok.Data;

import java.io.Serializable;

/**
 * @author huangl
 * @date 2019/11/5 16:16
 * @desciption 预分宗流程参数
 */
@Data
public class YfzProcessParam implements Serializable {
    private String jl;
    private String gljsh;
    private String gljqr;
    private String cssh;
    private Integer num;
}
