# SEC Project - Group 6

# Team

| Number | Name  | Email 
| :---: |:--------------------:| :--------------------------------------:| 
| 84740 | Manuel Sousa         | manuelvsousa@tecnico.ulisboa.pt         | 
| 83420 | Alexandra Figueiredo | alexandra.figueiredo@tecnico.ulisboa.pt |
| 78093 | Gonçalo Santos       | goncalo.v.santos@tecnico.ulisboa.pt     |

# Report

https://docs.google.com/document/d/1mtFV9VeYQm8_UqbLZi23UULT_nh_jZsGeqYXtVheErI/edit?usp=sharing

# Run instructions

1. Open Terminal && `cd notary`
2. Paste `export MAVEN_OPTS=-Djava.library.path=/usr/local/lib`
3. mvn clean install tomcat7:run
4. `cd user`
5. mvn clean -Dport=${userID nº} install tomcat7:run (e.g. mvn clean -Dport=3 install tomcat7:run -> starts server with userID -3)


# Generate keys

1. `cd keys`
2. `java RSAKeyGenerator w users/user1.key users/user1.pub`
3. `java EncryptPrivKey users/user1.key users/user1.enc.key password1`

# To run the demo

Open DEMO.md file and follow the instructions described

# Notary CC

Change the boolean to true in the Bootstrap in the notary directory and in the NotaryAbstract in the notary-client directory if you want to use the CC.