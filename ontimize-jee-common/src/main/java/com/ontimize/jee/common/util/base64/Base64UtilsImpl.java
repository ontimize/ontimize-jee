package com.ontimize.jee.common.util.base64;

public class Base64UtilsImpl implements IBase64Utils {

    private IBase64Decoder decoder;

    private IBase64Encoder encoder;

    @Override
    public IBase64Decoder getDecoder() {
        if (this.decoder == null) {
            this.decoder = new Base64UtilsDecoder();
        }

        return this.decoder;
    }

    @Override
    public IBase64Encoder getEncoder() {

        if (this.encoder == null) {
            this.encoder = new Base64UtilsEncoder();
        }

        return this.encoder;
    }

}
