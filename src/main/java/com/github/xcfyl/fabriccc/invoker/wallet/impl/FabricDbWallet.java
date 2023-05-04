package com.github.xcfyl.fabriccc.invoker.wallet.impl;

import com.github.xcfyl.fabriccc.invoker.config.DbWalletConfigProperties;
import com.github.xcfyl.fabriccc.invoker.context.FabricContext;
import com.github.xcfyl.fabriccc.invoker.utils.SM2Utils;
import com.github.xcfyl.fabriccc.invoker.wallet.FabricAbstractWallet;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletInfo;
import com.github.xcfyl.fabriccc.invoker.wallet.WalletStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * 使用数据库实现的钱包
 *
 * @author 西城风雨楼
 */
@Slf4j
public class FabricDbWallet extends FabricAbstractWallet {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<WalletInfo> rowMapper = (rs, rowNum) -> {
        Integer walletId = rs.getInt("wallet_id");
        String publicKey = rs.getString("public_key");
        String privateKey = rs.getString("private_key");
        String name = rs.getString("username");
        String password = rs.getString("password");
        String mspId = rs.getString("mspId");
        Date createTime = rs.getDate("create_time");
        Date expiredTime = rs.getDate("expired_time");
        Integer status = rs.getInt("status");
        String publicKeyHash = rs.getString("public_key_hash");
        String extended = rs.getString("extended");
        // 对privateKey进行解密
        if (privateKey == null) {
            throw new RuntimeException("privateKey未找到");
        }
        // 先对私钥的字节进行Base64解码
        byte[] privateKeyDecode = Base64.getDecoder().decode(privateKey.getBytes());
        // 然后对解码后的私钥进行解密
        byte[] decryptPrivateKey = SM2Utils.decrypt(FabricDbWallet.this.privateKey, privateKeyDecode);
        byte[] decodePublicKey = Base64.getDecoder().decode(publicKey.getBytes());
        byte[] publicKeyHashDecode = Base64.getDecoder().decode(publicKeyHash);

        WalletInfo walletInfo = new WalletInfo();
        walletInfo.setStatus(status);
        walletInfo.setPassword(password);
        walletInfo.setUsername(name);
        walletInfo.setWalletId(walletId);
        walletInfo.setMspId(mspId);
        walletInfo.setExpiredTime(expiredTime);
        walletInfo.setCreateTime(createTime);
        walletInfo.setPrivateKey(new String(decryptPrivateKey));
        walletInfo.setPublicKey(new String(decodePublicKey));
        walletInfo.setPublicKeyHash(publicKeyHashDecode);
        walletInfo.setExtended(extended);
        return walletInfo;
    };

