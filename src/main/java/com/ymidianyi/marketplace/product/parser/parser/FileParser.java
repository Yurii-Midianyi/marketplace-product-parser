package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import java.nio.file.Path;

public interface FileParser {

    ProductExportFileDto parse(Path file);

    boolean supports(String fileExtension);
}
