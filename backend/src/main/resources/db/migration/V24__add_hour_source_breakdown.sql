ALTER TABLE hour_transaction
    ADD COLUMN source_breakdown VARCHAR(255) NULL COMMENT '消耗课时来源明细，例如 EXPERIENCE=1.00;PAID=0.50' AFTER hour_source;
