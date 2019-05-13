# SEC Project - Group 6

# Team

| Number | Name  | Email 
| :---: |:--------------------:| :--------------------------------------:| 
| 84740 | Manuel Sousa         | manuelvsousa@tecnico.ulisboa.pt         | 
| 83420 | Alexandra Figueiredo | alexandra.figueiredo@tecnico.ulisboa.pt |
| 78093 | Gonçalo Santos       | goncalo.v.santos@tecnico.ulisboa.pt     |

# Report 2

https://docs.google.com/document/d/1ALIJ_2aMnaFGY0fx5H-MDFJSAu-WlXO2FNbZm1IpJ1s/edit?usp=sharing

# Run instructions

1. Open Terminal && `cd notary`
2. mvn clean -Dport=${userID nº} install tomcat7:run (e.g. mvn clean -Dport=3 install tomcat7:run -> starts notary with userID -3) NEED TO RUN 4
4. `cd user`
5. mvn clean -Dport=${userID nº} install tomcat7:run (e.g. mvn clean -Dport=3 install tomcat7:run -> starts server with userID -3)


# To run the demo

Open DEMO.md file and follow the instructions described

# Notary CC

Change the boolean to true in the Bootstrap in the notary directory and in the NotaryAbstract in the notary-client directory if you want to use the CC.