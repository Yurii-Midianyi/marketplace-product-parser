package com.ymidianyi.marketplace.product.parser.scanner;

import java.nio.file.Path;

public interface FileMover {
    Path moveToProcessed(Path file);
    Path moveToFailed(Path file, String errorMessage);

}
