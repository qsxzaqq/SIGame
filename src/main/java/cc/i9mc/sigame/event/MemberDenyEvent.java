package cc.i9mc.sigame.event;

import cc.i9mc.sigame.data.MemberResult;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Created by JinVan on 2021-01-14.
 */
public class MemberDenyEvent extends Event {
    private final MemberResult memberResult;
    private final HandlerList handlers = new HandlerList();

    public MemberDenyEvent(MemberResult memberResult) {
        this.memberResult = memberResult;
    }

    public String getName() {
        return memberResult.getName();
    }

    public UUID getUUID() {
        return memberResult.getUuid();
    }

    public String getTarget() {
        return memberResult.getTarget();
    }

    public UUID getTargetUUID() {
        return memberResult.getTargetUuid();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
