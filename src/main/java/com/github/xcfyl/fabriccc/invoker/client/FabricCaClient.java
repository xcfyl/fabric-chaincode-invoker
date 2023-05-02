package com.github.xcfyl.fabriccc.invoker.client;

import cn.hutool.core.util.StrUtil;
import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

/**
 * @author 西城风雨楼
 */
public class FabricCaClient {
    private final FabricContext context;

    public FabricCaClient(FabricContext context) {
        this.context = context;
    }

    public boolean registerUser(String username, String password) {
        try {
            HFCAClient hfcaClient = context.getHfCAClient();
            if (hfcaClient == null) {
                return false;
            }
            RegistrationRequest registrationRequest = new RegistrationRequest(username);
            registrationRequest.setSecret(password);
            String affiliation = context.getAdmin().getAffiliation();
            registrationRequest.setAffiliation(affiliation);
            String secret = hfcaClient.register(registrationRequest, context.getAdmin());
            return !StrUtil.isBlank(secret);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean revokeUser(String revokedUser) {
        try {
            HFCAClient hfcaClient = context.getHfCAClient();
            if (hfcaClient == null) {
                return false;
            }
            hfcaClient.revoke(context.getAdmin(), revokedUser, "revoked");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User enrollUser(String username, String password) {
        HFCAClient client = context.getHfCAClient();
        if (client == null) {
            return null;
        }
        Enrollment enrollment;
        try {
            enrollment = client.enroll(username, password);
        } catch (Exception e) {
            return null;
        }
        FabricUser fabricUser = new FabricUser();
        fabricUser.setEnrollment(enrollment);

        return fabricUser;
    }
}
