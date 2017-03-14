# NouvolaTeamCityPlugin
This is the plugin of Nouvola DiveCloud with TeamCity

## Build

### Dependencies
- Java 8
- Apache Maven

Create a distribution jar file

```
git clone <this repository>
cd nouvolaDiveCloudPlugin
mvn package
```
## Install

Install the plugin in your TeamCity server

```
cp target/nouvolaDiveCloudPlugin.jar <team city home dir>/plugins/.
```

Restart the TeamCity server

The build runner will show up during configuration of a build step
