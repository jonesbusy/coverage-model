package edu.hm.hafner.coverage.parser;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.coverage.ClassNode;
import edu.hm.hafner.coverage.CoverageParser;
import edu.hm.hafner.coverage.CoverageParser.ProcessingMode;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.ModuleNode;
import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.coverage.PackageNode;
import edu.hm.hafner.coverage.TestCase;
import edu.hm.hafner.coverage.TestCase.TestResult;
import edu.hm.hafner.coverage.TestCount;

import static edu.hm.hafner.coverage.assertions.Assertions.*;

class NunitParserTest extends AbstractParserTest {
    private static final String EMPTY = "-";

    @Override
    CoverageParser createParser(final ProcessingMode processingMode) {
        return new NunitParser(processingMode);
    }

    @Override
    protected String getFolder() {
        return "nunit";
    }

    @Test
    void shouldReadReport() {
        ModuleNode tree = readReport("nunit.xml");

        assertThat(tree).hasName(EMPTY);
        assertThat(getPackage(tree)).hasName("-");
        assertThat(getFirstClass(tree)).hasName("Tests");
        assertThat(getFirstTest(tree).getDescription()).contains("Expected string length 4 but was 5. Strings differ at index 4");

        assertThat(tree.aggregateValues()).contains(new TestCount(4));
    }

    @Test
    void shouldReadReportInV2Format() {
        ModuleNode tree = readReport("nunit2-format.xml");

        assertThat(tree).hasName(EMPTY);
        assertThat(getPackage(tree)).hasName("-");
        assertThat(getFirstClass(tree)).hasName("MockTestFixture");
        assertThat(getFirstTest(tree).getDescription()).contains("Intentional failure");

        assertThat(tree.aggregateValues()).contains(new TestCount(28));
    }

    @Test
    void shouldReadReportWithoutErrorMessage() {
        ModuleNode tree = readReport("nunit-no-message.xml");

        assertThat(tree).hasName(EMPTY);
        assertThat(getPackage(tree)).hasName("-");
        assertThat(getFirstClass(tree)).hasName("Tests");
        assertThat(getFirstTest(tree).getDescription()).contains("");
        assertThat(tree.aggregateValues()).contains(new TestCount(4));
    }

    @Test
    void shouldReadReportWithoutFailure() {
        ModuleNode tree = readReport("nunit-no-failure-block.xml");

        assertThat(tree).hasName(EMPTY);
        assertThat(getPackage(tree)).hasName("-");
        assertThat(getFirstClass(tree)).hasName("Tests");
        assertThat(getFirstTest(tree).getDescription()).contains("");
        assertThat(tree.aggregateValues()).contains(new TestCount(4));
    }

    @Test
    void shouldReadReportWithInvalidStatus() {
        ModuleNode tree = readReport("nunit-invalid-status.xml");

        assertThat(tree).hasName(EMPTY);
        assertThat(getPackage(tree)).hasName("-");
        assertThat(getFirstClass(tree)).hasName("Tests");
        assertThat(tree.aggregateValues()).contains(new TestCount(4));
    }

    private PackageNode getPackage(final Node node) {
        var children = node.getChildren();
        assertThat(children).hasSize(1).first().isInstanceOf(PackageNode.class);

        return (PackageNode) children.get(0);
    }

    private ClassNode getFirstClass(final Node node) {
        var packageNode = getPackage(node);

        var children = packageNode.getChildren();
        assertThat(children).isNotEmpty().first().isInstanceOf(ClassNode.class);

        return (ClassNode) children.get(0);
    }

    private TestCase getFirstTest(final Node node) {
        return node.getAll(Metric.CLASS).stream()
                .map(ClassNode.class::cast)
                .map(ClassNode::getTestCases)
                .flatMap(Collection::stream)
                .filter(test -> test.getResult() == TestResult.FAILED)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No failed test found"));
    }
}
