package moe.caa.multilogin.api.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

public class ValueUtil {
    /**
     * UUID 转 bytes
     *
     * @param uuid 需要转换的 uuid
     * @return 转换后的 bytes
     */
    public static byte[] uuidToBytes(UUID uuid) {
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return uuidBytes;
    }

    /**
     * bytes 转 UUID
     *
     * @param bytes 需要转换的 bytes
     * @return 转换后的 UUID
     */
    public static UUID bytesToUuid(byte[] bytes) {
        if (bytes.length != 16) return null;
        int i = 0;
        long msl = 0;
        for (; i < 8; i++) {
            msl = (msl << 8) | (bytes[i] & 0xFF);
        }
        long lsl = 0;
        for (; i < 16; i++) {
            lsl = (lsl << 8) | (bytes[i] & 0xFF);
        }
        return new UUID(msl, lsl);
    }

    /**
     * 通过字符串生成 UUID
     *
     * @param uuid 字符串
     * @return 匹配的 uuid， 否则为空
     */
    public static UUID getUuidOrNull(String uuid) {
        UUID ret = null;
        try {
            ret = UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        } catch (Exception ignored) {
        }
        return ret;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String transPapi(String s, Pair<?, ?>... pairs) {
        for (int i = 0; i < pairs.length; i++) {
            s = s.replace("{" + pairs[i].getValue1() + "}", pairs[i].getValue2().toString());
            s = s.replace("{" + i + "}", pairs[i].getValue2().toString());
        }
        return s;
    }

    public static String transPapi(String s, List<Pair<?, ?>> pairs) {
        for (int i = 0; i < pairs.size(); i++) {
            s = s.replace("{" + pairs.get(i).getValue1() + "}", pairs.get(i).getValue2().toString());
            s = s.replace("{" + i + "}", pairs.get(i).getValue2().toString());
        }
        return s;
    }
}