package com.github.xcfyl.fabriccc.invoker.service;

import com.github.xcfyl.fabriccc.invoker.annotation.ChainCodeProxy;
import com.github.xcfyl.fabriccc.invoker.annotation.Invoke;
import org.hyperledger.fabric.sdk.User;

/**
 * @author 西城风雨楼
 */
@ChainCodeProxy(channelName = "mychannel")
public interface InvokeService {

    @Invoke(timeout = 10000000, resultHandler = MyInvokeHandler.class)
    void person(User user, String name, String age);
}
