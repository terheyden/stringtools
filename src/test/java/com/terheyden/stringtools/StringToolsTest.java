package com.terheyden.stringtools;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class StringToolsTest {

    @Test
    public void testAddIndent() {

        String str = "  indent";
        assertEquals(StringTools.addIndent(str, 2), "    indent");

        str = " indent";
        assertEquals(StringTools.addIndent(str, -10), "indent");

        str = "  indent";
        assertEquals(StringTools.addIndent(str, -2), "indent");

        str = "  indent1\n  indent2";
        assertEquals(StringTools.addIndent(str, -2), "indent1\nindent2");

        str = "indent1\n  indent2";
        assertEquals(StringTools.addIndent(str, -2), "indent1\nindent2");

        str = " indent1\n  indent2";
        assertEquals(StringTools.addIndent(str, -2), "indent1\nindent2");
    }

    @Test
    public void testConvertBulletMarkup() {

        String text =
            "* item1\n" +
            "** sub1\n" +
            "** sub2";

        String result =
            "- item1\n" +
            "  - sub1\n" +
            "  - sub2";

        String origSyntax = "* |** |*** ";
        String newSyntax = "- |  - |    - ";

        assertEquals(StringTools.convertBulletMarkup(text, origSyntax, newSyntax), result);
    }

    @Test
    public void testBulletIndentOutdent() {

        String text =
            "* item1\n" +
            "** sub1\n" +
            "** sub2";
        String indent =
            "** item1\n" +
            "*** sub1\n" +
            "*** sub2";
        String outdent =
            "item1\n" +
            "* sub1\n" +
            "* sub2";
        String markup = "* |** |*** ";

        // Basic no-op test:
        assertEquals(StringTools.convertBulletMarkup(text, markup, markup, 0), text);
        // Test indenting:
        assertEquals(StringTools.convertBulletMarkup(text, markup, markup, 1), indent);
        // Test outdenting:
        assertEquals(StringTools.convertBulletMarkup(text, markup, markup, -1), outdent);
    }

    @Test
    public void testTrimChars() {

        String text = " * item1\n   * item2\n        *****     item3";

        // Verify basic use case:
        assertEquals(StringTools.trimChars(text, " *", "beginning"), "item1\nitem2\nitem3");

        // Verify bad args returns original str:
        assertEquals(StringTools.trimChars(text, null, "beginning"), text);

        // Verify EOL trimming:
        text = "item1\nitem2222\nitem333);";
        assertEquals(StringTools.trimChars(text, "123);", "end"), "item\nitem\nitem");
    }

    @Test
    public void testInsertTemplateText() {

        String text = "google; google.com\nyahoo; yahoo.com";
        String template = "%1 = www.%2";
        String resultStr = "google = www.google.com\nyahoo = www.yahoo.com";

        // Basic use case - simple variable insert:
        assertEquals(StringTools.insertTemplateText(template, text), resultStr);

        // Multiple param usage, and some extra characters:

        text = "Google's Website!; google.com/blah/?=hi\n  Yahoo!  ; yahoo.com  \n\n\n\n";
        template = "<a href=\"%2\">%1 (%2)</a>";
        resultStr = "<a href=\"google.com/blah/?=hi\">Google's Website! (google.com/blah/?=hi)</a>\n<a href=\"yahoo.com\">Yahoo! (yahoo.com)</a>";
        assertEquals(StringTools.insertTemplateText(template, text), resultStr);

        // Test only 1 param:

        text = "Google\nYahoo!";
        template = "Website: %1";
        resultStr = "Website: Google\nWebsite: Yahoo!";
        assertEquals(StringTools.insertTemplateText(template, text), resultStr);

        // Test a bunch of params:
        text = "1; joe; joe.com\n2; bob; bob.com\n";
        template = "INSERT (%1, %2, '%3')";
        resultStr = "INSERT (1, joe, 'joe.com')\nINSERT (2, bob, 'bob.com')";
        assertEquals(StringTools.insertTemplateText(template, text), resultStr);

        // Test empty params:

        text = "Joe; White\nFred;\nBilly Bob; Billington\n;Johnson\n;";
        template = "First = %1; Last = %2";
        resultStr = "First = Joe; Last = White\nFirst = Fred; Last = \nFirst = Billy Bob; Last = Billington\nFirst = ; Last = Johnson\nFirst = ; Last = ";
        assertEquals(StringTools.insertTemplateText(template, text), resultStr);
    }

    @Test
    public void testSorting() {

        String text = "\nbanana\n\napple\ncarrot\n\n";
        String result = "apple\nbanana\ncarrot";
        assertEquals(StringTools.sortAlpha(text), result);

        result = "carrot\nbanana\napple";
        assertEquals(StringTools.sortAlphaReverse(text), result);

        // Test casing:

        text = "carrot\nBanana\napple";
        result = "apple\nBanana\ncarrot";
        assertEquals(StringTools.sortAlpha(text, true), result);

        result = "Banana\napple\ncarrot";
        assertEquals(StringTools.sortAlpha(text), result);

        result = "carrot\nBanana\napple";
        assertEquals(StringTools.sortAlphaReverse(text, true), result);

        result = "carrot\napple\nBanana";
        assertEquals(StringTools.sortAlphaReverse(text), result);
    }

    @Test
    public void testRemoveLines() {

        String text = "1. bullet\nand\n2. bullet\n\n";
        String result = "1. bullet\n2. bullet";
        assertEquals(StringTools.removeLinesContaining(true, text, "[0-9]+"), result);
    }

    @Test
    public void testRemoveDupes() {

        String text = "i am\ndupe\nlooking for\ndupe";
        String result = "i am\ndupe\nlooking for";
        assertEquals(StringTools.removeDuplicateLines(text), result);
    }

    @Test
    public void testRegexReplaceText() {

        String text = "put on multi lines";
        String result = "put\non\nmulti\nlines";
        String find = "\\s+";
        String replace = "\\n";

        // Test that the user can enter "\\n" and we'll convert it to "\n" for them:
        assertEquals(StringTools.regexReplaceText(text, find, replace), result);
    }

    @Test
    public void testSmartReplaceText() {

        String text = "int USER_NAME_ID = 0;\n" +
            "String userName = \"\";\n" +
            "\n" +
            "// Check the user name.\n" +
            "\n" +
            "void checkUsername() {\n" +
            "    if (userName == null) {\n" +
            "        throw new Exception(\"User name is invalid. Error: \" + USER_NAME_ID);\n" +
            "    }\n" +
            "}";

        String result = "int USER_ADDR_ID = 0;\n" +
            "String userAddr = \"\";\n" +
            "\n" +
            "// Check the user addr.\n" +
            "\n" +
            "void checkUseraddr() {\n" +
            "    if (userAddr == null) {\n" +
            "        throw new Exception(\"User addr is invalid. Error: \" + USER_ADDR_ID);\n" +
            "    }\n" +
            "}";

        String find = "userName";
        String replace = "userAddr";

        assertEquals(StringTools.smartReplaceText(text, find, replace), result);
    }

    @Test
    public void testCopyJavaDocs() {

        String input =
            "    /**\n" +
            "     * The user's awesome name.\n" +
            "     */\n" +
            "    private String userName;\n" +
            "\n" +
            "    public String getUserName() {\n" +
            "        return userName;\n" +
            "    }\n" +
            "\n" +
            "    public void setUserName(String userName) {\n" +
            "        this.userName = userName;\n" +
            "    }";

        String result =
            "    /**\n" +
            "     * The user's awesome name.\n" +
            "     */\n" +
            "    private String userName;\n" +
            "\n" +
            "    /**\n" +
            "     * The user's awesome name.\n" +
            "     */\n" +
            "    public String getUserName() {\n" +
            "        return userName;\n" +
            "    }\n" +
            "\n" +
            "    public void setUserName(String userName) {\n" +
            "        this.userName = userName;\n" +
            "    }";

        assertEquals(StringTools.javaCopyVarDocs(input), result);

        input =
            "    /**\n" +
            "     * The list of users.\n" +
            "     */\n" +
            "    private List<String> userList;\n" +
            "\n" +
            "    /**\n" +
            "     * This object's ID.\n" +
            "     */\n" +
            "\n"+
            "    private int id;\n" +
            "\n" +
            "    public List<String> getUserList() {\n" +
            "        return userList;\n" +
            "    }\n" +
            "\n" +
            "    public Integer getId() {\n" +
            "        return id;\n" +
            "    }";

        result =
            "    /**\n" +
            "     * The list of users.\n" +
            "     */\n" +
            "    private List<String> userList;\n" +
            "\n" +
            "    /**\n" +
            "     * This object's ID.\n" +
            "     */\n" +
            "\n" +
            "    private int id;\n" +
            "\n" +
            "    /**\n" +
            "     * The list of users.\n" +
            "     */\n" +
            "    public List<String> getUserList() {\n" +
            "        return userList;\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * This object's ID.\n" +
            "     */\n" +
            "    public Integer getId() {\n" +
            "        return id;\n" +
            "    }";

        // Assert basic use case:
        assertEquals(StringTools.javaCopyVarDocs(input), result);

        // Assert we don't insert when not needed:
        assertEquals(StringTools.javaCopyVarDocs(result), result);
    }

    @Test
    public void testCopyFlexDocs() {

        String input =
            "    /**\n" +
            "     * The user's awesome name.\n" +
            "     */\n" +
            "    private var _userName:String;\n" +
            "\n" +
            "    public function get userName():String {\n" +
            "        return _userName;\n" +
            "    }\n" +
            "\n" +
            "    public function set userName(userName:String):void {\n" +
            "        _userName = userName;\n" +
            "    }";

        String result =
            "    /**\n" +
            "     * The user's awesome name.\n" +
            "     */\n" +
            "    private var _userName:String;\n" +
            "\n" +
            "    /**\n" +
            "     * The user's awesome name.\n" +
            "     */\n" +
            "    public function get userName():String {\n" +
            "        return _userName;\n" +
            "    }\n" +
            "\n" +
            "    public function set userName(userName:String):void {\n" +
            "        _userName = userName;\n" +
            "    }";

        // Assert basic test passes:
        assertEquals(StringTools.flexCopyVarDocs(input), result);

        // Assert we don't insert the doc when not needed:
        assertEquals(StringTools.flexCopyVarDocs(result), result);
    }
}
