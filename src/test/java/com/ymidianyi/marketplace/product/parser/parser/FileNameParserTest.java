package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileNameParserTest {

    private FileNameParser parser;

    @BeforeEach
    void setUp() {
        parser = new FileNameParser();
    }

    @Test
    void shouldParseValidCsvFileName() {
        FileNameMetadata metadata = parser.parse("products_PARTNER-A_2026-03-23.csv");

        assertThat(metadata.partnerId()).isEqualTo("PARTNER-A");
        assertThat(metadata.exportDate()).isEqualTo(LocalDate.of(2026, 3, 23));
    }

    @Test
    void shouldParseValidJsonFileName() {
        FileNameMetadata metadata = parser.parse("products_MY-PARTNER_2026-01-15.json");

        assertThat(metadata.partnerId()).isEqualTo("MY-PARTNER");
        assertThat(metadata.exportDate()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void shouldThrowForInvalidFileName() {
        assertThatThrownBy(() -> parser.parse("invalid_file.csv"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid_file.csv");
    }

    @Test
    void shouldThrowForFileNameWithoutDate() {
        assertThatThrownBy(() -> parser.parse("products_PARTNER-A.csv"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
