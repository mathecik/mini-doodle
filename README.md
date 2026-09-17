# Mini Doodle

A small meeting scheduling service written in Java and Spring Boot.
Users can open time slots in their calendar, turn them into meetings,
and check when they are free or busy.

## How to run

You need Docker (with Docker Compose) installed.

Run the command below to start the application together with its database:

```bash
docker compose up --build
```

To check the health, run this command in another terminal tab:

```bash
curl localhost:8080/actuator/health
```

It should return `{"status":"UP"}`.

To stop the application, press Ctrl+C, or run `docker compose down`.

Once running, the API documentation is available at
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

Metrics are exposed for Prometheus at
[http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus).

To run the tests (Docker required, the tests start their own database):

```bash
./gradlew test
```

## API

All endpoints are under `/api/v1`. Times are ISO-8601 and stored in UTC.

| Method | Path | Description |
|---|---|---|
| POST | `/users` | Create a user (a calendar is created with them) |
| GET | `/users/{userId}` | Get a user |
| POST | `/users/{userId}/slots` | Create a time slot |
| GET | `/users/{userId}/slots?from=&to=&status=` | List slots in a range, optionally by status |
| PUT | `/users/{userId}/slots/{slotId}` | Change the time or duration of a slot |
| PATCH | `/users/{userId}/slots/{slotId}/status` | Mark a slot FREE or BUSY |
| DELETE | `/users/{userId}/slots/{slotId}` | Delete a slot |
| GET | `/users/{userId}/availability?from=&to=` | Free and busy slots with totals |
| POST | `/users/{userId}/meetings` | Schedule a meeting on a free slot |
| GET | `/users/{userId}/meetings/{meetingId}` | Get a meeting |
| DELETE | `/users/{userId}/meetings/{meetingId}` | Cancel a meeting and free the slot |

Slot duration is between 30 and 480 minutes. Range queries are limited to 31 days.

### Example

```bash
# create a user
curl -X POST localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "minion", "email": "minion@example.com"}'

# create the second user (optional : to test meeting schedule with participants)
curl -X POST localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username": "totoro", "email": "totoro@example.com"}'

# create a slot
curl -X POST localhost:8080/api/v1/users/1/slots \
  -H "Content-Type: application/json" \
  -d '{"startTime": "2026-09-21T10:00:00Z", "durationMinutes": 30}'

# schedule a meeting on that slot
curl -X POST localhost:8080/api/v1/users/1/meetings \
  -H "Content-Type: application/json" \
  -d '{"slotId": 1, "title": "Planning", "participantIds": [2]}'

# see the availability for that day
curl "localhost:8080/api/v1/users/1/availability?from=2026-09-21T00:00:00Z&to=2026-09-22T00:00:00Z"
```

## Domain model

```
User 1 --- 1 Calendar 1 --- * TimeSlot 1 --- 0..1 Meeting * --- * User (participants)
```


Each user gets a **Calendar** when they are created. Calendar is not exposed
through the API, it only exists inside the service.

A **TimeSlot** belongs to one calendar and has a start time, an end time and a
status (FREE or BUSY). Slots in the same calendar cannot overlap.

A **Meeting** is booked on a free slot and has a title, an optional description
and participants. Booking marks the slot BUSY, cancelling frees it again.

## Design decisions

**Flyway for the schema.** The schema is versioned with the code, so docker
compose and the tests always get the same tables. Hibernate only validates the
entities against it.

**Calendar as a domain concept.** The task asks for a personal calendar, so each
user has one. It is a thin entity for now, but it is where calendar level
settings like working hours would go.

**Slots store start and end time.** The API takes a start time and a duration,
and the service calculates the end. Duration is always derived from the two
times, so they cannot disagree.

**One meeting, one slot.** Duration is configurable, so a 90 minute meeting is
simply a 90 minute slot. No need to combine several slots.

**Status belongs to the slot.** A slot can be BUSY without a meeting, for time
the user wants to block. A slot with a meeting cannot be edited, deleted or
marked free until the meeting is cancelled.

**Concurrency.** The slot has a `@Version` field, so two people cannot book the
same slot at once, the second request gets a 409. The unique constraint on
`meeting.slot_id` is the last line of defense. There is a test with two threads
booking the same slot where only one succeeds.

**Overlapping slots** are rejected by a query before the insert. Two concurrent
requests could still slip through, which I left as a known limitation.

**Bounded queries.** Listing slots and availability both require a time range of
at most 31 days, and the query uses an index on `(calendar_id, start_time)`, so
one request cannot load an unbounded number of rows.

**What I left out.** For hundreds of users and thousands of slots one service
with Postgres is enough, so there is no cache or message queue. Errors use
Spring's `ProblemDetail`, so they have a consistent shape without a custom error
class.

## Testing

Integration tests run against a real Postgres started by Testcontainers, so they
use the same Flyway schema as the application. They cover the endpoints and the
main rules: overlapping slots, booking a busy slot, and two threads booking the
same slot where only one succeeds.


## Not implemented / next steps

- Authentication: the user comes from the path, so there is no access control.
- Participants' own calendars are not updated when they are added to a meeting.
- Bulk slot creation, for example "9:00 to 12:00 in 30 minute slots".
- Overlap protection in the database (a Postgres exclusion constraint) or a lock
  on the calendar row, to close the gap under concurrent requests.
- Pagination for slot listing, so ranges longer than 31 days can be queried.
- Per-user time zones: times are handled in UTC only.
- Recurring slots, for example "every weekday, same hours".
