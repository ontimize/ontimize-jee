<!-- ## [Unreleased] -->
<!-- ### Breaking changes ⚠ -->
<!-- ### Added ✔️-->
<!-- ### Changed 🛠️-->
<!-- ### Deprecated 🛑-->
<!-- ### Removed 🗑️-->
<!-- ### Fixed 🐛-->
<!-- ### Security 🛡️-->

## [Unreleased]
## [5.14.0] - 2025-11-03
### Added ✔️
- **OpenAI Client**

    An OpenAI client has been implemented with a method to process images. This method accepts a parameter of type ProcessRequest<T>, which contains the following:
    - The image file to be processed.
    - An optional prompt to provide additional instructions (the default behavior is to analyze the image and return it in a specific format).
    - The number of retries for the request.
    - The output class type (T) expected as a result.
    
    The client is initialized via a constructor that receives an OpenAiClientConfig object. This configuration object includes:
    - The user's API key.
    - The model to be used.
    - The maximum number of tokens allowed per request.
    - The temperature setting (which controls how creative the responses should be).
### Changed
- **ApplicationContextUtils**: Simplified the getBeanForName method for more streamlined service retrieval.
- **DefaultDaoExtensionHelper**: Improved property placeholder handling with Java streams for type safety and clarity, addressing SonarQube findings.
- **Log4j2LoggerHelper**: Clarified variable names, added null checks, enhanced type safety, and improved Javadoc documentation.
- **LogbackLoggerHelper**: Improved Javadoc documentation and suppress false positives on Sonar.
- **OntimizeJdbcDaoSupport**: Refactored SQL placeholder and property handling, introduced a helper method, corrected key retrieval logic, and improved type safety using Java streams to resolve SonarQube issues.
- **OntimizeJdbcDaoSupportTest**: Updated mocks to match the modified method signatures.
- **SharePreferencesBeanDefinitionParser**: Added null check and refactored engine parsing logic for safer and clearer configuration handling.
- **XMLClientUtilities**: Refactored string concatenation to use StringBuilder for improved readability and performance.
## [5.13.0] - 2025-09-11
### Added ✔️
* **OntimizeJdbcDaoSupport**: Created executeSQLStatement() to use DDL statements. #175
## [5.12.1] - 2025-04-11
### Changed 🛠️
* **OntimizeJdbcDaoSupport**: Changed visibility of composeSQL method (protected → public) #174
## [5.12.0] - 2025-03-12
### Added ✔️
* **Multi tenant**: Allow to configure tenants using the preferences. #153
* **LdapAuthenticationMechanism**: Allow to use SSL in LDAP connections. #144
* **Template transformation**: Apply template transformation with ExpressionKey and FilterKey. #166
* **OntimizeJDBCDaoSupport**: Allow searching for a list of values in columns with any data type with IN operator in BasicExpression. #167
### Changed 🛠️
* **Spring Framework**: Update Spring Framework version from 5.3.6 → 5.3.18 #154
* **ORestController**: Improved logging of exceptions displayed when performing basic CRUD operations of a service. #160
## [5.11.0] - 2024-02-23
### Added ✔️
* **Multi tenant**: Ask the tenant provider to load a tenant when it can not be found.
* **JaCoCo**: Add JaCoCo coverage on Sonar.
* **Export**: Added boolean values to excel export.
### Changed 🛠️
* **Preferences**: Now use name convention instead of harcoded columns.
* **Send mail action**: Update the mailing action with organizational secrets. Modified the title if the recipient is for testing.
* **Sonar**: Upgrade Java version on Sonar action.
### Fixed 🐛
* **Export**: Solved bug with pdf exporter.
## [5.10.0] - 2023-11-06
### Added ✔️
* **Keycloak**: Allow to use the Keycloak authentication on non-http calls.
* **Ontimize SQLStatementHandler**: Allow searching for a pattern in columns with any data type.
### Changed 🛠️
* **Security**: Do not allow logging in using the password hash.
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

[unreleased]: https://github.com/ontimize/ontimize-jee/compare/5.14.0...HEAD
[5.14.0]: https://github.com/ontimize/ontimize-jee/compare/5.13.0...5.14.0
[5.13.0]: https://github.com/ontimize/ontimize-jee/compare/5.12.1...5.13.0
[5.12.1]: https://github.com/ontimize/ontimize-jee/compare/5.12.0...5.12.1
[5.12.0]: https://github.com/ontimize/ontimize-jee/compare/5.11.0...5.12.0
[5.11.0]: https://github.com/ontimize/ontimize-jee/compare/5.10.0...5.11.0
[5.10.0]: https://github.com/ontimize/ontimize-jee/compare/5.9.0...5.10.0
[5.9.0]: https://github.com/ontimize/ontimize-jee/compare/5.8.0...5.9.0
[5.8.0]: https://github.com/ontimize/ontimize-jee/compare/5.7.0...5.8.0
[5.7.0]: https://github.com/ontimize/ontimize-jee/compare/5.6.0...5.7.0
