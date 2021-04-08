/*
 *
 */
package com.ontimize.jee.desktopclient.components.servermanagement.window.list;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MainApplication;

/**
 * Copiado de Sentence de utilmize
 */

/**
 * The Class Sentence.
 */
public class SentenceSql {

    private static final Logger logger = LoggerFactory.getLogger(SentenceSql.class);

    /** The next code. */
    protected static int nextCode = -1;

    /** The code. */
    protected int code;

    /** The sql sentence. */
    protected String sqlSentence;

    /**
     * Instantiates a new sentence.
     * @param sentence the sentence
     * @param user the user
     */
    public SentenceSql(String sentence, String user) {
        super();
        this.code = SentenceSql.getNextCode(user);
        this.sqlSentence = sentence;
    }

    /**
     * Instantiates a new sentence.
     * @param sentence the sentence
     * @param codeIncluded the code included
     * @param user the user
     */
    public SentenceSql(String sentence, boolean codeIncluded, String user) {
        super();
        if (codeIncluded) {
            this.code = Integer.parseInt(sentence.substring(0, sentence.indexOf("@")));
        } else {
            this.code = SentenceSql.getNextCode(user);
        }
        this.sqlSentence = sentence.substring(sentence.indexOf("@") + 1);
    }

    /**
     * Reset code.
     */
    public static void resetCode() {
        SentenceSql.nextCode = 0;
    }

    /**
     * Gets the sQL sentence.
     * @return the sQL sentence
     */
    public String getSQLSentence() {
        return this.sqlSentence;
    }

    /**
     * Sets the sql sentence.
     * @param sqlSentence the new sql sentence
     */
    public void setSqlSentence(String sqlSentence) {
        this.sqlSentence = sqlSentence;
    }

    /**
     * Gets the code.
     * @return the code
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Gets the next code.
     * @param user the user
     * @return the next code
     */
    public static int getNextCode(String user) {
        try {
            if (SentenceSql.nextCode == -1) {
                try {
                    MainApplication application = (MainApplication) ApplicationManager.getApplication();
                    String preference = application.getPreferences().getPreference(user, "SQLSentenceCode");
                    if (preference != null) {
                        SentenceSql.nextCode = Integer.parseInt(preference);
                    } else {
                        SentenceSql.nextCode = 0;
                    }
                } catch (Exception e) {
                    SentenceSql.logger.trace(null, e);
                    SentenceSql.nextCode = 0;
                }
            }
            SentenceSql.nextCode++;
            return SentenceSql.nextCode;
        } finally {
            try {
                MainApplication application = (MainApplication) ApplicationManager.getApplication();
                application.getPreferences()
                    .setPreference(user, "SQLSentenceCode", String.valueOf(SentenceSql.nextCode));
                application.getPreferences().savePreferences();
            } catch (Exception e) {
                SentenceSql.logger.trace(null, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.code + "@" + this.sqlSentence;
    }

}
