BEGIN
	EXECUTE IMMEDIATE 'create or replace function test (v_id in number) return number is
	  begin
	   if v_id  is null then
	    return 0;
	    end if;
	    return v_id;
	  end;';
END;
/