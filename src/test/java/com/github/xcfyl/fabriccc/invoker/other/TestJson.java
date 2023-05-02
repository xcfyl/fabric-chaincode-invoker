package com.github.xcfyl.fabriccc.invoker.other;

import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 西城风雨楼
 */
@SpringBootTest
public class TestJson {
    @Test
    public void test() {
        Boolean aTrue = JSONUtil.toBean("true", Boolean.class);
        System.out.println(aTrue);
    }
}
