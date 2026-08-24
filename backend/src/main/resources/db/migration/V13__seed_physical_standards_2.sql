-- V9 续: 网球掷远、双脚连续跳、10米折返跑、立定跳远、坐位体前屈、走平衡木

-- 网球掷远 - 男 (越高越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3', 0, 1.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3', 2.0, 2.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3', 3.0, 3.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3', 4.0, 5.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3', 5.6, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3.5', 0, 1.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3.5', 2.0, 2.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3.5', 3.0, 4.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3.5', 4.5, 5.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '3.5', 5.6, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4', 0, 2.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4', 3.0, 3.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4', 4.0, 4.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4', 5.0, 6.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4', 6.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4.5', 0, 2.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4.5', 3.0, 4.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4.5', 4.5, 6.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4.5', 6.5, 8.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '4.5', 8.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5', 0, 3.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5', 4.0, 5.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5', 5.5, 7.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5', 7.5, 9.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5', 9.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 网球掷远 - 女
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3', 0, 1.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3', 1.5, 2.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3', 2.5, 3.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3', 3.5, 5.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3', 5.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3.5', 0, 1.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3.5', 2.0, 2.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3.5', 3.0, 3.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3.5', 4.0, 5.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '3.5', 5.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4', 0, 2.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4', 2.5, 3.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4', 3.5, 4.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4', 4.5, 5.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4', 5.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4.5', 0, 2.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4.5', 2.5, 3.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4.5', 3.5, 4.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4.5', 4.5, 5.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '4.5', 5.6, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5', 0, 2.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5', 3.0, 4.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5', 4.5, 5.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5', 6.0, 7.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5', 7.6, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5.5', 0, 3.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5.5', 3.5, 4.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5.5', 5.0, 6.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5.5', 6.5, 8.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '5.5', 8.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '6', 0, 3.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '6', 3.5, 4.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '6', 5.0, 6.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '6', 6.5, 8.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'FEMALE', '6', 8.6, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 双脚连续跳 - 男 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3', 19.7, 25.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3', 13.1, 19.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3', 9.2, 13.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3', 6.6, 9.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3', 0, 6.5, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3.5', 17.0, 21.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3.5', 11.2, 16.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3.5', 8.3, 11.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3.5', 6.1, 8.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '3.5', 0, 6.0, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4', 13.2, 17.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4', 9.2, 13.1, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4', 7.1, 9.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4', 5.6, 7.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4', 0, 5.5, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4.5', 11.3, 14.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4.5', 8.2, 11.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4.5', 6.5, 8.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4.5', 5.3, 6.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '4.5', 0, 5.2, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5', 9.9, 12.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5', 7.3, 9.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5', 6.0, 7.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5', 5.1, 5.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5', 0, 5.0, 5, 'EXCELLENT', 5.0, 1, 1);

-- 双脚连续跳 - 女
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3', 20.1, 25.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3', 13.5, 20.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3', 9.8, 13.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3', 7.1, 9.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3', 0, 7.0, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3.5', 17.1, 21.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3.5', 11.3, 17.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3.5', 8.5, 11.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3.5', 6.2, 8.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '3.5', 0, 6.1, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4', 13.5, 17.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4', 9.6, 13.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4', 7.4, 9.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4', 5.9, 7.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4', 0, 5.8, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4.5', 12.0, 14.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4.5', 8.6, 11.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4.5', 6.8, 8.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4.5', 5.5, 6.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '4.5', 0, 5.4, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5', 10.1, 12.7, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5', 7.6, 10.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5', 6.2, 7.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5', 5.2, 6.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5', 0, 5.1, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5.5', 9.3, 11.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5.5', 7.0, 9.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5.5', 5.8, 6.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5.5', 4.9, 5.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '5.5', 0, 4.8, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '6', 8.4, 10.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '6', 6.3, 8.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '6', 5.3, 6.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '6', 4.6, 5.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'FEMALE', '6', 0, 4.5, 5, 'EXCELLENT', 5.0, 1, 1);

-- 10米折返跑 - 男 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3', 12.9, 15.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3', 10.3, 12.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3', 9.1, 10.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3', 8.0, 9.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3', 0, 7.9, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3.5', 11.4, 14.0, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3.5', 9.5, 11.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3.5', 8.4, 9.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3.5', 7.5, 8.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '3.5', 0, 7.4, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4', 10.2, 12.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4', 8.6, 10.1, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4', 7.7, 8.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4', 6.9, 7.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4', 0, 6.8, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4.5', 9.8, 11.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4.5', 8.1, 9.7, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4.5', 7.3, 8.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4.5', 6.7, 7.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '4.5', 0, 6.6, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5', 9.0, 10.3, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5', 7.7, 8.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5', 7.0, 7.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5', 6.4, 6.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5', 0, 6.3, 5, 'EXCELLENT', 5.0, 1, 1);

