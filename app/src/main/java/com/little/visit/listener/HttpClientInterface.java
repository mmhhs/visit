package com.little.visit.listener;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface HttpClientInterface {

    String post(String url,Map<String, Object> argsMap) throws Exception;

    String get(String url,Map<String, Object> argsMap) throws Exception;

    String put(String url,Map<String, Object> argsMap) throws Exception;

    String uploadFiles(String url, Map<String, Object> param,List<File> files, String key) throws Exception;
}