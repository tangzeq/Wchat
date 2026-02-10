package tangzeqi.com.tools.folder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileEncodingDetector {
    public static String detectEncoding(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead = input.read(buffer);

            // 检查是否是UTF-8 BOM
            if (bytesRead >= 3 && buffer[0] == (byte)0xEF &&
                    buffer[1] == (byte)0xBB && buffer[2] == (byte)0xBF) {
                return "UTF-8";
            }

            // 检查是否是UTF-16 BE BOM
            if (bytesRead >= 2 && buffer[0] == (byte)0xFE &&
                    buffer[1] == (byte)0xFF) {
                return "UTF-16BE";
            }

            // 检查是否是UTF-16 LE BOM
            if (bytesRead >= 2 && buffer[0] == (byte)0xFF &&
                    buffer[1] == (byte)0xFE) {
                return "UTF-16LE";
            }

            // 简单的UTF-8检测
            boolean isUtf8 = true;
            for (int i = 0; i < bytesRead; i++) {
                byte b = buffer[i];
                if ((b & 0x80) != 0) {
                    // 多字节序列
                    int expectedBytes = 0;
                    if ((b & 0xE0) == 0xC0) expectedBytes = 1;
                    else if ((b & 0xF0) == 0xE0) expectedBytes = 2;
                    else if ((b & 0xF8) == 0xF0) expectedBytes = 3;
                    else {
                        isUtf8 = false;
                        break;
                    }

                    for (int j = 1; j <= expectedBytes && i + j < bytesRead; j++) {
                        if ((buffer[i + j] & 0xC0) != 0x80) {
                            isUtf8 = false;
                            break;
                        }
                    }
                    if (!isUtf8) break;
                    i += expectedBytes;
                }
            }

            return isUtf8 ? "UTF-8" : System.getProperty("file.encoding");
        }
    }
}

