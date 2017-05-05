### smi-service-virtualnetwork

An IPAM micro-service that persists network/VLAN settings, and static IP reservation system.

### Purpose
The virtualnetwork (IPAM) Docker container is a spring-boot micro-service that allows a user to define one or more logical virtual networks with associated IPV4 network definitions for later use.  It performs basic validation and persists network/VLAN IP configuration data, to include any optional Static IP ranges.  The entered data is persisted in a postgres database (linked or attached via settings).  

Also provided is API's  for reserving and assigning Static IP addresses that may be associated with a network configuration.  

To optionally secure the REST endpoints, the micro-service comes compiled with spring-security-oauth2 libraries, and endpoints have pre-defined roles annotated.

The micro-service also serves as a reference implementation for a stand-alone virtualnetwork JAR library (smi-lib-virtualnetwork) that can be used independently when writing your own implementation. 

---

### How to Launch

Under Construction. Docker container not yet published on DockerHub..... 

##### Option 1. Linking to a postgres database:
1. Start the postgres database first
~~~
docker run --name postgresql -e POSTGRES_PASSWORD=foo -d postgres:9.6.1-alpine
~~~
2. Start the virtualnetwork docker container with the link option
~~~
sudo docker run --name virtualnetwork -p 0.0.0.0:46016:46016 --link=postgresql:db -e SPRING_PROFILES_ACTIVE=linked -e DB_POSTGRES_PASSWORD=foo -d rackhd/virtualnetwork:latest
~~~

##### Option 2. Passing connection settings in with the environment tag:
~~~
docker run --name virtualnetwork -p 0.0.0.0:46016:46016 -e DB_POSTGRES_PASSWORD=foo -e DB_PORT_5432_TCP_PORT=5432 -e DB_PORT_5432_TCP_ADDR=1.2.3.4 -d rackhd/virtualnetwork:latest
~~~
~~~

~~~
##### Option 3. Docker Compose (without Conusl K/V store) example:
~~~
version: '2'

services:
  virtualnetwork:
    container_name: virtualnetwork
    image: rackhd/virtualnetwork:latest
    depends_on:
    - postgresql
    ports:
    - 46016:46016
    volumes:
    - /var/log/dell/:/var/log/dell/
    mem_limit: 512m
    environment:
    - _JAVA_OPTIONS=-Xmx64m -Xss256k
    - "JAVA_OPTS=-Dspring.datasource.url=jdbc:postgresql://1.2.3.4:5432/postgres -Dspring.datasource.password=Dell123$"

  postgresql:
    container_name: postgresql
    image: postgres:9.6.1-alpine
    ports:
    - 5432:5432
    mem_limit: 512m
    environment:
      - POSTGRES_PASSWORD=Dell123$$
~~~
~~~
 
~~~
##### Option 4. Docker Compose using Conusl K/V store for properties example:
~~~
version: '2'

services:
  virtualnetwork:
    container_name: virtualnetwork
    image: rackhd/virtualnetwork:latest
    depends_on:
    - consul
    - postgresql
    network_mode: "host"
    ports:
    - 46015:46015
    extra_hosts:
    - "service-registry:<<replace with consul ip address>>"
    volumes:
    - /var/log/dell/:/var/log/dell/
    #- /etc/ssl/:/etc/ssl
    mem_limit: 512m
    environment:
    - "SPRING_PROFILES_ACTIVE=consul"
    - _JAVA_OPTIONS=-Xmx64m -Xss256k

  postgresql:
    container_name: postgresql
    image: postgres:9.6.1-alpine
    ports:
    - 5432:5432
    mem_limit: 512m
    environment:
      - POSTGRES_PASSWORD=Dell123$$
~~~
~~~

~~~ 
Example properties posted to config\virtualnetwork\data of the consul K\V store
~~~
server:
  port: 46016
 # ssl:
 #   key-store: /etc/ssl/keystore.jks
 #   key-store-password: changeit

