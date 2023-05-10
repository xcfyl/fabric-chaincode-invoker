package com.github.xcfyl.fabriccc.invoker.request;

import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.handler.ResultHandler;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author 西城风雨楼
 */
public class InitRequest extends AbstractFabricRequest<String> {
    public InitRequest(FabricContext context, String channelName, ResultHandler<String> resultHandler, long timeout) {
        super(context.getAdmin(), context, channelName, String.class, null, resultHandler, timeout);
    }

    @Override
    protected Collection<ProposalResponse> getResponses() throws Exception {
        InstantiateProposalRequest request = hfClient.newInstantiationProposalRequest();
        request.setChaincodeID(CommonUtils.getChainCodeId(fabricContext));
        request.setProposalWaitTime(timeout);
        request.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        request.setFcn("init");
        request.setArgs("");
        Map<String, byte[]> tm = new HashMap<>(100);
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));

        final Collection<ProposalResponse> responses;
        request.setTransientMap(tm);
        String policyPath = fabricContext.getFabricConfig().getChainCodeConfig().getPolicyPath();
        if (policyPath != null && policyPath.length() != 0) {
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File(policyPath));
            request.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
        }

        responses = channel.sendInstantiationProposal(request);

        return responses;
    }
}
