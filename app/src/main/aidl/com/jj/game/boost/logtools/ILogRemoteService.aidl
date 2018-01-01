package com.jj.game.boost.logtools;

interface ILogRemoteService {
    void saveIP(String ip);
    void savePort(String port);
    void saveTime(String time);
    String getIP();
    String getPort();
    String getTime();
    void ping();
    void storageDelay();
}