### Problem

👋 Thanks for opening a [pull request](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md#submitting-a-pull-request)! Please include a brief summary of the problem your change is trying to solve, or bug fix. If your change fixes a bug or you'd like to provide context on why you're making the change, please [link the issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) as follows:

Closes: #ISSUE-NUMBER

### Solution

Please describe your change as it relates to the problem, or bug fix, as well as any dependencies. If your change requires a database schema migration, please describe the schema modification(s) and whether it's a _backwards-incompatible_ or _backwards-compatible_ change.

> **Note:** All database schema changes require discussion. Please [link the issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue) for context.

### Checklist

- [ ] You've [signed-off](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md#sign-your-work) your work
- [ ] Your changes are accompanied by tests (_if relevant_)
- [ ] Your change contains a [small diff](https://kurtisnusbaum.medium.com/stacked-diffs-keeping-phabricator-diffs-small-d9964f4dcfa6) and is self-contained
- [ ] You've updated any relevant documentation (_if relevant_)
- [ ] You've updated the [`CHANGELOG.md`](https://github.com/MarquezProject/marquez/blob/main/CHANGELOG.md#unreleased) with details about your change under the "Unreleased" section (_if relevant, depending on the change, this may not be necessary_)
- [ ] You've versioned your `.sql` database schema migration according to [Flyway's naming convention](https://flywaydb.org/documentation/concepts/migrations#naming) (_if relevant_)
- [ ] You've included a [header](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md#copyright--license) in any source code files (_if relevant_)