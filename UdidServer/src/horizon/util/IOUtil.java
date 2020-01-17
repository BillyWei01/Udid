package horizon.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[4096];
        int count;
        try {
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
        }finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    public static String streamToString(InputStream in) throws IOException {
        int len = Math.max(in.available(), 1024);
        ByteArrayOutputStream out = new ByteArrayOutputStream(len);
        copy(in, out);
        return new String(out.toByteArray());
    }
}