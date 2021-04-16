DECLARE
    Zahl1 number(2);
    Text varchar(20) := '${myText}';
BEGIN
    EXECUTE IMMEDIATE "
        select number_of_greetings into Zahl1 from ${tableName} where text='${myText}';"
END;
/