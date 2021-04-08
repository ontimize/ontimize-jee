package com.ontimize.jee.desktopclient.components.messaging;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.tools.StringTools;

/**
 * The Class UToastNoblockingPanel.
 */
public class ToastNoblockingPanel extends AbstractToastPanel {

    private static final Logger logger = LoggerFactory.getLogger(ToastNoblockingPanel.class);

    /**
     * The percent of screen allowed as maximum message size. Else te content in under a ScrollPane, the
     * will be avialable anywhere.
     */
    public static double MAXIMUM_WIDTH_PERCENT = 0.9;

    /**
     * The percent of screen allowed as maximum message size. Else te content in under a ScrollPane, the
     * will be avialable anywhere.
     */
    public static double MAXIMUM_HEIGHT_PERCENT = 0.8;

    /**
     * The percent of screen destinated to title (the descripton will be the unit minus this) when both
     * messages are shown.
     */
    public static double TITLE_VERSUS_DESCTIPTION_PERCENT = 0.2;

    /** The msg label. */
    protected JTextPane msgLabel;

    /** The dsc label. */
    protected JTextPane dscLabel;

    /** The ico label. */
    protected JLabel icoLabel;

    /** The message scroll panel */
    protected JScrollPane scrollMsg;

    /** The desc scroll panel */
    protected JScrollPane scrollDsc;

    protected boolean bothMessages;

    /**
     * Instantiates a new u toast noblocking panel.
     */
    public ToastNoblockingPanel() {
        super();
        this.init();
    }

