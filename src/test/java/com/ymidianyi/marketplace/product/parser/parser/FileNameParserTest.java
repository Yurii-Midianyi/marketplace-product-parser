package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class FileNameParserTest {

    private final FileNameParser fileNameParser = new FileNameParser();

    @Test
    public void testParseFileCorrectRegex(){
        Path path = Path.of("someFolder/products_TestPartner_2026-04-01.csv");
        FileNameMetadata data = fileNameParser.parseFileName(path);
        assertThat(data.partnerId()).isEqualTo("TestPartner");
        assertThat(data.exportDate()).isEqualTo("2026-04-01");
    }

    @Test
    public void testParseFileNameWrongRegex(){
        Path path = Path.of("someFolder/failedName_2026-04-01.csv");
        assertThatThrownBy(()->fileNameParser.parseFileName(path))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid file name");
    }

}
