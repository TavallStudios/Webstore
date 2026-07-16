# Project Novus Code Review, Git Hygiene, and Engineering Issue Workflow

> **Status:** Active  
> **Applies to:** All Project Novus contributors, maintainers, automation, and AI-assisted development  
> **Purpose:** Protect production, preserve professional traceability, and keep engineering decisions understandable after their original context fades.

## 1. Core Policy

GitHub is the authoritative record for Project Novus production review.

**No change may enter `main` without accountable review recorded on GitHub.**

Accountable review uses one of two paths:

1. **Independent review**
   - The current change is presented through a GitHub pull request.
   - At least one qualified human other than the author approves the current diff.
   - Reviewable changes after approval require renewed approval.
2. **Owner self-promotion**
   - This path is limited to an authorized repository owner when they authored the pull request and no separate qualified reviewer is available.
   - The owner reviews the complete current diff and posts an **Owner Self-Review** record on the pull request.
   - The record names the scope, validation, untested paths, risks, rollback, and merge decision.
   - Reviewable changes after the record require a new Owner Self-Review.

Both paths require configured checks to pass, blocking conversations to be resolved, requested changes to be cleared, and the change to reach `main` through the GitHub merge flow.

GitHub does not allow an author to approve their own pull request. The owner path therefore uses a visible review record and, when required, the repository-owner or ruleset-bypass merge control instead of a fictional self-approval.

Automated reviews, AI reviews, status checks, comments, or reactions support review but satisfy neither independent approval nor owner self-review.

There is no ordinary direct-push substitute for executable production review. An incident may justify a smaller pull request and expedited owner review, but the decision and validation must still be recorded.

## 2. Branch Model

The canonical flow is:

```text
working/* -> staging/* -> main
```

`main` is the authoritative production source. Deployment from `main` is manual unless another production document explicitly changes that behavior.

### 2.1 Working branches

All ordinary changes begin on a focused branch named:

```text
working/<short-description>
```

Examples:

```text
working/timer-audit-recovery
working/companion-breeding-ui
working/docs-quality-foundation
```

A working branch should contain one coherent feature, fix, refactor, documentation change, or investigation outcome. Split unrelated systems into separate branches.

Working-branch pull requests target the appropriate `staging/*` branch. Use draft pull requests while work is incomplete or direction feedback is needed.

### 2.2 Staging branches

Staging branches collect a reviewable release or integration scope:

```text
staging/<release-or-scope>
```

Examples:

```text
staging/pre-alpha-foundation
staging/timer-runtime
staging/docs-quality-foundation
```

A staging branch must remain coherent enough to validate as one integrated change. It is not a permanent junk drawer for every branch that happened to compile on a Tuesday.

Pull requests from `staging/*` into `main` are production promotion pull requests and receive the full production review described in this document.

### 2.3 `main`

`main` is production and must normally:

- reject direct pushes;
- require pull requests;
- require either independent approval or a current Owner Self-Review record;
- dismiss stale independent approvals when reviewable changes are pushed;
- require renewed accountable review after the most recent reviewable push;
- allow an authorized repository owner to merge an authored pull request after the owner self-promotion requirements are met;
- require blocking conversations to be resolved;
- require configured checks to pass;
- block deletion;
- block force pushes;
- restrict merges to trusted maintainers.

### 2.4 Repository Owner Authority

Repository owner authority is determined by the repository or organization access controls and is not enumerated in this public workflow. When GitHub requires an explicit account or team for mechanical ownership, that assignment belongs in repository configuration such as `.github/CODEOWNERS` or an access-controlled operations record.

An authorized repository owner may use the owner self-promotion path for an authored pull request. They may also commit, push, or force-push `main` when they intentionally exercise emergency owner authority, but direct mutation is a last-resort exception and does not extend to automation or other contributors.

Ordinary production work uses a pull request so the repository retains the current diff, review record, check results, merge decision, and rollback context.

### 2.5 Hotfixes

A production hotfix must:

1. branch from the current `main` state;
2. contain only the minimum safe correction;
3. pass through a GitHub pull request;
4. receive independent approval or a current Owner Self-Review record;
5. run the checks available during the incident;
6. be reconciled into any active staging branch;
7. link an incident, blocker, or follow-up issue when further work remains.

Urgency changes the size of the process, not whether the process exists.

## 3. Code Review Flow

### 3.1 Before implementation

- Read the relevant design, progression, architecture, and workflow documents.
- Search open and closed issues for blockers, accepted directions, rejected approaches, and related investigations.
- Inspect the existing implementation before proposing new classes or systems.
- Identify the owning modules, expected tests, migration risk, failure behavior, and rollback path.
- Create or link an issue when the work crosses the issue threshold in Section 6.

