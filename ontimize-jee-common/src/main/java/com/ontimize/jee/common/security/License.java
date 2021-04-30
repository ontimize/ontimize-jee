package com.ontimize.jee.common.security;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class License implements Serializable {

    byte[] contents = null;

    byte[] signature = null;

    public License(byte[] c, byte[] sig) {
        this.contents = c;
        this.signature = sig;
    }

    public String getContentsStr() throws UnsupportedEncodingException {
        return new String(this.contents, "UTF-8");
    }

    public byte[] getContents() {
        return this.contents;
    }

    public byte[] getSignature() {
        return this.signature;
    }

}
