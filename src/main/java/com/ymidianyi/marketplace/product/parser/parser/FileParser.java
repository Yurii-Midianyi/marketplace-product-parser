package com.ymidianyi.marketplace.product.parser.parser;

import com.ymidianyi.marketplace.product.parser.dto.IncomingProductExport;
import com.ymidianyi.marketplace.product.parser.dto.IngestionFormat;
import com.ymidianyi.marketplace.product.parser.dto.ProductExportFileDto;

public interface FileParser {

    ProductExportFileDto parse(IncomingProductExport incomingProductExport);

    boolean supports(IngestionFormat format);
}
