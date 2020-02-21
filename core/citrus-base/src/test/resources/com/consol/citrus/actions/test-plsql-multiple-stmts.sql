DECLARE
    Zahl1 number(2);
    Text varchar(20) := 'Hello World!';
BEGIN
    EXECUTE IMMEDIATE "
        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';"
END;
/
DECLARE
    Zahl1 number(2);
    Text varchar(20) := 'Hello World!';
BEGIN
    EXECUTE IMMEDIATE "
        select number_of_greetings into Zahl1 from Greetings where text='Hello World!';"
END;
/