    /**
     * Inits the.
     */
    protected void init() {
        this.msgLabel = new JTextPane();
        this.dscLabel = new JTextPane();
        this.icoLabel = new JLabel();

        this.msgLabel.setEnabled(false);

        this.msgLabel.setForeground(new Color(0xffffff));
        this.dscLabel.setForeground(new Color(0xffffff));
        this.msgLabel.setDisabledTextColor(new Color(0xffffff));
        this.dscLabel.setDisabledTextColor(new Color(0xffffff));
        this.msgLabel.setBackground(new Color(0, 0, 0, 0));
        this.dscLabel.setBackground(new Color(0, 0, 0, 0));
        this.msgLabel.setBorder(BorderFactory.createEmptyBorder());
        this.dscLabel.setBorder(BorderFactory.createEmptyBorder());
        this.msgLabel.setOpaque(false);
        this.dscLabel.setOpaque(false);
        this.msgLabel.setEditable(false);
        this.dscLabel.setEditable(false);

        this.scrollMsg = new AdaptableScrollPane(this.msgLabel, ToastNoblockingPanel.TITLE_VERSUS_DESCTIPTION_PERCENT);
        this.scrollDsc = new AdaptableScrollPane(this.dscLabel,
                1 - ToastNoblockingPanel.TITLE_VERSUS_DESCTIPTION_PERCENT);

        this.setLayout(new GridBagLayout());
        this.add(this.icoLabel, new GridBagConstraints(0, 0, 1, 2, 0, 1, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 3, 3, 10), 0, 0));
        this.add(this.scrollMsg, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(3, 0, 2, 3), 0, 0));
        this.add(this.scrollDsc, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(2, 0, 3, 0), 0, 0));
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(new LineBorder(Color.black, 2, true),
                        new LineBorder(Color.white, 2, true)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    }

    /**
     * Configure UI according to message,
     */
    @Override
    public void setMessage(final ToastMessage msg) {
        this.setMessage(msg, true);
    }

    /**
     * Configure UI according to message, allowing to configure where to use or not scrolls.
     * @param msg
     * @param useScroll
     */
    protected void setMessage(ToastMessage msg, boolean useScroll) {
        // First step, configure layout: to use or not scroll and determine if both scrolls
        this.setMessageLayout(msg, useScroll);

        // Set icon
        this.setMessageIcon(msg);

        // Set message label
        this.setMessageLabel(msg);

        // Set description label
        this.setMessageDesc(msg, useScroll);
    }

    /**
     * Configure UI layout, according message
     * @param msg
     * @param useScroll
     */
    protected void setMessageLayout(ToastMessage msg, boolean useScroll) {
        this.bothMessages = !StringTools.isEmpty(msg.getTranslatedMessage(null))
                && !StringTools.isEmpty(msg.getTranslatedDescription(null));
        if (useScroll) {
            this.remove(this.msgLabel);
            this.remove(this.dscLabel);
            this.scrollMsg.setViewportView(this.msgLabel);
            this.scrollDsc.setViewportView(this.dscLabel);
            this.add(this.scrollMsg, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(3, 0, 2, 3), 0, 0));
            this.add(this.scrollDsc, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(2, 0, 3, 0), 0, 0));
        } else {
            this.remove(this.scrollMsg);
            this.remove(this.scrollDsc);
            this.add(this.msgLabel, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(3, 0, 2, 3), 0, 0));
            this.add(this.dscLabel, new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH, new Insets(2, 0, 3, 0), 0, 0));
        }
    }

    /**
     * Configure UI icon, according message
     * @param msg
     */
    protected void setMessageIcon(ToastMessage msg) {
        this.icoLabel.setIcon(msg.getIcon());
    }

    /**
     * Configure UI title/message, according message
     * @param msg
     */
    protected void setMessageLabel(ToastMessage msg) {
        String msgText = msg.getTranslatedMessage(null);
        this.msgLabel
            .setContentType((msgText != null) && msgText.toLowerCase().startsWith("<html>") ? "text/html" : "text");
        if ((msgText == null) || "text/html".equals(this.msgLabel.getContentType())) {
            this.msgLabel.setText(msgText);
        } else {
            String splittedText = this.considerToSplitTextLongerThanScreen(msgText, this.msgLabel.getFont(),
                    this.msgLabel.getContentType());
            this.msgLabel.setText(splittedText);
        }
    }

    /**
     * Configure UI description, according message
     * @param msg
     * @param useScroll
     */
    protected void setMessageDesc(ToastMessage msg, boolean useScroll) {
        String descText = msg.getTranslatedDescription(null);
        this.dscLabel
            .setContentType((descText != null) && descText.toLowerCase().startsWith("<html>") ? "text/html" : "text");
        if ((descText == null) || "text/html".equals(this.dscLabel.getContentType())) {
            this.dscLabel.setText(descText);
        } else {
            String splittedText = this.considerToSplitTextLongerThanScreen(descText, this.dscLabel.getFont(),
                    this.dscLabel.getContentType());
            this.dscLabel.setText(splittedText);
        }
        Component dscRoot = useScroll ? this.scrollDsc : this.dscLabel;
        dscRoot.setVisible(!StringTools.isEmpty(this.dscLabel.getText()));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.utilmize.client.gui.toast.AbstractToastPanel#getResponse(com.utilmize.client.gui.toast.
     * UToastMessage)
     */
    @Override
    public Object getResponse(ToastMessage message) {
        return null;
    }

    /**
     * Calcule valid size.
     * @param text the text
     * @param minimunSize the minimun size
     * @param contentType the content type
     * @param font the font
     * @return the dimension
     */
    private static Dimension calculeValidSize(String text, Dimension minimunSize, String contentType, Font font) {
        if ((text == null) || "".equals(text)) {
            return minimunSize;
        }
        try {
            if ("text/html".equals(contentType)) {
                return ToastNoblockingPanel.calculeValidSizeHTMLText(text, minimunSize, font);
            } else {
                return ToastNoblockingPanel.calculeValidSizePlainText(text, minimunSize, font);
            }
        } catch (Exception e) {
            ToastNoblockingPanel.logger.trace(null, e);
            return minimunSize;
        }
    }

    /**
     * Calcule valid size plain text.
     * @param text the text
     * @param minimunSize the minimun size
     * @param font the font
     * @return the dimension
     */
    private static Dimension calculeValidSizePlainText(String text, Dimension minimunSize, Font font) {
        List<String> lines = ToastNoblockingPanel.calculeTextLines(text, ToastNoblockingPanel.getLineSeparators(null));
        int maxWidth = minimunSize.width;
        for (String s : lines) {
            maxWidth = Math.max(maxWidth, ToastNoblockingPanel.calculeLineWidth(s, font));
        }

        // Nota: no se que le pasa al layout, pero a veces se le va la calculando tamanos, asi que metemos
        // algo de margen
        return new Dimension(maxWidth, (lines.size() + 1) * (font.getSize() + 3));
    }

    private static List<String> calculeTextLines(String text, String contentType) {
        if ((text == null) || "".equals(text)) {
            return Arrays.asList(new String[] { text });
        }
        try {
            if ("text/html".equals(contentType)) {
                return ToastNoblockingPanel.calculeTextLines(text, ToastNoblockingPanel.getLineSeparators(contentType));
            } else {
                return ToastNoblockingPanel.calculeTextLines(text, ToastNoblockingPanel.getLineSeparators(contentType));
            }
        } catch (Exception e) {
            ToastNoblockingPanel.logger.trace(null, e);
            return Arrays.asList(new String[] { text });
        }
    }

    private static List<String> calculeTextLines(String text, List<String> separators) {
        List<String> lines = new ArrayList<>(Arrays.asList(text));

        boolean hasSeparators = false;
        while (hasSeparators = ToastNoblockingPanel.hasSeparators(lines, separators)) {
            lines = ToastNoblockingPanel.splitMultiple(lines, separators);
        }

        return lines;
    }

    private static boolean hasSeparators(List<String> lines, List<String> separators) {
        for (String line : lines) {
            for (String sep : separators) {
                if (line.split(sep).length > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<String> splitMultiple(List<String> lines, List<String> separators) {
        for (String line : lines) {
            for (String sep : separators) {
                List<String> semiRes = ToastNoblockingPanel.calculeTextLinesSingle(line, sep);
                if (semiRes.size() > 1) {
                    // split this one and restart
                    ArrayList<String> copy = new ArrayList<>(lines);
                    int idx = copy.indexOf(line);
                    copy.remove(line);
                    copy.addAll(idx, semiRes);
                    return copy;
                }
            }
        }
        return lines;
    }

    /**
     * Calcule plain text lines with only one separator.
     * @param text the text
     * @param separator the separator
     * @return the list
     */
    private static List<String> calculeTextLinesSingle(String text, String separator) {
        return new ArrayList<>(Arrays.asList(text.split(separator)));
    }

    /**
     * Calcule valid size html text.
     * @param text the text
     * @param minimunSize the minimun size
     * @param font the font
     * @return the dimension
     */
    private static Dimension calculeValidSizeHTMLText(String text, Dimension minimunSize, Font font) {
        String translatedText = ToastNoblockingPanel.convertHTMLToPlain(text);
        return ToastNoblockingPanel.calculeValidSizePlainText(translatedText, minimunSize, font);
    }

    /**
     * Convert html to plain.
     * @param text the text
     * @return the string
     */
    private static String convertHTMLToPlain(String text) {
        // TODO usar el html document mejor
        String s = text;

        // Drop line breaks out of <body></body>
        int initBodyTagStart = Math.max(0, s.indexOf("<body>"));
        int endBodyTag = Math.min(s.length(), s.indexOf("</body>"));
        s = s.substring(0, initBodyTagStart).replaceAll("\r", "").replaceAll("\n", "").replaceAll(" ", "")
                + s.substring(initBodyTagStart, endBodyTag + "</body>".length())
                    .replaceAll("\r", "")
                    .replaceAll("\n", "")
                    .replaceAll("  ", " ")
                    .replaceAll("  ", " ")
                + s.substring(endBodyTag + "</body>".length())
                    .replaceAll("\r", "")
                    .replaceAll("\n", "")
                    .replaceAll(" ", "");

        // Drop basic tags
        s = s.replaceAll("<html>", "")
            .replaceAll("</html>", "")
            .replaceAll("<body>", "")
            .replaceAll("</body>", "")
            .replaceAll("<head>", "")
            .replaceAll("</head>", "")
            .replaceAll("<b>", "")
            .replaceAll("</b>", "")
            .replaceAll("<i>", "")
            .replaceAll("</i>", "")
            .replaceAll("<u>", "")
            .replaceAll("</u>", "");

        // Consider spcial tags that affect in rendering new lines
        s = s.replaceAll("<br>", ToastNoblockingPanel.getDefaultLineSeparator(null))
            .replaceAll("<ol><li>", ToastNoblockingPanel.getDefaultLineSeparator(null))
            .replaceAll("<ul><li>", ToastNoblockingPanel.getDefaultLineSeparator(null))
            .replaceAll("<li>", ToastNoblockingPanel.getDefaultLineSeparator(null))
            .replaceAll("</ol>", "")
            .replaceAll("<ul>", "")
            .replaceAll("</ul>", "")
            .replaceAll("</li>", "")
            .replaceAll("<ol>", "")
            .replaceAll("</ol>", "")
            .replaceAll("<ul>", "")
            .replaceAll("</ul>", "")
            .replaceAll("<center>", ToastNoblockingPanel.getDefaultLineSeparator(null))
            .replaceAll("</center>", "");

        // Remove other unccomon labels
        while ((s.indexOf("<") >= 0) && (s.indexOf(">") >= 0)) {
            int initTagStart = s.indexOf("<");
            int initTagEnd = s.indexOf(">");

            String label = s.substring(initTagStart + 1, initTagEnd);
            label = label.contains(" ") ? label.substring(0, label.indexOf(" ")) : label;

            int endTagStart = s.indexOf("</" + label + ">");
            int endTagEnd = endTagStart + ("</" + label + ">").length();
            if (endTagStart < 0) {
                endTagStart = endTagEnd = s.length();
            }
            s = s.substring(0, initTagStart) + s.substring(initTagEnd + 1, endTagStart) + s.substring(endTagEnd);
        }

        return s;
    }

    /**
     * Consider to split text longer than screen.
     * @param msgText the msg text
     * @param font the font
     * @param contentType the content type
     * @return the string
     */
    private String considerToSplitTextLongerThanScreen(String msgText, Font font, String contentType) {
        int maxWidth = this.getMaximumWidth();

        if (StringTools.isEmpty(msgText) || (ToastNoblockingPanel.calculeLineWidth(msgText, font) < maxWidth)) {
            return msgText;
        }

        StringBuilder finalString = new StringBuilder();
        List<String> lines = ToastNoblockingPanel.calculeTextLines(msgText, contentType);
        for (String s : lines) {
            if (ToastNoblockingPanel.calculeLineWidth(s, font) > maxWidth) {
                // This line is too longer than screen, we need to split, How? if Contains white spaces characters
                // try to divide by it, else force division at maximum
                List<String> subLines = this.splitLine(s, font, maxWidth);
                for (String s2 : subLines) {
                    finalString
                        .append(finalString.length() == 0 ? ""
                                : ToastNoblockingPanel.getDefaultLineSeparator(contentType))
                        .append(s2);
                }
            } else {
                finalString
                    .append(finalString.length() == 0 ? "" : ToastNoblockingPanel.getDefaultLineSeparator(contentType))
                    .append(s);
            }
        }

        return finalString.toString();
    }

    /**
     * Calcule line width.
     * @param s the s
     * @param font the font
     * @return the int
     */
    public static int calculeLineWidth(String s, Font font) {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(font).stringWidth(s);
    }

    /**
     * Gets the line separator.
     * @param contentType the content type
     * @return the line separator
     */
    protected static String getDefaultLineSeparator(String contentType) {
        return "text/html".equals(contentType) ? "<br>" : "\r\n";
    }

    /**
     * Gets the line separator.
     * @param contentType the content type
     * @return the line separator
     */
    protected static List<String> getLineSeparators(String contentType) {
        return "text/html".equals(contentType) ? Arrays.asList(new String[] { "<br>", "<li>", "<center>", "<tr>" })
                : Arrays.asList(new String[] { "\r\n", "\n" });
    }

    /**
     * Split line.
     * @param s the s
     * @param font the font
     * @param maxWidth the max width
     * @return the list
     */
    protected List<String> splitLine(String s, Font font, int maxWidth) {
        List<String> newLines = new ArrayList<>();

        if ((s == null) || "".equals(s)) {
            return newLines;
        }

        if (s.contains(" ")) {
            int whiteSpace = s.lastIndexOf(" ");
            String subLine1 = s.substring(0, whiteSpace);
            while ((ToastNoblockingPanel.calculeLineWidth(subLine1, font) > maxWidth) && subLine1.contains(" ")) {
                whiteSpace = subLine1.lastIndexOf(" ");
                subLine1 = s.substring(0, whiteSpace);
            }
            if (ToastNoblockingPanel.calculeLineWidth(subLine1, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine1, font, maxWidth));
            } else {
                newLines.add(subLine1);
            }
            String subLine2 = s.substring(whiteSpace + 1);
            if (ToastNoblockingPanel.calculeLineWidth(subLine2, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine2, font, maxWidth));
            } else {
                newLines.add(subLine2);
            }
        } else if (s.contains("\r\n")) {
            String subLine1 = s.substring(0, s.indexOf("\r\n"));
            if (ToastNoblockingPanel.calculeLineWidth(subLine1, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine1, font, maxWidth));
            } else {
                newLines.add(subLine1);
            }
            String subLine2 = s.substring(s.indexOf("\r\n") + 1);
            if (ToastNoblockingPanel.calculeLineWidth(subLine2, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine2, font, maxWidth));
            } else {
                newLines.add(subLine2);
            }
        } else if (s.contains("\n")) {
            String subLine1 = s.substring(0, s.indexOf("\n"));
            if (ToastNoblockingPanel.calculeLineWidth(subLine1, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine1, font, maxWidth));
            } else {
                newLines.add(subLine1);
            }
            String subLine2 = s.substring(s.indexOf("\n") + 1);
            if (ToastNoblockingPanel.calculeLineWidth(subLine2, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine2, font, maxWidth));
            } else {
                newLines.add(subLine2);
            }
        } else {
            String subLine1 = s.substring(0, s.length() / 2);
            if (ToastNoblockingPanel.calculeLineWidth(subLine1, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine1, font, maxWidth));
            } else {
                newLines.add(subLine1);
            }
            String subLine2 = s.substring(s.length() / 2);
            if (ToastNoblockingPanel.calculeLineWidth(subLine2, font) > maxWidth) {
                newLines.addAll(this.splitLine(subLine2, font, maxWidth));
            } else {
                newLines.add(subLine2);
            }
        }
        return newLines;
    }

    protected int getMaximumWidth() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) (screenSize.width * ToastNoblockingPanel.MAXIMUM_WIDTH_PERCENT);
    }

    protected int getMaximumHeight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return (int) (screenSize.height * ToastNoblockingPanel.MAXIMUM_WIDTH_PERCENT);
    }

    /**
     * Custom JCsollPane that limit dimension, to ensure always enter in the screen
     */
    protected class AdaptableScrollPane extends JScrollPane {

        protected double heightPercent;

        public AdaptableScrollPane(Component comp, double percent) {
            super(comp);
            this.heightPercent = percent;
            this.setOpaque(false);
            this.getViewport().setOpaque(false);
            this.setBorder(BorderFactory.createEmptyBorder());
            this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            if (preferredSize.width > ToastNoblockingPanel.this.getMaximumWidth()) {
                preferredSize.width = ToastNoblockingPanel.this.getMaximumWidth();
            }
            if (preferredSize.height > (ToastNoblockingPanel.this.getMaximumHeight()
                    * (ToastNoblockingPanel.this.bothMessages ? this.heightPercent : 1.0))) {
                preferredSize.height = (int) (ToastNoblockingPanel.this.getMaximumHeight()
                        * (ToastNoblockingPanel.this.bothMessages ? this.heightPercent : 1.0));
            }
            return preferredSize;
        }

    }

}
