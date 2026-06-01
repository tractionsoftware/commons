package com.tractionsoftware.commons.html;

import com.google.common.collect.ImmutableMap;
import com.tractionsoftware.commons.io.StringWriteUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class HtmlUtilTest {

    private static final String getPrintLiteralText(String text) {
        return StringWriteUtil.getPrintedString(
            out -> HtmlUtil.printLiteralText(out, text)
        );
    }

    private static final String getPrintTagAttributeValue(String value) {
        return StringWriteUtil.getPrintedString(
            out -> HtmlUtil.printTagAttributeValue(out, value)
        );
    }

    private static final String getPrintOption(String text, String value, boolean selected) {
        return getPrintOption(text, value, selected, null);
    }

    private static final String getPrintOption(String text, String value, boolean selected, String className) {
        Map<String,String> attrs;
        if (className != null) {
            attrs = ImmutableMap.of(HtmlUtil.ATTRIBUTE_NAME_CLASS, className);
        }
        else {
            attrs = ImmutableMap.of();
        }
        return StringWriteUtil.getPrintedString(
            out -> HtmlUtil.printOption(out, text, value, selected, attrs)
        );
    }

    @Test
    public void test_getLiteralTextNull() {
        assertNull(HtmlUtil.getLiteralText(null));
    }

    @Test
    public void test_getLiteralTextEmpty() {
        assertEquals("", HtmlUtil.getLiteralText(""));
    }

    @Test
    public void test_getLiteralTextBlank() {
        assertEquals(" \t ", HtmlUtil.getLiteralText(" \t "));
    }

    @Test
    public void test_getLiteralTextSame1() {
        String input = "Simple Text";
        assertSame(input, HtmlUtil.getLiteralText(input));
    }

    @Test
    public void test_getLiteralTextSame2() {
        String input = "Dave's Same Test";
        assertSame(input, HtmlUtil.getLiteralText(input));
    }

    @Test
    public void test_getLiteralTextSame3() {
        String input = "It's the \"Same Old Story (Same Old Song and Dance)\"";
        assertSame(input, HtmlUtil.getLiteralText(input));
    }

    @Test
    public void test_getLiteralTextChanged1() {
        assertEquals(
            "S&amp;P 500",
            HtmlUtil.getLiteralText("S&P 500")
        );
    }

    @Test
    public void test_getLiteralTextChanged2() {
        assertEquals(
            "So Called \"Elites\", the bane of education &amp; government.",
            HtmlUtil.getLiteralText("So Called \"Elites\", the bane of education & government.")
        );
    }

    @Test
    public void test_getLiteralTextChanged3() {
        assertEquals(
            "Take a &lt; b &amp; a &gt; z.",
            HtmlUtil.getLiteralText("Take a < b & a > z.")
        );
    }

    @Test
    public void test_getLiteralTextChanged4() {
        assertEquals(
            "&lt;&amp;&amp;&amp;&gt;",
            HtmlUtil.getLiteralText("<&&&>")
        );
    }

    @Test
    public void test_getLiteralTextChanged5() {
        assertEquals(
            "&lt;X&gt;",
            HtmlUtil.getLiteralText("<X>")
        );
    }


    @Test
    public void test_printLiteralTextNull() {
        assertEquals("", getPrintLiteralText(null));
    }

    @Test
    public void test_printLiteralTextEmpty() {
        assertEquals("", getPrintLiteralText(""));
    }

    @Test
    public void test_printLiteralTextBlank() {
        assertEquals(" \t ", getPrintLiteralText(" \t "));
    }

    @Test
    public void test_printLiteralTextEquals1() {
        String input = "Simple Text";
        assertEquals(input, getPrintLiteralText(input));
    }

    @Test
    public void test_printLiteralTextEquals2() {
        String input = "Dave's Same Test";
        assertEquals(input, getPrintLiteralText(input));
    }

    @Test
    public void test_printLiteralTextEquals3() {
        String input = "It's the \"Same Old Story (Same Old Song and Dance)\"";
        assertEquals(input, getPrintLiteralText(input));
    }

    @Test
    public void test_printLiteralTextChanged1() {
        assertEquals(
            "S&amp;P 500",
            getPrintLiteralText("S&P 500")
        );
    }

    @Test
    public void test_printLiteralTextChanged2() {
        assertEquals(
            "So Called \"Elites\", the bane of education &amp; government.",
            getPrintLiteralText("So Called \"Elites\", the bane of education & government.")
        );
    }

    @Test
    public void test_printLiteralTextChanged3() {
        assertEquals(
            "Take a &lt; b &amp; a &gt; z.",
            getPrintLiteralText("Take a < b & a > z.")
        );
    }

    @Test
    public void test_printLiteralTextChanged4() {
        assertEquals(
            "&lt;&amp;&amp;&amp;&gt;",
            getPrintLiteralText("<&&&>")
        );
    }

    @Test
    public void test_printLiteralTextChanged5() {
        assertEquals(
            "&lt;X&gt;",
            getPrintLiteralText("<X>")
        );
    }

    @Test
    public void test_getTagAttributeValueNull() {
        assertNull(HtmlUtil.getTagAttributeValue(null));
    }

    @Test
    public void test_getTagAttributeValueEmpty() {
        assertEquals("", HtmlUtil.getTagAttributeValue(""));
    }

    @Test
    public void test_getTagAttributeValueBlank() {
        assertEquals(" \t ", HtmlUtil.getTagAttributeValue(" \t "));
    }

    @Test
    public void test_getTagAttributeValueSame1() {
        String input = "Simple Text";
        assertSame(input, HtmlUtil.getTagAttributeValue(input));
    }

    @Test
    public void test_getTagAttributeValueSame2() {
        String input = "Dave's Same Test";
        assertSame(input, HtmlUtil.getTagAttributeValue(input));
    }

    @Test
    public void test_getTagAttributeValueChanged1() {
        assertEquals(
            "S&amp;P 500",
            HtmlUtil.getTagAttributeValue("S&P 500")
        );
    }

    @Test
    public void test_getTagAttributeValueChanged2() {
        assertEquals(
            "So Called &quot;Elites&quot;, the bane of education &amp; government.",
            HtmlUtil.getTagAttributeValue("So Called \"Elites\", the bane of education & government.")
        );
    }

    @Test
    public void test_getTagAttributeValueChanged3() {
        assertEquals(
            "Take a &lt; b &amp; a &gt; z.",
            HtmlUtil.getTagAttributeValue("Take a < b & a > z.")
        );
    }

    @Test
    public void test_getTagAttributeValueChanged4() {
        assertEquals(
            "&lt;&amp;&amp;&amp;&gt;",
            HtmlUtil.getTagAttributeValue("<&&&>")
        );
    }

    @Test
    public void test_getTagAttributeValueChanged5() {
        assertEquals(
            "&lt;X&gt;",
            HtmlUtil.getTagAttributeValue("<X>")
        );
    }

    @Test
    public void test_getTagAttributeValueChanged6() {
        assertEquals(
            "&quot;Too Big to Fail&quot; was a phrase we got sick of in the 2000s.",
            HtmlUtil.getTagAttributeValue("\"Too Big to Fail\" was a phrase we got sick of in the 2000s.")
        );
    }

    @Test
    public void test_printTagAttributeValueNull() {
        assertEquals(
            "",
            getPrintTagAttributeValue(null)
        );
    }

    @Test
    public void test_printTagAttributeValueEmpty() {
        assertEquals("", getPrintTagAttributeValue(""));
    }

    @Test
    public void test_printTagAttributeValueBlank() {
        assertEquals(" \t ", getPrintTagAttributeValue(" \t "));
    }

    @Test
    public void test_printTagAttributeValueEquals1() {
        String input = "Simple Text";
        assertEquals(input, getPrintTagAttributeValue(input));
    }

    @Test
    public void test_printTagAttributeValueEquals2() {
        String input = "Dave's Same Test";
        assertEquals(input, getPrintTagAttributeValue(input));
    }

    @Test
    public void test_printTagAttributeValueChanged1() {
        assertEquals(
            "S&amp;P 500",
            getPrintTagAttributeValue("S&P 500")
        );
    }

    @Test
    public void test_printTagAttributeValueChanged2() {
        assertEquals(
            "So Called &quot;Elites&quot;, the bane of education &amp; government.",
            getPrintTagAttributeValue("So Called \"Elites\", the bane of education & government.")
        );
    }

    @Test
    public void test_printTagAttributeValueChanged3() {
        assertEquals(
            "Take a &lt; b &amp; a &gt; z.",
            getPrintTagAttributeValue("Take a < b & a > z.")
        );
    }

    @Test
    public void test_printTagAttributeValueChanged4() {
        assertEquals(
            "&lt;&amp;&amp;&amp;&gt;",
            getPrintTagAttributeValue("<&&&>")
        );
    }

    @Test
    public void test_printTagAttributeValueChanged5() {
        assertEquals(
            "&lt;X&gt;",
            getPrintTagAttributeValue("<X>")
        );
    }

    @Test
    public void test_printTagAttributeValueChanged6() {
        assertEquals(
            "&quot;Too Big to Fail&quot; was a phrase we got sick of in the 2000s.",
            getPrintTagAttributeValue("\"Too Big to Fail\" was a phrase we got sick of in the 2000s.")
        );
    }

    @Test
    public void testPrintOption1() {
        assertEquals(
            "<option value=\"dave\" class=\"awesome\" selected>Dave Shepperton</option>",
            getPrintOption("Dave Shepperton", "dave", true, "awesome")
        );
    }

    @Test
    public void testPrintOption2() {
        assertEquals(
            "<option value=\"someguy\">Some Guy</option>",
            getPrintOption("Some Guy", "someguy", false)
        );
    }

    @Test
    public void test_getClassicTextHtml1() {
        String input =
            "When typing a sentence, one is supposed to leave two spaces after the period.&nbsp;&nbsp;Like that.";
        String expected = "When typing a sentence, one is supposed to leave two spaces after the period.  Like that.";
        String actual = HtmlUtil.getClassicTextHtml(input);
        assertEquals(expected, actual);
    }

    @Test
    public void test_getClassicTextHtml2() {
        String input =
            "&quot;Don't tell me 1 &gt; 2,&quot; she said angrily.&nbsp;&nbsp;&quot;You're &lt; me.  Outside; you &amp; me, right now!";
        String expected = "\"Don't tell me 1 > 2,\" she said angrily.  \"You're < me.  Outside; you & me, right now!";
        String actual = HtmlUtil.getClassicTextHtml(input);
        assertEquals(expected, actual);
    }

    @Test
    public void test_getClassicTextHtml3() {
        String input = "I'm going to end with one of those & things &amp; you're going to not break.&nbsp;&nbsp;&";
        String expected = "I'm going to end with one of those & things & you're going to not break.  &";
        String actual = HtmlUtil.getClassicTextHtml(input);
        assertEquals(expected, actual);
    }
    @Test
    public void test_getTextSnippetFromHtmlContentNull() {
        assertEquals("", HtmlUtil.getTextSnippetFromHtml(null, 500, "..."));
    }

    @Test
    public void test_getTextSnippetFromHtmlContentEmpty() {
        assertEquals("", HtmlUtil.getTextSnippetFromHtml("", 500, "..."));
    }

    @Test
    public void test_getTextSnippetFromHtmlContentBlank1() {
        assertEquals("", HtmlUtil.getTextSnippetFromHtml("      ", 500, "..."));
    }

    @Test
    public void test_getTextSnippetFromHtmlContentBlank2() {
        String s = "      \n\n\n\t\n\n";
        assertEquals("", HtmlUtil.getTextSnippetFromHtml(s, 100, "..."));
    }

    @Test
    public void test_getTextSnippetFromHtmlContentBlank3() {
        String s = "<div></div><p><br><br>";
        assertEquals("", HtmlUtil.getTextSnippetFromHtml(s, 100, "..."));
    }

    @Test
    public void test_getTextSnippetFromHtmlContentTruncated1() {
        // "Hello, there!" he said cheerfully ...
        // 12345678901234567890123456789012345678
        String s =
            "<div>&quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about. &quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about.</div>";
        assertEquals(
            "\"Hello, there!\" he said cheerfully ...",
            HtmlUtil.getTextSnippetFromHtml(s, 38, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentTruncated2() {
        // "Hello, there!" he said cheerfully ...
        // 12345678901234567890123456789012345678
        String s =
            "<div>&quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about.</div>";
        assertEquals(
            "\"Hello, there!\" he said cheerfully...",
            HtmlUtil.getTextSnippetFromHtml(s, 37, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentTruncated3() {
        // "Hello, there!" he said cheerfully ...
        // 12345678901234567890123456789012345678
        String s =
            "<div>&quot;Hello, there!&quot; he said cheerfully as he lifted his arms into the air and shook his hands about.</div>";
        assertEquals(
            "\"Hello, there!\" he said ...",
            HtmlUtil.getTextSnippetFromHtml(s, 36, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentTruncated4() {
        // The ...
        // 1234567
        String s = "The coolest rock show I ever saw was in 1997 at Government Center in Boston.";
        assertEquals(
            "The ...",
            HtmlUtil.getTextSnippetFromHtml(s, 7, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentLongTokenHasToBeTruncated1() {
        // ABCDEFGHIJKL...
        // 123456789012345
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ.";
        assertEquals(
            "ABCDEFGHIJKL...",
            HtmlUtil.getTextSnippetFromHtml(s, 15, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentLongTokenCantFit1() {
        String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ.";
        assertEquals(
            "...",
            HtmlUtil.getTextSnippetFromHtml(s, 14, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentTruncated5() {
        String s = "6.2.38.build1776.";
        assertEquals(
            "6.2.38...",
            HtmlUtil.getTextSnippetFromHtml(s, 9, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentNoisyMarkupAndTokensWithTextThatFitsComfortably() {
        String s =
            "<html><body><SPAN class=\"Apple-style-span\" style=\"border-collapse: separate; color: rgb(0, 0, 0); font-family: 'Lucida Sans Typewriter'; font-size: 14px; font-style: normal; font-variant: normal; font-weight: normal; letter-spacing: normal; line-height: normal; orphans: 2; text-align: auto; text-indent: 0px; text-transform: none; white-space: normal; widows: 2; word-spacing: 0px; -webkit-border-horizontal-spacing: 0px; -webkit-border-vertical-spacing: 0px; -webkit-text-decorations-in-effect: none; -webkit-text-size-adjust: auto; -webkit-text-stroke-width: 0; \"></SPAN><DIV style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">The best grunge of 1992 was [[Traction:Pearl Jam - Ten|Pearl Jam\\'s Ten]].</div>";
        assertEquals(
            "The best grunge of 1992 was [[Traction:Pearl Jam - Ten|Pearl Jam\\'s Ten]].",
            HtmlUtil.getTextSnippetFromHtml(s, 100, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentNoisyMarkupWithTextThatFitsComfortably1() {
        String s =
            "<html><body><SPAN class=\"Apple-style-span\" style=\"border-collapse: separate; color: rgb(0, 0, 0); font-family: 'Lucida Sans Typewriter'; font-size: 14px; font-style: normal; font-variant: normal; font-weight: normal; letter-spacing: normal; line-height: normal; orphans: 2; text-align: auto; text-indent: 0px; text-transform: none; white-space: normal; widows: 2; word-spacing: 0px; -webkit-border-horizontal-spacing: 0px; -webkit-border-vertical-spacing: 0px; -webkit-text-decorations-in-effect: none; -webkit-text-size-adjust: auto; -webkit-text-stroke-width: 0; \"></SPAN><DIV style=\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">The best grunge of 1992 was <a href=\"/traction#/page/Traction/Pearl%20Jam%20%2d%20Ten\" traction_tokentype=\"wikilink\" traction_rs=\"[[ /link ::Traction 'Pearl Jam - Ten' label='Pearl Jam\\'s Ten' ]]\" target=\"_self\" traction_wikilink_showname=\"false\">Pearl Jam's Ten</a>.</div>";
        assertEquals(
            "The best grunge of 1992 was Pearl Jam's Ten.",
            HtmlUtil.getTextSnippetFromHtml(s, 100, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlContentFitsExactlyIntoMaximumRequestedLength() {
        String s =
            "<h1>The humpback whale is my favorite.</h1>\n<p>They're so majestic and beautiful. If you've ever been on a whale-watching expedition and seen one, you'd know what I'm talking about.</p>";
        assertEquals(
            "The humpback whale is my favorite. They're so majestic and beautiful. If you've ever been on a whale-watching expedition and seen one, you'd know what I'm talking about.",
            HtmlUtil.getTextSnippetFromHtml(s, 169, "...")
        );
    }

    @Test
    public void test_getTextSnippetFromHtmlEmptyBecauseOf0MaxLength() {
        String s = "This can be whatever we want, because it's not going to come out the other end.";
        assertEquals("", HtmlUtil.getTextSnippetFromHtml(s, 0, "..."));
    }

    @Test
    public void test_getTextSnippetFromHtmlWithEntities() {
        String s = "Here&#39;s my 2&#162;: sentences have two spaces after the period. &nbsp;Don&#39;t you agree?";
        assertEquals(
            "Here's my 2¢: sentences have two spaces after the period. Don't you agree?",
            HtmlUtil.getTextSnippetFromHtml(s, 100, "...")
        );
    }

}
