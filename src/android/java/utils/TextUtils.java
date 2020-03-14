package com.xylink.sdk.sample.utils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhangyazhou
 * @date 2018/9/15
 */
public class TextUtils {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * 空字符串
     */
    public static final String EMPTY  = "";
    /**
     * 逗号
     */
    public static final String COMMA  = ",";
    /**
     * 加号
     */
    public static final String PLUS = "+";
    /**
     * 连字符 - 或者减号
     */
    public static final String HYPHEN = "-";

    public static final String AT = "@";

    public static boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }

    public static boolean isNotEmpty(String text) {
        return text != null && text.length() > 0;
    }

    public static boolean isJSON(String data) {
        return data != null && (data.startsWith("{") || data.startsWith("["));
    }

    public static String removeSpecialCharacters(String origin) {
        String regEx="[，。\\？\\！：、@……“；‘～\\.\\-（《〈〔*&\\［【——｀\\#￥\\%ˇ•\\+\\=\\｛ˉ¨．\\｜〃‖々「『〖∶＇＂／\\＊\\＆＼＃\\＄\\％︿＿\\＋－＝＜,.\\?\\!\\:/\\@...\\\";\\'\\~()<>\\*&\\[\\]\\`#\\$\\%\\^\\+\\-=\\{\\}|〗』」\\｝】\\］〕〉》）\\’\\”]+";
        Pattern p   =   Pattern.compile(regEx);
        Matcher m   =   p.matcher(origin);
        return m.replaceAll("").trim();
    }
}
