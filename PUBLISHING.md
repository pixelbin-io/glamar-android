# GlamAR Android SDK Publishing Guide

This is a minimal guide for publishing the GlamAR Android SDK to Maven Central via Sonatype Central Portal.

## Prerequisites

- Ensure you have the latest version code ready
- Update the version number in the Gradle build file (`coordinates("io.pixelbin.glamar", "glamar", "1.0.1")`)
- Create and push a git tag matching the version

## Repository Information

The GlamAR Android SDK is hosted pixelbin Github

```text
https://github.com/pixelbin-io/glamar-android
```

## Credentials Setup

### 1. Sonatype Central Portal Credentials

You need a Sonatype Central Portal account with access to the `io.pixelbin.glamar` group ID.

#### Generate Access Tokens (Recommended)

Instead of using your actual username and password, it's recommended to use access tokens:

1. Log in to [Sonatype Central Portal](https://central.sonatype.com/)

## Sonatype ossh and nexus repository manager account details
Username:	pixelbin@dev
email: dev@pixelbin.io
password: Captureretail@123

## glamar user token
<server>
<id>${server}</id>
<username>v5Er7V</username>
<password>p9JDU2TysmZcWzS2Ndne2unbYhiVYzcEX</password>
</server>

### 2. GPG Signing Key Setup

Maven Central requires all artifacts to be signed with a GPG key.

#### Generate a GPG Key on macOS (if you don't have one)

On macOS, you can use GPG Suite or the command line:

```bash
# Install GPG if you don't have it
brew install gnupg

# Generate a key
gpg --gen-key
```

Follow the prompts to create a key. Use dev@pixelbin.io email address
And optional dev@pixelbin as password or you can use your own password but make sure to remember it.

#### Export Your Public Key

```bash
# List your keys to find your key ID
gpg --list-keys --keyid-format SHORT

# Export your public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

mkdir -p ~/.gnupg
gpg --export-secret-keys YOUR_KEY_ID > ~/.gnupg/secring.gpg
ls -l ~/.gnupg/secring.gpg

gpg --export-secret-keys -o ~/Library/Application\ Support/gnupg/secring.gpg

//in case if you cannot find your securing.gpg file

find ~/.gnupg -name "secring.gpg"
```

Replace `YOUR_KEY_ID` with your GPG key ID (the 8-character ID shown after "pub" in the key listing).

#### Option 1: Configure Signing in Gradle Properties

Create a `~/.gradle/gradle.properties` file with your Sonatype credentials:

```properties
signing.keyId=your gpg key
signing.password=dev@pixelbin or your own password
signing.secretKeyRingFile=/Users/[replace with your mac username]/.gnupg/secring.gpg // or whatever path you stored securing.gpg
# Sonatype credentials using access tokens
mavenCentralUsername=v5Er7V
mavenCentralPassword=p9JDU2TysmZcWzS2Ndne2unbYhiVYzcEX
```

## Publishing Steps

### 1. Validate the Project

```bash
./gradlew clean build
```

Ensure all tests pass and the build completes successfully.

### 2. Publish to Maven Central

```bash
./gradlew publishToMavenCentral
```

This command will:

- Build the project
- Generate the necessary artifacts
- Sign all artifacts with your GPG key
- Upload to Sonatype Central Portal

### 3. Release Process

The release process with the vanniktech-maven-publish plugin is automated. After publishing, your artifacts will be automatically released to Maven Central.

If you need to manually check the status:

1. Visit [Sonatype Central Portal](https://central.sonatype.com/)
2. Log in with your credentials
3. Navigate to "Deployments" to see your published artifacts

### 4. Verify Publication

After the release process completes (which may take some time), verify your package is available:

```bash
# Check if your package is available on Maven Central
curl -s "https://repo1.maven.org/maven2/io/pixelbin/glamar/glamar/1.0.1/"
```

You can also check the [Maven Central Repository](https://central.sonatype.org/artifact/io.pixelbin.glamar/glamar/) directly.

## Troubleshooting

### Authentication Issues

- Verify your Sonatype credentials are correct
- Ensure you have permissions to publish to the `io.pixelbin.glamar` group ID
- Check that your GPG key is properly configured

### Signing Issues

- Verify your GPG key is correctly set up in `gradle.properties`
- Ensure your GPG key password is correct
- Try exporting your GPG key again if using GPG 2.1+

### Publication Issues

- Check that all required POM information is provided (already configured in your build file)
- Ensure all dependencies are correctly specified
- Verify that your artifacts pass Sonatype's requirements