### 3.2 During implementation

- Keep the change focused.
- Commit at meaningful, working checkpoints.
- Avoid unrelated cleanup.
- Update the linked issue when new evidence changes the direction.
- Keep tests and documentation with the behavior they explain.
- Treat generated code as untrusted until its APIs and behavior are verified.

### 3.3 Before opening a pull request

The author must:

- review the complete diff;
- remove temporary logging, debug code, dead code, and accidental generated files;
- run the relevant automated checks;
- perform any required bot or manual verification;
- document untested paths honestly;
- sync with the target branch;
- resolve obvious conflicts;
- update documentation and progression evidence when behavior changed;
- link issues accurately with `Closes #...`, `Fixes #...`, or `Refs #...`.

Use `Closes` or `Fixes` only when the pull request fully resolves the issue. Use `Refs` when it contributes without completing the issue.

### 3.4 Pull request state

Keep a pull request in **Draft** while implementation, validation, or its description is incomplete.

Mark it ready for review only when:

- the intended scope is complete;
- the author reviewed the diff;
- validation was run and recorded;
- risks and gaps are disclosed;
- the pull request is reasonably sized for human review.

### 3.5 Accountable Review

Independent reviewers and authorized repository owners evaluate:

- correctness against requested behavior;
- compatibility with [CODE_ARCHITECTURE.md](CODE_ARCHITECTURE.md);
- system ownership and duplicate-system risk;
- failure and recovery behavior;
- persistence and migration safety;
- concurrency and lifecycle behavior;
- permission and security boundaries;
- performance and operational impact;
- logging and audit behavior;
- test quality and missing validation;
- naming and maintainability;
- unrelated work hidden in the diff;
- fabricated APIs or shallow assumptions in generated code.

Independent approval means the reviewer accepts responsibility for the reviewed state. It is not a ceremonial green button awarded because the diff projected confidence.

When an authorized repository owner authors the pull request and uses owner self-promotion, add a pull-request comment using this structure:

```text
Owner Self-Review

Scope:
- What is being promoted.

Validation:
- Checks, tests, harnesses, and manual verification actually completed.

Untested:
- Known validation not performed.

Risks:
- Material production, data, security, migration, or operational risks.

Rollback:
- Exact revert or recovery approach.

Decision:
- Ready for merge at <current commit SHA>.
```

The recorded SHA must match the pull request head at merge time. A later reviewable commit invalidates the record.

### 3.6 Changes after approval

Review is required again when:

- new commits alter executable or operational behavior;
- conflict resolution changes the diff;
- a rebase introduces meaningful changes;
- generated files or dependency locks change;
- migration, configuration, or deployment instructions change;
- the pull request scope expands.

### 3.7 Automated review

Codex automatic GitHub review is the advisory automated reviewer.

It should run when a pull request is:

- opened;
- updated with reviewable changes;
- moved from draft to ready for review.

Automated review may identify defects, architectural drift, missing tests, and suspicious assumptions. It must not:

- count as independent human approval or owner self-review;
- merge a pull request;
- push directly to `staging/*` or `main`;
- resolve human review conversations;
- override a human request for changes;
- describe its own generated work as independently approved.

### 3.8 Merge guidance

- Use **Squash and merge** for `working/*` pull requests into `staging/*`.
- Use a **merge commit** for `staging/*` promotion into `main` when preserving the release boundary is useful.
- Delete merged working branches unless they have an explicit continuing purpose.
- Do not rewrite shared branch history without coordination.

## 4. Git Hygiene

### 4.1 Atomic commits

Each commit represents one understandable change.

A good commit:

- contains related code, tests, and documentation;
- is small enough to review;
- leaves the branch usable or documents the intentional intermediate state;
- can be reverted without dragging unrelated work with it.

Avoid mixing:

- formatting with behavior changes;
- unrelated refactors with features;
- dependency upgrades with gameplay work;
- separate fixes without a shared system boundary;
- generated output without its source or explanation.

Commit boundaries come from the actual staged changes. Do not force work into a fixed checklist when the code reveals better boundaries.

### 4.2 Commit messages

Every commit uses one or more typed subject lines followed by the structured body:

```text
Type: Capitalized concise action

Reason:
- Why the change is needed.

Changes:
- What changed.

Validation:
- What was run or inspected.
```

Allowed subject types are:

- `Build`
- `Added`
- `Changed`
- `Removed`
- `Fixed`
- `Clean`
- `Test`
- `Docs`
- `License`

`Meta` is not an allowed type.

