package com.github.xcfyl.fabriccc.invoker.wallet;

import com.github.xcfyl.fabriccc.invoker.config.DbWalletConfigProperties;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.utils.DateTransUtils;
import com.github.xcfyl.fabriccc.invoker.utils.FabricCryptoUtils;
import com.github.xcfyl.fabriccc.invoker.utils.SM2Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.User;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @author 西城风雨楼
 */
@Slf4j
public abstract class FabricAbstractWallet implements IFabricWallet  {
    protected final FabricContext fabricContext;
    protected final DbWalletConfigProperties walletConfig;

    protected WalletAddInterceptor walletAddInterceptor;

    protected WalletRemoveInterceptor walletRemoveInterceptor;
    protected WalletQueryInterceptor walletQueryInterceptor;

    private final ApplicationContext applicationContext;

    protected PrivateKey privateKey;

    protected PublicKey publicKey;

    protected KeyPair keyPair;

    public FabricAbstractWallet(FabricContext fabricContext, DbWalletConfigProperties config, ApplicationContext applicationContext) {
        this.fabricContext = fabricContext;
        this.walletConfig = config;
        this.applicationContext = applicationContext;
        initKeyPair();
    }

    public synchronized void setWalletAddInterceptor(WalletAddInterceptor walletAddInterceptor) {
        this.walletAddInterceptor = walletAddInterceptor;
    }

    public synchronized void setWalletRemoveInterceptor(WalletRemoveInterceptor walletRemoveInterceptor) {
        this.walletRemoveInterceptor = walletRemoveInterceptor;
    }

    public synchronized void setWalletQueryInterceptor(WalletQueryInterceptor walletQueryInterceptor) {
        this.walletQueryInterceptor = walletQueryInterceptor;
    }

    @Override
    public synchronized boolean clearWallet() {
        List<WalletInfo> walletInfos = listWallet();

        if (walletInfos == null || walletInfos.size() == 0) {
            return false;
        }

        if (walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.preRemove(keyPair, walletInfos, true);
            if (res != null) {
                return res;
            }
        }

        boolean doRes = doClearWallet();

        if (doRes && walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.afterRemove(keyPair, walletInfos, true);
            if (res != null) {
                return res;
            }
        }

        return doRes;
    }

    @Override
    public synchronized List<WalletInfo> listWallet() {
        List<WalletInfo> walletInfos = doListWallet();
        if (walletQueryInterceptor != null) {
            return walletQueryInterceptor.filterWalletInfo(keyPair, walletInfos);
        }
        return walletInfos;
    }

    @Override
    public synchronized List<WalletInfo> listWallet(WalletStatus status) {
        List<WalletInfo> walletInfos = doListWallet(status);
        if (walletQueryInterceptor != null) {
            return walletQueryInterceptor.filterWalletInfo(keyPair, walletInfos);
        }
        return walletInfos;
    }

    @Override
    public synchronized List<WalletInfo> listWallet(String username, WalletStatus status) {
        List<WalletInfo> walletInfos = doListWallet(username, status);
        if (walletQueryInterceptor != null) {
            return walletQueryInterceptor.filterWalletInfo(keyPair, walletInfos);
        }
        return walletInfos;
    }

    @Override
    public synchronized List<WalletInfo> listWallet(String username) {
        List<WalletInfo> walletInfos = doListWallet(username);
        if (walletQueryInterceptor != null) {
            return walletQueryInterceptor.filterWalletInfo(keyPair, walletInfos);
        }
        return walletInfos;
    }

    @Override
    public synchronized boolean addUser(User user, Date expiredTime) {
        FabricUser fabricUser = (FabricUser) user;
        WalletInfo walletInfo = new WalletInfo();
        Integer walletId = walletConfig.getWalletId();
        LocalDate now = LocalDate.now();
        String publicKey = fabricUser.getEnrollment().getCert();
        String privateKey = new String(FabricCryptoUtils.getPrivateKeyBytes(fabricUser));
        String username = fabricUser.getName();
        String password = fabricUser.getPassword();
        String mspId = fabricUser.getMspId();
        byte[] publicKeyHash = fabricContext.hash(publicKey.getBytes());
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

        // 这里相当于是什么都没有做，没有进行加密或者解密

        if (walletAddInterceptor != null) {
            Boolean res = walletAddInterceptor.preAdd(keyPair, walletInfo);
            if (res != null) {
                return res;
            }
        }

        boolean doRes = doAddUser(walletInfo);

        if (doRes && walletAddInterceptor != null) {
            Boolean res = walletAddInterceptor.afterAdd(keyPair, walletInfo);
            if (res != null) {
                return res;
            }
        }
        return doRes;
    }

    @Override
    public synchronized boolean removeUser(User user, boolean force) {
        List<WalletInfo> walletInfos = listWallet(user.getName());
        if (walletInfos == null || walletInfos.size() == 0) {
            return false;
        }

        if (walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.preRemove(keyPair, walletInfos, force);
            if (res != null) {
                return res;
            }
        }

        boolean doRes = doRemoveUser(walletInfos, force);

        if (doRes && walletRemoveInterceptor != null) {
            Boolean res = walletRemoveInterceptor.afterRemove(keyPair, walletInfos, force);
            if (res != null) {
                return res;
            }
        }
        return doRes;
    }

    protected abstract boolean doClearWallet();

    protected abstract List<WalletInfo> doListWallet();

    protected abstract List<WalletInfo> doListWallet(WalletStatus status);

    protected abstract List<WalletInfo> doListWallet(String username, WalletStatus status);

    protected abstract List<WalletInfo> doListWallet(String username);

    protected abstract boolean doAddUser(WalletInfo walletInfo);

    protected abstract boolean doRemoveUser(List<WalletInfo> walletInfoList, boolean force);

    private void initKeyPair() {
        Resource pkResource = applicationContext.getResource("classpath:" + walletConfig.getPublicKeyPath());
        Resource skResource = applicationContext.getResource("classpath:" + walletConfig.getPrivateKeyPath());


        // 在这初始化私钥和公钥
        try (InputStream pkResourceInputStream = pkResource.getInputStream();
             InputStream skReourceInputStream = skResource.getInputStream()) {
            if (log.isDebugEnabled()) {
                log.debug("公钥路径: {}", pkResource.getFile().getPath());
                log.debug("私钥路径: {}", skResource.getFile().getPath());
            }
            byte[] skBytes = IOUtils.toByteArray(skReourceInputStream);
            byte[] pkBytes = IOUtils.toByteArray(pkResourceInputStream);
            publicKey = SM2Utils.loadPublicKeyFromFile(new StringReader(new String(pkBytes)));
            privateKey = SM2Utils.loadPrivateKeyFromFile(new StringReader(new String(skBytes)), null);
            keyPair = new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            log.debug("初始化钱包公私钥对失败");
            throw new RuntimeException("初始化钱包公私钥对失败");
        }
    }
}
