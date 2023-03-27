--Test SQL statements with variables
DELETE * FROM ERRORS WHERE STATUS='${resolvedStatus}';
DELETE * FROM CONFIGURATION WHERE VERSION=${version};