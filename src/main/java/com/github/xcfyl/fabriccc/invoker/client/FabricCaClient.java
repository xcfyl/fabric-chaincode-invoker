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
    private final User caAdmin;

    public FabricCaClient(FabricContext context) {
        this.context = context;
        FabricUser admin = (FabricUser) context.getAdmin();
        String adminName = admin.getName();
        String password = admin.getPassword();
        caAdmin = enrollUser(adminName, password);
    }

    /**
     * 向CA注册用户
     *
     * @param username
     * @param password
     * @return
     */
    public boolean registerUser(String username, String password) {
        try {
            HFCAClient hfcaClient = context.getHfCAClient();
            if (hfcaClient == null) {
                return false;
            }
            RegistrationRequest registrationRequest = new RegistrationRequest(username);
            registrationRequest.setSecret(password);
            String affiliation = caAdmin.getAffiliation();
            registrationRequest.setAffiliation(affiliation);
            String secret = hfcaClient.register(registrationRequest, caAdmin);
            return !StrUtil.isBlank(secret);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 撤销某个用户的证书
     *
     * @param revokedUser
     * @return
     */
    public boolean revokeUser(String revokedUser) {
        try {
            HFCAClient hfcaClient = context.getHfCAClient();
            if (hfcaClient == null) {
                return false;
            }
            hfcaClient.revoke(caAdmin, revokedUser, "revoked");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 注册的同时，获取该用户的证书
     *
     * @param username
     * @param passwd
     * @return
     */
    public User registerAndEnroll(String username, String passwd) {
        if (registerUser(username, passwd)) {
            return enrollUser(username, passwd);
        }
        return null;
    }

    /**
     * 获取某个用户的证书，前提是该用户已经注册过了
     *
     * @param username
     * @param password
     * @return
     */
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
        fabricUser.setName(username);
        fabricUser.setPassword(password);
        fabricUser.setMspId(context.getAdmin().getMspId());
        fabricUser.setEnrollment(enrollment);

        return fabricUser;
    }
}
