# Shimaging

Shimaging is a Java Swing image visualization library with reusable widgets and image sources for TIFF and common ImageIO formats.

## Requirements

- Java 21+
- Maven 3.6+ if you are not using the Maven Wrapper

## Maven Wrapper

The repository includes the Maven Wrapper, so a separate Maven installation is optional.
On first use, `./mvnw` downloads the pinned Maven distribution automatically.

## Build

```bash
./mvnw clean package
```

This produces a shaded executable jar at `target/shimaging-1.0.0-SNAPSHOT.jar`.

## Run the test apps

Run from Maven:

```bash
./mvnw exec:java
```

Run the primary test app directly:

```bash
java -jar target/shimaging-1.0.0-SNAPSHOT.jar
```

Run the composite TIFF demo app:

```bash
java -cp target/shimaging-1.0.0-SNAPSHOT.jar com.shimaging.CompositeTiffTestApp
```

## Test

```bash
./mvnw test
```

## Features

- Multi-page TIFF support via ImageIO + TwelveMonkeys TIFF plugin
- View controls: zoom, fit, rotate, brightness, contrast, invert
- Composite layering support for advanced render scenarios
- Swing-based image panel with toolbar and keyboard shortcuts

## Project Structure

```text
src/main/java/          Source code
src/main/resources/     Resources (icons, etc.)
src/test/java/          Unit tests
TestImages/             Sample image files for manual/demo usage
target/                 Build output
```

## License

See `LICENSE.TXT`.


