-- Left-right

SET SEARCH_PATH TO parlgov;
drop table if exists q4 cascade;

-- You must not change this table definition.


CREATE TABLE q4(
        countryName VARCHAR(50),
        r0_2 INT,
        r2_4 INT,
        r4_6 INT,
        r6_8 INT,
        r8_10 INT
);


CREATE VIEW partywithA AS
SELECT party.id AS party_id, country_id, left_right
FROM party JOIN party_position ON party.id = party_id
WHERE left_right IS NOT NULL
ORDER BY country_id;




CREATE VIEW partywithrange AS
SELECT party_id, country_id, CASE
		WHEN (left_right >= 0 and left_right < 2)  THEN '[0,2)'
		WHEN (left_right >= 2 and left_right < 4)  THEN '[2,4)'
		WHEN (left_right >= 4 and left_right < 6)  THEN '[4,6)'
		WHEN (left_right >= 6 and left_right < 8)  THEN '[6,8)'
		WHEN left_right >= 8 THEN '[8,10]'
	END AS range
FROM partywithA
ORDER BY country_id, range;




CREATE VIEW partywithrangecount AS
SELECT country_id, range, COUNT(*) AS countOfRange
FROM partywithrange
GROUP BY country_id, range
ORDER BY country_id;



CREATE VIEW countrywithr0_2 AS
SELECT country_id, range AS range1, countofrange AS countofRange0
FROM partywithrangecount
WHERE range = '[0,2)'
ORDER BY country_id;



CREATE VIEW countrywithr2_4 AS
SELECT country_id, range AS range2,  countofrange AS countofrange1
FROM partywithrangecount
WHERE range = '[2,4)'
ORDER BY country_id;



CREATE VIEW countrywithr4_6 AS
SELECT country_id, range AS range3,  countofrange AS countofrange2
FROM partywithrangecount
WHERE range = '[4,6)'
ORDER BY country_id;



CREATE VIEW countrywithr6_8 AS
SELECT country_id, range AS range4,  countofrange AS countofrange3
FROM partywithrangecount
WHERE range = '[6,8)'
ORDER BY country_id;



CREATE VIEW countrywithr8_10 AS
SELECT country_id, range AS range5, countofrange AS countofrange4
FROM partywithrangecount
WHERE range = '[8,10]'
ORDER BY country_id;




CREATE VIEW adhesive AS
SELECT countrywithr0_2.country_id, countofrange0 AS r0_2, countofrange1 AS r2_4, countofrange2 AS r4_6, countofrange3 AS r6_8, countofrange4 AS r8_10
FROM  countrywithr0_2 NATURAL JOIN countrywithr2_4 NATURAL JOIN countrywithr4_6 NATURAL JOIN countrywithr6_8 NATURAL JOIN countrywithr8_10
ORDER BY countrywithr0_2.country_id; 



CREATE VIEW adhesive2 AS
SELECT country.name AS countryName, r0_2, r2_4, r4_6, r6_8, r8_10
FROM adhesive JOIN country ON country.id = country_id
ORDER BY country_id;


INSERT INTO q4
SELECT countryName, r0_2, r2_4, r4_6, r6_8, r8_10
FROM adhesive2;
