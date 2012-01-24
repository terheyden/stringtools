package com.terheyden.stringtools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 */
public class StringTools {

    public static String addIndent(String text, int indentAmt) {

        StringBuilder builder = new StringBuilder(Math.abs(indentAmt));
        for (int i = 0; i < Math.abs(indentAmt); i++) {
            builder.append(" ");
        }

        String indentStr = builder.toString();

        // Break into separate lines.
        String[] lineList = split(text);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {
            if (indentAmt > 0) {
                finishedList.add(indentStr + line);
            } else if (indentAmt < 0) {
                if (line.indexOf(indentStr) == 0) {
                    finishedList.add(line.substring(Math.abs(indentAmt)));
                } else {
                    finishedList.add(line.trim());
                }
            }
        }

        return StringUtils.join(finishedList.toArray(), '\n');
    }


    /**
     * Changes one bullet type to another. E.g. -- my bullet ==> ** my bullet
     * @param text text to search for bullet text in
     * @param srcMarkup source bullet style: "* |** |*** "
     * @param destMarkup target bullet style: "- |  - |    - "
     * @param indentAmt 0=straight conversion, 1=convert and indent 1, -1=convert and outdent 1
     * @return transformed text
     */
    public static String convertBulletMarkup(String text, String srcMarkup, String destMarkup, int indentAmt) {

        // Break into separate lines.
        String[] lineList = split(text);
        // Split bullet styles:
        String[] origBullets = srcMarkup.split("\\|");
        String[] newBullets = destMarkup.split("\\|");

        if (origBullets.length != newBullets.length) {
            throw new IllegalArgumentException("Bullet syntax length is not equal!");
        }

        List<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {

            for (int count = origBullets.length - 1; count >= 0; count--) {

                if (line.startsWith(origBullets[count])) {

                    // They can indent / outdent during conversion.
                    int offset = count + indentAmt;

                    if (offset >= origBullets.length) {
                        offset = origBullets.length - 1;
                    }

                    String newBullet = offset < 0 ? "" : newBullets[offset];

                    line = newBullet + line.substring(origBullets[count].length());
                    break;
                }
            }

            finishedList.add(line);
        }

        return StringUtils.join(finishedList.toArray(), '\n');
    }

    public static String convertBulletMarkup(String text, String srcMarkup, String destMarkup) {
        return convertBulletMarkup(text, srcMarkup, destMarkup, 0);
    }

    public static String[] split(String text) {
        return text.split("[\r\n]");
    }

    /**
     * Insert some text at the beginning / end of each line in a block of text.
     * @param text
     * @param insertText
     * @param whereStr
     * @return
     */
    public static String insertText(String text, String insertText, String whereStr) {

        // Insert either at beginning or end for now.
        boolean eol = whereStr.toLowerCase().equals("end");

        // Break into separate lines.
        String[] lineList = split(text);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {

            if (eol) line += insertText;
            else line = insertText + line;
            finishedList.add(line);
        }

        return StringUtils.join(finishedList.toArray(), '\n');
    }

