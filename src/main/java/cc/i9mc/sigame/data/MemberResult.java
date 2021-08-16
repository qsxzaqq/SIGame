package cc.i9mc.sigame.data;

import lombok.Data;

import java.util.UUID;

/**
 * Created by JinVan on 2021-01-14.
 */
@Data
public class MemberResult {
    private UUID uuid;
    private String name;
    private UUID targetUuid;
    private String target;
    private MemberResultType memberResultType;

    public enum MemberResultType {
        ACCEPT, DENY
    }
}
