# AGENTS.md — Photon (Fabric port)

Fabric port of **Photon 2.2.7** (upstream `Low-Drag-MC/Photon`) for Minecraft 1.21.1 — a client-side VFX engine (particles, trails, beams, post-processing, timeline, shader graph) with an in-game FX editor. This fork is the future VFX foundation for `grebeshok105/Codex-Superheroes`. Current code and passing tests win if any document differs.

## 1. What this repo is

- `upstream/1.21` tracks the upstream NeoForge source at tag v2.2.7 (base commit `aac3bc1`).
- `1.21.1-fabric-2.2` is the active port branch — one clean port commit on top of upstream 2.2.7 plus subsequent fix commits. **This is the only branch that matters for work.**
- `source/1.21.1-fabric` (ELBGG, based on Photon 2.0.x) is a **reference-only** older fabric port — consult it, never merge it.
- Long-term intent (not current work): realtime VFX editor improvements, timeline, shader graph, post-processing, beams/trails/particles/meshes, custom UI. Do not preemptively build them.

## 2. Related repositories

| Repo | Branch | Role |
|---|---|---|
| `grebeshok105/Photon` (this) | `1.21.1-fabric-2.2` | the mod itself |
| `grebeshok105/LDLib2` | `fabric-1.21.1` | UI/library dep — vendored into `libs/` as a jar |
| `grebeshok105/KilaGraph` | `fabric-1.21.1` | node-graph dep — vendored into `libs/` AND nested inside the photon jar (`META-INF/jars`) |
| `Low-Drag-MC/Photon`, `Low-Drag-MC/LDLib2`, `Low-Drag-MC/KilaGraph` | `1.21` | upstream NeoForge sources — parity baseline |
| `ELBGG/*` | `*-fabric` | older fabric ports — reference only |

Changing LDLib2 or KilaGraph requires rebuilding its jar and re-vendoring it here — see §4.

## 3. Port invariants (hard rules)

- **Upstream parity first.** Any deviation from upstream `1.21` must have a concrete Fabric-related reason. No behavior simplification "to make the port easier". No re-adding architecture upstream already replaced. No blind merges — port changes deliberately.
- **Fork-only systems stay out** until actually needed (custom light system, legacy PostChain, Veil bridge, TachyonExporter — intentionally not ported).
- Fabric seams use the smallest possible API/mixin surface; mixins stay narrow, `@Unique` members, `defaultRequire=1`.
- `fabric.mod.json`: `environment: "*"` (dedicated-server side must keep working), `depends` uses **fabric version predicates** (`>=2.7`), never Maven intervals (`[2.7,)` parses as a literal string and fails resolution).
- Lang files: `en_us.json` and `ru_ru.json`/`zh_cn.json`/`pt_br.json` are edited together when adding keys.
- Bug classification is mandatory: a bug reproducible on upstream is an **upstream issue** — document it, never mask it with a fabric-specific hack. Fix only real port regressions.

## 4. Build & environment

```bash
export JAVA_HOME=$HOME/.jdks/temurin-21    # Temurin JDK 21 — provisioned by the Devin repo blueprint
./gradlew build --no-daemon              # full build: jar + remapJar (prod artifact in build/libs/)
./gradlew runClient --no-daemon          # dev client (DISPLAY=:0 on the Devin VM)
```

- **Production artifact is `build/libs/` (remapJar)**, not `build/devlibs/` (named-mapped dev jar).
- **Vendored jars**: `libs/ldlib2-fabric-1.21.1-2.7.jar` + `libs/kilagraph-fabric-1.21.1-21.1.0.15.jar`, referenced via `modImplementation files(...)`; kilagraph is ALSO nested into the photon jar via `jar { from('libs/…jar') { into 'META-INF/jars' } }` — byte-for-byte, **never `zipTree`** (explodes the nested jar; classes leak to root while mod metadata strands).
- **After replacing a vendored jar or touching ldlib2/kilagraph sources**: rebuild that repo's `remapJar`, copy into `libs/` (update `build.gradle` if the filename changed), then purge `rm -rf .gradle/loom-cache/remapped_mods build/loom-cache/remapped_working` before building — loom caches remapped file-deps by filename and will silently keep stale code.
- Runtime deps a user needs alongside the jar: Fabric Loader ≥0.18.2, Fabric API 0.116.9+1.21.1, fabric-language-kotlin, architectury-api, forge-config-api-port (already declared in `depends`).