    /**
     * Do a regex search / replace on a block of text. Replace all.
     * @param text
     * @param findRegex
     * @param replaceText
     * @return
     */
    public static String regexReplaceText(String text, String findRegex, String replaceText) {

        try {

            // Support user entering "\n" etc.
            text = convertUserSpecialInput(text);
            findRegex = convertUserSpecialInput(findRegex);
            replaceText = convertUserSpecialInput(replaceText);

            text = text.replaceAll(findRegex, replaceText);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    /**
     * Works best if find / replace is specified in camelCase.
     */
    public static String smartReplaceText(String text, String findText, String replaceText) {

        try {

            // Support user entering "\n" etc.
            text = convertUserSpecialInput(text);
            findText = convertUserSpecialInput(findText);
            replaceText = convertUserSpecialInput(replaceText);

            String camelFind = findText;
            String camelReplace = replaceText;
            String capFind = capitalize(findText);
            String capReplace = capitalize(replaceText);
            String upperFind = findText.toUpperCase();
            String upperReplace = replaceText.toUpperCase();
            String lowerFind = findText.toLowerCase();
            String lowerReplace = replaceText.toLowerCase();
            String lowerCapFind = capitalize(findText.toLowerCase());
            String lowerCapReplace = capitalize(replaceText.toLowerCase());
            String underscoreFind = smartInsertChar(findText, "_").toLowerCase();
            String underscoreReplace = smartInsertChar(replaceText, "_").toLowerCase();
            String underscoreUCFind = smartInsertChar(findText, "_").toUpperCase();
            String underscoreUCReplace = smartInsertChar(replaceText, "_").toUpperCase();
            String spaceFind = smartInsertChar(findText, " ").toLowerCase();
            String spaceReplace = smartInsertChar(replaceText, " ").toLowerCase();
            String spaceCapFind = smartInsertChar(capitalize(findText), " ");
            String spaceCapReplace = smartInsertChar(capitalize(replaceText), " ");
            String spaceLCapFind = capitalize(smartInsertChar(findText, " ").toLowerCase());
            String spaceLCapReplace = capitalize(smartInsertChar(replaceText, " ").toLowerCase());

            String[] findReplacePairs = {
                camelFind, camelReplace,
                capFind, capReplace,
                upperFind, upperReplace,
                lowerFind, lowerReplace,
                lowerCapFind, lowerCapReplace,
                underscoreFind, underscoreReplace,
                underscoreUCFind, underscoreUCReplace,
                spaceFind, spaceReplace,
                spaceCapFind, spaceCapReplace,
                spaceLCapFind, spaceLCapReplace
            };

            for (int count = 0; count < findReplacePairs.length; count += 2) {
                String find = findReplacePairs[count];
                String replace = findReplacePairs[count + 1];

                text = text.replaceAll(find, replace);
            }

            return text;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    /**
     * Inserts a char / string between camelCase (or TitleCase) words.
     * E.g. smartInsertChar("myUserName", "_") == "my_User_Name"
     */
    private static String smartInsertChar(String text, String insChar) {

        StringBuilder builder = new StringBuilder();

        for (int count = 0; count < text.length(); count++) {

            char c = text.charAt(count);

            if (builder.length() > 0 && Character.isUpperCase(c)) {
                builder.append(insChar);
            }

            builder.append(c);
        }

        return builder.toString();
    }

    /**
     * When the user enters "\n" convert it to a real \n in their text.
     * @param text text entered by the user
     * @return text with special chars converted
     */
    private static String convertUserSpecialInput(String text) {
        text = text.replaceAll("\\\\n", "\n");
        text = text.replaceAll("\\\\r", "\r");
        text = text.replaceAll("\\\\t", "\t");

        return text;
    }

    public static String convertJavadocToMediaWiki(String text) {

        // Matches:
        //   / * *
        //     * Blah blah blah info
        //     * /
        //   public void myMethod(arg1) {
        //
        // 1 = the multi-line javadoc comment
        // 2 = the method declaration
        Pattern javadocPat = Pattern.compile("(/\\*\\*.*?\\*/)\\s*(public\\s+[^{]+)", Pattern.DOTALL | Pattern.MULTILINE);
        Matcher javadocMatcher = javadocPat.matcher(text);
        StringBuilder builder = new StringBuilder();

        while (javadocMatcher.find()) {
            String javadocStr = javadocMatcher.group(1);
            String methodName = javadocMatcher.group(2).trim();

            // Convert the javadoc stuff:

            // Replace javadoc start: / * *
            javadocStr = javadocStr.replace("/**", "");
            // Replace javadoc end: * /
            javadocStr = Pattern.compile("^\\s*\\*/", Pattern.MULTILINE).matcher(javadocStr).replaceAll("");
            // Replace in-between lines, replace * with : (indentend line in MediaWiki)
            javadocStr = Pattern.compile("^\\s*\\*\\s*", Pattern.MULTILINE).matcher(javadocStr).replaceAll(":");
            // Remove <p/> instances:
            javadocStr = javadocStr.replaceAll("<p/>", "");
            // Replace javadoc {@link ClassName#methodName()} to {@link ClassName.methodName()}
            javadocStr = javadocStr.replaceAll("#", ".");
            // Replace javadoc {@keyword <contents>} with just <contents>:
            javadocStr = javadocStr.replaceAll("\\{@\\w+\\s+([^}]+)\\}", "<code>$1</code>");
            // Replace @param param my param description:
            javadocStr = javadocStr.replaceAll("@param\\s+([A-Za-z0-9_]+)", "<code>$1</code> =");
            javadocStr = javadocStr.replaceAll("@return", "returns");
            javadocStr = javadocStr.replaceAll("@exception", "throws exception:");
            javadocStr = javadocStr.replaceAll("@throws", "throws exception:");
            // I sometimes mark vars by putting them in braces: [myVar] so replace those:
            javadocStr = javadocStr.replaceAll("\\[([A-Za-z0-9_]+)\\]", "<code>$1</code>");

            builder.append(String.format("<code>%s</code>\n%s", methodName, javadocStr));
        }

        return builder.toString();
    }

    public static String capitalize(String text) {

        // Break into separate lines.
        String[] lineList = split(text);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {
            finishedList.add(StringUtils.capitalize(line));
        }

        return StringUtils.join(finishedList.toArray(), '\n');
    }

    public static String trimChars(String text, String charsToTrim, String fromBeginningOrEndStr) {

        if (text == null || charsToTrim == null || fromBeginningOrEndStr == null) {
            return text;
        }

        if (text.length() == 0 || charsToTrim.length() == 0 || fromBeginningOrEndStr.length() == 0) {
            return text;
        }

        boolean fromBOL = fromBeginningOrEndStr.equals("beginning") || fromBeginningOrEndStr.equals("begin + end");
        boolean fromEOL = fromBeginningOrEndStr.equals("end") || fromBeginningOrEndStr.equals("begin + end");

        // Break into separate lines.
        String[] lineList = split(text);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {
            if (fromBOL) line = trim(line, charsToTrim, false);
            if (fromEOL) line = trim(line, charsToTrim, true);
            finishedList.add(line);
        }

        return StringUtils.join(finishedList.toArray(), '\n');
    }

    private static String trim(String line, String charsToTrim, boolean trimFromEndOfLine) {

        if (!trimFromEndOfLine) {
            while (line.length() > 0 && charsToTrim.contains(String.valueOf(line.charAt(0)))) {
                line = line.substring(1);
            }
        } else {

            int len = line.length();

            while (len > 0 && charsToTrim.contains(String.valueOf(line.charAt(len - 1)))) {
                line = line.substring(0, len - 1);
                len--;
            }
        }

        return line;
    }

    public static String generateUUIDs(int howMany) {

        StringBuilder builder = new StringBuilder();

        // Append normal UUIDs:
        for (int count = 0; count < howMany; count++) {
            builder.append(UUID.randomUUID().toString()).append("\n");
        }

        builder.append("\n");

        // Append uppercase UUIDs:
        for (int count = 0; count < howMany; count++) {
            builder.append(UUID.randomUUID().toString().toUpperCase()).append("\n");
        }

        builder.append("\n");

        // Append uppercase w/ dashes removed:
        for (int count = 0; count < howMany; count++) {
            builder.append(UUID.randomUUID().toString().toUpperCase().replace("-", "")).append("\n");
        }

        return builder.toString();
    }

    public static String insertTemplateText(String templateStr, String text) {

        if (templateStr == null || templateStr.length() == 0) {
            return text;
        }

        if (text == null || text.length() == 0) {
            return text;
        }

        // Break into separate lines.
        String[] lineList = split(text);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {

            if (line.trim().length() == 0) {
                continue;
            }

            // Allow empty vars by making sure there's a space on either side of the ;
            if (line.startsWith(";")) {
                line = " " + line;
            }

            if (line.endsWith(";")) {
                line = line + " ";
            }

            // Break each line into params.
            String[] params = line.split(";");

            // Each line actually is the template with the params plugged in:
            line = templateStr;

            // Try to replace %1 with params[0], %2 with params[1], etc.
            for (int count = 0; count < params.length; count++) {
                String param = params[count].trim();
                String varText = "%" + (count + 1);
                line = line.replaceAll(varText, param);
            }

            finishedList.add(line);
        }

        return StringUtils.join(finishedList.toArray(), '\n');

    }

    public static String sortAlphaReverse(String text) {
        return sortAlphaReverse(text, false);
    }

    public static String sortAlphaReverse(String text, final boolean ignoreCase) {

        // Break into separate lines.
        String[] lineList = split(text);

        Arrays.sort(lineList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {

                if (o1 == null) {
                    return 1;
                }

                if (o2 == null) {
                    return -1;
                }

                if (ignoreCase) {
                    return o2.compareToIgnoreCase(o1);
                } else {
                    return o2.compareTo(o1);
                }
            }
        });

        return StringUtils.join(lineList, '\n').trim();
    }

    public static String sortAlpha(String text) {
        return sortAlpha(text, false);
    }

    public static String sortAlpha(String text, final boolean ignoreCase) {

        // Break into separate lines.
        String[] lineList = split(text);

        Arrays.sort(lineList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {

                if (o1 == null) {
                    return -1;
                }

                if (o2 == null) {
                    return 1;
                }

                if (ignoreCase) {
                    return o1.compareToIgnoreCase(o2);
                } else {
                    return o1.compareTo(o2);
                }
            }
        });

        return StringUtils.join(lineList, '\n').trim();
    }

    /**
     * Remove lines that contain / don't contain the specified regex.
     * @param removeLinesNotContaining should we remove lines that do or do not contain?
     */
    public static String removeLinesContaining(boolean removeLinesNotContaining, String text, String containRegex) {

        // For my own sanity:
        boolean removeLinesContaining = !removeLinesNotContaining;

        // Break into separate lines.
        String[] lineList = split(text);

        Pattern containPat = Pattern.compile(containRegex);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {

            boolean matchFound = containPat.matcher(line).find();
            boolean removeThisLine = (matchFound && removeLinesContaining) || (!matchFound && removeLinesNotContaining);

            if (!removeThisLine) {
                finishedList.add(line);
            }
        }

        return StringUtils.join(finishedList.toArray(), '\n').trim();
    }

    public static String removeDuplicateLines(String text) {
        return removeDuplicateLines(text, false);
    }

    public static String removeDuplicateLines(String text, boolean ignoreCase) {

        // Break into separate lines.
        String[] lineList = split(text);

        List<String> finishedList = new ArrayList<String>();
        List<String> dupeList = new ArrayList<String>();

        for (String line : lineList) {

            if (ignoreCase) {

                String lcLine = line.toLowerCase();
                if (!dupeList.contains(lcLine)) {
                    dupeList.add(lcLine);
                    finishedList.add(line);
                }
            } else {

                if (!finishedList.contains(line)) {
                    finishedList.add(line);
                }
            }
        }

        return StringUtils.join(finishedList.toArray(), '\n').trim();
    }

    /**
     * Return info about this regex.
     * Does it match the text, how many groups, etc.
     * @param text body of text
     * @param regex regex expression to run against the text
     * @param flags Pattern.DOTALL, MULTILINE, CASE_INSENSITIVE, etc.
     * @return text about matches
     */
    public static String getRegexInfo(String text, String regex, int flags) {

        try {

            if (text.isEmpty() || regex.isEmpty()) {
                return "";
            }

            Pattern pat = Pattern.compile(regex, flags);
            Matcher mat = pat.matcher(text);

            int matchCount = 0;
            StringBuilder builder = new StringBuilder();

            while (mat.find()) {

                matchCount++;

                builder.append(String.format("  Match: \"%s\"\n", mat.group(0)));

                // Weirdness alert: groupCount() doesn't include group(0), which is the full match.
                // Thus if groupCount() == 1, we want to display group(1), not group(0).

                for (int gCount = 1; gCount <= mat.groupCount(); gCount++) {
                    builder.append(String.format("  Group %d: \"%s\"\n", gCount, mat.group(gCount)));
                }

                builder.append("\n");
            }

            return String.format("Matches: %d\n%s", matchCount, builder.toString());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * "private varType myVar;"
     * group 1 = "varType"
     * group 2 = "myVar"
     */
    private static Pattern javaPrivateVarPat = Pattern.compile("^\\s*private\\s+([A-Za-z0-9<>\\[\\]_\\-]+)\\s+([A-Za-z0-9<>\\[\\]_\\-]+)");

    /**
     * "public varType getMyVar"
     * "public boolean isMyBool"
     * (You have to append 'MyVar' to the end of this str.)
     */
    private static String javaPublicPropertyPatStr = "^\\s*public\\s+[A-Za-z0-9<>\\[\\]_\\-]+\\s+(get|is)";

    private static Pattern jdComment1 = Pattern.compile("^\\s*/\\*+");
    private static Pattern jdComment2 = Pattern.compile("^\\s*\\*");
    private static Pattern jdComment3 = Pattern.compile("^\\s*\\*/");

    /**
     * "private var _myVar;"
     * group 1 = "var"
     * group 2 = "_myVar"
     */
    private static Pattern flexPrivateVarPat = Pattern.compile("^\\s*private\\s+(var)\\s+([A-Za-z0-9<>\\[\\]_\\-]+)");

    /**
     * "public function get myVar"
     * (You have to append 'myVar' to the end of this str.)
     */
    private static String flexPublicPropertyPatStr = "^\\s*public\\s+function\\s+get\\s+";

    private static Pattern fComment1 = Pattern.compile("^\\s*/\\*+");
    private static Pattern fComment2 = Pattern.compile("^\\s*\\*");
    private static Pattern fComment3 = Pattern.compile("^\\s*\\*/");

    /**
     * Copies javadocs from the private var to the public get function.
     */
    public static String javaCopyVarDocs(String javaSrc) {
        return copyVarDocs(javaSrc, javaPrivateVarPat, javaPublicPropertyPatStr, jdComment1, jdComment2, jdComment3,
            new PreparePublicPropertyName() {
                @Override
                public String getPublicPropertyName(String privateVarName) {
                    return capitalize(privateVarName);
                }
            });
    }

    public static String flexCopyVarDocs(String flexSrc) {
        return copyVarDocs(flexSrc, flexPrivateVarPat, flexPublicPropertyPatStr, fComment1, fComment2, fComment3,
            new PreparePublicPropertyName() {
                @Override
                public String getPublicPropertyName(String privateVarName) {
                    if (privateVarName.startsWith("_")) {
                        return privateVarName.substring(1);
                    } else {
                        return privateVarName;
                    }
                }
            });
    }

    private static String copyVarDocs(
        String src,
        Pattern privateVarPat, String publicPropertyPatStr,
        Pattern comment1, Pattern comment2, Pattern comment3,
        PreparePublicPropertyName publicPropertyNamePrep) {

        // Map of: [ "myVarName" : "/** myVarName comment ... */" ]
        Map<String, List<String>> varCommentMap = new HashMap<String, List<String>>();
        Collection<String> finishedList = new ArrayList<String>();
        List<String> commentList = new ArrayList<String>();

        // Break into separate lines.
        String[] lineList = split(src);

        for (String line : lineList) {

            // Skip blank lines between comments and vars.
            if (line.trim().isEmpty() && !commentList.isEmpty()) {
                finishedList.add(line);
                continue;
            }

            // Save any comments we come across.
            Matcher commentMat1 = comment1.matcher(line);
            Matcher commentMat2 = comment2.matcher(line);
            Matcher commentMat3 = comment3.matcher(line);
            boolean isComment = commentMat1.find() || commentMat2.find() || commentMat3.find();

            if (isComment) {
                commentList.add(line);
            }

            // Check for a private var definition:

            Matcher privateVarMat = privateVarPat.matcher(line);
            boolean isPrivateVarDef = privateVarMat.find();

            if (isPrivateVarDef && !commentList.isEmpty()) {

                String varName = privateVarMat.group(2);
                varCommentMap.put(varName, new ArrayList<String>(commentList));
            }

            // Check for a public property definition with no preceding comment:

            if (!isComment && !isPrivateVarDef && commentList.isEmpty()) {

                for (String varName : varCommentMap.keySet()) {

                    // Build the custom public property pattern:
                    String fullPublicPropPatStr = publicPropertyPatStr + publicPropertyNamePrep.getPublicPropertyName(varName);
                    Pattern publicPropPat = Pattern.compile(fullPublicPropPatStr);

                    // Does this line match the public property def?
                    Matcher publicPropMat = publicPropPat.matcher(line);
                    if (publicPropMat.find()) {
                        finishedList.addAll(varCommentMap.get(varName));
                        break;
                    }
                }
            }

            // If this isn't a comment-related line, reset the comment builder.
            if (!isComment && !commentList.isEmpty()) {
                commentList.clear();
            }

            finishedList.add(line);
        }

        return StringUtils.join(finishedList.toArray(), '\n');
    }

    private static interface PreparePublicPropertyName {
        public String getPublicPropertyName(String privateVarName);
    }
}

/* BASIC TEMPLATE:

        // Break into separate lines.
        String[] lineList = split(text);

        Collection<String> finishedList = new ArrayList<String>();

        for (String line : lineList) {
            finishedList.add(line);
        }

        return StringUtils.join(finishedList.toArray(), '\n');

*/
