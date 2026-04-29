package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.FileNameMetadata;
import com.ymidianyi.marketplace.product.parser.dto.ProductDto;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;
import com.ymidianyi.marketplace.product.parser.model.ProductState;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
public class CsvFileParserTest {

    @Autowired
    private CsvFileParser parser;

    @MockitoBean
    private FileNameParser fileNameParser;

    private FileNameMetadata fileNameMetadata;

    @BeforeEach
    public void setup() {
        LocalDate date = LocalDate.parse("2026-04-28");
        fileNameMetadata = new FileNameMetadata("Partner-G", date);
    }

    private Path getResourcePath(String subdir) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(
                getClass().getResource("/test-data/parser/csv/" + subdir + "/products_PARTNER-A_2026-03-23.csv")
        ).toURI());
    }


    @Test
    public void testSupportedExtensions(){
        assertThat(parser.supports("json")).isFalse();
        assertThat(parser.supports("csv")).isTrue();
        assertThat(parser.supports("exe")).isFalse();
    }

    @Test
    public void testParseFullyPopulatedData() throws URISyntaxException, IOException {
        when(fileNameParser.parseFileName(any(Path.class))).thenReturn(fileNameMetadata);
        ProductExportFileDto exportFileDto = parser.parse(getResourcePath("all-fields"));
        assertThat(exportFileDto).isNotNull();
        List<ProductDto> products = exportFileDto.products();
        assertThat(products).isNotNull();
        assertThat(products).isNotEmpty();
        ProductDto product = products.getFirst();
        assertThat(product.name()).isEqualTo("Apple Fruit");
        assertThat(product.sku()).isEqualTo("802999");
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("41238"));
        assertThat(product.specialPrice()).isEqualByComparingTo(new BigDecimal("35000"));
        assertThat(product.specialFrom()).isEqualTo("2026-03-01");
        assertThat(product.specialTo()).isEqualTo("2026-04-01");
        assertThat(product.state()).isEqualTo(ProductState.ACTIVE);
        assertThat(product.brand()).isEqualTo("Shelf 3");
        assertThat(product.categories()).contains("Golden apple Bundles");
        assertThat(product.imageUrl()).isEqualTo("https://img.example.com/apple.jpg");
    }

    @Test
    public void testParsingWithMissingData() throws URISyntaxException, IOException {
        when(fileNameParser.parseFileName(any(Path.class))).thenReturn(fileNameMetadata);
        ProductExportFileDto exportFileDto = parser.parse(getResourcePath("minimal"));
        assertThat(exportFileDto).isNotNull();
        List<ProductDto> products = exportFileDto.products();
        assertThat(products).isNotNull();
        assertThat(products).isNotEmpty();
        ProductDto product = products.getFirst();
        assertThat(product.name()).isEqualTo("Apple");
        assertThat(product.sku()).isEqualTo("802999");
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(product.specialPrice()).isNull();
        assertThat(product.specialFrom()).isNull();
        assertThat(product.specialTo()).isNull();
        assertThat(product.state()).isEqualTo(ProductState.ACTIVE);
        assertThat(product.brand()).isNull();
        assertThat(product.categories()).isEmpty();
        assertThat(product.imageUrl()).isNull();
    }

    @Test
    public void testParsingMultipleRows() throws URISyntaxException, IOException {
        when(fileNameParser.parseFileName(any(Path.class))).thenReturn(fileNameMetadata);
        ProductExportFileDto exportFileDto = parser.parse(getResourcePath("multiple-rows"));
        assertThat(exportFileDto).isNotNull();
        assertThat(exportFileDto.products()).hasSize(3);
        assertThat(exportFileDto.products().get(0).name()).isEqualTo("Banana Fruit");
        assertThat(exportFileDto.products().get(1).name()).isEqualTo("Strawberry F");
        assertThat(exportFileDto.products().get(2).name()).isEqualTo("Watermelon");
    }

    @Test
    public void testSingleCategoryIsInList() throws URISyntaxException, IOException {
        when(fileNameParser.parseFileName(any(Path.class))).thenReturn(fileNameMetadata);
        ProductExportFileDto exportFileDto = parser.parse(getResourcePath("single-category"));

        assertThat(exportFileDto.products().getFirst().categories()).hasSize(1);
        assertThat(exportFileDto.products().getFirst().categories()).containsExactly("Drinks");

    }

    @Test
    public void testParseOfTheFileName() throws URISyntaxException, IOException {
        when(fileNameParser.parseFileName(any(Path.class))).thenReturn(fileNameMetadata);

        ProductExportFileDto exportFileDto = parser.parse(getResourcePath("single-category"));
        assertThat(exportFileDto).isNotNull();
        assertThat(exportFileDto.partnerId()).isEqualTo("Partner-G");
        assertThat(exportFileDto.exportDate()).isEqualTo(LocalDate.parse("2026-04-28").atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
