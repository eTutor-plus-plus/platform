# eTutor++: Platform

#### This application was generated using JHipster 7.2.0, you can find documentation and help at [https://www.jhipster.tech/documentation-archive/v7.2.0](https://www.jhipster.tech/documentation-archive/v7.2.0).

## Development

### Dependencies

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools.
You will only need to run this command when dependencies change in [package.json](package.json).

```
yarn install
```

We use yarn scripts and [Angular CLI][] with [Webpack][] as our build system.

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

```
./mvnw
yarn start
```

Npm is also used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in [package.json](package.json). You can also run `yarn update` and `yarn install` to manage dependencies.
Add the `help` flag on any command to see how you can use it. For example, `yarn help update`.

The `yarn run` command will list all of the scripts available to run for this project.

2. [eTutor++-Objects](https://github.com/eTutor-plus-plus/objects): Objects common to different service of the etutor++ ecosystem are maintained in the [objects-project](https://github.com/eTutor-plus-plus/objects).
   To run a service depending on this project (like this one), you have to clone the repository and build the project with Maven:

```shell
git clone  https://github.com/eTutor-plus-plus/objects
cd objects
mvn clean install
```

Alternatively, you could donwload the latest jar from the [github action workflows](https://github.com/eTutor-plus-plus/objects/actions) in the repository of the objects-project. Click on the latest workflow and then on artifacts to do so. However, it would be required to install the jar into your local maven repository, so maybe installing it manually like described above is easier.

3. Other dependencies include:

- Java
- Maven
- Docker (optional but recommended)

### Setup

This application requires a [PostgreSQL](https://www.postgresql.org/)-database and a [Apache Jena Fuseki]() RDF graph database.
Connection details can be configured in the [properties](./src/main/java/at/jku/dke/etutor/config/ApplicationProperties.java).

To spin-up the necessary databases in Docker containers with configurations matching the default development settings, execute [this script](src/main/docker/etutor-databases/setup_databases.bat).
The script executes two commands:

```shell
docker-compose build
docker-compose up -d
```

The first command builds a custom image with an Apache Jena Fuseki server and the necessary configuration.
The second command starts three containers:

1. Fuseki
2. PostgreSQL
3. PGAdmin

The whole process may take some minutes, especially on first execution.

You can also manually start the required services or use the [local-deploy project](https://github.com/eTutor-plus-plus/local-deploy)

## Building for production

### Packaging as jar

To build the final jar and optimize the etutorPlusPlus application for production, run:

```
./mvnw -Pprod clean verify
```

This will concatenate and minify the client CSS and JavaScript files. It will also modify `index.html` so it references these new files.
To ensure everything worked, run:

```
java -jar target/*.jar
```

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.

Refer to [Using JHipster in production][] for more details.
After building for production, delete the [/target](./target)-folder if you want to proceed developing locally.

## Updating

This section describes relevant update-procedures for components of the etutorplusplus.

### Apache Jena Fuseki

To update the RDF-database, you have to backup the data, start the new Fuseki-server using the [configuration file](src/main/docker/etutor-databases/config_etutor_fulltext.ttl) with the following command:

```
fuseki-server --config config_etutor_fulltext.ttl
```

Afterwards, navigate to localhost:3030 and add the backup file to the /etutorpp-database.

Refer to [this](https://jena.apache.org/documentation/fuseki2/fuseki-server-protocol.html) official documentation on how to control a Fuseki server with HTTP requests.

### JHipster

To update JHipster, install the JHipster-Client with

```
npm install -g jhipster
```

Then, copy your local repository for safety reasons, and after commiting all changes,
execute the following command in the root of the project:

```
jhipster upgrade
```

This will create a new JHipster-application in a branch that diverges from your current branch
and integrate the etutorplusplus, which will require solving various merge conflicts.

### PostgreSQL

[pgupgrade](https://www.postgresql.org/docs/current/pgupgrade.html)

[jhipster homepage and latest documentation]: https://www.jhipster.tech
[jhipster 7.2.0 archive]: https://www.jhipster.tech/documentation-archive/v7.2.0
[using jhipster in development]: https://www.jhipster.tech/documentation-archive/v7.2.0/development/
[using docker and docker-compose]: https://www.jhipster.tech/documentation-archive/v7.2.0/docker-compose
[using jhipster in production]: https://www.jhipster.tech/documentation-archive/v7.2.0/production/
[running tests page]: https://www.jhipster.tech/documentation-archive/v7.2.0/running-tests/
[code quality page]: https://www.jhipster.tech/documentation-archive/v7.2.0/code-quality/
[setting up continuous integration]: https://www.jhipster.tech/documentation-archive/v7.2.0/setting-up-ci/
[node.js]: https://nodejs.org/
[npm]: https://www.npmjs.com/
[webpack]: https://webpack.github.io/
[browsersync]: https://www.browsersync.io/
[jest]: https://facebook.github.io/jest/
[leaflet]: https://leafletjs.com/
[definitelytyped]: https://definitelytyped.org/
[angular cli]: https://cli.angular.io/
