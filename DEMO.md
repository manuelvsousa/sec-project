# Demo

## Instructions 
0. If there is a notary.ser file in the notary directory, delete it (to start a clean notary)

1. cd sec-project
2. mvn clean install

## Start Notary
(new terminal)
cd notary
mvn clean install tomcat7:run

## Start User Servers
(new terminal)
cd user
mvn -Dport=1 install tomcat7:run
(new terminal && same folder)
cd user
mvn -Dport=2 install tomcat7:run

## Start User Clients

(new terminal)
cd user-client
mvn exec:java -Dexec.args="user1" # (password: password1)
(new terminal)
cd user-client
mvn exec:java -Dexec.args="user2" # (password: password2)

## Demo Instructions
On the user client 1: 
Press 4 to list the goods available in the system
Write: 2 good2
Write: 1 good2


On the user client 2:
Press 4 to list the goods available in the system
Write: 2 good2
Write: 3 good2 user1
Write: 2 good2
Write: 1 good1 (Returns error) 
Write: 5 (Prints certificates from notary)

## Attack simulator demo instructions 

(new terminal)
cd usermal
mvn clean install exec:java
Press 1 to simulate Replay attack
Press 2 to simulate Authenticity attack
Press 3 to simulate Integrity attack
