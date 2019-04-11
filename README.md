# SEC Project - Group 6
blalbaballbalba



# Team

| Number | Name  | Email 
| :---: |:--------------------:| :--------------------------------------:| 
| 84740 | Manuel Sousa         | manuelvsousa@tecnico.ulisboa.pt         | 
| 83420 | Alexandra Figueiredo | alexandra.figueiredo@tecnico.ulisboa.pt |
| 78093 | Gonçalo Santos       | goncalo.v.santos@tecnico.ulisboa.pt     |

# Report

https://docs.google.com/document/d/1lBuShvxhQHOjTxnslglhPNr0ehEu8zmIEtWuo1vYikQ/edit?fbclid=IwAR2yDkyI8WGWJ5W3L5HjGCctBo7Awmy24aFmX5PCelwEC-1q_QDYBKV4fUI


# TODO

- Mecanismo de seguranca da chave privada no notario, ie a pass ficar cifrada no objeto e decifrada cada vez que é para utilizar, ou ir a um ficheiro ler e usar.
- Gerar chave privada para o getStataOfGood e setIntention e usar certificacao com o CC para a transacao itself
- Certificar por cima nos users, e verificar no notario
# {DONE} - Notario mantem estado dos goods por user
- {DONE} - Pensar num ataque com replay attacks alterando o clock dos users

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
