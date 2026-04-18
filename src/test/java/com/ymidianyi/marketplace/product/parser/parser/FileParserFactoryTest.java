package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileParserFactoryTest {

    private FileParser jsonParser;
    private FileParser csvParser;
    private FileParserFactory factory;

    @BeforeEach
    void setUp() {
        jsonParser = mock(FileParser.class);
        csvParser = mock(FileParser.class);

        when(jsonParser.supports("json")).thenReturn(true);
        when(csvParser.supports("csv")).thenReturn(true);

        factory = new FileParserFactory(List.of(jsonParser, csvParser));
    }

    @Test
    void shouldReturnJsonParserForJsonExtension() {
        FileParser result = factory.getParser("json");
        assertThat(result).isSameAs(jsonParser);
    }

    @Test
    void shouldReturnCsvParserForCsvExtension() {
        FileParser result = factory.getParser("csv");
        assertThat(result).isSameAs(csvParser);
    }

    @Test
    void shouldThrowForUnsupportedExtension() {
        assertThatThrownBy(() -> factory.getParser("xml"))
                .isInstanceOf(UnsupportedFileFormatException.class)
                .hasMessageContaining("xml");
    }
}
