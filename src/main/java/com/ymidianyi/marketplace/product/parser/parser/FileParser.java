package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

import java.io.IOException;
import java.nio.file.Path;

public interface FileParser {

    ProductExportFileDto parse(IncomingProductExport incomingProductExport);

    boolean supports(IngestionFormat format);

    static String sourceRef(IncomingProductExport incomingProductExport) {
        return incomingProductExport.sourceRef() != null ? incomingProductExport.sourceRef() : "unknown";
    }
}
