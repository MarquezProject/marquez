# Releasing

1. Update [`CHANGELOG.md`](CHANGELOG.md)
2. Tag the release and prepare for the next version with:

   ```bash
   $ ./new-version.sh --release-version X.Y.Z --next-release X.Y.Z
   ```

   > **Tip:** Use `--help` to see script usage

3. Visit [CI](https://app.circleci.com/pipelines/github/MarquezProject/marquez?branch=main) to see the progress of the release! :rocket:
4. Visit [sonatype](https://oss.sonatype.org) to promote _java_ artifacts
5. Draft a [new release](https://github.com/MarquezProject/marquez/releases/new) using the release notes for `X.Y.Z` in **step 1** as the release description:

   ![](./docs/assets/images/new-release.png)
   
6. Close the [project](https://github.com/MarquezProject/marquez/projects) for the _current_ release
7. Create a [new project](https://github.com/MarquezProject/marquez/projects/new) for the _next_ release