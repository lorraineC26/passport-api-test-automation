# PassPort: API Test Automation Framework

A Java REST API test automation framework that validates the public
[restful-booker](https://restful-booker.herokuapp.com) API with functional,
negative, and data-driven tests, and runs them in CI on every push and pull
request.

## Tech stack

- **Java 21** (LTS)
- **Maven** for build and dependency management
- **TestNG** as the test framework (suites, data providers)
- **REST Assured** for sending HTTP requests and asserting on responses
- **GitHub Actions** for CI and test report publishing

## Getting started

### Prerequisites

- JDK 21
- Maven 3.9+

### Credentials

The suite authenticates against restful-booker with the public practice account
and reads the credentials from environment variables, so no secret is stored in
source control. Export both before running the tests:

```bash
export RESTFUL_BOOKER_USERNAME=admin
export RESTFUL_BOOKER_PASSWORD=password123
```

A missing variable fails the suite fast with a clear message rather than falling
back to a default.

### Run the tests

```bash
mvn clean test
```

A successful run ends with `BUILD SUCCESS` and reports the number of tests run.

## Project structure

```
passport-api-test-automation/
├── pom.xml                       # Maven config: dependencies, compiler target 21
├── testng.xml                    # TestNG suite definition
├── .github/workflows/ci.yml      # GitHub Actions pipeline
└── src/test/java/
    ├── config/                   # BaseTest: shared baseURI, RequestSpecification, timeouts
    ├── helpers/                  # Credentials, BookingApi, BookingFactory (test-data builders)
    └── tests/                    # SmokeTest, AuthTests, BookingTests, NegativeTests, DataDrivenTests
```

The framework is layered: request configuration (baseURI, headers, timeouts) is
defined once in a shared `RequestSpecification` in `BaseTest`, data-building and
API-call helpers live in `helpers/`, and the test classes express only what is
being verified.

## Test coverage

- **Functional** (`BookingTests`, `AuthTests`): create, read, update, and delete
  bookings; fetch an auth token; assert status codes and response fields on the
  happy path. Update and delete exercise the full token-authenticated flow.
- **Negative** (`NegativeTests`): querying a non-existent booking returns 404,
  deleting without a token returns 403, creating with missing fields is
  rejected, and invalid credentials fail authentication.
- **Data-driven** (`DataDrivenTests`): three TestNG `@DataProvider` methods feed
  multiple inputs into single test methods (valid booking payloads, invalid
  booking IDs, and invalid credential pairs), producing one test instance per
  data row.

The suite totals 35 test instances.

### Authentication note

restful-booker returns the auth token from `POST /auth` and expects it back in a
**Cookie** (`Cookie: token=...`), not an `Authorization: Bearer` header. The
token is fetched dynamically on each run and never hardcoded.

## Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs on every push and
pull request to `main`:

1. Check out the repository.
2. Set up JDK 21 (Temurin), matching the local toolchain and the `pom.xml`
   compiler target.
3. Run `mvn -B clean test`.
4. Upload the Surefire reports as a downloadable artifact.
5. Publish a pass/fail summary on the run page.

A failing test turns the run red and blocks the change (quality gate). Both the
artifact upload and the summary use `if: always()`, so reports are published even
when tests fail.

### CI credentials

The workflow injects the API credentials from repository secrets. In the GitHub
repository, add these under **Settings → Secrets and variables → Actions**:

- `RESTFUL_BOOKER_USERNAME`
- `RESTFUL_BOOKER_PASSWORD`

## Note on the target API

restful-booker runs on a free tier and may cold-start slowly or be briefly
unavailable. `BaseTest` sets connection and socket timeouts to fail fast on a
stalled request. If a request fails unexpectedly, check
[`/ping`](https://restful-booker.herokuapp.com/ping) (returns `Created` when the
service is alive) before suspecting the tests.
