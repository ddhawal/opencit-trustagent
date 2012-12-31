package com.intel.mtwilson.datatypes;

import com.intel.mtwilson.validation.ObjectModel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.annotate.JsonValue;

/**
 * Representation of a single MD5 Digest. An MD5 Digest is a 16-byte value.
 * 
 * @since 0.5.4
 * @author jbuhacoff
 */
public class Md5Digest extends AbstractMessageDigest {
    public Md5Digest() {
        super("MD5", 16);
    }
    
    public Md5Digest(byte[] value) {
        this();
        setBytes(value);
    }
    
    public Md5Digest(String hex) {
        this();
        setHex(hex);
    }
    
    public static Md5Digest valueOf(byte[] message) {
        return valueOf(Md5Digest.class, message);
//        MessageDigest hash = MessageDigest.getInstance("MD5");
//        byte[] digest = hash.digest(message);
//        return new Md5Digest(digest);
    }
}
