# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/10 (40.0%)
- **Function parity:** 45/249 matched (target 55) — 18.1%
- **Class/type parity:** 12/27 matched (target 21) — 44.4%
- **Combined symbol parity:** 57/276 matched (target 76) — 20.7%
- **Average inline-code cosine:** 0.47 (function body across 4 matched files)
- **Average documentation cosine:** 0.94 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `keyring.Error`
- **Similarity:** 0.35
- **Dependents:** 2
- **Priority Score:** 2000606.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 2. credential

- **Target:** `keyring.Credential`
- **Similarity:** 0.58
- **Dependents:** 1
- **Priority Score:** 1001204.1
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched
- **Missing types:** _none_

### 3. lib

- **Target:** `keyring.Entry`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 153006.3
- **Functions:** 13/28 matched (target 15)
- **Missing functions:** `entry_from_constructor`, `entry_from_constructor_and_attributes`, `test_round_trip`, `test_round_trip_secret`, `generate_random_string_of_len`, `generate_random_string`, `generate_random_bytes_of_len`, `test_empty_service_and_user`, `test_missing_entry`, `test_empty_password`, `test_round_trip_ascii_password`, `test_round_trip_non_ascii_password`, `test_round_trip_random_secret`, `test_update`, `test_noop_get_update_attributes`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 4. mock

- **Target:** `keyring.Mock`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 22604.2
- **Functions:** 21/23 matched (target 29)
- **Missing functions:** `default`, `entry_new`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 9/10 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

