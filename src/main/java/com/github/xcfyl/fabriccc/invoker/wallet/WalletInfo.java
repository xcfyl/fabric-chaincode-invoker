package com.github.xcfyl.fabriccc.invoker.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @author 西城风雨楼
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WalletInfo {
    private Integer walletId;
    private String publicKey;
    private String privateKey;
    private String username;
    private String password;
    private String mspId;
    private Date createTime;
    private Date expiredTime;
    private Integer status;
    private byte[] publicKeyHash;
}
