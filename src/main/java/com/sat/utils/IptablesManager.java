package com.sat.utils;


public interface IptablesManager {
    void blockIPPort(String ip, int port);
    void unblockIPPort(String ip, int port);
    void openIPPort(String ip, int port);
    void blockIcmp(String ip);//禁止单个ping
    void blockALLIcmp();//禁止所有ping
    void acceptIcmp(String ip);//单个ping的通行规则
    void DacceptIcmp(String ip);//删掉单个ping的通行规则
    void openIcmp(String ip);//删掉禁止单个ping
    void blockTcpTemp(String ip);
    void openSelfPort(int port);
}
