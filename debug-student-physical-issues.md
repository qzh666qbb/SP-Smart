# Debug Session: student-physical-issues

Status: [OPEN]

## Symptoms
- Admin/system cannot normally input/create student records.
- Physical test record deletion shows success in the dialog/toast, but the record still exists afterward.

## Environment
- Local workspace: `/Users/bytedance/SP-Smart`
- Remote server hint from user: `123.56.82.49`, `workbench connect -i i-2zejbvnux3ujnwkwh4vr`

## Hypotheses
1. Frontend write API fallback/mock path reports success even when the backend request did not delete or create data.
2. Backend physical delete performs soft delete but the list query does not filter `deleted_at`, so the record appears after deletion.
3. The delete call sends an incorrect ID or hits a route that does not map to the physical record being listed.
4. Student creation fails because admin form payload and backend DTO validation/schema are inconsistent.
5. Remote deployment is not running the same build or database migrations as the local workspace.

## Evidence Log
- 2026-10-10 remote service: `training-backend` is active and running `/opt/training/backend/app.jar`.
- Backend log contains successful delete entries for physical record `recordId=2`:
  `PhysicalTestApplicationService : 删除体测记录: recordId=2`
- Remote DB evidence:
  `physical_test_record` has `total=2, active=2, deleted=0`; rows `id=1` and `id=2` both have `deleted_at=NULL`.
  This confirms the delete path returned success but did not persist `deleted_at`.
- Local code evidence:
  `PhysicalTestApplicationService.deleteTestRecord` sets `record.deletedAt`, but `PhysicalTestRepositoryImpl.toRecordEntity` and `toRecordModel` do not map `deletedAt`.
  Query methods also do not filter `deleted_at IS NULL`.
- Student creation log evidence:
  repeated `Duplicate entry '001' for key 'uk_student_no'` on `/business/students`.
- Remote DB evidence:
  `student.id=1, student_no=001` is already soft-deleted (`deleted_at=2026-10-10 13:11:48`) but still occupies the unique key.

## Next Steps
1. [DONE] Map `deletedAt` in physical repository entity/model conversions and filter deleted records from read queries.
2. [DONE] On student soft delete, rename `studentNo` to a tombstone value to release the unique key.
3. [DONE] Add Flyway migration V21 to remediate existing soft-deleted student rows.
4. [DONE] Build backend with JDK 17, upload jar, backup remote jar, restart `training-backend`.

## Fix Applied
- `PhysicalTestRecordEntity`: added `deletedAt`.
- `PhysicalTestRepositoryImpl`: mapped `deletedAt` both ways and added `deleted_at IS NULL` filters to record read methods.
- `StudentApplicationService.deleteStudent`: marks deleted students as `DEL_<id>_<oldNo>` before soft deletion.
- `V21__fix_soft_delete_visibility.sql`: adds physical `deleted_at` if absent and remediates soft-deleted student numbers.

## Post-Fix Evidence
- Backend package: `mvn -q -DskipTests package` succeeded with JDK 17.
- Remote deployment: `/opt/training/backend/app.jar` replaced after backup; `training-backend` restarted and is `active`.
- Flyway: schema migrated from v20 to v21 successfully.
- DB post-fix:
  - `student.id=1` changed from `student_no=001` to `student_no=DEL_1_001`, so `001` is available again.
  - `physical_test_record.id=2` compensated with `deleted_at=2026-10-10 15:27:11`.
  - `physical_test_record` count: `total=2`, `active=1`, `deleted=1`.

## Remaining Verification
- User should refresh admin web and retry:
  1. Add student with `studentNo=001`.
  2. Delete a physical record and confirm it disappears after refresh.
