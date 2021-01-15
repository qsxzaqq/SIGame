package cc.i9mc.sigame.data;

import lombok.Data;

import java.util.UUID;

/**
 * Created by JinVan on 2021-01-12.
 */
@Data
public class SIGameJoin {
    private String server;
    private UUID uuid;
    private UUID player;
    private JoinType joinType;

    public enum JoinType {
        MEMBER, TRUST, DEFAULT
    }
}
