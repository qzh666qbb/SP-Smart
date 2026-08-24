-- V9c: 补充缺失年龄段体测评分标准 + 体重(BMI)评分标准

-- ================================
-- 一、男5.5岁 补充：身高/网球掷远/双脚连续跳/10米折返跑/立定跳远/坐位体前屈/走平衡木
-- ================================

-- 身高 - 男 5.5岁 (越高越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5.5', 0, 105.69, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5.5', 105.7, 110.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5.5', 110.3, 114.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5.5', 114.6, 119.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '5.5', 119.7, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 网球掷远 - 男 5.5岁 (越远越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5.5', 0, 2.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5.5', 3.0, 4.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5.5', 4.6, 6.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5.5', 6.1, 8.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '5.5', 8.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 双脚连续跳 - 男 5.5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5.5', 8.71, 10.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5.5', 6.5, 8.7, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5.5', 5.3, 6.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5.5', 4.4, 5.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '5.5', 0, 4.39, 5, 'EXCELLENT', 5.0, 1, 1);

-- 10米折返跑 - 男 5.5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5.5', 8.81, 10.1, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5.5', 7.4, 8.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5.5', 6.7, 7.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5.5', 6.2, 6.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '5.5', 0, 6.19, 5, 'EXCELLENT', 5.0, 1, 1);

-- 立定跳远 - 男 5.5岁 (越远越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5.5', 0, 65, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5.5', 66, 81, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5.5', 82, 95, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5.5', 96, 109, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '5.5', 110, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 坐位体前屈 - 男 5.5岁 (数值越大越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5.5', 0, 5.09, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5.5', 5.1, 8.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5.5', 8.4, 11.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5.5', 11.7, 14.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '5.5', 15.0, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 男 5.5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5.5', 9.51, 11.8, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5.5', 6.4, 9.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5.5', 4.6, 6.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5.5', 3.8, 4.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '5.5', 0, 3.79, 5, 'EXCELLENT', 5.0, 1, 1);

-- ================================
-- 二、男6岁 补充：身高/网球掷远/双脚连续跳/10米折返跑/立定跳远/坐位体前屈/走平衡木
-- ================================

-- 身高 - 男 6岁 (越高越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '6', 0, 107.19, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '6', 107.2, 111.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '6', 112.0, 116.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '6', 116.2, 121.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='HEIGHT'), 'MALE', '6', 121.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 网球掷远 - 男 6岁 (越远越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '6', 0, 3.49, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '6', 3.5, 5.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '6', 5.1, 7.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '6', 7.1, 9.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='TENNIS_THROW'), 'MALE', '6', 9.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 双脚连续跳 - 男 6岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '6', 8.71, 10.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '6', 6.6, 8.7, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '6', 5.4, 6.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '6', 4.7, 5.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BOTH_FEET_JUMP'), 'MALE', '6', 0, 4.69, 5, 'EXCELLENT', 5.0, 1, 1);

-- 10米折返跑 - 男 6岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '6', 8.61, 9.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '6', 7.2, 8.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '6', 6.5, 7.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '6', 5.9, 6.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SHUTTLE_RUN'), 'MALE', '6', 0, 5.89, 5, 'EXCELLENT', 5.0, 1, 1);

-- 立定跳远 - 男 6岁 (越远越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '6', 0, 70, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '6', 71, 88, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '6', 89, 102, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '6', 103, 118, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='STANDING_LONG_JUMP'), 'MALE', '6', 119, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 坐位体前屈 - 男 6岁 (数值越大越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '6', 0, 4.89, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '6', 4.9, 8.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '6', 9.0, 12.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '6', 12.5, 15.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='SIT_AND_REACH'), 'MALE', '6', 16.0, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 男 6岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '6', 8.41, 10.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '6', 6.3, 8.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '6', 5.3, 6.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '6', 4.6, 5.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'MALE', '6', 0, 4.59, 5, 'EXCELLENT', 5.0, 1, 1);

-- ================================
-- 三、女 走平衡木（所有年龄段：3 / 3.5 / 4 / 4.5 / 5 / 5.5 / 6）
-- ================================

