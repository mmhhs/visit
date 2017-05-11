package com.little.visit.util;

public class StringUtil {

    private StringUtil() {
        throw new AssertionError();
    }

    /**
     * is null or its length is 0
     * 
     * <pre>
     * isEmpty(null) = true;
     * isEmpty(&quot;&quot;) = true;
     * isEmpty(&quot;  &quot;) = false;
     * </pre>
     * 
     * @param str
     * @return if string is null or its size is 0, return true, else return false.
     */
    public static boolean isEmpty(CharSequence str) {
        if (str==null){
            return true;
        }
        if (str.equals("")){
            return true;
        }
        if (str.length() <= 0){
            return true;
        }
        return false;
    }



}
