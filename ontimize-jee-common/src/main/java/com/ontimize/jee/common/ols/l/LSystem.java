package com.ontimize.jee.common.ols.l;

import com.ontimize.jee.common.security.License;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.text.SimpleDateFormat;

public interface LSystem {

    // Codigos
    public static final int OK = 0;

    public static final int ERROR = -1;

    public static final int ERROR_SIGN = -2;

    public static final int ERROR_PRODUCT_CODE = -3;

    public static final int ERROR_IP_SERVER_DETECTION = -4;

    public static final int ERROR_IP_SERVER = -5;

    public static final int ERROR_MAC_DETECTION = -6;

    public static final int ERROR_MAC_INVALID = -7;

    public static final int ERROR_NO_LICENSE = -9;

    public static final int ERROR_NO_PK = -10;

    public static final int ERROR_PRODUCT_CODE_INCONSISTENT = -11;

    public static final int ERROR_NO_PC = -12;

    public static final int ERROR_NO_PC_At_L = -13;

    public static final int ERROR_NO_SERIAL_N = -14;

    public static final int ERROR_LOCAL_DETECTION = -16;

    public static final int ERROR_LOCAL_DETECTION_At_L = -17;

    public static final int ERROR_UNDEFINED = -18;

    public static final int ERROR_TOO_MANY_USERS = -19;

    public static final int ERROR_IP_CLIENT = -20;

    public static final int ERROR_NO_SUCH_FUNCTIONALITY = -21;

    public static final int ERROR_FUNCTIONALITY_CONVERSION = -22;

    public static final int ERROR_IP_CLIENT_ONLY_SAME_AS_SERVER = -23;

    public static final int ERROR_LOCAL = -24;

    public static final int ERROR_HOSTNAME_NOT_VALID = -25;

    public static final int ERROR_NO_VERSION_VERIFICATION_TYPE = -26;

    public static final int ERROR_NO_VERSION_VERIFICATION_VERIFIER = -27;

    public static final int ERROR_ONTIMIZE_VERSION_LICENSE_ERROR = -28;

    public static final int ERROR_NO_LICENSE_TYPE = -29;

    public static final int ERROR_LV = -30;

    public static final int ERROR_VI_NOT_INIT = -31;

    // Licenses basic fields
    public static final String PUBLIC_KEY = "PublicKey";

    public static final String NOT_LOCAL = "NoLocal";

    public static final String PRIVATE_KEY = "PrivateKey";

    public static final String IPSERVER = "IPServer";

    public static final String IPCLIENT = "IPClient";

    public static final String MAC = "MAC";

    public static final String PRODUCT_CODE = "ProductCode";

    public static final String SERIAL = "Serial";

    public static final String FUNCTIONALITY = "Functionality";

    public static final String LEGALADVICE = "LegalAdvice";

    public static final String ONTIMIZEVERSION = "OntimizeVersion";

    public static final String ONTIMIZEVERSIONVERIFICATIONTYPE = "OntimizeVersionVerificationType";

    public static final String ONTIMIZEVERSIONVERIFIER = "OntimizeVersionVerifier";

    public static final String TYPE = "Type";

    public static final String UOLI = "UniversalIdentificator";

    public static final String L_TITLE = "Title";

    public static final String L_MESSAGE = "Message";

    public static final String WEBGENERATION = "Web";

    // Campos referentes al algoritmo de la licencia
    public static final String SIGNPROVIDER = "Provider";

    public static final String SIGNALGORITHM = "Algorithm";

    public static final String ERROR_UNDEFINED_MESSAGE = "LSystem.ERROR_UNDEFINED";

    public static final String ERROR_PRODUCT_CODE_MESSAGE = "LSystem.NO_PRODUCT_CODE";

    public static final String ERROR_TO_MANY_USERS_MESSAGE = "LSystem.ERROR_TO_MANY_USERS";

    public static final String ERROR_IP_CLIENT_MESSAGE = "LSystem.ERROR_IP_CLIENT_MESSAGE";

