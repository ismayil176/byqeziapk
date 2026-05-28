-- Optional helper index for Google Play Billing promotion purchases.
-- The endpoint stores purchase_token in promotion_payments.epoint_transaction
-- and Google order id in bank_transaction to avoid schema-breaking changes.
CREATE INDEX IF NOT EXISTS idx_promotion_payments_google_token ON promotion_payments(epoint_transaction);
CREATE INDEX IF NOT EXISTS idx_promotion_payments_google_order ON promotion_payments(bank_transaction);
