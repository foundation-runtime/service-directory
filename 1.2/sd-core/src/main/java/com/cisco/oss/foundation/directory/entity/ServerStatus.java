package com.cisco.oss.foundation.directory.entity;


public enum ServerStatus {
    Unknown (-1),

    Disconnected (0),

    SyncConnected (3),

    AuthFailed (4),

    ConnectedReadOnly (5),

    SaslAuthenticated(6),

    Expired (-112);

    private final int intValue;     // Integer representation of value
                                    // for sending over wire

    ServerStatus(int intValue) {
        this.intValue = intValue;
    }

    public int getIntValue() {
        return intValue;
    }

    public static ServerStatus valueOf(int intValue) {
        switch(intValue) {
            case   -1: return ServerStatus.Unknown;
            case    0: return ServerStatus.Disconnected;
            case    3: return ServerStatus.SyncConnected;
            case    4: return ServerStatus.AuthFailed;
            case    5: return ServerStatus.ConnectedReadOnly;
            case    6: return ServerStatus.SaslAuthenticated;
            case -112: return ServerStatus.Expired;

            default:
                return null;
        }
    }
}
