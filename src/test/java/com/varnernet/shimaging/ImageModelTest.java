package com.varnernet.shimaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class ImageModelTest {

    @Test
    void imageEventsAreDispatchedOnEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Ensure EDT is started before async render event checks.
        });

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        ImageModel model = new ImageModel(new SingleImageSource(image));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean onEdt = new AtomicBoolean(false);

        model.addImageListener(new ImageEventListener() {
            @Override
            public void imageChanged(ImageEvent ie) {
                if (ie.getType() == ImageEvent.IMAGE_INVALID) {
                    onEdt.set(SwingUtilities.isEventDispatchThread());
                    latch.countDown();
                }
            }

            @Override
            public void imageError(ImageEvent ie) {
                // no-op
            }
        });

        model.queueRender();
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        model.haltRender();

        assertTrue(completed, "Expected IMAGE_INVALID event from queued render");
        assertTrue(onEdt.get(), "Expected model events to dispatch on EDT");
    }

    @Test
    void brightnessAdjustmentsDoNotMutateTheSourceRaster() throws Exception {
        BufferedImage sourceImage = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        int sourceRgb = 0x00303030;
        sourceImage.setRGB(1, 1, sourceRgb);

        ImageModel model = new ImageModel(new SingleImageSource(sourceImage));

        awaitImageInvalid(model, model::queueRender);
        awaitImageInvalid(model, () -> model.setBrightness(25));

        int brightenedRgb = model.getImage().getRGB(1, 1) & 0x00FFFFFF;
        assertEquals(sourceRgb, sourceImage.getRGB(1, 1) & 0x00FFFFFF,
                "Brightness changes must not mutate the cached source raster");
        assertNotEquals(sourceRgb, brightenedRgb,
                "Rendered image should reflect the brightness change");

        awaitImageInvalid(model, () -> model.setBrightness(0));

        assertEquals(sourceRgb, sourceImage.getRGB(1, 1) & 0x00FFFFFF,
                "Resetting brightness must still leave the source raster unchanged");
        assertEquals(sourceRgb, model.getImage().getRGB(1, 1) & 0x00FFFFFF,
                "Resetting brightness should restore the original rendered pixels");

        model.haltRender();
    }

    private static void awaitImageInvalid(ImageModel model, Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ImageEventListener> listenerRef = new AtomicReference<>();

        ImageEventListener listener = new ImageEventListener() {
            @Override
            public void imageChanged(ImageEvent ie) {
                if (ie.getType() == ImageEvent.IMAGE_INVALID) {
                    ImageEventListener registered = listenerRef.getAndSet(null);
                    if (registered != null) {
                        model.removeImageListener(registered);
                    }
                    latch.countDown();
                }
            }

            @Override
            public void imageError(ImageEvent ie) {
                // no-op
            }
        };

        listenerRef.set(listener);
        model.addImageListener(listener);
        action.run();

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Expected IMAGE_INVALID event after render request");
    }

    private static final class SingleImageSource implements ImageSource {
        private BufferedImage image;

        private SingleImageSource(BufferedImage image) {
            this.image = image;
        }

        @Override
        public BufferedImage getImage(int index) {
            if (index != 0) {
                return null;
            }
            return image;
        }

        @Override
        public Image getThumbnail(int index) {
            return getImage(index);
        }

        @Override
        public int getImageCount() {
            return image == null ? 0 : 1;
        }

        @Override
        public String getImageName() {
            return "single";
        }

        @Override
        public void dispose() {
            if (image != null) {
                image.flush();
                image = null;
            }
        }
    }
}