# remove this section if enforcing security
security:
  ignored:
   - /**

spring:
  jpa:
    database: POSTGRESQL
    properties:
      hibernate:
        default_schema: virtualnetwork
    show-sql: true
    generate-ddl: true
    hibernate:
      ddl-auto: update
  datasource:
    platform: postgres
    url: jdbc:postgresql://1.2.3.4:5432/postgres
    username: postgres
    password: Dell123$
~~~
~~~

~~~
Example security section with Keycloak OAuth2 enabled
~~~
security:
  # ignored:
  # - /**
  oauth2:
    client:
      clientId: "spring-boot-demos"
      clientSecret: "2f53b44a-774a-4394-8a98-138476503d24"
      accessTokenUri: "http://100.68.123.174:8089/auth/realms/Test1/protocol/openid-connect/token"
      userAuthorizationUri: "http://100.68.123.174:8089/auth/realms/Test1/protocol/openid-connect/auth"
      tokenName: "oauth_token"
      authenticationScheme: "header"
      clientAuthenticationScheme: "header"
    resource:
      userInfoUri: "http://100.68.123.174:8089/auth/realms/Test1/protocol/openid-connect/userinfo"
~~~
~~~

~~~ 



---

### How to Use


#### API Definitions

A swagger UI is provided by the microservice at http://<ip>:46016/swagger-ui.html

#### Example Usage Scenario

A typical flow could be that a quantity of IP addresses is needed for potential use by an entity during a workflow, or other business logic of the consuming application.

Step 1.  The user or application makes a call to the micro-service to persist the configuration for one or more networks.  For this scenario, one or more of the networks is configured with a static IP range.

```
{
 "name":"Network1",
 "description":"The First Network",
 "type":[] /*Enum (PUBLIC_LAN,PRIVATE_LAN,STORAGE_ISCSI_SAN,STORAGE_FCOE_SAN,OOB_OR_INFRASTRUCTURE_MANAGEMENT,HYPERVISOR_MANAGEMENT,HYPERVISOR_MIGRATION,HYPERVISOR_CLUSTER_PRIVATE,PXE,FILESHARE,FIP_SNOOPING,HARDWARE_MANAGEMENT)*/,
 "vlanId":90, // Allowed values are 1 to 4000 from UI. Values above 4000 are reserved for ICEE. VlanId for OOB_OR_INFRASTRUCTURE_MANAGEMENT networks must be empty or null.
 "static":true,
 "staticIpv4NetworkConfiguration":
    {
        "gateway":"172.162.0.1",
        "subnet":"255.255.255.0",
        "dnsSuffix":"abc.com",
        "primaryDns":"172.162.0.1",
        "secondaryDns":"172.162.0.2",
        "ipRange":
        [
                {
                    "startingIp":"172.162.0.90",
                    "endingIp":"172.162.0.92" // Maximum number of ip addresses allowed per range is 1000.
                }
                {} /* one or more*/
         
        ]
    }
}
```
Example payload (above) - can be used with REST "POST" to endpoint "/api/1.0/networks"

Step 2.  The user or application makes a call to the micro-service to lookup previously entered network configuration data as a list, by Name, or by Type.

Step 3. The user makes a call to the micro-service to RESERVE N number of IP addresses from network X for a process with identifier Y.   The micro-service returns an array of IP addresses to fulfill the request, or an error.

Step 4.  The user makes a call to the micro-service to ASSIGN IP address A from network X to Usage ID Z1.

Step 5.  The user makes another call to the micro-service to ASSIGN IP address B from network X to Usage ID Z2.

Step 6. The user determines that he does not need IP address C.  A call is made to the micro-service to RELEASE IP address C from network X.  Alternatively, a call can be made by an external process to release any reserved IP addresses that are not assigned within a given expiration period.

---

### Support
Slack Channel: codecommunity.slack.com
