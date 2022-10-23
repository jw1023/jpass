Overview
--------
0. mvn clean package in root directory

1. Put user.policy and ../target/jpass-1.0.0-RELEASE.jar to D:/password-manager/

2. Run in D:/password-manager/ dir with command : java  -Djava.security.manager -Djava.security.policy=user.policy -jar jpass-1.0.0-RELEASE.jar
