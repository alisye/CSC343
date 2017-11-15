-- Participate

SET SEARCH_PATH TO parlgov;
drop table if exists q3 cascade;

-- You must not change this table definition.

create table q3(
        countryName varchar(50),
        year int,
        participationRatio real
);

-- You may find it convenient to do this for each of the views
-- that define your intermediate steps.  (But give them better names!)

DROP VIEW IF EXISTS Raw_Participation_Ratios CASCADE;
DROP VIEW IF EXISTS Participation_Ratios CASCADE;
DROP VIEW IF EXISTS Invalid_Countries CASCADE;
DROP VIEW IF EXISTS Valid_Countries CASCADE;
DROP VIEW IF EXISTS Valid_Country_Names CASCADE;


--finds participation ratios of all elctions between 2001 and 2016
CREATE VIEW Raw_Participation_Ratios AS
SELECT country_id, EXTRACT(YEAR FROM e_date) AS year,
  sum(votes_cast) as votes, sum(electorate) as electorate,
  CAST(votes_cast as FLOAT)/electorate as participation_ratio
from election
where EXTRACT(YEAR FROM e_date) <=2016
      and EXTRACT(YEAR FROM e_date) >= 2001
group by country_id, id
order by country_id, year;



--calculates the average participation of all countries
CREATE VIEW Participation_Ratios AS
  SELECT country_id, year,
    avg(CAST(participation_ratio as float)) as participation_ratio
  from Raw_Participation_Ratios
  GROUP BY country_id, year
  ORDER BY country_id;




--all countries whose participation ratios were greater in a previous year (not increasing by year)
CREATE VIEW Invalid_Countries AS
  SELECT DISTINCT PR1.country_id
  from Participation_Ratios PR1 join Participation_Ratios PR2
    ON PR1.country_id = PR2.country_id
  WHERE PR1.year < PR2.year
        and PR1.participation_ratio > PR2.participation_ratio;



--Takes the set difference of all countires and all countires whose participation ratio is not monotomically increasing also reports the participation ratio of each year
CREATE VIEW Valid_Countries AS
  SELECT country_id, year, participation_ratio
  FROM Participation_Ratios PR
  WHERE NOT exists(
    SELECT country_id
    FROM Invalid_Countries IC
    WHERE PR.country_id = IC.country_id
  );


--replaces country id with country name
CREATE VIEW Valid_Country_Names AS
 SELECT country.name as cname, year, participation_ratio
 FROM Valid_Countries join country
   on Valid_Countries.country_id = country.id;


-- the answer to the query 
insert into q3 (SELECT cname, year, participation_ratio from Valid_Country_Names);

