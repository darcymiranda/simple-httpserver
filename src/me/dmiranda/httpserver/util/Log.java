package me.dmiranda.httpserver.util;

import java.util.Date;

/**
 * Created by dmiranda on 7/29/2014.
 */
public class Log {

    public static final int DEBUG = 1, INFO = 2, ERROR = 3, NONE = 4;
    private static boolean B_DEBUG, B_INFO, B_ERROR, B_NONE;
    private static int type;

    public static void set(int type){
        Log.type = type;
        B_DEBUG = type <= DEBUG;
        B_INFO = type <= INFO;
        B_ERROR = type <= ERROR;
        B_NONE = type <= NONE;
    }

    public static void error(String desc, String msg){
        if(B_ERROR) print("ERROR", desc, msg);
    }

    public static void error(String desc, int msg){
        if(B_ERROR) error(desc, Integer.toString(msg));
    }

    public static void error(String desc, Exception ex){
        if(B_ERROR){ error(desc, ex.toString()); ex.printStackTrace(); }
    }

    public static void debug(String desc, String msg){
        if(B_DEBUG) print("DEBUG", desc, msg);
    }

    public static void debug(String desc, int msg){
        if(B_DEBUG) debug(desc, Integer.toString(msg));
    }

    public static void debug(String desc, Exception ex){
        if(B_DEBUG){ debug(desc, ex.toString()); ex.printStackTrace(); }
    }

    public static void info(String desc, String msg){
        if(B_INFO) print("LOG", desc, msg);
    }

    public static void info(String desc, int msg){
        if(B_INFO) info(desc, Integer.toString(msg));
    }

    private static void print(String type, String desc, String msg){

        StringBuilder sb = new StringBuilder();
        sb.append(new Date());
        sb.append(" - [");
        sb.append(type);
        sb.append("] ");
        sb.append(desc);
        sb.append(": ");
        sb.append(msg);
        System.out.println(sb);
    }

}
