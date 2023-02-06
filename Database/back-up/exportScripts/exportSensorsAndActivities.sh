#!/bin/bash
sudo mysql -e "
    use test;

    SELECT
        \"id\",
        \"id_activity\",
        \"id_step\",
        \"timestamp\",
        \"user_id\",
        \"date\"
    UNION
    ALL
    SELECT
        *,
        FROM_UNIXTIME (timestamp / 1000) AS date
    FROM
        steps_for_activities_registry 
    WHERE
        user_id LIKE \"MR-___\"
            INTO OUTFILE '/var/lib/mysql-files/steps_for_activities_registry.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n';

    SELECT
        'id',
        'device_type',
        'sensor_type',
        \"values_x\",
        \"values_y\",
        \"values_z\",
        \"timestamp\",
        \"user_id\",
        \"date\"
    UNION
    ALL (
        SELECT
            *,
            FROM_UNIXTIME (timestamp / 1000)
        FROM
            sensors
        WHERE
            device_type != 0 AND
            user_id LIKE \"MR-___\"
        ORDER BY
            user_id ASC,
            timestamp ASC INTO OUTFILE '/var/lib/mysql-files/sensors.csv' FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\n'
    );
";

source moveMysqlFilesToSavedData.sh
