# 创建钱包数据库相关的表

# 删除钱包信息表
DROP TABLE IF EXISTS t_wallet_info;

# 创建钱包信息表
CREATE TABLE IF NOT EXISTS t_wallet_info(
    wallet_id INT COMMENT '钱包的id，自增主键',
    public_key VARCHAR(4096) COMMENT '证书公钥',
    private_key VARCHAR(4096) COMMENT '证书私钥',
    username VARCHAR(256) COMMENT '所属的用户',
    password VARCHAR(256) COMMENT 'CA密码',
    mspId VARCHAR(256) COMMENT 'MSP ID号',
    create_time DATETIME COMMENT '创建时间',
    expired_time DATETIME COMMENT '过期时间',
    status INT COMMENT '当前钱包的状态',
    public_key_hash VARCHAR(256) COMMENT '公钥的hash值',
    PRIMARY KEY(wallet_id, username, public_key_hash)
);

