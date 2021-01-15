package cc.i9mc.sigame.data;

import lombok.Data;

import java.util.UUID;

/**
 * Created by JinVan on 2021-01-14.
 */
@Data
public class MemberRequest {
    private UUID uuid;
    private String name;
    private String target;
}
