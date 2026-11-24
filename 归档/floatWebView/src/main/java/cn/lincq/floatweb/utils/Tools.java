package cn.lincq.floatweb.utils;

public class Tools {
    public static boolean isUrl(String url) {
        String urlRegex = "^(http://|https://|file://).+";
        return url != null && !url.isEmpty() && url.matches(urlRegex);
    }
}
