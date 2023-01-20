[![](https://jitpack.io/v/jaks-mimuw-kava-org/Arabica.svg)](https://jitpack.io/#jaks-mimuw-kava-org/Arabica)
[![JUnit and Build](https://github.com/jaks-mimuw-kava-org/Arabica/actions/workflows/unit.yaml/badge.svg)](https://github.com/jaks-mimuw-kava-org/Arabica/actions/workflows/unit.yaml)

# Arabica
Aleksander Tudruj, Karol Kuźniak

## Description
Arabica is the best base for your coffee you can get. Same here. Arabica is the best Servlet Container for your application.

## Installation
Build in development mode:
```bash
mvn clean install -P dev
```
Build in production mode (`-P prod` is default):
```bash
mvn clean install
```
If you don't have maven installed, you can use `mvnw` script instead of `mvn` command.

## Actions explanation
- `unit.yaml` - unit tests - on every pull request and commit to master branch

## Examples
To see examples of usage, check out [Arabica-examples][#Examples] directory.


[#Examples]: https://github.com/jaks-mimuw-kava-org/Arabica-examples
