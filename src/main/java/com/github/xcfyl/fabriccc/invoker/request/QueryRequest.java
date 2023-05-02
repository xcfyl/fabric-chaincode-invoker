package com.github.xcfyl.fabriccc.invoker.request;

import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.User;

import java.util.Collection;

/**
 * @author 西城风雨楼
 */
public class QueryRequest<T> extends AbstractFabricRequest<T> {
    private final String funcName;
    private final String[] args;

    public QueryRequest(User user, FabricContext context, Class<T> resultClazz,
                        String channelName, String funcName, String[] args, long timeout) {
        super(user, context, channelName, resultClazz, null, timeout);
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    protected Collection<ProposalResponse> getResponses() throws Exception {
        QueryByChaincodeRequest request = hfClient.newQueryProposalRequest();
        request.setChaincodeID(CommonUtils.getChainCodeId(fabricContext));
        request.setProposalWaitTime(timeout);
        request.setFcn(funcName);
        if (args != null && args.length != 0) {
            request.setArgs(args);
        }

        Collection<ProposalResponse> responses;
        responses = channel.queryByChaincode(request);
        return responses;
    }
}
