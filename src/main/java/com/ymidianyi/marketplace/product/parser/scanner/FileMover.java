package com.ymidianyi.marketplace.product.parser.scanner;

import java.nio.file.Path;

public interface FileMover {
    void moveToProcessed(Path file);
    void moveToFailed(Path file, String errorMessage);

}