    public static final String LICENSE_ROOT_OK_MESSAGE = "LSystem.OK";

    public static final String ERROR_MESSAGE = "LSystem.ERROR";

    public static final String ERROR_SIGN_MESSAGE = "LSystem.ERROR_SIGN";

    public static final String ERROR_IP_SERVER_MESSAGE = "LSystem.ERROR_IP_SERVER";

    public static final String ERROR_IP_SERVER_DETECTION_MESSAGE = "LSystem.ERROR_IP_SERVER_DETECTION";

    public static final String ERROR_MAC_DETECTION_MESSAGE = "LSystem.ERROR_MAC_DETECTION";

    public static final String ERROR_MAC_INVALID_MESSAGE = "LSystem.ERROR_MAC_INVALID";

    public static final String ERROR_NO_LICENSE_MESSAGE = "LSystem.ERROR_NO_LICENSE";

    public static final String ERROR_NO_PK_MESSAGE = "LSystem.ERROR_NO_PK";

    public static final String ERROR_PRODUCT_CODE_INCONSISTENT_MESSAGE = "LSystem.LRootPCInconsitent";

    public static final String ERROR_NO_PC_MESSAGE = "LSystem.ERROR_NO_PC";

    public static final String ERROR_NO_PC_At_L_MESSAGE = "LSystem.ERROR_NO_PCAtL";

    public static final String ERROR_NO_SERIAL_N_MESSAGE = "LSystem.ERROR_NO_SERIAL_N";

    public static final String ERROR_LOCAL_DETECTION_MESSAGE = "LSystem.ERROR_LOCAL_DETECTION";

    public static final String ERROR_LOCAL_DETECTION_At_L_MESSAGE = "LSystem.ERROR_LOCAL_DETECTION_At_L";

    public static final String ERROR_NO_SUCH_FUNCTIONALITY_MESSAGE = "LSystem.ERROR_NO_FUNCTIONALITY";

    public static final String ERROR_FUNCTIONALITY_CONVERSION_MESSAGE = "LSystem.ERROR_FUNCTIONALITY_CONVERSION";

    public static final String ERROR_IP_CLIENT_ONLY_SAME_AS_SERVER_MESSAGE = "LSystem.ERROR_IP_CLIENT_ONLY_SAME_AS_SERVER";

    public static final String ERROR_LOCAL_MESSAGE = "LSystem.ERROR_LOCAL";

    public static final String ERROR_HOSTNAME_NOT_VALID_MESSAGE = "LSystem.ERROR_HOSTNAME_NOT_VALID_MESSAGE";

    public static final String ERROR_LICENSE_TYPE_UNKNOWN_MESSAGE = "LSystem.ERROR_LICENSE_TYPE_UNKNOWN";

    public static final String ERROR_NO_VERSIONVERIFICATIONTYPE_MESSAGE = "LSystem.ERROR_NO_VERSIONVERIFICATIONTYPE";

    public static final String ERROR_NO_VERSIONVERIFICATIONVERIFER_MESSAGE = "LSystem.ERROR_NO_VERSIONVERIFICATIONVERIFER";

    public static final String ERROR_ONTIMIZE_VERSION_LICENSE_MESSAGE = "LSystem.ERROR_ONTIMIZE_VERSION_LICENSE";

    public static final String ERROR_NO_LICENSE_TYPE_MESSAGE = "LSystem.ERROR_NO_LICENSE_TYPE";

    public static final String ERROR_LV_MESSAGE = "LSystem.ERROR_LV";

    public static final String ERROR_VI_NOT_INIT_MESSAGE = "LSystem.ERROR_VI_NOT_INIT";

    public SimpleDateFormat getDateFormat();

    public String getMessageByCode(int i);

    public byte[] sign(byte[] contents, PrivateKey pk, String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, InvalidKeyException;

    public boolean checkLSign(License l, PublicKey pk);

    public String searchFieldValue(String field, String licenseContents, String separator, String fieldValueSeparator);

}
