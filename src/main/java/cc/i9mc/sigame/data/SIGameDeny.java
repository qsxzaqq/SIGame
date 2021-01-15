package cc.i9mc.sigame.data;

import lombok.Getter;

/**
 * Created by JinVan on 2021-01-14.
 */
public enum SIGameDeny {
    ALLOW("通过"),
    DATA_NULL("数据加载失败, 请重试"),
    WARP_NULL("该岛屿未开启传送");

    @Getter
    private final String message;

    SIGameDeny(String message) {
        this.message = message;
    }
}
