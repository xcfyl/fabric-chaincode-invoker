package com.github.xcfyl.fabriccc.invoker.request;

import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.utils.CommonUtils;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest;

import java.io.File;
import java.util.Collection;

/**
 * @author 西城风雨楼
 */
public class InstallRequest extends AbstractFabricRequest<Boolean> {

    public InstallRequest(FabricContext context, long timeout) {
        super(context.getAdmin(), context, Boolean.class, null, null, timeout);
    }

    @Override
    protected Collection<ProposalResponse> getResponses() throws Exception {
        // 创建安装链码的请求对象
        InstallProposalRequest installRequest = hfClient.newInstallProposalRequest();
        installRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        // 获取chaincodeId对象
        ChaincodeID chainCodeId = CommonUtils.getChainCodeId(fabricContext);
        installRequest.setChaincodeID(chainCodeId);
        installRequest.setUserContext(hfClient.getUserContext());
        // 设置链码的工程名称
        installRequest.setChaincodePath(chainCodeId.getPath());
        // 设置链码的版本
        installRequest.setChaincodeVersion(chainCodeId.getVersion());
        // 设置提交提议的等待时间
        installRequest.setProposalWaitTime(timeout);

        Collection<ProposalResponse> responses;
        installRequest.setChaincodeSourceLocation(new File(fabricContext.getConfig().getChainCodeConfig().getGopath()));
        installRequest.setProposalWaitTime(timeout);
        responses = hfClient.sendInstallProposal(installRequest, channel.getPeers());
        return responses;
    }
}