## 5. Known port quirks (do not re-debug)

- Mixin targets must never reference synthetic lambdas (`lambda$load$0`-style names) — intermediary maps them differently and prod apply fails. Anchor on named methods or wrap the enclosing call (see `WorldLoaderMixin`).
- Chat screens that run commands close themselves AND can fire a stale `setScreen(null)` a tick later — open screens from commands via `PhotonClientListeners.openScreenAfterTicks(screen, 2)`.
- `PayloadTypeRegistry` must register every clientbound payload S2C-side before `ClientPlayNetworking.registerGlobalReceiver` (upstream uses bidirectional).
- `modImplementation files('libs/x.jar')` does NOT put that jar's own `META-INF/jars` contents on the compile classpath — direct deps need explicit coordinates.
- Forge Config API Port is the intentional `net.neoforged.neoforge.common.ModConfigSpec` provider — residual neoforged imports in config code are by design.
- `pkill -f KnotClient` inside a shell command kills that command's own shell — use `pkill -f "[K]notClient"`, and never `&&`-chain after pkill.
- On the Devin VM, GL is llvmpipe software (~4 fps): timing-sensitive UI checks and frame-count assertions flake there — classify env flakes vs real bugs before reporting.

## 6. Verification

- `./gradlew build` green is necessary, never sufficient on its own.
- For jar-affecting changes, verify the prod jar: `unzip -l` shows exactly one intact `META-INF/jars/kilagraph-*.jar`, no `com/lowdragmc/kilagraph` root entries, no `neoforge` leftovers, `fabric.mod.json` has `environment:"*"` and correct `depends`.
- Runtime-affecting changes want a `runClient` smoke (boot to menu, mod resolution clean, zero mixin apply errors) and, where the change is user-visible, in-game verification **by the main agent only**.
- **Sub-agents never do in-game/UI verification** — no runClient checks, no screenshots/recordings. They implement, build, and report "unverified". The user (or the main agent, when delegated) verifies in game.
- KilaGraph gametests live in that repo (`runGameTestServer`, ~650 tests) — run them there when touching KilaGraph semantics.

## 7. SESSION.md — mandatory, append-only

`SESSION.md` at the repo root is the canonical cross-session handoff.

- **At the start of every work session, read it first** — it carries where the port stands and what's next.
- **Append a new entry at the end of every session** (or substantial milestone): date + session name/id (e.g. `2026-10-04 — devin-dbd624cb…`), what was done, key decisions and why, verification performed, open items, next steps.
- **Never rewrite or trim old entries yourself** — append only. When the file approaches ~500 lines, **suggest to the user that they let you clean/compact it** — the user decides.
- Commit `SESSION.md` with the work — nothing important may live only in chat, memory, or the local machine.

## 8. Git workflow

- Work lands on `1.21.1-fabric-2.2` (or a `devin/…` task branch off it when asked for PRs).
- Conventional commits, imperative mood, one logical change each (e.g. `fix: …`, `build: …`).
- `git fetch && git rebase` before push; retry once on rejection; **never force-push**; never push to upstream's branches.
- `libs/` jars are git-tracked despite the gitignore — stage them with `git add -f`; deletions of tracked-but-ignored files need `git rm --cached` or `git add` on the exact path.

## 9. Context map

- `AGENTS.md` — this file (project contract).
- `SESSION.md` — append-only cross-session handoff (read first).
- `upstream/1.21` git ref — parity baseline for any "is this ours or upstream's" question; `git diff upstream/1.21 -- <path>` answers it.
- Devin environment blueprint (org/repo settings) — provisions Temurin JDK 21 into `$HOME/.jdks/temurin-21` on every VM; if `java` is wrong, check `JAVA_HOME` first.

Authority when sources disagree: current code + passing tests → this file → `SESSION.md` → upstream reference ports.