    public FabricDbWallet(FabricContext fabricContext, JdbcTemplate jdbcTemplate,
                          DbWalletConfigProperties walletConfig, ApplicationContext applicationContext) {
        // 首先创建数据库数据源
        super(fabricContext, walletConfig, applicationContext);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean doClearWallet() {
        PreparedStatementCreator psc = con -> {
            String sql = "DELETE FROM t_wallet_info WHERE wallet_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, walletConfig.getWalletId());
            return ps;
        };
        int update = jdbcTemplate.update(psc);
        return update != 0;
    }

    @Override
    public List<WalletInfo> doListWallet() {
        PreparedStatementCreator psc = con -> {
            String sql = "SELECT * FROM t_wallet_info WHERE wallet_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, walletConfig.getWalletId());
            return ps;
        };
        return jdbcTemplate.query(psc, rowMapper);
    }

    @Override
    public List<WalletInfo> doListWallet(WalletStatus status) {
        PreparedStatementCreator psc = con -> {
            String sql = "SELECT * FROM t_wallet_info WHERE wallet_id = ? AND status = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, walletConfig.getWalletId());
            ps.setInt(2, status.ordinal());
            return ps;
        };
        return jdbcTemplate.query(psc, rowMapper);
    }

    @Override
    public List<WalletInfo> doListWallet(String username, WalletStatus status) {
        PreparedStatementCreator psc = con -> {
            String sql = "SELECT * FROM t_wallet_info WHERE wallet_id = ? AND status = ? and username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, walletConfig.getWalletId());
            ps.setInt(2, status.ordinal());
            ps.setString(3, username);
            return ps;
        };
        return jdbcTemplate.query(psc, rowMapper);
    }

    @Override
    public List<WalletInfo> doListWallet(String username) {
        PreparedStatementCreator psc = con -> {
            String sql = "SELECT * FROM t_wallet_info WHERE wallet_id = ? and username = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, walletConfig.getWalletId());
            ps.setString(2, username);
            return ps;
        };
        return jdbcTemplate.query(psc, rowMapper);
    }

    /**
     * 使用钱包私钥对数据进行加密
     *
     * @param origin
     * @return
     */
    public byte[] encrypt(String origin) {
        return SM2Utils.encrypt(publicKey, origin);
    }

    /**
     * 使用钱包公钥对数据进行解密
     *
     * @param origin
     * @return
     */
    public byte[] decrypt(byte[] origin) {
        return SM2Utils.decrypt(privateKey, origin);
    }

    @Override
    public boolean doAddUser(WalletInfo walletInfo) {
        // 这里将User对象插入数据库中
        PreparedStatementCreator psc = con -> {
            String sql = "INSERT INTO t_wallet_info(wallet_id, public_key, private_key, username, password, mspid, create_time, expired_time, status, public_key_hash, extended) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, walletInfo.getWalletId());
            // 获取公钥的字节数组
            byte[] publicKeyBytes = walletInfo.getPublicKey().getBytes();
            // 获取私钥的字节数组
            byte[] privateKeyBytes = walletInfo.getPrivateKey().getBytes();
            // 对私钥进行加密
            byte[] privateKeyEncrypt = SM2Utils.encrypt(FabricDbWallet.this.publicKey, new String(privateKeyBytes));
            // 对加密后的私钥进行Base64编码
            byte[] privateKeyEncode = Base64.getEncoder().encode(privateKeyEncrypt);
            // 对公钥进行Base64编码
            byte[] publicKeyEncode = Base64.getEncoder().encode(publicKeyBytes);
            // 对私钥的编码进行加密
            ps.setString(2, new String(publicKeyEncode));
            ps.setString(3, new String(privateKeyEncode));
            ps.setString(4, walletInfo.getUsername());
            ps.setString(5, walletInfo.getPassword());
            ps.setString(6, walletInfo.getMspId());
            ps.setDate(7, new java.sql.Date(walletInfo.getCreateTime().getTime()));
            // 默认证书三年后过期
            ps.setDate(8, new java.sql.Date(walletInfo.getExpiredTime().getTime()));
            ps.setInt(9, walletInfo.getStatus());
            byte[] publicKeyHashEncode = Base64.getEncoder().encode(walletInfo.getPublicKeyHash());
            ps.setString(10, new String(publicKeyHashEncode));
            ps.setString(11, walletInfo.getExtended());
            return ps;
        };
        return jdbcTemplate.update(psc) != 0;
    }

    @Override
    public boolean doRemoveUser(List<WalletInfo> walletInfoList, boolean force) {
        // 这里将User对象插入数据库中
        for (WalletInfo walletInfo : walletInfoList) {
            PreparedStatementCreator psc = con -> {
                String sql;
                PreparedStatement ps;
                if (force) {
                    sql = "DELETE FROM t_wallet_info WHERE wallet_id = ? and username = ?";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, walletConfig.getWalletId());
                    ps.setString(2, walletInfo.getUsername());
                } else {
                    sql = "UPDATE t_wallet_info SET status = ? WHERE wallet_id = ? and username = ? and status != ?";
                    ps = con.prepareStatement(sql);
                    ps.setInt(1, WalletStatus.EXPIRED.ordinal());
                    ps.setInt(2, walletConfig.getWalletId());
                    ps.setString(3, walletInfo.getUsername());
                    ps.setInt(4, WalletStatus.EXPIRED.ordinal());
                }
                return ps;
            };
            int update = jdbcTemplate.update(psc);
            if (update == 0) {
                return false;
            }
        }
        return true;
    }
}
