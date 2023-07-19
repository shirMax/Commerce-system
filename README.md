# SE_Workshop_15B
Workshop on Software Engineering Project - BGU 2023

## Collaborators:
Roi Tiefenbrunn - 209829340

Nadav Kurin - 204890008

Amit Ganon - 314770629

Shir Maximof - 206561623

Oran Ben-Noon - 314627571


## Configuration
Open the config.properties file located in the src/main/resources of the project.

Update the database configuration with the provided credentials:

dbType = 'postgresql';
host = '139.59.209.55';
port = 5432;
db = 'dev_db'; 
user = 'postgres';
password = '123';
Save the changes to the config.js file.

where:
dbType: Specifies the type of the database. In this case, it is set to 'postgresql', indicating that the system is using PostgreSQL as the database.

host: Represents the IP address or hostname of the database server. The provided value is '139.59.209.55', which is the specific address where the database is hosted.

port: Specifies the port number on which the database server is listening. The provided value is 5432, which is the default port for PostgreSQL.

db: Indicates the name of the database to be used. In this case, the value is 'dev_db', representing the specific database that the system will connect to.

user: Specifies the username for connecting to the database. The provided value is 'postgres', which is the username associated with the PostgreSQL database.

password: Represents the password for the specified database user. The provided value is '123', which is the password associated with the PostgreSQL user.

mode (optional): This field allows for specifying the mode of operation for the system. It can have three possible values:
	* create: This mode is used when you want to reset the database during system startup. It will drop the existing tables and recreate them from scratch.
	* update: This mode is used when you want to continue using the existing database without making any changes to the data.
	* create-delete: This mode is used when you want to reset the database during system startup and also delete all data when the system is shut down. It combines the functionality of both create and delete operations.
	To use the desired mode, you can add the mode field to the config.properties file and set its value accordingly based on your requirements.

Update the following properties with the provided values:

scenarioNumber = 3
serviceURL = https://php-server-try.000webhostapp.com/

Save the changes to the config.properties file.

Scenario Number: The scenarioNumber property represents the specific scenario or data flow to be executed by the system. In this case, the value is set to 3. You can change this to 1/2/3.
The scenario number determines the sequence of actions, data transformations, or specific functionalities that will be triggered within the system. Each scenario typically represents a unique use case or a specific set of conditions for processing data.

ServiceURL: The serviceURL property specifies the URL of the external service that the system will interact with. In this case, the value is set to https://php-server-try.000webhostapp.com/.
# Commerce-system

##homepage-
![image](https://github.com/shirMax/Commerce-system/assets/110455848/36c122b4-98e6-40e1-844b-bed74d468d8a)

##signup-
![image](https://github.com/shirMax/Commerce-system/assets/110455848/760c0d15-f464-4009-83e7-155ec355e01c)

##my stores-
![image](https://github.com/shirMax/Commerce-system/assets/110455848/aec924c7-4e0e-4174-a8db-b986fe63a154)

![image](https://github.com/shirMax/Commerce-system/assets/110455848/5e87478b-a9ad-483d-a8c9-ca4ae17aa16c)

##about-
![image](https://github.com/shirMax/Commerce-system/assets/110455848/ba3870b4-789b-4fd7-8f7d-bbb5cc7e6886)

![image](https://github.com/shirMax/Commerce-system/assets/110455848/7e1104c0-ba63-4356-b457-38f2888b4505)

#search-
![image](https://github.com/shirMax/Commerce-system/assets/110455848/77014a5c-48d7-429e-8942-20a61a7c1f94)










