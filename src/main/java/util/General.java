package util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class General {

    public enum OSs {WINDOWS, OTHER}

    public static final Random random = new Random(System.nanoTime());

    public static OSs OS;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }

    public static String toString(byte[] bytes) {
        return new String(bytes);
    }

    public static String getExtension(String fileName) {
        char ch;
        int len;
        if (fileName == null ||
                (len = fileName.length()) == 0 ||
                (ch = fileName.charAt(len - 1)) == '/' || ch == '\\' || //in the case of aLong directory
                ch == '.') //in the case of . or ..
            return "";
        int dotInd = fileName.lastIndexOf('.'),
                sepInd = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (dotInd <= sepInd)
            return "";
        else
            return fileName.substring(dotInd + 1).toLowerCase();
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    public static String formatDate(long v) {
        return dateFormat.format(new Date(v));
    }

    public static String makeOSfriendly(String path) {
        switch (OS) {
            case OTHER:
                return path.replaceAll("\\\\", "/");
            case WINDOWS:
                return path.replaceAll("/", "\\\\");
        }
        return path;
    }

    public static String getFileName(String filePath) {
        if (filePath == null || filePath.length() == 0)
            return "";
        filePath = filePath.replaceAll("[/\\\\]+", "/");
        int len = filePath.length(),
                upCount = 0;
        while (len > 0) {
            //remove trailing separator
            if (filePath.charAt(len - 1) == '/') {
                len--;
                if (len == 0)
                    return "";
            }
            int lastInd = filePath.lastIndexOf('/', len - 1);
            String fileName = filePath.substring(lastInd + 1, len);
            switch (fileName) {
                case ".":
                    len--;
                    break;
                case "..":
                    len -= 2;
                    upCount++;
                    break;
                default:
                    if (upCount == 0)
                        return fileName;
                    upCount--;
                    len -= fileName.length();
                    break;
            }
        }
        return "";
    }

    public static String strRela(String absolute, String relative) {
        if (!absolute.substring(absolute.length() - 1).equals(File.pathSeparator)) absolute += File.pathSeparator;
        if (absolute.length() > relative.length()) {
            return absolute.substring(relative.length());
        } else {
            return absolute;
        }
    }

}
