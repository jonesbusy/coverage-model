package edu.hm.hafner.coverage;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static edu.hm.hafner.coverage.assertions.Assertions.*;

class ValueTest {

    @Test
    void shouldReturnCorrectValueOfCoverage() {
        Value container = Value.valueOf("CONTAINER: 1/1");

        assertThat(container)
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("MODULE: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("PACKAGE: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("FILE: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("CLASS: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("METHOD: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("LINE: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("INSTRUCTION: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("BRANCH: 1/1"))
                .isInstanceOf(Coverage.class);
        assertThat(Value.valueOf("MUTATION: 1/1"))
                .isInstanceOf(Coverage.class);

        assertThat((Coverage) container)
                .hasCovered(1)
                .hasMissed(0);
    }

    @Test
    void shouldReturnCorrectValueOfFractionValue() {
        Value fractionValue = Value.valueOf("COMPLEXITY_DENSITY: 1/1");

        assertThat(fractionValue)
                .isInstanceOf(FractionValue.class);
        assertThat((FractionValue) fractionValue)
                .hasFraction(Fraction.getFraction(1, 1));
    }

    @Test
    void shouldReturnCorrectValueOfCyclomaticComplexity() {
        Value cyclomaticComplexity = Value.valueOf("COMPLEXITY: 1");

        assertThat(cyclomaticComplexity)
                .isInstanceOf(CyclomaticComplexity.class);
        assertThat((CyclomaticComplexity) cyclomaticComplexity)
                .hasValue(1);
    }

    @Test
    void shouldReturnCorrectValueOfLinesOfCode() {
        Value linesOfCode = Value.valueOf("LOC: 1");

        assertThat(linesOfCode)
                .isInstanceOf(LinesOfCode.class);
        assertThat((LinesOfCode) linesOfCode)
                .hasValue(1);
    }

    @Test
    void shouldThrowExceptionOnInvalidStringRepresentation() {
        String badRepresentation = "Bad representation";
        String badNumber = "COMPLEXITY: BadNumber";

        assertThatIllegalArgumentException()
                .isThrownBy(() -> Value.valueOf(badRepresentation))
                .withMessageContaining(String.format("Cannot convert '%s' to a valid Value instance.", badRepresentation));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Value.valueOf(badNumber))
                .withMessageContaining(String.format("Cannot convert '%s' to a valid Value instance.", badNumber));
    }

    @Test
    void shouldGetValue() {
        var linesOfCode = new LinesOfCode(10);
        var cyclomaticComplexity = new CyclomaticComplexity(20);

        List<Value> values = List.of(linesOfCode, cyclomaticComplexity);

        assertThat(Value.getValue(Metric.LOC, values))
                .isEqualTo(linesOfCode);
        assertThat(Value.getValue(Metric.COMPLEXITY, values))
                .isEqualTo(cyclomaticComplexity);
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> Value.getValue(Metric.LINE, values))
                .withMessageContaining("No value for metric");
    }

    @Test
    void shouldAdhereToEquals() {
        EqualsVerifier.simple().forClass(Value.class).verify();
    }
}