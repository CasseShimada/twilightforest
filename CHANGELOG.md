# Changelog

## [1.0.0-beta.1] - 2026-07-22

This Fabric 26.2 prerelease repairs the special-item resource pipeline, makes custom block-model
rendering lifecycle-safe, and replaces ambiguous Twilight Forest return-portal lookups with exact,
persistent portal-shape links.

### Fixed

- Migrated special item models to Minecraft 26.2 item definitions and special renderers, including
  the missing brazier definition, shield sprite lookup, skull-candle transforms, and stale pillar
  inventory texture reference.
- Made connected-texture, force-field, giant-block, and patch model parts immutable for each
  collection pass, and guaranteed renderer context cleanup after failures.
- Replaced X/Z-only return-portal cache aliases with dimension-aware canonical anchors, shape
  fingerprints, exact bidirectional links, atomic invalidation, and safe legacy-cache migration.
- Bounded portal discovery to loaded chunks and removed the synchronous full-height 200-block
  volume sweep that could stall the server thread.

### Compatibility

- Added server-authoritative Diggus Maximus API 1 policy hooks and data tags, with per-candidate
  progression, ownership, block-entity, portal, and Giant Pick safety checks.
- Added server-authoritative Carry On API 1 pickup/place/stack policy, explicit safe and unsafe
  matrices, ownership checks, and hard relocation tags.
- Added native WTHIT 20 common/client plugins for Drying Rack progress, Quest Ram missing wool,
  and creative-only Chiseled Canopy Bookshelf spawner data while preserving Jade support.
- Kept all three integrations optional and verified that no optional API or runtime implementation
  classes are bundled in either release archive.

### Verification

- Added final-resource-union graph tests, custom-model performance contracts, portal persistence and
  migration tests, optional-compat policy tests, and an isolated real-client/dedicated-server
  compatibility matrix harness.
- Added automated handling of Minecraft's experimental-world confirmation through the real GUI
  mouse-input path so unattended client tests cannot stop at the warning screen.
