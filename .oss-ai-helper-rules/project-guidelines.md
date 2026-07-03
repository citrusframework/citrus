# Project Guidelines

This rule file contains branching, commit, PR, and task-finding conventions for the project. Commands read this file to determine how to name branches, format commits, and search for tasks.

- **Fix branch:** `fix/<ISSUE_NUMBER>`
- **Feature branch:** `feature/<ISSUE_NUMBER>-<short-slug>`
- **Bugfix branch:** `bugfix/<ISSUE_NUMBER>`
- **Quick-fix branch:** `quick-fix/<short-slug>`
- **SonarCloud branch:** _(not configured)_
- **Commit format (fix):** `fix(#<ISSUE_NUMBER>): <brief description of fix>`
- **Commit format (quick-fix):** `chore: <brief description>`
- **CI-issue branch:** `ci-issue/<short-slug>`
- **Commit format (ci-issue):** `ci: <brief description>`
- **PR creation:** always
- **Find-task source:** GitHub labels
- **Find-task beginner label:** `good first issue`
- **Find-task intermediate label:** `enhancement`
- **Find-task experienced label:** `help wanted`
- **Scope-too-large redirect:** `/oss-create-issue`
