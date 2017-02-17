package com.little.visit.impl;

import com.little.visit.listener.HttpClientInterface;

import java.io.File;
import java.util.List;
import java.util.Map;

public class LittleHttpClient implements HttpClientInterface {

    public LittleHttpClient() {
    }

    public String post(String url, Map<String, Object> argsMap) throws Exception {
        return "";
    }

    public String get(String url, Map<String, Object> argsMap) throws Exception {
        return "";
    }

    public String put(String url, Map<String, Object> argsMap) throws Exception {
        return "";
    }

    public String uploadFiles(String url, Map<String, Object> param, List<File> files, String key) throws Exception {
        return "";
    }
}