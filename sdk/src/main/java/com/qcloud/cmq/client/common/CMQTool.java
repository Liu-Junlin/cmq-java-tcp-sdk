package com.qcloud.cmq.client.common;

import com.qcloud.cmq.client.exception.MQServerException;
import com.qcloud.cmq.client.http.Json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CMQTool {
    private static final Logger log = LoggerFactory.getLogger(CMQTool.class);
    private static char[] b64c = new char[]{'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/'};

    private static final String CONTENT_CHARSET = "UTF-8";

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static String base64_encode(byte[] data) {
        StringBuffer sb = new StringBuffer();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;
        while (i < len) {
            b1 = data[i++] & 0xff;
            if (i == len) {
                sb.append(b64c[b1 >>> 2]);
                sb.append(b64c[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            if (i == len) {
                sb.append(b64c[b1 >>> 2]);
                sb.append(b64c[((b1 & 0x03) << 4)
                        | ((b2 & 0xf0) >>> 4)]);
                sb.append(b64c[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }
            b3 = data[i++] & 0xff;
            sb.append(b64c[b1 >>> 2]);
            sb.append(b64c[((b1 & 0x03) << 4)
                    | ((b2 & 0xf0) >>> 4)]);
            sb.append(b64c[((b2 & 0x0f) << 2)
                    | ((b3 & 0xc0) >>> 6)]);
            sb.append(b64c[b3 & 0x3f]);
        }
        return sb.toString();
    }

    public static String sign(String src, String key, String method)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        Mac mac;
        if ("sha1".equals(method)) {
            mac = Mac.getInstance(HMAC_ALGORITHM);
        } else {
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        }
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CONTENT_CHARSET), mac.getAlgorithm());
        mac.init(secretKey);
        byte[] digest = mac.doFinal(src.getBytes(CONTENT_CHARSET));
        return base64_encode(digest);
    }


    public static void checkResult(String result) {
        if (result == null || "".equals(result.trim())) {
            log.error("result is empty");
            throw new MQServerException(0, "result is empty");
        }
        checkResult(new JSONObject(result));
    }

    public static void checkResult(JSONObject jsonObj) {
        if (jsonObj.isNull("code")) {
            log.error("can't find field code in result:" + jsonObj.toString());
            throw new MQServerException(0, "can't find field code in result:" + jsonObj.toString());
        }
        int code = jsonObj.getInt("code");
        if (code != 0) {
            log.error("error response:" + jsonObj.toString());
            throw new MQServerException(code, jsonObj.getString("message"));
        }
    }

}
