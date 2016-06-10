## Installation


```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.kodgemisi</groupId>
    <artifactId>jwt-validation</artifactId>
    <version>-SNAPSHOT</version>
</dependency>
```

## Usage

You must provide some properties that are shown below. For example,

`application.yml` :
```yml
jwt:
  header: Authorization
  publicKey: -----BEGIN PUBLIC KEY----- ... -----END PUBLIC KEY-----
```
