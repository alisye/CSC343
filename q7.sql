-- Alliances

SET SEARCH_PATH TO parlgov;
drop table if exists q7 cascade;

-- You must not change this table definition.

DROP TABLE IF EXISTS q7 CASCADE;
CREATE TABLE q7(
        countryId INT, 
        alliedPartyId1 INT, 
        alliedPartyId2 INT
);

-- You may find it convenient to do this for each of the views
-- that define your intermediate steps.  (But give them better names!)
DROP VIEW IF EXISTS Intermediate_Pairings CASCADE;
DROP VIEW IF EXISTS Pairings CASCADE;
DROP VIEW IF EXISTS Num_Elections CASCADE;
DROP VIEW IF EXISTS Num_Pairings CASCADE;
DROP VIEW IF EXISTS Pairings_Ratio CASCADE;

CREATE VIEW Intermediate_Pairings AS
  select P1.election_id as election_id, P1.party_id as party1_id,
    P2.party_id as party2_id
  from election_result P1 join election_result P2
      on P1.election_id = P2.election_id
  WHERE P1.party_id < P2.party_id
      and (P1.alliance_id = P2.alliance_id or
      P1.alliance_id = P2.id
         or P1.id = P2.alliance_id)
  order by P1.election_id;

CREATE VIEW Pairings AS
  select country_id, election_id, party1_id, party2_id
  from Intermediate_Pairings join election on election_id = id
  order by election_id;

CREATE VIEW Num_Pairings AS
  SELECT country_id, party1_id, party2_id, count(distinct election_id) as num_pairings
  from Pairings
  GROUP BY party1_id, party2_id, country_id
  ORDER BY num_pairings DESC;

CREATE VIEW Num_Elections AS
  SELECT country_id, count(*) as total_elections
  FROM election
  GROUP BY country_id;

CREATE VIEW Pairings_Ratio AS
  SELECT Num_Pairings.country_id, party1_id, party2_id, num_pairings,
    CAST(num_pairings AS float) / total_elections as election_ratio
  FROM Num_Pairings join Num_Elections
      on Num_Pairings.country_id = Num_Elections.country_id
  WHERE (CAST(num_pairings AS float) / total_elections) >= 0.3
  ORDER BY election_ratio DESC;

-- SELECT * from Pairings_Ratio;

-- the answer to the query 
insert into q7 SELECT country_id, party1_id, party2_id from Pairings_Ratio;

