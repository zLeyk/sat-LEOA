package com.sat.utils;

public interface IptablesManager {
    void blockIPPort(String ip, int port);
    void unblockIPPort(String ip, int port);
    void openIPPort(String ip, int port);
    void openSelfPort(int port);
}
