# Demo

## Instructions 
0. If there is a notary.ser file in the notary directory, delete it (to start a clean notary)

1. cd sec-project
2. mvn clean install

## Start Notary
3.cd notary
4. mvn clean install tomcat7:run

## Start User Servers
(new terminal)
6. cd user
7. mvn -Dport=1 install tomcat7:run
(new terminal)
8. mvn -Dport=2 install tomcat7:run

## Start User Clients

(new terminal)
9. cd user-client
10. mvn exec:java -Dexec.args="user1" (password: password1)
(new terminal)
11. cd user-client
12. mvn exec:java -Dexec.args="user2" (password: password2)

## Demo Instructions
On the user client 1: 
15. Press 4 to list the goods available in the system
16. Write: 2 good2
17. Write: 1 good2


On the user client 2:
18. Press 4 to list the goods available in the system
19. Write: 2 good2
20. Write: 3 good2 user1
21. Write: 2 good2
22. Write: 1 good1 (Returns error) 

## Attack simulator demo instructions 

(new terminal)
23. mvn clean install exec:java
24. Press 1 to simulate Replay attack
25. Press 2 to simulate Authenticity attack
26. Press 3 to simulate Integrity attack