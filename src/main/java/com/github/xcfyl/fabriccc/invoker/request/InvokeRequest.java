package com.github.xcfyl.fabriccc.invoker.request;

import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.handler.ResultHandler;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author 西城风雨楼
 */
public class InvokeRequest<T> extends AbstractFabricRequest<T> {
    private final String funcName;
    private final String[] args;

    public InvokeRequest(FabricContext context, User user, Class<T> resultClazz, Class<?> genericClass, String channelName,
                         String funcName, String[] args, ResultHandler<T> resultHandler, long timeout) {
        super(user, context, channelName, resultClazz, genericClass, resultHandler, timeout);
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    protected Collection<ProposalResponse> getResponses() throws Exception {
        TransactionProposalRequest request = hfClient.newTransactionProposalRequest();
        request.setChaincodeID(CommonUtils.getChainCodeId(fabricContext));
        request.setFcn(funcName);
        if (args != null && args.length != 0) {
            request.setArgs(args);
        }
        request.setProposalWaitTime(timeout);
        Map<String, byte[]> map = new HashMap<>(10);
        map.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        map.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        map.put("result", ":)".getBytes(UTF_8));
        map.put("event", "!".getBytes(UTF_8));

        Collection<ProposalResponse> responses;
        request.setTransientMap(map);
        responses = channel.sendTransactionProposal(request, channel.getPeers());
        return responses;
    }
}
