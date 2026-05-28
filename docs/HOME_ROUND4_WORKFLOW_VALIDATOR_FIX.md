# HOME ROUND 4 — Workflow / Validator Fix

Fixes GitHub Actions failure where the workflow still ran `scripts/validate_home_round3.py`.

Changes:
- `.github/workflows/android-build.yml` now runs `python3 scripts/validate_home_round4.py`.
- `scripts/validate_home_round3.py` was made compatible with the current Round 4 model-disable implementation, so manual old validator runs do not fail on `Model disabled for non-exact brand`.
- No UI layout changes were made in this fix.

Test focus after build: same Home Round 4 UI tests — brand field, BYD model dropdown, banner, cards, footer.
