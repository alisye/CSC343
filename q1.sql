-- VoteRange

SET SEARCH_PATH TO parlgov;
drop table if exists q1 cascade;

-- You must not change this table definition.

create table q1(
year INT,
countryName VARCHAR(50),
voteRange VARCHAR(20),
partyName VARCHAR(100)
);

--a table with the party short names that party_id is refering to   
CREATE VIEW partyEL AS
SELECT name_short AS name, election_id, votes
FROM election_result JOIN party ON party.id = party_id;  


--A table with a election_id, the year of the election, the country name where the election happened, and the valid votes in the election
CREATE VIEW yearCO AS
SELECT country.name AS CountryName, EXTRACT(YEAR FROM e_date) AS year, election.id AS election_id, votes_valid     
FROM country JOIN election ON country.id = country_id;


--A table with the name of the party that part of each election in yearCO and how many votes each party recived
CREATE VIEW partyear AS
SELECT CountryName, year, partyEL.name AS partyName, votes, votes_valid 
FROM partyEL JOIN yearCO ON partyEL.election_id = yearCO.election_id
ORDER BY CountryName, year, partyName; 
 


--A table that calculates the percentage of votes a party recives by dividing the votes each party recives by the total number of valid votes
CREATE VIEW yearCOav AS
SELECT CountryName, year, partyName, CAST (SUM(votes) AS FLOAT)/SUM(votes_valid) AS percentage
FROM partyear 
GROUP BY CountryName, year, partyName;

 
--Goes through each percentage calculated in yearCOav and replaces the percentage with the correct (lb-up] label
CREATE VIEW glue AS
SELECT CountryName, year, partyName, CASE WHEN percentage <= 0.05 THEN '(0-5]'
	    WHEN (percentage > 0.05 and percentage <= 0.10) THEN '(5-10]'
	    WHEN (percentage > 0.10 and percentage <= 0.20) THEN '(10-20]'
	    WHEN (percentage > 0.20 and percentage <= 0.30) THEN '(20-30]'
	    WHEN (percentage > 0.30 and percentage <= 0.40) THEN '(30-40]'
	    WHEN percentage > 0.4 THEN '(40-100]'
	    WHEN percentage is NULL THEN NULL
       END AS voteRange
FROM yearCOav;


--Select only from valid years (1996 to 2016)
INSERT INTO q1
SELECT year, CountryName, voteRange, partyName
FROM glue
WHERE year >= 1996 and year <= 2016;

