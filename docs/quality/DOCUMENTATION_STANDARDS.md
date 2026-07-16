# Project Novus Documentation Standards

> **Status:** Active  
> **Applies to:** Project Novus system, architecture, command, message, format, and progression documentation  
> **Purpose:** Keep each document's authority clear and prevent planned behavior, implementation status, and final contracts from collapsing into one contradictory pile.

## 1. Core Rules

Project Novus documentation is part of the engineering contract.

- Every document must state what it owns and what it must not define.
- One subject must have one canonical source of truth.
- Final documents define accepted behavior; progression documents report audited implementation status.
- Draft documents may propose behavior but must not claim that the behavior is implemented or approved.
- Code, schemas, tests, issues, and pull requests provide implementation evidence. A confident paragraph does not.
- Extend an existing owning document before creating a parallel document for the same rules.
- Split a supporting document only when the main system document would become materially harder to read.
- The main system document must summarize and link any delegated command, permission, format, message, schema, or integration document.

Documentation should reduce ambiguity. Producing three files that disagree with each other is merely distributed ambiguity with better filenames.

## 2. Document Lifecycle and Naming

Use uppercase descriptive filenames with the system name first.

| State | File Name Pattern | Authority |
| --- | --- | --- |
| Working design | `SOME_SYSTEM_FINAL_DRAFT.md` | Proposed behavior still open to material design changes. |
| Complete candidate without human validation | `SOME_SYSTEM_FINAL_DRAFT_NHV.md` | Candidate final contract awaiting human review and approval. |
| Accepted final contract | `SOME_SYSTEM_FINAL.md` | Canonical product and technical contract for the system. |
| Implementation tracker | `SOME_SYSTEM_PROGRESSION.md` | Audited implementation, integration, and verification status only. |
| Commands and permissions draft | `SOME_SYSTEM_COMMANDS_AND_PERMISSIONS_DRAFT.md` | Proposed command and access contract delegated by the system document. |
| Formats and system messages draft | `SOME_SYSTEM_MESSAGES_AND_FORMATS_DRAFT.md` | Proposed visual formats, placeholders, and system message contract delegated by the system document. |

### Promotion Rules

- `FINAL_DRAFT` may change freely while the design is being developed.
- `FINAL_DRAFT_NHV` means the candidate is structurally complete but has not received confirmed human validation.
- `FINAL` requires human review of the current candidate and removal of unresolved draft language.
- Renaming a file does not promote its authority by itself. The owning content, links, status header, and review evidence must also be updated.
- A progression document never becomes a final document. They answer different questions.
- Planned behavior in a final contract must still be reported accurately in progression as `Designed`, `Partially Implemented`, or another evidence-backed status.

## 3. Final Tech and Design Document

The final tech and design document is the main source of truth for a system.

Use:

```text
SOME_SYSTEM_FINAL_DRAFT.md
SOME_SYSTEM_FINAL_DRAFT_NHV.md
SOME_SYSTEM_FINAL.md
```

The document should contain only sections that apply, normally in this order:

1. **About**
   - Purpose.
   - Player, staff, developer, or operational value.
   - Scope and non-goals.
2. **Ownership Rules**
   - What the system owns.
   - What connected systems own.
   - Behavior the system must not duplicate.
3. **System Rules and Behavior**
   - Canonical product rules.
   - State transitions and invariants.
   - Administrative and player-facing behavior.
4. **Technical Structure**
   - Owning modules and packages.
   - Important interfaces, handlers, repositories, registries, routers, and orchestrators.
   - Platform boundaries.
5. **Data Model and Storage**
   - Authoritative storage.
   - Cache, registry, file, and runtime ownership.
   - Recovery, reconciliation, migration, and audit requirements.
6. **Runtime Flows**
   - Primary success paths.
   - Failure, cancellation, expiration, reload, shutdown, and recovery paths.
7. **Commands and Permissions Summary**
   - Important command roots and authority rules.
   - Link a delegated command document when detailed command coverage would overwhelm the system document.
8. **Formats and System Messages Summary**
   - Important player-facing outputs, placeholders, and delivery rules.
   - Link a delegated format and message document when detailed coverage is substantial.
9. **Integrations**
   - Inputs and outputs shared with other systems.
   - Ordering, consistency, and failure ownership across boundaries.
10. **Validation Requirements**
    - Automated, integration, Mineflayer, client, operational, migration, and recovery checks required before implementation may be called verified.
11. **Final Rules Summary**
    - Short list of the decisions future work must preserve.

Do not add empty sections merely to satisfy the list. Remove sections that do not apply and keep the remaining order readable.

## 4. Command and Permission Document

Use a delegated command and permission document only when the system has enough command or access complexity to justify it.

```text
SOME_SYSTEM_COMMANDS_AND_PERMISSIONS_DRAFT.md
```

The document owns detailed command syntax and access behavior. It must not redefine the system's product rules, storage model, or architecture.

Recommended structure:

1. **About**
2. **Command Ownership Rules**
3. **Command Access Rules**
   - Permission nodes.
   - Rank or power requirements.
   - Player, console, proxy, Paper, web, or Discord availability.
   - Self-target, other-target, equal-power, and higher-power rules.
4. **Commands**
   - Base commands.
   - Player commands.
   - Staff and administrative commands.
   - Debug commands when intentionally supported.
