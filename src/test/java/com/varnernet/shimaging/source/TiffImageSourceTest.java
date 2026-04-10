package com.varnernet.shimaging.source;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class TiffImageSourceTest {

    @Test
    void loadsMultipageTiffFromTestImages() throws Exception {
        Path tiffPath = Path.of("TestImages", "150812000400.tif");
        assertTrue(Files.exists(tiffPath), "Expected test TIFF at " + tiffPath.toAbsolutePath());

        TiffImageSource source = new TiffImageSource(tiffPath.toFile());
        try {
            assertTrue(source.getImageCount() > 0, "Expected at least one TIFF page");
            assertNotNull(source.getImage(0), "Expected first page to be readable");
        } finally {
            source.dispose();
        }
    }
}

