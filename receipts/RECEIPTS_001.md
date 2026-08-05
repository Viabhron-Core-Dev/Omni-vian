# Receipts Log

2026-08-05T09:14:35-07:00
* Requested: Add OmniRoute's Node.js binary and dependency requirements to the blueprint phase where OmniRoute is built.
* Files touched: `/BLUEPRINT.md`
* Action: Added sub-bullets under Phase 4 detailing the requirement for a pre-compiled Node.js binary (v20.20.2+, arm64-v8a) and the fallback configuration needed for `better-sqlite3` to use a pure JavaScript engine (`node:sqlite` or `sql.js`) to avoid native build tool requirements. `bcryptjs` was noted as pure JS and safe.
* Verification: Not tested. (Documentation update only).
* Deviation: None.
* Known issue/Follow-up: Need to source the correct `arm64-v8a` Node.js binary during implementation.
