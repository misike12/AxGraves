# GitHub Actions Workflow Documentation

## Overview

This repository includes an automated CI/CD workflow to build and release the AxGraves plugin. The workflow handles:

1. **Continuous Integration**: Builds the plugin on every push and pull request
2. **Artifact Storage**: Uploads built `.jar` files for easy download
3. **Release Automation**: Automatically attaches plugin `.jar` to GitHub releases

## Workflow File

**Location**: `.github/workflows/build-and-release.yml`

## How It Works

### Build Job (Runs on Push/PR)

The build job runs automatically on:
- Pushes to `main`, `master`, or `develop` branches
- Pull requests targeting `main`, `master`, or `develop` branches

**Steps:**
1. Checks out the repository code
2. Sets up Java 21 (Temurin distribution)
3. Builds the plugin using Maven with dependency caching
4. Extracts the version from `pom.xml`
5. Uploads the built `.jar` file as a GitHub Actions artifact

**Artifact Details:**
- **Name**: `AxGraves-{version}` (e.g., `AxGraves-1.24.0`)
- **Contents**: The compiled plugin JAR from `target/`
- **Retention**: 30 days
- **Location**: Actions tab → Select workflow run → Artifacts section

### Release Job (Runs on Release)

The release job runs automatically when:
- A new GitHub Release is published

**Steps:**
1. Checks out the repository code
2. Sets up Java 21 (Temurin distribution)
3. Builds the plugin using Maven
4. Extracts the version from `pom.xml`
5. Uploads the `.jar` file as a release asset

**Release Asset:**
- Automatically attached to the GitHub Release
- Available for download from the Releases page

## Downloading Builds

### From GitHub Actions (Development Builds)

1. Navigate to the **Actions** tab in the repository
2. Select a workflow run (look for green checkmarks ✓)
3. Scroll down to the **Artifacts** section
4. Click on `AxGraves-{version}` to download the ZIP file
5. Extract the `.jar` file from the downloaded ZIP

### From GitHub Releases (Official Releases)

1. Navigate to the **Releases** page in the repository
2. Find the desired release version
3. Scroll down to the **Assets** section
4. Click on `AxGraves-{version}.jar` to download directly

## Requirements

### Build Environment

- **Java Version**: 21 (Temurin distribution)
- **Build Tool**: Maven 3.6+
- **Dependencies**:
  - Spigot API 1.21.3-R0.1-SNAPSHOT
  - AxAPI 1.6.0 (all classifier)
  - bStats 3.1.0

### GitHub Actions Runner

- Uses: `ubuntu-latest`
- All required tools are pre-installed in GitHub Actions runners
- Maven dependencies are cached for faster builds

## Testing the Workflow

### Test with a Commit

1. Make a small change to the repository
2. Commit and push to a monitored branch (`main`, `master`, or `develop`)
3. Navigate to the Actions tab
4. Watch the workflow run in real-time
5. Verify the artifact is uploaded successfully

### Test with a Pull Request

1. Create a new branch with changes
2. Open a pull request to `main`, `master`, or `develop`
3. The workflow will run automatically
4. Review build status in the PR checks section
5. Download the artifact to test the build

### Test with a Release

1. Navigate to the Releases page
2. Click "Draft a new release"
3. Create a new tag (e.g., `v1.24.0`)
4. Fill in release title and description
5. Click "Publish release"
6. The workflow will run automatically
7. The `.jar` file will be attached to the release within minutes

## Troubleshooting

### Build Fails

**Check the logs:**
1. Go to Actions tab
2. Click on the failed workflow run
3. Click on the "build" or "release" job
4. Expand failed steps to see error messages

**Common issues:**
- Maven dependency resolution failures (check repository accessibility)
- Java version mismatch (should be 21)
- `pom.xml` syntax errors
- Missing dependencies in external repositories

### Artifact Not Found

**Possible causes:**
- Build failed before artifact upload
- Artifact retention period expired (30 days)
- `target/` directory doesn't contain expected `.jar` file

**Solutions:**
- Check build logs for errors
- Verify Maven build completes successfully
- Check if `.jar` file is created in `target/` directory

### Release Asset Not Attached

**Possible causes:**
- Build failed in release job
- Release event didn't trigger workflow
- Missing `GITHUB_TOKEN` permissions (should be automatic)

**Solutions:**
- Ensure release is "published" (not draft)
- Check workflow run in Actions tab
- Verify `GITHUB_TOKEN` has write permissions

## Workflow Configuration

### Modify Trigger Branches

Edit `.github/workflows/build-and-release.yml`:

```yaml
on:
  push:
    branches:
      - main          # Add or remove branches
      - develop
      - feature/*     # Can use wildcards
```

### Change Java Version

Edit the setup-java step:

```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'  # Change version here
    distribution: 'temurin'
```

### Adjust Artifact Retention

Edit the upload-artifact step:

```yaml
- name: Upload build artifact
  uses: actions/upload-artifact@v4
  with:
    retention-days: 30  # Change retention period (1-90 days)
```

## Best Practices

1. **Semantic Versioning**: Use semantic versioning for releases (e.g., v1.24.0)
2. **Release Notes**: Always include detailed release notes
3. **Pre-release Testing**: Test builds from Actions artifacts before creating releases
4. **Version Consistency**: Keep `pom.xml` version in sync with release tags
5. **Branch Protection**: Enable branch protection on main branches
6. **Status Badges**: Add workflow status badges to README.md

## Status Badge

Add this badge to your README.md to show workflow status:

```markdown
![Build Status](https://github.com/misike12/AxGraves/workflows/Build%20and%20Release%20AxGraves/badge.svg)
```

## Support

For issues with the workflow:
1. Check this documentation
2. Review workflow logs in Actions tab
3. Open an issue in the repository with:
   - Workflow run URL
   - Error messages
   - Steps to reproduce
