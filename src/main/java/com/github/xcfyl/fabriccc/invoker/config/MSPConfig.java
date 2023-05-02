package com.github.xcfyl.fabriccc.invoker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 西城风雨楼
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MSPConfig {
    /**
     * mspID
     */
    private String mspId;

    /**
     * 所属的机构
     */
    private String affiliation;
}
