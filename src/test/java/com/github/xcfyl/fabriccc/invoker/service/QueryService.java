package com.github.xcfyl.fabriccc.invoker.service;

import com.github.xcfyl.fabriccc.invoker.annotation.ChainCodeProxy;
import com.github.xcfyl.fabriccc.invoker.annotation.Query;
import org.hyperledger.fabric.sdk.User;

/**
 * @author 西城风雨楼
 */
@ChainCodeProxy(channelName = "mychannel")
public interface QueryService {

    @Query(timeout = 100000)
    Person person(User user, String name, String age);
}
