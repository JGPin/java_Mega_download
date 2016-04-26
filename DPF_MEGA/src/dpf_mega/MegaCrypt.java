package dpf_mega;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MegaCrypt {
    private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] IA = new int[256];
    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++)
            IA[CA[i]] = i;
        IA['='] = 0;
    }

    public static byte[] aes_cbc_decrypt(byte[] data, byte[] key) {
        String iv = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
        byte[] output = null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            output = cipher.doFinal(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static byte[] aInt_to_aByte(int... intKey) {
        byte[] buffer = new byte[intKey.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        for (int i = 0; i < intKey.length; i++) {
            bb.putInt(intKey[i]);
        }
        return bb.array();
    }

    public static int[] aByte_to_aInt(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int[] res = new int[bytes.length / 4];
        for (int i = 0; i < res.length; i++) {
            res[i] = bb.getInt(i * 4);
        }
        return res;
    }

    public final static byte[] base64_url_decode_byte(String str){
        str += "==".substring((2 - str.length() * 3) & 3);
        str = str.replace("-", "+").replace("_", "/").replace(",", "");
    
        int sLen = str != null ? str.length() : 0;
        if (sLen == 0)
        return new byte[0];

        int sepCnt = 0;
        for (int i = 0; i < sLen; i++)
            if (IA[str.charAt(i)] < 0)
                sepCnt++;

        if ((sLen - sepCnt) % 4 != 0)
            return null;

        int pad = 0;
        for (int i = sLen; i > 1 && IA[str.charAt(--i)] <= 0;)
            if (str.charAt(i) == '=')
                pad++;

        int len = ((sLen - sepCnt) * 6 >> 3) - pad;

        byte[] dArr = new byte[len];

        for (int s = 0, d = 0; d < len;) {
            int i = 0;
            for (int j = 0; j < 4; j++) {
                int c = IA[str.charAt(s++)];
                if (c >= 0)
                i |= c << (18 - j * 6);
                else
                j--;
            }

            dArr[d++] = (byte) (i >> 16);
            if (d < len) {
                dArr[d++]= (byte) (i >> 8);
                if (d < len)
                    dArr[d++] = (byte) i;
            }
        }
        return dArr;
    }
}