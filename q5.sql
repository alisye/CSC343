-- Committed

SET SEARCH_PATH TO parlgov;
drop table if exists q5 cascade;

-- You must not change this table definition.

CREATE TABLE q5(
        countryName VARCHAR(50),
        partyName VARCHAR(100),
        partyFamily VARCHAR(50),
        stateMarket REAL
);



CREATE VIEW recentCabinet AS
SELECT id, country_id
FROM cabinet
WHERE EXTRACT(YEAR FROM start_date) >= 1997
ORDER BY country_id;



CREATE VIEW countryCabAct AS
SELECT country_id, party_id, cabinet_id
FROM recentCabinet JOIN cabinet_party ON recentCabinet.id = cabinet_id
ORDER BY country_id, party_id, cabinet_id;




CREATE VIEW countryCabShould AS
SELECT party.country_id, party.id AS party_id, cabinet.id AS cabinet_id
FROM party JOIN cabinet ON party.country_id = cabinet.country_id
ORDER BY country_id, party_id, cabinet_id;




CREATE VIEW notcommited AS
(SELECT * FROM countryCabShould) EXCEPT ALL (SELECT * FROM countryCabAct);



CREATE VIEW notcommitedParty AS
SELECT DISTINCT party_id AS id
FROM notcommited
ORDER BY party_id;



CREATE VIEW commitedparty AS
(SELECT id FROM party) EXCEPT (SELECT id FROM notcommitedParty);



CREATE VIEW binder AS
SELECT country_id, party.id AS party_id, party.name AS partyName
FROM party JOIN commitedparty ON party.id = commitedparty.id
ORDER BY party.id;



CREATE VIEW binder2 AS
SELECT country_id, binder.party_id, partyName, family
FROM binder LEFT JOIN party_family ON party_family.party_id = binder.party_id
ORDER BY binder.party_id;


CREATE VIEW binder3 AS
SELECT country_id, binder2.party_id, partyName, family, state_market
FROM binder2 LEFT JOIN party_position ON party_position.party_id = binder2.party_id
ORDER BY binder2.party_id;


CREATE VIEW binder4 AS
SELECT country.name AS countryName, partyName, family, state_market
FROM binder3 JOIN country ON country_id = country.id
ORDER BY country.name;



INSERT INTO q5
SELECT countryName, partyName, family, state_market
FROM binder4;
