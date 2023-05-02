package com.github.xcfyl.fabriccc.invoker.wallet;

import com.github.xcfyl.fabriccc.invoker.client.FabricCryptoClient;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.wallet.impl.FabricDbWallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author 西城风雨楼
 */
@SpringBootTest
public class TestDbWallet {
    @Autowired
    private FabricContext fabricContext;

    @Autowired
    private FabricDbWallet fabricDbWallet;

    @Test
    public void testListWallet() {
        System.out.println(fabricDbWallet.listWallet());
        System.out.println(fabricDbWallet.listWallet("admin"));
        System.out.println(fabricDbWallet.listWallet("admin", WalletStatus.EXPIRED));
        System.out.println(fabricDbWallet.listWallet(WalletStatus.EXPIRED));
    }

    @Test
    public void queryWalletInfo() {
        List<WalletInfo> walletInfos = fabricDbWallet.listWallet("admin");
        WalletInfo walletInfo = walletInfos.get(0);
        byte[] hash = walletInfo.getPublicKeyHash();

        FabricCryptoClient fabricCryptoClient = fabricContext.getFabricCryptoClient();
        byte[] hash1 = fabricCryptoClient.hash(fabricContext.getAdmin().getEnrollment().getCert().getBytes());

        System.out.println(Arrays.equals(hash1, hash));
    }

    @Test
    public void testClearWallet() {
        if (fabricDbWallet.clearWallet()) {
            System.out.println("清空钱包成功");
        } else {
            System.out.println("清空钱包失败");
        }
    }


    @Test
    public void testAddUser() {
        if (fabricDbWallet.addUser(fabricContext.getAdmin(), null)) {
            System.out.println("添加用户成功");
        } else {
            System.out.println("添加用户失败");
        }
    }

    @Test
    public void testRemoveUserForce() {
        if (fabricDbWallet.removeUser(fabricContext.getAdmin(), true)) {
            System.out.println("删除用户成功");
        } else {
            System.out.println("删除用户失败");
        }
    }

    @Test
    public void testRemoveUser() {
        if (fabricDbWallet.removeUser(fabricContext.getAdmin(), false)) {
            System.out.println("删除用户成功");
        } else {
            System.out.println("删除用户失败");
        }
    }
}
