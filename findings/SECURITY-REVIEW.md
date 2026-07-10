# Security review — automatic-content-tags

Methodology: aligned with the internal `jahia-security-scan` audit process — deterministic
scanner layer (gitleaks, Semgrep, syft SBOM, osv-scanner) followed by a manual adversarial
pass with a reachability gate. Evidence levels follow the same convention:
✅ live-confirmed · ⚠️ statically-confirmed · ❌ not exploitable.

Date: 2026-07-10 · Scope: this repository (v1.0.0-SNAPSHOT) and its predecessor
`smonier/anthropic-tags`. Raw scanner output: [`findings/scans/`](scans/).

---

## ACT-SEC-001 — Live Anthropic API key committed in predecessor repo — ✅ confirmed, remediated

- **CVSS 3.1**: 7.5 (AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N)
- **Where**: `anthropic-tags` repo, `src/main/resources/META-INF/configurations/org.jahia.se.modules.anthropic.cfg`,
  present since the initial commit and pushed to GitHub.
- **Root cause**: the default OSGi config was committed with a real `sk-ant-api03-…` key instead of a placeholder.
- **Evidence**: key read directly from the file and from git history (`d26942b`, `e99c474`).
  Notably **gitleaks (default ruleset) does not detect this key** in either working tree or
  history (`scans/gitleaks-legacy-repo.json` is empty) — the `sk-ant-api03-` prefix is not
  covered by a default rule. Do not rely on gitleaks alone for Anthropic keys.
- **Remediation**: key revoked in the Anthropic console (2026-07-10); this repository was
  started with fresh history; the shipped `.cfg` now contains empty keys and a warning.
  The old GitHub repository should be archived or deleted.

## ACT-SEC-002 — System-session content read in predecessor — ✅ confirmed, remediated

- **CVSS 3.1**: 4.3 (AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N)
- **Where**: `AnthropicServiceImpl.getTextFromNode` / `generateAutoTags` used
  `doExecuteWithSystemSessionAsUser(null, …)` to read node content, bypassing the caller's
  read permissions (any path could be passed to the public OSGi service).
- **Remediation**: `ContentTagsService.generateTags(JCRNodeWrapper, String)` now takes the
  node resolved from the caller's own session (the render action enforces
  `jcr:write_default` on it). No system session anywhere in the module.

## ACT-SEC-003 — GET allowed on the tag-generation action in predecessor — ✅ confirmed, remediated

- **Where**: `AnthropicAction` accepted `GET,POST` while being CSRF-whitelisted, so a simple
  cross-site `<img>`/link could trigger LLM calls with the victim's session (token-burn /
  cost abuse; no persistent write).
- **Remediation**: `GenerateContentTagsAction` is POST-only, still requires an authenticated
  user with write permission on the node, and returns 400 without a `tagLanguage`.

## ACT-SEC-004 — CSRF-guard whitelist on the action — ⚠️ accepted residual risk

- **Where**: `org.jahia.modules.jahiacsrfguard-automatic-content-tags.cfg` whitelists
  `*.generateContentTagsAction.do` (the Content Editor dialog `fetch` does not carry a
  CSRF token — this is the standard Jahia module pattern).
- **Residual risk**: a cross-site form POST could trigger tag generation with the victim's
  cookie (cost abuse only: the response cannot be read cross-origin, and tags are only
  written client-side through the editor form, which is CSRF-protected). Modern SameSite
  cookie defaults block this in practice.
- **Decision**: accepted; mitigations are POST-only + authentication + per-node write
  permission + `llm.max.source.chars` capping request size.

## ACT-SEC-005 — Prompt injection via content text — ⚠️ statically confirmed, low

- Content authors control the text sent to the model, so they can inject instructions and
  influence the returned tags. Tags are stored as plain strings in `j:tagList` and rendered
  escaped by Jahia UIs; parser caps output at 20 tags of 64 chars. Impact limited to tag
  content the same author could set by hand. No action needed beyond the existing caps.

## ACT-SEC-006 — SSRF surface — ❌ not exploitable

- Provider base URLs come exclusively from the admin-managed OSGi configuration; nothing
  from the HTTP request reaches the URL. The JDK HTTP client is configured to never follow
  redirects, so a misconfigured provider URL cannot be bounced elsewhere.

## Dependency scan (syft + osv-scanner, 2026-07-10)

- **Java (bundle runtime)**: only `jsoup 1.18.3` is embedded — no known CVEs (predecessor
  shipped 1.13.1: CVE-2021-37714, CVE-2022-36033 — fixed by the upgrade). The Anthropic
  Java SDK and its transitive okhttp/kotlin stack were removed entirely.
- **JS**: `axios` (23 advisories) was an unused devDependency — removed. Remaining
  osv-scanner hits (`scans/osv.json`) are build-time toolchain packages (webpack, babel,
  eslint chain) that are not shipped in the Module Federation bundle; `lodash 4.17.21` is
  the latest release with no fixed version available upstream. Re-scan on dependency bumps.

## Scanner layer summary

| Tool | Result |
|---|---|
| gitleaks (this repo) | 0 findings |
| gitleaks (legacy repo history) | 0 findings — **false negative** on the real key (ACT-SEC-001) |
| semgrep `--config auto` (317 rules) | 0 findings |
| osv-scanner | 0 runtime-reachable vulnerabilities after remediation |
| syft | SBOM at `scans/sbom.cdx.json`; Maven build also emits CycloneDX (`java-bom.cdx.json`) |
