package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import java.io.IOException;
import java.nio.file.Path;

public interface FileParser {

    ProductExportFileDto parse(Path file) throws IOException;

    boolean supports(String fileExtension);
}
