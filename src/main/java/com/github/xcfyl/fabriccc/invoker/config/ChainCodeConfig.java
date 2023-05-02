package com.github.xcfyl.fabriccc.invoker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 西城风雨楼
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainCodeConfig {
    /**
     * 链码工程的名称
     */
    private String projectName;

    /**
     * gopath路径
     */
    private String gopath;

    /**
     * 链码的版本
     */
    private String version;

    /**
     * 链码部署上链的名称
     */
    private String name;

    /**
     * 链码背书的路径
     */
    private String policyPath;
}
