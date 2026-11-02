-- Ensure historical students also have a zero-balance hour account.
-- Hours must subsequently be added through the administrator recharge flow,
-- which creates the corresponding hour transaction.
INSERT INTO student_hour_account (
    student_id,
    total_recharged,
    total_consumed,
    total_refunded,
    remaining_hours,
    paid_hours,
    gift_hours,
    experience_hours,
    activity_hours,
    version
)
SELECT
    s.id,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0
FROM student s
LEFT JOIN student_hour_account a ON a.student_id = s.id
WHERE a.id IS NULL;