Multiple typed subject lines may appear at the top of one commit when every line describes the same coherent system boundary:

```text
Added: Register typed timer audit repository
Test: Cover timer audit recovery

Reason:
- Timer recovery had no durable verification boundary.

Changes:
- Added the repository binding and recovery coverage.

Validation:
- mvn -pl minecraft-framework/backend-api test
```

Subject rules:

- Use `Type: Action`, not Conventional Commit syntax.
- Capitalize the first word after the colon.
- Describe the result, not the act of editing files.
- Avoid vague subjects such as `Update`, `Changes`, `Stuff`, `Fix`, `Final`, or `WIP`.

Validation must state what actually happened. Use `Not run` with a reason when validation was not performed; never promote the existence of a test file into evidence that it passed.

### 4.3 Working tree discipline

Before committing:

- inspect `git status`;
- inspect the staged diff;
- stage only the intended files;
- confirm no secrets or credentials are included;
- confirm environment files are ignored;
- remove build output, logs, crash reports, IDE state, and temporary artifacts unless intentionally versioned;
- check that line-ending or formatting churn is not hiding the real diff.

Never commit:

- API keys, access tokens, or passwords;
- private certificates or keys;
- production credentials;
- unredacted player or customer data;
- sensitive database dumps;
- local-only environment state.

If a secret reaches Git history, rotate it. Deleting the visible line is not remediation; it is putting a blanket over a fire.

### 4.4 Syncing and history

Before significant work, update local knowledge of the target branches. Use fast-forward pulls where possible.

Rebase personal working branches when it improves review clarity. Never rewrite a branch other contributors are using without coordination.

When a force push is necessary on a personal working branch, use `--force-with-lease`. Force pushes to shared staging branches are discouraged and force pushes to `main` are limited to the repository owner authority described above.

### 4.5 Scope control

Do not make unrelated drive-by changes.

When unrelated work is discovered:

- leave it unchanged;
- record it in an issue if it meets the issue threshold;
- handle it in a separate branch and commit history.

A small cleanup may remain only when it directly supports the current change and does not obscure review.

### 4.6 AI-assisted work

The human contributor remains responsible for every committed line regardless of who or what typed it.

For AI-assisted changes:

- provide the relevant repository documents and issues;
- require repository discovery before implementation;
- verify every referenced API and dependency;
- run tests instead of trusting generated claims;
- inspect the complete diff;
- disclose material uncertainty;
- do not allow the agent to approve its own work;
- use a working branch for non-trivial changes.

## 5. Production Promotion

A pull request from `staging/*` to `main` must explain:

- the release scope;
- included issues and working pull requests;
- important behavior changes;
- migrations and configuration requirements;
- automated, bot, and manual test evidence;
- known risks and untested paths;
- rollback steps;
- post-deployment verification.

Before merging:

- [ ] The target is `main` and the source is the intended staging branch.
- [ ] The current diff has independent approval or a valid Owner Self-Review record.
- [ ] Stale approvals or owner records were dismissed or renewed.
- [ ] Required checks are passing.
- [ ] Blocking conversations are resolved.
- [ ] Included issues and pull requests are linked.
- [ ] Migration and configuration changes are documented.
- [ ] Rollback steps are documented.
- [ ] Post-deployment checks are documented.
- [ ] No automation bypass is being treated as review.

## 6. GitHub Issues as Engineering Context

Issues are a durable engineering record, not merely a task list.

Create or update an issue when a challenge:

- blocks meaningful progress;
- changes or may change architecture or system direction;
- exposes an architectural limitation;
- requires investigation before implementation;
- affects multiple modules or future systems;
- creates meaningful production, security, persistence, migration, or operational risk;
- records a temporary compromise that must be revisited;
- has multiple realistic solutions with important tradeoffs;
- is important context for future human or AI work;
- cannot be responsibly explained only in a commit or pull request.

Do not create issues for every tiny task, routine formatting, temporary notes, or a trivial correction fully explained by one small pull request.

Suggested labels include:

- `blocker`
- `architecture`
- `decision`
- `investigation`
- `technical-debt`
- `production-risk`
- `security`
- `performance`
- `data`
- `ai-context`
- `needs-decision`
- `accepted-direction`

Direction-setting issues should contain:

### Summary

A concise explanation of the problem or decision.

### Current behavior

What the system does now.

### Why this matters

The practical user, production, architecture, or delivery impact.

### Evidence

Logs, code paths, tests, screenshots, metrics, reproduction steps, or related pull requests.

### Constraints

Technical, operational, compatibility, schedule, and product limits.

### Options considered

Realistic approaches and their tradeoffs.

### Current direction

The selected direction, temporary assumption, or unresolved question.

