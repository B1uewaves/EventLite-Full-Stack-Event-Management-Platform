# EventLite — Full-Stack Event Management Platform

A full-stack event management application developed as part of the
University of Manchester's COMP23412 Software Engineering course.

EventLite provides event and venue management, search and sorting,
REST APIs, and social sharing through an external Mastodon API.

This was a collaborative team project built on a university-provided
codebase. This repository retains the original Git history so that
individual contributions and authorship remain visible.

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring HATEOAS

**Web & APIs**
- REST APIs
- HAL / HATEOAS
- Mastodon API
- HTML

**Testing & Development**
- JUnit
- Mockito
- AssertJ
- Maven
- Git / GitLab

## My Contributions

My work focused primarily on backend development, API design,
external API integration, search/sorting, and automated testing.

### REST & HATEOAS API

Implemented REST endpoints for exposing venue resources using
Spring MVC and Spring HATEOAS.

Key work included:

- Built `VenuesControllerApi` with REST endpoints returning
  `EntityModel` and `CollectionModel` responses.
- Implemented `VenueModelAssembler` using
  `RepresentationModelAssembler`.
- Added HATEOAS links for venue resources, including:
  - `self`
  - associated `events`
  - `next3events`
- Implemented a `/venues/{id}/next3events` endpoint.
- Added a bounded JPA query to retrieve the next three events
  ordered by date and time.
- Added unit tests for venue service behaviour using Mockito.

### Event Search & Sorting

Implemented keyword search and deterministic ordering for the
event listing functionality.

Key work included:

- Added case-insensitive event search.
- Implemented multi-field sorting using Java Streams and
  `Comparator`.
- Sorted upcoming events chronologically by date and event name.
- Sorted past events in reverse chronological order.
- Refactored repository queries so that data retrieval and
  presentation/sorting logic remained separated.
- Added unit and integration tests covering search results,
  ordering, and edge cases using Mockito and AssertJ.

### Mastodon API Integration

Implemented end-to-end event sharing through the Mastodon API.

Key work included:

- Created `MastodonService` and `MastodonServiceImpl`.
- Integrated Spring `RestClient` with Mastodon's
  `/api/v1/statuses` endpoint.
- Added `POST /events/{id}/share` request handling.
- Externalised Mastodon instance and authentication configuration.
- Updated Spring Security configuration for the sharing endpoint.
- Added a CSRF-protected event-sharing form to the UI.
- Implemented success/error feedback and redirect handling.
- Added Mockito controller tests covering API invocation,
  success/error behaviour and redirects.

## Architecture

The application follows a layered Spring architecture:

    Web / UI
        │
        ▼
    Controllers
        │
        ▼
    Services
        │
        ▼
    Repositories
        │
        ▼
    Database

External integrations such as Mastodon are encapsulated behind
service interfaces rather than being called directly from controllers.

## Testing

The project uses automated tests across the service and controller
layers.

My additions include tests for:

- Event search and ordering
- Venue validation and duplicate handling
- REST-related service behaviour
- Mastodon API invocation
- Success and failure paths
- Redirect behaviour

Mockito is used to isolate dependencies, while AssertJ/JUnit are
used for assertions and behavioural verification.

## Running Locally

Clone the repository:

    git clone https://github.com/B1uewaves/EventLite-Full-Stack-Event-Management-Platform.git
    cd EventLite-Full-Stack-Event-Management-Platform

Run using Maven:

    ./mvnw spring-boot:run

On Windows:

    mvnw.cmd spring-boot:run

## Project Context & Attribution

EventLite was developed collaboratively as part of the University of
Manchester COMP23412 Software Engineering course.

The project contains university-provided starter code and work from
multiple team members. I do not claim sole authorship of the complete
codebase.

The original Git history has been preserved to maintain attribution
for individual contributions.

Original copyright and licence notices are retained in accordance
with the project's licence.