-- 走平衡木 - 女 3岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3', 33.21, 51.2, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3', 18.4, 33.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3', 11.0, 18.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3', 6.8, 10.9, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3', 0, 6.79, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 女 3.5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3.5', 29.61, 47.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3.5', 15.4, 29.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3.5', 9.8, 15.3, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3.5', 5.9, 9.7, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '3.5', 0, 5.89, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 女 4岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4', 21.91, 35.5, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4', 12.6, 21.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4', 7.3, 12.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4', 4.7, 7.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4', 0, 4.69, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 女 4.5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4.5', 17.61, 27.6, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4.5', 10.5, 17.6, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4.5', 6.4, 10.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4.5', 4.0, 6.3, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '4.5', 0, 3.99, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 女 5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5', 14.41, 22.4, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5', 8.1, 14.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5', 5.2, 8.0, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5', 3.5, 5.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5', 0, 3.49, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 女 5.5岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5.5', 11.81, 17.9, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5.5', 6.9, 11.8, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5.5', 4.7, 6.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5.5', 3.3, 4.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '5.5', 0, 3.29, 5, 'EXCELLENT', 5.0, 1, 1);

-- 走平衡木 - 女 6岁 (时间越短越好)
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '6', 10.21, 15.6, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '6', 6.0, 10.2, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '6', 4.2, 5.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '6', 3.0, 4.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='BALANCE_BEAM'), 'FEMALE', '6', 0, 2.99, 5, 'EXCELLENT', 5.0, 1, 1);

-- ================================
-- 四、体重 WEIGHT 评分标准（按男女各年龄段，基于标准BMI区间估算）
--   等级：TERRIBLE(很瘦/很胖) / INADEQUATE(偏瘦/偏重) / PASS(正常) / GOOD(良好) / EXCELLENT(标准)
--   注：体重区间(kg)参考中国3-6岁儿童体重百分位标准（P3/P15/P50/P85/P97）
-- ================================

-- 体重 - 男 3岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3', 0, 11.49, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3', 11.5, 12.4, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3', 12.5, 15.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3', 16.0, 17.4, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3', 17.5, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 男 3.5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3.5', 0, 12.29, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3.5', 12.3, 13.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3.5', 13.4, 17.1, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3.5', 17.2, 18.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '3.5', 18.7, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 男 4岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4', 0, 13.19, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4', 13.2, 14.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4', 14.4, 18.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4', 18.5, 20.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4', 20.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 男 4.5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4.5', 0, 14.19, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4.5', 14.2, 15.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4.5', 15.4, 19.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4.5', 19.9, 21.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '4.5', 21.6, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 男 5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5', 0, 15.19, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5', 15.2, 16.5, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5', 16.6, 21.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5', 21.3, 23.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5', 23.2, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 男 5.5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5.5', 0, 16.29, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5.5', 16.3, 17.7, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5.5', 17.8, 22.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5.5', 22.9, 24.8, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '5.5', 24.9, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 男 6岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '6', 0, 17.39, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '6', 17.4, 18.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '6', 19.0, 24.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '6', 24.5, 26.6, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'MALE', '6', 26.7, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 3岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3', 0, 10.99, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3', 11.0, 12.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3', 12.1, 15.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3', 15.6, 17.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3', 17.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 3.5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3.5', 0, 11.89, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3.5', 11.9, 12.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3.5', 13.0, 16.6, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3.5', 16.7, 18.2, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '3.5', 18.3, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 4岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4', 0, 12.79, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4', 12.8, 13.9, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4', 14.0, 17.8, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4', 17.9, 19.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4', 19.6, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 4.5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4.5', 0, 13.79, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4.5', 13.8, 15.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4.5', 15.1, 19.2, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4.5', 19.3, 21.0, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '4.5', 21.1, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5', 0, 14.69, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5', 14.7, 16.0, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5', 16.1, 20.5, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5', 20.6, 22.5, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5', 22.6, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 5.5岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5.5', 0, 15.69, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5.5', 15.7, 17.1, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5.5', 17.2, 21.9, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5.5', 22.0, 24.1, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '5.5', 24.2, 999, 5, 'EXCELLENT', 5.0, 1, 1);

-- 体重 - 女 6岁
INSERT INTO physical_score_standard (item_id, gender, age_group, min_value, max_value, score, level, score_value, created_by, updated_by) VALUES
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '6', 0, 16.79, 1, 'TERRIBLE', 1.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '6', 16.8, 18.3, 2, 'INADEQUATE', 2.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '6', 18.4, 23.4, 3, 'PASS', 3.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '6', 23.5, 25.8, 4, 'GOOD', 4.0, 1, 1),
((SELECT id FROM physical_test_item WHERE item_code='WEIGHT'), 'FEMALE', '6', 25.9, 999, 5, 'EXCELLENT', 5.0, 1, 1);
