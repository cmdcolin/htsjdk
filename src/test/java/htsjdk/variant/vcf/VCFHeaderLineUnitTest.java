package htsjdk.variant.vcf;

import htsjdk.variant.VariantBaseTest;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class VCFHeaderLineUnitTest extends VariantBaseTest {

    @Test
    public void testEncodeVCFHeaderLineWithUnescapedQuotes() {

        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("ID", "VariantFiltration");
        attributes.put("CommandLineOptions", "filterName=[ANNOTATION] filterExpression=[ANNOTATION == \"NA\" || ANNOTATION <= 2.0]");

        final String encodedAttributes = VCFHeaderLine.toStringEncoding(attributes);
        assertNotNull(encodedAttributes);

        final String expectedEncoding = "<ID=VariantFiltration,CommandLineOptions=\"filterName=[ANNOTATION] filterExpression=[ANNOTATION == \\\"NA\\\" || ANNOTATION <= 2.0]\">";
        assertEquals(encodedAttributes, expectedEncoding);
    }

    @Test
    public void testEncodeVCFHeaderLineWithEscapedQuotes() {

        final Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("ID", "VariantFiltration");
        attributes.put("CommandLineOptions", "filterName=[ANNOTATION] filterExpression=[ANNOTATION == \\\"NA\\\" || ANNOTATION <= 2.0]");

        final String encodedAttributes = VCFHeaderLine.toStringEncoding(attributes);
        assertNotNull(encodedAttributes);

        final String expectedEncoding = "<ID=VariantFiltration,CommandLineOptions=\"filterName=[ANNOTATION] filterExpression=[ANNOTATION == \\\"NA\\\" || ANNOTATION <= 2.0]\">";
        assertEquals(encodedAttributes, expectedEncoding);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid count number, with fixed count the number should be 1 or higher: .*")
    public void testFormatNumberExceptions() {
        new VCFFormatHeaderLine("test",
                0,
                VCFHeaderLineType.Integer,
                "");
    }

    @Test(expectedExceptions = { IllegalArgumentException.class }, expectedExceptionsMessageRegExp = "Invalid count number, with fixed count the number should be 1 or higher: .*")
    public void testInfoNumberExceptions() {
        new VCFInfoHeaderLine("test",
                0,
                VCFHeaderLineType.Integer,
                "");
    }

    @Test
    public void testNumberExceptionFlag() {
        // Should not raise an exception
        new VCFInfoHeaderLine("test",
                0,
                VCFHeaderLineType.Flag,
                "");
    }
}
