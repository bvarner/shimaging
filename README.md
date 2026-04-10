# Shimaging

Shimaging is a Java Swing image visualization library with reusable widgets and image sources for TIFF and common ImageIO formats.

## Requirements

- Java 21+
- Maven 3.6+ if you are not using the Maven Wrapper

## Maven Wrapper

The repository includes the Maven Wrapper, so a separate Maven installation is optional.
On first use, `./mvnw` downloads the pinned Maven distribution automatically.

## GitHub Packages (Maven)

This project is configured to publish Maven artifacts to GitHub Packages.

### Publish from CI

- The workflow at `.github/workflows/publish.yml` runs on pushes to `main` and `feature/**`.
- On `main`, it publishes the base release version from `<revision>` (with `-SNAPSHOT` removed).
- On `feature/**`, it publishes a `-SNAPSHOT` version.
- Repository permissions required: `packages: write`.

## CI and Devcontainer Pipeline

This repository now follows the same style as `gerb4j`:

- `.devcontainer/devcontainer.json` uses Java 21 + Maven 3.9.9 and preloads dependencies.
- `.github/workflows/ci.yml` runs `./mvnw -B verify` in a devcontainer-based job.
- `.github/workflows/devcontainer.yml` prebuilds and pushes `ghcr.io/bvarner/shimaging-devcontainer`.
- `.github/workflows/javadoc.yml` generates Maven site output and deploys it to GitHub Pages.
- `.github/workflows/publish.yml` publishes Maven packages from devcontainer-based CI.

### Publish locally

Create `~/.m2/settings.xml` with a `github` server entry:

```xml
<settings>
  <servers>
	<server>
	  <id>github</id>
	  <username>YOUR_GITHUB_USERNAME</username>
	  <password>YOUR_GITHUB_TOKEN</password>
	</server>
  </servers>
</settings>
```

Then deploy:

```bash
./mvnw deploy
```

### Consume from another Maven project

Add this repository to the consuming project's `pom.xml`:

```xml
<repositories>
  <repository>
	<id>github</id>
	<url>https://maven.pkg.github.com/bvarner/shimaging</url>
  </repository>
</repositories>
```

Add this dependency:

```xml
<dependency>
  <groupId>com.varnernet.shimaging</groupId>
  <artifactId>shimaging</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

The consumer also needs credentials in `~/.m2/settings.xml` with a matching server id (`github`) and a token that has at least `read:packages`.
For local publishing, use a token with `write:packages`.

## Documentation Site

The static documentation site lives in `docs/` and is intended to be published with GitHub Pages.

For a repository-hosted GitHub Pages site, configure:

- **Source**: Deploy from a branch
- **Branch**: `main`
- **Folder**: `/docs`

The `docs/` directory includes a `.nojekyll` marker so the site is served as plain static content.

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
java -cp target/shimaging-1.0.0-SNAPSHOT.jar com.varnernet.shimaging.CompositeTiffTestApp
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
docs/                   GitHub Pages static documentation site
src/main/java/          Source code
src/main/resources/     Resources (icons, etc.)
src/test/java/          Unit tests
TestImages/             Sample image files for manual/demo usage
target/                 Build output
```

## License

See `LICENSE.TXT`.


