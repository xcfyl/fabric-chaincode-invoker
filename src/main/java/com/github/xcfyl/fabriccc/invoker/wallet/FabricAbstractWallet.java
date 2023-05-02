package com.github.xcfyl.fabriccc.invoker.wallet;

import com.github.xcfyl.fabriccc.invoker.config.DbWalletConfigProperties;
import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.utils.DateTransUtils;
import com.github.xcfyl.fabriccc.invoker.utils.FabricCryptoUtils;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import org.hyperledger.fabric.sdk.User;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author 西城风雨楼
 */
public abstract class FabricAbstractWallet implements IFabricWallet {
    protected final FabricContext fabricContext;
    protected final DbWalletConfigProperties walletConfig;

    protected WalletAddInterceptor walletAddInterceptor;

    protected WalletRemoveInterceptor walletRemoveInterceptor;

    protected PrivateKey privateKey;

    protected PublicKey publicKey;

    public FabricAbstractWallet(FabricContext fabricContext, DbWalletConfigProperties config) {
        this.fabricContext = fabricContext;
        this.walletConfig = config;
    }

    public void setWalletAddInterceptor(WalletAddInterceptor walletAddInterceptor) {
        this.walletAddInterceptor = walletAddInterceptor;
    }

    public void setWalletRemoveInterceptor(WalletRemoveInterceptor walletRemoveInterceptor) {
        this.walletRemoveInterceptor = walletRemoveInterceptor;
    }

    public DbWalletConfigProperties getWalletConfig() {
        return walletConfig;
    }

    @Override
    public boolean clearWallet() {
        List<WalletInfo> walletInfos = listWallet();

        if (walletInfos == null || walletInfos.size() == 0) {
            return false;
        }

        if (walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.preRemove(walletInfos, true);
            if (res != null) {
                return res;
            }
        }

        boolean doRes = doClearWallet();

        if (doRes && walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.afterRemove(walletInfos, true);
            if (res != null) {
                return res;
            }
        }

        return doRes;
    }

    @Override
    public List<WalletInfo> listWallet() {
        return doListWallet();
    }

    @Override
    public List<WalletInfo> listWallet(WalletStatus status) {
        return doListWallet(status);
    }

    @Override
    public List<WalletInfo> listWallet(String username, WalletStatus status) {
        return doListWallet(username, status);
    }

    @Override
    public List<WalletInfo> listWallet(String username) {
        return doListWallet(username);
    }

    @Override
    public boolean addUser(User user, Date expiredTime) {
        FabricUser fabricUser = (FabricUser) user;
        WalletInfo walletInfo = new WalletInfo();
        Integer walletId = walletConfig.getWalletId();
        LocalDate now = LocalDate.now();
        String publicKey = fabricUser.getEnrollment().getCert();
        String privateKey = new String(FabricCryptoUtils.getPrivateKeyBytes(fabricUser));
        String username = fabricUser.getName();
        String password = fabricUser.getPassword();
        String mspId = fabricUser.getMspId();
        byte[] publicKeyHash = fabricContext.getFabricCryptoClient().hash(publicKey.getBytes());
        Date createTime = DateTransUtils.localDateToUtilDate(now);
        if (expiredTime == null) {
            expiredTime = DateTransUtils.localDateToUtilDate(now.plusYears(3));
        }
        Integer status = WalletStatus.NORMAL.ordinal();

        walletInfo.setWalletId(walletId);
        walletInfo.setPublicKey(publicKey);
        walletInfo.setUsername(username);
        walletInfo.setPrivateKey(privateKey);
        walletInfo.setPassword(password);
        walletInfo.setMspId(mspId);
        walletInfo.setCreateTime(createTime);
        walletInfo.setExpiredTime(expiredTime);
        walletInfo.setStatus(status);
        walletInfo.setPublicKeyHash(publicKeyHash);

        if (walletAddInterceptor != null) {
            Boolean res = walletAddInterceptor.preAdd(walletInfo);
            if (res != null) {
                return res;
            }
        }

        boolean doRes = doAddUser(walletInfo);

        if (doRes && walletAddInterceptor != null) {
            Boolean res = walletAddInterceptor.afterAdd(walletInfo);
            if (res != null) {
                return res;
            }
        }
        return doRes;
    }

    @Override
    public boolean removeUser(User user, boolean force) {
        List<WalletInfo> walletInfos = listWallet(user.getName());
        if (walletInfos == null || walletInfos.size() == 0) {
            return false;
        }

        if (walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.preRemove(walletInfos, force);
            if (res != null) {
                return res;
            }
        }

        boolean doRes = doRemoveUser(walletInfos, force);

        if (doRes && walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.afterRemove(walletInfos, force);
            if (res != null) {
                return res;
            }
        }
        return doRes;
    }

    public abstract boolean doClearWallet();

    public abstract List<WalletInfo> doListWallet();

    public abstract List<WalletInfo> doListWallet(WalletStatus status);

    public abstract List<WalletInfo> doListWallet(String username, WalletStatus status);

    public abstract List<WalletInfo> doListWallet(String username);

    public abstract boolean doAddUser(WalletInfo walletInfo);

    public abstract boolean doRemoveUser(List<WalletInfo> walletInfoList, boolean force);
}
