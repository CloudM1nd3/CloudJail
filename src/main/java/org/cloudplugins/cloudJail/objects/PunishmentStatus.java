package org.cloudplugins.cloudJail.objects;

import org.cloudplugins.cloudJail.configs.Messages;

public enum PunishmentStatus {
    ACTIVE(Messages.STATUS_ACTIVE),
    EXPIRED(Messages.STATUS_EXPIRED),
    RELEASED(Messages.STATUS_RELEASED);

    private final Messages message;

    PunishmentStatus(Messages message) {
        this.message = message;
    }

    public Messages getMessage() {
        return message;
    }

}
