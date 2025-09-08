package com.official.lockr.acceptance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAcceptanceTest extends AcceptanceTest{

    @Test
    void testA() {
        final String s = "hello World";
        assertThat(s).isEqualTo("hello World");
    }
}
