-- Alliances

SET SEARCH_PATH TO parlgov;

SELECT * from q7 ORDER BY 
  countryid DESC, alliedpartyid1 DESC, alliedpartyid2 DESC;