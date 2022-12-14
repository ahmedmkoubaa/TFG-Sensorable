#!/bin/bash
sudo mysql -e "
    use test;
    SELECT
        \"id\",
        \"title\",
        \"description\"
    UNION
    ALL
    SELECT
        *
    FROM
        activities INTO OUTFILE '/var/lib/mysql-files/list_of_activities.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n';

    SELECT
        \"id\",
        \"title\"
    UNION
    ALL
    SELECT
        *
    FROM
        activity_steps INTO OUTFILE '/var/lib/mysql-files/list_of_activity_steps.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n';

    SELECT
        \"id\",
        \"id_activity\",
        \"id_step\"
    UNION
    ALL
    SELECT
        *
    FROM
        steps_for_activities INTO OUTFILE '/var/lib/mysql-files/list_of_steps_for_activities.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n';
";
source moveMysqlFilesToSavedData.sh
