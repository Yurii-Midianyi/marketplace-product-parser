package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.exception.UnsupportedFileFormatException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@DirtiesContext
public class FileParserFactoryTest {

    @Autowired
    private FileParserFactory fileParserFactory;

    @Test
    public void testFileParserFactory() {
        FileParser jsonParser = fileParserFactory.getParser("json");
        assertThat(jsonParser).isNotNull();
        assertThat(jsonParser).isInstanceOf(JsonFileParser.class);
        FileParser csvParser = fileParserFactory.getParser("csv");
        assertThat(csvParser).isNotNull();
        assertThat(csvParser).isInstanceOf(CsvFileParser.class);
    }

    @Test
    public void testThrowExceptionOnInvalidExtension() {
        assertThatThrownBy(()->fileParserFactory.getParser("xml"))
                .isInstanceOf(UnsupportedFileFormatException.class)
                .hasMessage("Extension not supported: xml");
    }

}
