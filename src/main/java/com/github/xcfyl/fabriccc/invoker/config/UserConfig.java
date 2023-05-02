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
public class UserConfig {
    /**
     * 是否是admin
     */
    private Boolean isAdmin;

    /**
     * admin名称
     */
    private String usernameForCA;

    /**
     * admin密码
     */
    private String passwdForCA;

    /**
     * admin公钥证书文件路径
     */
    private String certPath;

    /**
     * admin私钥证书文件路径
     */
    private String privateKeyPath;
}