-- 10米折返跑 - 女
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3', 13.5, 16.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3', 10.6, 13.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3', 9.4, 10.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3', 8.2, 9.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3', 0, 8.1, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3.5', 12.1, 14.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3.5', 9.8, 12.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3.5', 8.7, 9.7, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3.5', 7.7, 8.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '3.5', 0, 7.6, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4', 10.9, 13.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4', 9.1, 10.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4', 8.1, 9.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4', 7.2, 8.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4', 0, 7.1, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4.5', 10.3, 12.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4.5', 8.6, 10.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4.5', 7.7, 8.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4.5', 7.0, 7.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '4.5', 0, 6.9, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5', 9.7, 11.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5', 8.1, 9.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5', 7.3, 8.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5', 6.7, 7.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5', 0, 6.6, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5.5', 9.1, 10.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5.5', 7.7, 9.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5.5', 7.0, 7.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5.5', 6.4, 6.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '5.5', 0, 6.3, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '6', 8.6, 10.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '6', 7.3, 8.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '6', 6.6, 7.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '6', 6.1, 6.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'FEMALE', '6', 0, 6.0, 5, 'EXCELLENT', 5.0, 1, 1);

-- 立定跳远 - 男 (越远越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3', 0, 29, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3', 30, 42, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3', 43, 58, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3', 59, 76, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3', 77, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3.5', 0, 34, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3.5', 35, 52, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3.5', 53, 69, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3.5', 70, 84, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '3.5', 85, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4', 0, 46, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4', 47, 64, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4', 65, 79, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4', 80, 95, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4', 96, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4.5', 0, 54, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4.5', 55, 72, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4.5', 73, 88, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4.5', 89, 102, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '4.5', 103, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5', 0, 64, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5', 65, 79, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5', 80, 95, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5', 96, 110, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5', 111, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 立定跳远 - 女
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3', 0, 28, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3', 29, 39, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3', 40, 54, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3', 55, 71, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3', 72, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3.5', 0, 33, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3.5', 34, 49, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3.5', 50, 64, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3.5', 65, 81, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '3.5', 82, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4', 0, 43, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4', 44, 59, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4', 60, 73, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4', 74, 89, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4', 90, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4.5', 0, 49, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4.5', 50, 67, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4.5', 68, 80, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4.5', 81, 96, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '4.5', 97, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5', 0, 59, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5', 60, 74, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5', 75, 88, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5', 89, 102, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5', 103, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5.5', 0, 65, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5.5', 66, 81, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5.5', 82, 95, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5.5', 96, 109, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '5.5', 110, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '6', 0, 70, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '6', 71, 86, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '6', 87, 100, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '6', 101, 116, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'FEMALE', '6', 117, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 坐位体前屈 - 男 (数值越大越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3', 0, 4.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3', 4.9, 8.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3', 8.6, 11.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3', 11.7, 14.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3', 15.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3.5', 0, 4.6, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3.5', 4.7, 8.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3.5', 8.5, 11.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3.5', 11.6, 14.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '3.5', 15.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4', 0, 4.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4', 4.5, 8.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4', 8.5, 11.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4', 11.5, 14.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4', 15.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4.5', 0, 4.1, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4.5', 4.2, 7.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4.5', 8.0, 10.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4.5', 11.0, 14.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '4.5', 14.5, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5', 0, 3.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5', 3.5, 7.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5', 7.6, 10.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5', 11.0, 14.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5', 14.5, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 坐位体前屈 - 女
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3', 0, 6.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3', 6.3, 9.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3', 10.0, 12.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3', 13.0, 15.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3', 16.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3.5', 0, 6.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3.5', 6.3, 9.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3.5', 10.0, 12.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3.5', 13.0, 15.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '3.5', 16.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4', 0, 5.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4', 6.0, 9.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4', 10.0, 12.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4', 13.0, 15.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4', 16.0, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4.5', 0, 5.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4.5', 6.0, 9.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4.5', 10.0, 12.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4.5', 13.0, 16.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '4.5', 16.1, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5', 0, 5.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5', 5.5, 9.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5', 9.7, 13.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5', 13.2, 16.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5', 16.7, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5.5', 0, 5.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5.5', 5.5, 9.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5.5', 9.7, 12.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5.5', 13.0, 16.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '5.5', 16.8, 999, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '6', 0, 5.3, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '6', 5.4, 9.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '6', 9.6, 12.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '6', 13.0, 16.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'FEMALE', '6', 16.8, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 男 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3', 30.1, 48.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3', 16.9, 30.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3', 10.6, 16.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3', 6.6, 10.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3', 0, 6.5, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3.5', 32.5, 49.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3.5', 17.4, 32.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3.5', 10.8, 17.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3.5', 5.9, 9.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '3.5', 0, 5.8, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4', 21.6, 33.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4', 11.6, 21.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4', 7.4, 11.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4', 4.9, 7.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4', 0, 4.8, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4.5', 17.9, 28.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4.5', 9.9, 17.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4.5', 6.3, 9.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4.5', 4.3, 6.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '4.5', 0, 4.2, 5, 'EXCELLENT', 5.0, 1, 1),

((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5', 14.1, 22.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5', 7.9, 14.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5', 5.3, 7.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5', 3.7, 5.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5', 0, 3.6, 5, 'EXCELLENT', 5.0, 1, 1);
