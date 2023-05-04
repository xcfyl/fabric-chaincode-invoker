package com.github.xcfyl.fabriccc.invoker.wallet;

import com.github.xcfyl.fabriccc.invoker.config.DbWalletConfigProperties;
import com.github.xcfyl.fabriccc.invoker.user.FabricUser;
import com.github.xcfyl.fabriccc.invoker.utils.DateTransUtils;
import com.github.xcfyl.fabriccc.invoker.utils.FabricCryptoUtils;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.utils.SM2Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.User;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
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
public abstract class FabricAbstractWallet implements IFabricWallet, ApplicationContextAware {
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

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
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

        KeyPair keyPair = new KeyPair(this.publicKey, this.privateKey);
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
    public boolean removeUser(User user, boolean force) {
        List<WalletInfo> walletInfos = listWallet(user.getName());
        if (walletInfos == null || walletInfos.size() == 0) {
            return false;
        }

        KeyPair keyPair = new KeyPair(this.publicKey, this.privateKey);
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

    public abstract boolean doClearWallet();

    public abstract List<WalletInfo> doListWallet();

    public abstract List<WalletInfo> doListWallet(WalletStatus status);

    public abstract List<WalletInfo> doListWallet(String username, WalletStatus status);

    public abstract List<WalletInfo> doListWallet(String username);

    public abstract boolean doAddUser(WalletInfo walletInfo);

    public abstract boolean doRemoveUser(List<WalletInfo> walletInfoList, boolean force);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Resource walletKeyResource = applicationContext.getResource("classpath:.");
        try {
            String path = walletKeyResource.getURI().getPath();
            // 读取配置文件中指定位置的私钥
            String privateKeyPath = walletConfig.getPrivateKeyPath();
            // 读取配置文件中指定位置的公钥
            String publicKeyPath = walletConfig.getPublicKeyPath();
            Path pkPath = Paths.get(path, publicKeyPath);
            Path skPath = Paths.get(path, privateKeyPath);

            if (!Files.exists(pkPath) || !Files.exists(skPath)) {
                // 如果当前公私钥对已经存在了，那么就不进行创建
                if (!Files.exists(pkPath)) {
                    Files.createFile(pkPath);
                }
                if (!Files.exists(skPath)) {
                    Files.createFile(skPath);
                }
                KeyPair keyPair = SM2Utils.generateKeyPair();
                if (keyPair == null) {
                    throw new RuntimeException("生成钱包的公私钥对失败");
                }
                log.debug("钱包公私钥不存在，创建钱包公私钥");
                SM2Utils.saveKeyPairInPem(keyPair, pkPath.toFile().getPath(), skPath.toFile().getPath());
            } else {
                log.debug("找到了钱包公私钥，使用默认公私钥: pk: {}, sk: {}", pkPath.toFile().getPath(),
                        skPath.toFile().getPath());
            }
        } catch (Exception e) {
            throw new RuntimeException("获取钱包的公私钥相对路径失败");
        }

        Resource pkResource = applicationContext.getResource("classpath:wallet_db_pk");

        Resource skResource = applicationContext.getResource("classpath:wallet_db_sk");

        // 在这初始化私钥和公钥
        try (InputStream pkResourceInputStream = pkResource.getInputStream();
             InputStream skReourceInputStream = skResource.getInputStream()) {
            byte[] skBytes = IOUtils.toByteArray(skReourceInputStream);
            byte[] pkBytes = IOUtils.toByteArray(pkResourceInputStream);
            log.debug("公钥: {}", new String(skBytes));
            publicKey = SM2Utils.loadPublicKeyFromFile(new StringReader(new String(pkBytes)));
            privateKey = SM2Utils.loadPrivateKeyFromFile(new StringReader(new String(skBytes)), null);
        } catch (Exception e) {
            log.debug("初始化钱包公私钥对失败");
            throw new RuntimeException("初始化钱包公私钥对失败");
        }
    }
}
