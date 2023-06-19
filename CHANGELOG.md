<!-- ## [Unreleased] -->
<!-- ### Added ✔️-->
<!-- ### Changed 🛠️-->
<!-- ### Deprecated 🛑-->
<!-- ### Removed 🗑️-->
<!-- ### Fixed 🐛-->
<!-- ### Security 🛡️-->

## [Unreleased]
### Changed 🛠️
* **Release mail**: The mail template of a new version has been modified, it now contains a unique image and the changes that have been written in the changelog for that specific version.
* **Changelog**: The structure of the CHANGELOG.md file has been modified so that it follows the structure shown at  [keepachangelog](https://keepachangelog.com/)
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

[unreleased]: https://github.com/ontimize/ontimize-jee/compare/5.8.0...HEAD
[5.8.0]: https://github.com/ontimize/ontimize-jee/compare/5.7.0...5.8.0
[5.7.0]: https://github.com/ontimize/ontimize-jee/compare/5.6.0...5.7.0