### Acceptance or exit criteria

What must become true before the issue is resolved.

### Related work

Links to code, documentation, issues, pull requests, incidents, or external references.

### AI implementation context

What a future coding agent must preserve, inspect, avoid, or verify.

Issues evolve with evidence. Record rejected approaches and why they failed, link implementation pull requests, and close issues only when their acceptance criteria are satisfied.

## 7. Reusable AI Prompt for Issue Discipline

<details>
<summary><strong>AI prompt: Use Project Novus issues as engineering context</strong></summary>

```text
You are working in the Project Novus repository.

Use GitHub Issues as durable engineering context for blockers, investigations, architectural limitations, production risks, and decisions that materially influence implementation. Do not use Issues as a noisy checklist for every small coding task.

Before changing code:

- Search open and closed Issues for the systems, modules, errors, constraints, and architecture involved.
- Read linked comments, pull requests, and decisions before proposing a design.
- Inspect the existing implementation and extend or correct it rather than creating a parallel system.
- Identify blockers, accepted directions, rejected approaches, temporary compromises, and unresolved decisions.
- Include relevant issue numbers in the implementation plan and pull request.

Create or update an issue only when the work reveals a blocker, material architectural decision, cross-system constraint, production risk, investigation, or temporary compromise that future contributors need to understand.

During implementation:

- update the issue when evidence changes the direction;
- record rejected approaches and why they were rejected;
- link commits and pull requests;
- use Closes or Fixes only when the implementation fully resolves the issue;
- use Refs when the work is related but incomplete;
- do not close an issue merely because code was written.

Use a working/* branch for the change, target the appropriate staging/* branch, and preserve the staging/* -> main production promotion boundary. No change may enter main without accountable review on the current diff. Use independent human approval when another qualified reviewer is available; otherwise an authorized repository owner may use the documented Owner Self-Review path. Automated review is advisory and satisfies neither path.

At the end, report issues reviewed, inherited decisions, files changed, validation performed, remaining risks, and the exact review or promotion work still required.
```

</details>

## 8. Author Checklist

- [ ] The branch and target are correct.
- [ ] The complete diff was self-reviewed.
- [ ] The change is focused.
- [ ] Relevant issues and architecture documents were reviewed.
- [ ] Tests and checks were actually run and recorded.
- [ ] Manual and bot verification is documented where required.
- [ ] Known gaps are disclosed.
- [ ] Configuration, migration, rollback, and recovery behavior are documented.
- [ ] No secrets or accidental artifacts are included.
- [ ] Generated code and APIs were verified.
- [ ] The pull request explains what changed and why.

## 9. Reviewer Checklist

- [ ] The review is independent, or the author is an authorized repository owner using a valid Owner Self-Review record.
- [ ] The requested behavior is implemented correctly.
- [ ] Existing architecture is respected.
- [ ] No duplicate or parallel system was introduced.
- [ ] Failure, data, security, permission, and lifecycle behavior were considered.
- [ ] Tests are meaningful and passing.
- [ ] Documentation and migration notes are sufficient.
- [ ] Issue decisions were followed or deliberately amended.
- [ ] Blocking conversations are resolved.
- [ ] Approval applies to the current diff.

## 10. Recommended Repository Configuration

### `working/*`

- allow contributor pushes;
- run relevant CI on pushes and pull requests;
- allow `--force-with-lease` on personal branches;
- delete after merge unless intentionally retained.

### `staging/*`

- require pull requests from working branches;
- require configured checks;
- block deletion while active;
- discourage history rewrites;
- restrict direct pushes to trusted integrators where practical.

### `main`

- require pull requests;
- require accountable review through independent approval or owner self-promotion;
- set required approving reviews to zero while there is only one qualified maintainer, or grant the authorized repository owner an explicit ruleset bypass;
- do not enable required CODEOWNERS approval until a second qualified code owner can approve owner-authored pull requests;
- dismiss stale approvals and invalidate stale Owner Self-Review records;
- require renewed review after the latest reviewable push;
- require conversation resolution;
- require configured checks;
- block deletion;
- block force pushes except for authorized repository owner authority;
- use `CODEOWNERS` to identify responsible owners without creating an impossible self-approval requirement.

Critical paths may include CI, deployment, migrations, security, networking, persistence, and production configuration. Actual paths and owners must follow the repository rather than a copied placeholder.

## 11. Adoption and Exceptions

Changes to this workflow should be introduced through a documentation pull request and reviewed before enforcement rules change.

Any exception must be explicit, narrow, reviewable, and approved by a maintainer. An undocumented exception is not flexibility; it is policy decay wearing business casual.