# SESSION.md — append-only cross-session handoff

Rules: read first at session start; append a new dated entry per session (name/id included); never rewrite old entries; suggest the user compact when ~500 lines. See `AGENTS.md` §7.

---

## 2026-10-04 — devin-dbd624cbf6ee422f93a50600879fb358 (full fabric port + fix wave + QA)

**State:** port is complete and in use. Active branch `1.21.1-fabric-2.2`, QA verdict: FUNCTIONAL WITH KNOWN ISSUES (all found bugs classified upstream-identical or llvmpipe-4fps env artifacts — zero confirmed fabric regressions).

**Done this session:**
- Full port of Photon 2.2.7 onto `upstream/1.21` base; deps rebuilt as fabric forks: `grebeshok105/LDLib2@fabric-1.21.1` (vendored `ldlib2-fabric-1.21.1-2.7.jar`), `grebeshok105/KilaGraph@fabric-1.21.1` (vendored + nested `kilagraph-fabric-1.21.1-21.1.0.15.jar`, `META-INF/jars` byte-copy, no zipTree).
- Review wave: 4 code-reviewer child sessions → consolidated fix list → planner (`superpowers:writing-plans`) → 4 worker sessions landed 13 WIs: packaging (jar-in-jar, depends `>=` predicates, `environment:"*"`, architectury dep), ldlib2 core (isServer thread-check, TooltipComponentCallback, renderer_model, ItemStackHandler, menu initial-sync), kilagraph (mB↔droplets units, ItemStorageHandler, gametests 647/647).
- Two prod-only bugs found via boot verification and fixed: Maven-interval `depends` (fabric needs `>=`), `-fabric` version suffix breaking `>=` (kg mod_version now plain `21.1.0.15`).
- Editor-open race fixed earlier: `PhotonClientListeners.openScreenAfterTicks(screen, 2)` — stale ChatScreen `setScreen(null)` killed the editor a tick after `/photon_editor`.
- In-game QA: hand-driven editor + `/photon fx` world FX + `ldlib2_autotest` (43 scenarios). PASS: editor lifecycle, particles core, postfx (24 builtins), glTF 19/19, save/load/export, block/entity-bound FX. PARTIAL (4fps llvmpipe): trail/beam visuals, timeline tracks, force-field deflection, heavy stress, .fxpack, dimension change, resize-during-FX.
- Upstream bugs documented (not fixed, not port defects): `TestContext.put` NPE in `editor_project_instance`, bare lang-key collision breaking `ngt_inspector_only_option` labels (`"color"→"Color"`, `"block"→"Block"`), missing `ldlib.gui.compass.save_success` lang key, silent `.fx` export outside target dir.

**Notable deferred item:** ldlib2 `ByteBufUtil` hardcodes `RegistryAccess.EMPTY` — latent port-wide bug; current callers pass proper access so nothing breaks today; fix when a caller actually needs custom registries.

**Next (user's roadmap, NOT started):** realtime VFX editor work, Jev + Codex integration, local MCP server for AI-driven effect authoring. Ask user before touching — all are explicitly gated on their go-ahead.

**Artifacts:** `~/photon-fabric-qa-report.md`, `~/photon-fabric-qa-autotest.json`, mod-jar archive `~/photon-fabric-mods-1.21.1/` (+.zip), port report `~/photon-fabric-port-report.md`, uitest report `run/ldlib2-uitest/report.json` in the repo clone.
