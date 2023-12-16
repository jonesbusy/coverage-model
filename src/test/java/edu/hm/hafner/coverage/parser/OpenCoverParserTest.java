package edu.hm.hafner.coverage.parser;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import edu.hm.hafner.coverage.CoverageParser;
import edu.hm.hafner.coverage.ModuleNode;

@DefaultLocale("en")
class OpenCoverParserTest extends AbstractParserTest {

    @Override
    CoverageParser createParser() {
        return new OpenCoverParser();
    }

    @Test
    void shouldReadReport() {
        readExampleReport();
    }

    private ModuleNode readExampleReport() {
        return readReport("opencover.xml", new OpenCoverParser());
    }

    @Override
    protected String getFolder() {
        return "opencover";
    }

}