5. **Command Details**
   - Syntax and aliases.
   - Arguments and defaults.
   - Required permission and minimum authority.
   - Targeting, cooldown, confirmation, and audit behavior.
   - Success and failure behavior.
6. **Permission Nodes**
   - Player nodes.
   - Staff and administrative nodes.
   - Bypass nodes.
   - Wildcard or owner behavior when supported.
7. **Permission Matrix**
8. **Failure Rules**
9. **Final Rules Summary**

Every command must identify its owning runtime. Shared backend behavior may be platform-neutral, but command registration, sender types, and delivery belong to the platform that exposes the command.

## 5. Format and System Message Document

Use a delegated format and system message document when a system contains substantial visual formats, placeholders, reusable messages, or delivery-specific behavior.

```text
SOME_SYSTEM_MESSAGES_AND_FORMATS_DRAFT.md
```

Formats describe rendered structure. Messages describe reusable user-facing or operational communication. They may share one document because both define how system state is communicated, not how that state is stored or mutated.

Recommended structure:

1. **About**
2. **Format and Message Ownership Rules**
3. **Placeholder Rules**
   - Placeholder keys.
   - Data ownership.
   - Missing-value behavior.
   - Escaping and formatting safety.
4. **Formats**
   - Player-facing formats.
   - Staff or administrative formats.
   - Native UI, chat, book, title, action bar, web, Discord, or log formats where applicable.
5. **System Messages**
   - Success messages.
   - Failure and validation messages.
   - Permission messages.
   - Administrative and audit messages.
   - Recovery, fallback, or degraded-state messages.
6. **Delivery and Rendering Rules**
   - Owning delivery surface.
   - Runtime resolution behavior.
   - Fallback behavior.
   - Reload and cache behavior.
7. **Final Rules Summary**

Detailed text or visual examples may live beside the document in the system folder. The document must still provide the raw format or a precise textual description so the rule survives when an image inevitably wanders off to wherever missing screenshots go.

## 6. Progression Documents

Progression documents use:

```text
SOME_SYSTEM_PROGRESSION.md
```

They report what is demonstrably implemented, integrated, tested, blocked, or missing at an exact audited commit.

Progression documents must follow [SYSTEM_PROGRESSION_TEMPLATE.md](../progression/SYSTEM_PROGRESSION_TEMPLATE.md).

They must not:

- define new product behavior;
- replace the final system contract;
- infer completion from file count, class count, or test-file presence;
- claim visual success from a harness that cannot observe visuals;
- hide omitted tests, manual checks, blockers, or unverified recovery paths.

Update the progression document in the same coherent change as the implementation or audit evidence it reports.

## 7. Folder and Delegation Rules

System documentation lives beneath the narrowest owning system folder:

```text
docs/<system>/
├── SOME_SYSTEM_FINAL.md
├── SOME_SYSTEM_PROGRESSION.md
├── SOME_SYSTEM_COMMANDS_AND_PERMISSIONS_DRAFT.md
├── SOME_SYSTEM_MESSAGES_AND_FORMATS_DRAFT.md
└── imgs/
```

Only create files that the system actually needs.

Cross-project engineering policy belongs under `docs/quality`, including:

- [CODE_ARCHITECTURE.md](CODE_ARCHITECTURE.md)
- [GIT_WORKFLOW.md](GIT_WORKFLOW.md)
- this document

Rules:

- Keep schemas and migrations in their executable repository locations; summarize and link them from the owning system document.
- Keep images beneath the owning system folder unless they are genuinely shared assets.
- Do not create a global commands, messages, interfaces, handlers, or schemas document that erases system ownership.
- A delegated document must link back to its owning final document once that final document exists.
- The final document must summarize delegated rules rather than forcing readers to assemble the system contract through a scavenger hunt.

## 8. Archive Rules

Archive a document only when it is obsolete, conflicts with an active owner, or would otherwise compete as a source of truth. Do not archive a useful implementation reference merely because it is old; update its metadata or move it to the correct owning folder instead.

- Archived documents live beneath `docs/archives/` and preserve their former relative path where practical.
- Every archived document begins with an archive banner naming its former path, authority, reason, and current replacement or owner.
- Archived documents are historical evidence only. They must not define current product rules, architecture, commands, permissions, formats, schemas, or implementation status.
- Update normal inbound links to the active replacement. Link to an archive only when historical evidence is intentionally required.
- Record every archived document in [the archive index](../archives/README.md).
- Do not revise archived content to look current. Preserve the original beneath the archive banner.
- Deleting an archive requires the same human review as deleting other project documentation.

## 9. Change and Review Rules

Documentation changes follow [GIT_WORKFLOW.md](GIT_WORKFLOW.md).

Before accepting a documentation change, confirm:

- [ ] The document type and lifecycle state are explicit.
- [ ] The owning system and folder are correct.
- [ ] The document does not duplicate another source of truth.
- [ ] Proposed behavior is not presented as implemented behavior.
- [ ] Final behavior has current human validation.
- [ ] Progress claims name evidence and an audited commit.
- [ ] Delegated documents are summarized and linked by their owner.
- [ ] Commands, permissions, formats, messages, schemas, and integrations are defined in the correct document.
- [ ] Links resolve and examples use current Project Novus names, packages, and modules.
- [ ] Obsolete drafts are removed, archived, or clearly marked so they cannot compete with the accepted contract.