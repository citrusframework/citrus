-- clean up database
DELETE FROM CUSTOMERS;
DELETE FROM ORDERS;

-- insert data
INSERT INTO 
CUSTOMERS 
VALUES (1, 'Christoph', 'VIP Customer');

INSERT INTO ORDERS VALUES(1, 'requestTag', 'conversationId', 'creation_date', 'Migrate');
INSERT INTO ORDERS VALUES(2, 'requestTag', 'conversationId', 'creation_date', NULL);
COMMIT;