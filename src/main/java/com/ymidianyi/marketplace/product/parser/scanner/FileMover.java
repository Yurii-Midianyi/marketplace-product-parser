package com.ymidianyi.marketplace.product.parser.scanner;

import java.io.IOException;
import java.nio.file.Path;

public interface FileMover {

    Path moveToProcessed(Path file) throws IOException;

    Path moveToFailed(Path file, String errorMessage) throws IOException;
}
