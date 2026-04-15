package com.ymidianyi.marketplace.product.parser.scanner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileScanner {

    List<Path> scan(Path directory) throws IOException;
}
