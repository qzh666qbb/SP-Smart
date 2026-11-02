INSERT INTO physical_test_batch
    (id, batch_name, batch_code, template_id, test_date, location, status, created_by, updated_by)
SELECT 1, '默认体测批次', 'DEFAULT-001', t.id, CURRENT_DATE, NULL, 'IN_PROGRESS', 1, 1
FROM physical_test_template t
WHERE t.status = 'ENABLED' AND t.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM physical_test_batch b WHERE b.id = 1)
ORDER BY t.id
LIMIT 1;
