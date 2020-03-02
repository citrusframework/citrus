select NAME
from CUSTOMERS
where CUSTOMER_ID='${customerId}';

select COUNT(1) as overall_cnt
from ERRORS;

select ORDER_ID
from ORDERS
where DESCRIPTION LIKE 'Migrate%';

select DESCRIPTION
from ORDERS
where ORDER_ID = 2;