package com.github.xcfyl.fabriccc.invoker.wallet;

import org.hyperledger.fabric.sdk.User;

import java.util.Date;
import java.util.List;

/**
 * @author 西城风雨楼
 */
public interface IFabricWallet {
    /**
     * 清空钱包中的所有身份信息
     *
     * @return
     */
    boolean clearWallet();

    /**
     * 列出当前钱包中的所有身份信息
     *
     * @return
     */
    List<WalletInfo> listWallet();

    /**
     * 列出当前钱包中所有处于status状态的钱包信息
     *
     * @param status
     * @return
     */
    List<WalletInfo> listWallet(WalletStatus status);

    /**
     * 列出username用户下处于某个状态的钱包信息
     *
     * @param username
     * @param status
     * @return
     */
    List<WalletInfo> listWallet(String username, WalletStatus status);

    /**
     * 列出指定用户的所有身份信息
     *
     * @param username
     * @return
     */
    List<WalletInfo> listWallet(String username);

    /**
     * 将一个新的身份添加到钱包中
     *
     * @param user
     * @param expiredTime
     * @return
     */
    boolean addUser(User user, Date expiredTime);

    /**
     * 删除用户信息
     *
     * @param user 被删除的用户
     * @param force 如果为true，那么将其从数据库中彻底删除，否则只是逻辑删除
     * @return
     */
    boolean removeUser(User user, boolean force);
}
