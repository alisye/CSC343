SET SEARCH_PATH TO parlgov;
drop table if exists q2 cascade;

-- You must not change this table definition.

create table q2(
countryName VARCHaR(100),
partyName VARCHaR(100),
partyFamily VARCHaR(100),
wonElections INT,
mostRecentlyWonElectionId INT,
mostRecentlyWonElectionYear INT
);


CREATE VIEW mostvotes AS
SELECT election_id, MAX(votes) AS winning_votes
FROM election_result
GROUP BY election_id
ORDER BY election_id; 




CREATE VIEW winners AS
SELECT election_result.election_id, country_id, party_id, votes, e_date
FROM (mostvotes JOIN election_result ON mostvotes.election_id = election_result.election_id) JOIN election ON election.id = mostvotes.election_id
WHERE votes = winning_votes
ORDER BY election_id;




CREATE VIEW numberwon AS
SELECT country_id, party_id, COUNT(*) AS won
FROM winners
GROUP BY country_id, party_id
ORDER BY country_id, party_id;




CREATE VIEW numberofParties AS
SELECT country_id, COUNT(id) AS parties
FROM party
GROUP BY country_id
ORDER BY country_id;




CREATE VIEW totalwon AS
SELECT country_id, SUM(won) AS totalwins
FROM numberwon
GROUP BY (country_id)
ORDER BY country_id;




CREATE VIEW averageCO AS
SELECT totalwon.country_id, CAST(totalwins AS FLOAT)/parties AS average_wins
FROM totalwon JOIN numberofParties ON totalwon.country_id = numberofParties.country_id
ORDER BY country_id;




CREATE VIEW morethanthree AS
SELECT numberwon.country_id, party_id, won
FROM numberwon JOIN averageCO ON numberwon.country_id = averageCO.country_id
WHERE won > 3 * average_wins;




CREATE VIEW mostrecentwin AS
SELECT country_id, party_id, MAX(e_date) AS recent
FROM winners
GROUP BY country_id, party_id
ORDER BY recent;




CREATE VIEW mostrecentelection AS
SELECT election_id, mostrecentwin.country_id, mostrecentwin.party_id, recent
FROM mostrecentwin JOIN winners ON mostrecentwin.country_id = winners.country_id and mostrecentwin.party_id = winners.party_id
WHERE e_date = recent
ORDER BY election_id;




CREATE VIEW tape1 AS
SELECT morethanthree.country_id, morethanthree.party_id, won, election_id AS mostrecentId, recent AS mostrecentDate
FROM mostrecentelection JOIN morethanthree ON morethanthree.party_id =mostrecentelection.party_id
ORDER BY morethanthree.country_id;




CREATE VIEW tape2 AS
SELECT tape1.country_id, tape1.party_id, won, mostrecentid, mostrecentdate, family
FROM tape1 LEFT JOIN party_family ON tape1.party_id = party_family.party_id
ORDER BY tape1.country_id;




CREATE VIEW tape3 AS
SELECT country.name AS countryName, party.name AS partyName, won, mostrecentid, mostrecentdate, family
FROM (tape2 JOIN country ON tape2.country_id = country.id) JOIN party ON tape2.party_id = party.id
ORDER BY tape2.country_id;

CREATE VIEW tape4 AS
SELECT countryName, partyName, won, mostrecentid, mostrecentdate, CASE WHEN family=NULL THEN ' ' ELSE family END AS family
FROM tape3; 



INSERT INTO q2
SELECT countryName, partyName, family, won, mostrecentid, EXTRACT(YEAR FROM mostrecentdate)
FROM tape4;
