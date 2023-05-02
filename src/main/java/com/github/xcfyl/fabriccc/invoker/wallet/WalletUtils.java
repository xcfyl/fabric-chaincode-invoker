package com.github.xcfyl.fabriccc.invoker.wallet;

import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.utils.FabricCryptoUtils;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

import java.security.PrivateKey;

/**
 * @author 西城风雨楼
 */
public class WalletUtils {
    public static User parseUser(WalletInfo walletInfo) {
        FabricUser fabricUser = new FabricUser();
        PrivateKey privateKey = FabricCryptoUtils.getPrivateKey(walletInfo.getPrivateKey().getBytes());
        Enrollment enrollment = new X509Enrollment(privateKey, walletInfo.getPublicKey());
        fabricUser.setEnrollment(enrollment);
        fabricUser.setName(walletInfo.getUsername());
        fabricUser.setPassword(walletInfo.getPassword());
        fabricUser.setMspId(walletInfo.getMspId());
        return fabricUser;
    }
}
