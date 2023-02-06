#!/bin/bash
echo "REMEMBER TO SET THE ENVIRONMENT VARIABLES (USER, PASS, DATABASE) ...!!!!";

mysql --user=$HOST --password=$PASSWORD $DATABASE < sensors.sql;
mysql --user=$HOST --password=$PASSWORD $DATABASE < adls.sql;
mysql --user=$HOST --password=$PASSWORD $DATABASE < activitiesRecord.sql;
mysql --user=$HOST --password=$PASSWORD $DATABASE < insertDefaultAdls.sql;
mysql --user=$HOST --password=$PASSWORD $DATABASE < adls.sql;
mysql --user=$HOST --password=$PASSWORD $DATABASE < insertDefaultActivitiesRecord.sql;

