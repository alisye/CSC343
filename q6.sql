-- Sequences

SET SEARCH_PATH TO parlgov;
drop table if exists q6 cascade;

-- You must not change this table definition.

CREATE TABLE q6(
        countryName VARCHAR(50),
        cabinetId INT, 
        startDate DATE,
        endDate DATE,
        pmParty VARCHAR(100)
);

--adds party_id from cabinet_party to the regular cabinet table
CREATE VIEW detailedCab AS
SELECT cabinet.id AS cabinet_id, start_date, previous_cabinet_id AS prev_id, party_id
FROM cabinet_party JOIN cabinet ON cabinet.id = cabinet_id
ORDER BY cabinet.id;



--the end date of each cabinet will be the start_date of the cabinet that has the current cabient as it's previous cabinet
--using this adds a end date to each cabinet
CREATE VIEW startend AS
SELECT DISTINCT c2.cabinet_id AS cab_id, c2.start_date AS c2_start_date, c1.start_date AS c2_end_date  
FROM detailedCab c1 JOIN detailedCab c2 ON c1.prev_id = c2.cabinet_id
ORDER BY c2.cabinet_id;



--cabinets with missing pm information
CREATE VIEW missingpm AS
SELECT id AS cabinet_id, start_date, c2_end_date AS end_date, country_id
FROM  cabinet LEFT JOIN startend ON cab_id = id
ORDER BY cab_id;



--put together cabients that have pm information and cabinets that don't
CREATE VIEW foundpm AS
SELECT missingpm.cabinet_id, start_date, end_date, country_id, party_id
FROM missingpm LEFT JOIN cabinet_party ON missingpm.cabinet_id = cabinet_party.cabinet_id
WHERE pm = TRUE or pm is NULL
ORDER BY missingpm.cabinet_id;



--replaces country id with country name
CREATE VIEW cement AS
SELECT name, cabinet_id, start_date, end_date, party_id
FROM foundpm JOIN country ON country.id = country_id
ORDER BY cabinet_id;


--adds the name of the pm party instead of party id
CREATE VIEW cement2 AS
SELECT cement.name AS countryName, cabinet_id, start_date, end_date, party.name AS pmParty
FROM cement JOIN party ON party.id = party_id
ORDER BY cabinet_id;



INSERT INTO q6
SELECT countryName, cabinet_id, start_date, end_date, pmParty
FROM cement2;
