# vcs/core/ado/pullrequest

Jackson-annotated model objects for the Azure DevOps Git REST API — specifically the pull request surface. Use these as the target types when deserializing ADO API responses with `ObjectMapper`.

No HTTP client is included. This module is purely the model layer.

---

## What's in here

All classes live under `io.github.wypeboard.foundation.vcs.ado.pullrequest.model`.

### Core entities

| Class | Represents |
|---|---|
| `AdoGitPullRequest` | A pull request — reviewers, commits, merge status, draft flag, dates |
| `AdoGitPullRequestIteration` | A push or rebase that updated the PR's source branch |
| `AdoGitPullRequestChange` | A file change within an iteration |
| `AdoThread` | A comment thread on the PR (both user comments and system messages) |
| `AdoThreadComment` | A single comment or reply within a thread |
| `AdoIdentityRef` | A person or group identity (author, reviewer, etc.) |
| `AdoIdentityRefWithVote` | An `AdoIdentityRef` with a reviewer vote attached |

### Supporting types

| Class | Represents |
|---|---|
| `AdoConnectionData` | Response from the undocumented `/connData` endpoint — the authenticated identity |
| `AdoGitCommitRef` | A git commit reference |
| `AdoPropertiesCollection` | The `{"key": {"$type":"…","$value":"…"}}` property bag used on PRs and threads |
| `AdoThreadContext` | File path and line positions for a code comment |
| `AdoCommentIterationContext` | The iteration context at the time a thread was created |
| `AdoPrThreadContext` | Combined thread context for creating a new thread via the API |

### Enumerations

| Enum | Values |
|---|---|
| `AdoGitPullRequestStatus` | `active`, `abandoned`, `completed`, `all`, `notSet` |
| `AdoReviewVote` | `APPROVED` (10), `APPROVED_WITH_SUGGESTIONS` (5), `NO_VOTE` (0), `WAITING_FOR_AUTHOR` (-5), `REJECTED` (-10) |
| `AdoThreadStatus` | `active`, `pending`, `fixed`, `wontFix`, `closed`, `unknown` |
| `AdoCommentType` | `text`, `system`, `codeChange`, `unknown` |
| `AdoIterationReason` | `create`, `push`, `forcePush`, `rebase`, `retarget`, `resolveConflicts`, `unknown` |
| `AdoVersionControlChangeType` | `add`, `edit`, `delete`, `rename`, `merge`, and others |

### Request helpers

| Class | Purpose |
|---|---|
| `JsonPatch` | Builder for JSON Patch (`RFC 6902`) request bodies — `add`, `remove`, `replace`, `copy`, `move`, `test` |
| `RequestObject` | Builder for arbitrary JSON request bodies |
| `Wrapped<T>` | Deserializer target for ADO's paginated `{"value":[…],"count":N}` responses |

### Convenience methods worth knowing

```java
AdoGitPullRequest pr = ...;

// Check if a given identity is a reviewer
pr.isReviewer(myIdentity);

// Inspect review votes
AdoIdentityRefWithVote reviewer = pr.getReviewers().get(0);
reviewer.getVote().hasApproved();    // true for APPROVED or APPROVED_WITH_SUGGESTIONS
reviewer.getVote().isBlocking();     // true for WAITING_FOR_AUTHOR or REJECTED

// Thread helpers
AdoThread thread = ...;
thread.getRootComment();             // Optional<AdoThreadComment> with lowest id
thread.hasTextComments();            // true if any comment is TEXT type
thread.isUserStartedThread();        // false for system-generated threads

// Thread status
AdoThreadStatus.openStatuses();      // Set of statuses that block PR completion
AdoThreadStatus.resolvedStatuses();  // Set of non-open statuses

// Building a file comment thread context
AdoPrThreadContext ctx = AdoPrThreadContext
    .fromPullRequestChange(change)
    .atIteration(latestIteration);

// Building a JSON Patch request
String body = JsonPatch.create()
    .replace("/status", "completed")
    .add("/completionOptions/deleteSourceBranch", "true")
    .toJson(mapper::writeValueAsString);
```

---

## Dependency

```kotlin
implementation("io.github.wypeboard.foundation:core-vcs-ado-pullrequest:0.1.0")
```

Jackson is `compileOnly` — your application brings it:

```kotlin
implementation("com.fasterxml.jackson.core:jackson-databind")
implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // for OffsetDateTime
```

Register the JSR-310 module on your `ObjectMapper`:

```java
ObjectMapper mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```