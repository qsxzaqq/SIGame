package cc.i9mc.sigame.data;

import lombok.Data;
import lombok.Getter;

import java.util.UUID;

/**
 * Created by JinVan on 2021-01-12.
 */
@Data
public class GameJoin {
    private String server;
    private UUID uuid;
    private UUID player;

    public enum JoinResult {
        ALLOW("通过"),
        DATA_NULL("数据加载失败, 请重试"),
        MEMBER_NULL("该岛屿成员不在线，请稍后重试"),
        WARP_NULL("该岛屿未开启传送");

        @Getter
        private final String message;

        JoinResult(String message) {
            this.message = message;
        }
    }
}
