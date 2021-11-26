package horizon.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class IOUtil {
    public static boolean makeFileIfNotExist(File file) throws IOException {
        if (file.isFile()) {
            return true;
        } else {
            File parent = file.getParentFile();
            return parent != null && (parent.isDirectory() || parent.mkdirs()) && file.createNewFile();
        }
    }

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
        } finally {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    public static String streamToString(InputStream in) throws IOException {
        int len = Math.max(in.available(), 1024);
        ByteArrayOutputStream out = new ByteArrayOutputStream(len);
        copy(in, out);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public static byte[] getBytes(File file) throws IOException {
        if (!file.isFile()) {
            return null;
        }
        long len = file.length();
        if ((len >> 32) != 0) {
            throw new IllegalArgumentException("file too large");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) len);
        copy(new FileInputStream(file), out);
        return out.toByteArray();
    }

}