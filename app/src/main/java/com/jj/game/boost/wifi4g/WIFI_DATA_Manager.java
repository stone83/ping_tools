package com.jj.game.boost.wifi4g;

/**
 * Created by huzd on 2017/7/6.
 */

public class WIFI_DATA_Manager {
    private static AbstractWIFI_DATA instance = null;
    private WIFI_DATA_Manager(){

    }
    public static AbstractWIFI_DATA getInstance(){
        if(null == instance){
            synchronized (WIFI_DATA_Manager.class){
                if (null == instance){
                    if(AbstractWIFI_DATA.getNetType().equals(Const.WIFI)){
                        instance = new WifiControl();
                    } else if(AbstractWIFI_DATA.getNetType().equals(Const.DATA)) {
                        instance = new DataControl();
                    }
                }
            }
        }
        return instance;
    }
    public static void release(){
        instance = null;
    }
}
