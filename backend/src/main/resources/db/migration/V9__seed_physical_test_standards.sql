-- V9: 体测7项+五级评分标准种子数据

-- 1. 插入7项体测指标
INSERT INTO physical_test_item (item_code, item_name, item_type, unit, gender_specific, age_min, age_max, description, status, sort_order, created_by, updated_by) VALUES
('HEIGHT', '身高', 'HEIGHT', '厘米', 1, 3, 6, '幼儿身高测量', 'ENABLED', 1, 1, 1),
('WEIGHT', '体重', 'WEIGHT', '千克', 1, 3, 6, '幼儿体重测量', 'ENABLED', 2, 1, 1),
('TENNIS_THROW', '网球掷远', 'POWER', '米', 1, 3, 6, '网球掷远测试', 'ENABLED', 3, 1, 1),
('BOTH_FEET_JUMP', '双脚连续跳', 'SKILL', '秒', 1, 3, 6, '双脚连续跳测试', 'ENABLED', 4, 1, 1),
('SHUTTLE_RUN', '10米折返跑', 'SPEED', '秒', 1, 3, 6, '10米折返跑测试', 'ENABLED', 5, 1, 1),
('STANDING_LONG_JUMP', '立定跳远', 'POWER', '厘米', 1, 3, 6, '立定跳远测试', 'ENABLED', 6, 1, 1),
('SIT_AND_REACH', '坐位体前屈', 'FLEXIBILITY', '厘米', 1, 3, 6, '坐位体前屈测试', 'ENABLED', 7, 1, 1),
('BALANCE_BEAM', '走平衡木', 'SKILL', '秒', 1, 3, 6, '走平衡木测试', 'ENABLED', 8, 1, 1)
ON DUPLICATE KEY UPDATE item_name=VALUES(item_name);

-- 2. 插入默认体测模板
INSERT INTO physical_test_template (template_name, template_code, target_gender, min_age, max_age, description, status, created_by, updated_by) VALUES
('3~6岁幼儿体质测评模板', 'TPL_3TO6', 'ALL', 3, 6, '3~6岁幼儿体质测评标准模板', 'ACTIVE', 1, 1)
ON DUPLICATE KEY UPDATE template_name=VALUES(template_name);

-- 3. 模板关联8项指标
INSERT INTO physical_template_item (template_id, item_id, required, sort_order)
SELECT t.id, i.id, 1, i.sort_order
FROM physical_test_template t
CROSS JOIN physical_test_item i
WHERE t.template_code = 'TPL_3TO6'
ON DUPLICATE KEY UPDATE sort_order=VALUES(sort_order);

-- 4. 五级评分标准（基于Excel数据）
-- 等级：TERRIBLE(很差,1分)、INADEQUATE(较差,2分)、PASS(合格,3分)、GOOD(良好,4分)、EXCELLENT(优秀,5分)

-- 身高 - 男
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3', 0, 91.19, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3', 91.2, 95.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3', 95.5, 99.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3', 99.4, 104.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3', 104.2, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3.5', 0, 94.09, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3.5', 94.1, 98.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3.5', 98.3, 102.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3.5', 102.1, 106.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '3.5', 107.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4', 0, 97.49, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4', 97.5, 101.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4', 102.0, 105.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4', 105.5, 110.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4', 110.5, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4.5', 0, 99.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4.5', 100.0, 104.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4.5', 104.7, 108.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4.5', 108.5, 113.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '4.5', 113.2, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5', 0, 103.09, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5', 103.1, 107.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5', 107.9, 111.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5', 112.0, 116.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5', 117.0, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 身高 - 女
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3', 0, 89.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3', 90.0, 94.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3', 94.7, 98.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3', 98.1, 103.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3', 103.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3.5', 0, 92.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3.5', 93.0, 97.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3.5', 97.6, 101.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3.5', 101.2, 105.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '3.5', 105.6, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4', 0, 96.59, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4', 96.6, 100.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4', 101.0, 104.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4', 104.5, 108.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4', 109.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4.5', 0, 98.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4.5', 99.0, 103.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4.5', 103.7, 107.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4.5', 107.4, 111.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '4.5', 112.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5', 0, 101.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5', 102.0, 106.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5', 106.6, 110.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5', 110.5, 115.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5', 115.5, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5.5', 0, 104.49, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5.5', 104.5, 109.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5.5', 109.3, 113.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5.5', 113.5, 118.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '5.5', 118.5, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '6', 0, 106.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '6', 107.0, 111.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '6', 112.0, 116.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '6', 116.7, 121.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'FEMALE', '6', 121.8, 999, 5, 'EXCELLENT', 5.0, 1, 1);
