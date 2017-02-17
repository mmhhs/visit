package com.little.visit;

public class TaskConstant {
    public final static int POST = 1;
    public final static int GET = 2;
    public final static int PUT = 3;
    public final static int UPLOAD = 4;//上传文件


    public enum TaskResult
    {
        OK,
        ERROR,
        CANCELLED,
        NETERROR,
        NOTHING
    }
}