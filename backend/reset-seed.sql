-- ============================================================
-- Reset seed data (Solo lo generado por SalesInitializer)
-- Orden seguro respetando FK: hijos → padres
-- ============================================================

BEGIN;

-- 1. Eliminar datos de tablas que dependen de otras (hojas)
DELETE FROM stock_notes;
DELETE FROM audits;
DELETE FROM order_details;
DELETE FROM supplies;
DELETE FROM payments;

-- 2. Eliminar tablas que dependen de clients / users / products
DELETE FROM orders;

-- 3. Eliminar tablas padre (sin dependencias FK)
DELETE FROM clients;

-- 4. Resetear secuencias IDENTITY para que empiece desde 1
ALTER SEQUENCE clients_id_seq RESTART WITH 1;
ALTER SEQUENCE orders_id_seq RESTART WITH 1;
ALTER SEQUENCE order_details_id_seq RESTART WITH 1;
ALTER SEQUENCE supplies_id_seq RESTART WITH 1;
ALTER SEQUENCE payments_id_seq RESTART WITH 1;
ALTER SEQUENCE stock_notes_id_seq RESTART WITH 1;
ALTER SEQUENCE audits_id_seq RESTART WITH 1;

COMMIT;
