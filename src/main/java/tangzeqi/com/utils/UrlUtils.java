package tangzeqi.com.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {

    public static String processUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;

        String cleanUrl = url.trim();
        // 补全协议
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") &&
                !cleanUrl.startsWith("file://") && !cleanUrl.startsWith("about:")) {
            cleanUrl = "https://" + cleanUrl;
        }
        // 验证URL
        try {
            new URL(cleanUrl);
        } catch (MalformedURLException e) {
            return null;
        }
        return cleanUrl;
    }

}
