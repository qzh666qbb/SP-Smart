-- 早期代码曾把 user_id 写入 parent_id。created_by 可明确识别该类记录。
-- 先删除已经存在规范关系的重复旧记录，避免更新时触发唯一键冲突。
DELETE legacy
FROM parent_student_relation legacy
JOIN parent_profile pp
  ON pp.user_id = legacy.parent_id
 AND pp.user_id = legacy.created_by
 AND pp.deleted_at IS NULL
JOIN parent_student_relation canonical
  ON canonical.parent_id = pp.id
 AND canonical.student_id = legacy.student_id
 AND canonical.id <> legacy.id;

UPDATE parent_student_relation legacy
JOIN parent_profile pp
  ON pp.user_id = legacy.parent_id
 AND pp.user_id = legacy.created_by
 AND pp.deleted_at IS NULL
SET legacy.parent_id = pp.id
WHERE legacy.parent_id <> pp.id;
