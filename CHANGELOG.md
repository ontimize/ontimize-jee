<!-- ## [Unreleased] -->
<!-- ### Added ✔️-->
<!-- ### Changed 🛠️-->
<!-- ### Deprecated 🛑-->
<!-- ### Removed 🗑️-->
<!-- ### Fixed 🐛-->
<!-- ### Security 🛡️-->

## [Unreleased]
* **Security**: Do not allow logging in using the password hash.
* **Keycloak**: Allow to use the Keycloak authentication on non-http calls.
## [5.9.0] - 2023-07-31
### Added ✔️
* **Multi tenant**: Add feature to set tenants on non-secured requests.
### Changed 🛠️
* **Release mail**: The mail template of a new version has been modified, it now contains a unique image and the changes that have been written in the changelog for that specific version.
* **Changelog**: The structure of the CHANGELOG.md file has been modified so that it follows the structure shown at  [keepachangelog](https://keepachangelog.com/).
* **Sonarcloud**: Fix some sonarcloud smells.
### Fixed 🐛
* **SQLStatementBuilder**: Modify condition in a BasicExpression when its left operator is a field, now it correctly gets the value and correctly executes the LIKE in the query depending on whether it is UPPER or LOWER.
* **Preferences**: Fix the behavior when setting preference columns, now it takes into account the name-convention of the database columns.
## [5.8.0] - 2023-06-12
### Added ✔️
* **Keycloak**: Allow to configure roles without a database.
### Fixed 🐛
* **DefaultOntimizeDaoHelper**: ISQLAdapter is now taking in consideration.
* **Ontimize rest controller**: Get the next level message when the current one is null.
## [5.7.0] - 2023-04-20
### Added ✔️
* **Test**: Added test to StatementHandlers.
* **Password encryption**: Now can use BCrypt as password encoder.
* **Keycloak**: Allow to customize the source of the keycloak settings.
* **UUID**: Add JDBC UUID support.
### Changed 🛠️
* **POI Version**: POI version updated to 5.2.3.
* **POM**: Sorted pom alphabetically and sorted, extracted version to properties and put all dependencies into dependency manager.
* **Sonar**: Fix some sonar code smells.

[unreleased]: https://github.com/ontimize/ontimize-jee/compare/5.9.0...HEAD
[5.9.0]: https://github.com/ontimize/ontimize-jee/compare/5.8.0...5.9.0
[5.8.0]: https://github.com/ontimize/ontimize-jee/compare/5.7.0...5.8.0
[5.7.0]: https://github.com/ontimize/ontimize-jee/compare/5.6.0...5.7.0
