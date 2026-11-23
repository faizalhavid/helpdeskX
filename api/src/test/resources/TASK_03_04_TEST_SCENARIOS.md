# HelpdeskX Challenge 01 — Task 03/04 Test Scenarios

Source of truth: the requirements supplied for this task. No production Java implementation was used to derive expected behavior.

## Assumptions / contracts to confirm

The implementation contract was not provided. API tests use the following provisional conventions and are disabled until confirmed:

- Incident collection: `GET /api/incidents`
- Assignment: `PATCH /api/incidents/{incidentId}/assign` with JSON `{"assigneeId":"..."}`
- Status: `PATCH /api/incidents/{incidentId}/status` with JSON `{"status":"ASSIGNED"}`
- Pagination: `page` (default 0), `size` (default 20), sorting by Spring-style `sort=field,DIRECTION`
- Filters: `status`, `priority`, `category`, `keyword`, `startDate`, `endDate`
- Dates are ISO-8601 date strings (`yyyy-MM-dd`) unless the repository confirms an exact format.
- Incident, assignee, and request identifiers are UUID strings.
- Not-found incident/user: 404; invalid transition/reversed range/invalid IDs or values: 400; expected conflict-style rule violations may be 409 if the API contract explicitly defines it.
- Soft-delete policy is assumed to be hide deleted incidents. The exact deleted field/column and API setup must be confirmed.

These assumptions must not be treated as requirements. The exact routes, JSON fields, status codes, error envelope, enum representations, and test database setup remain blockers.

## Task 03 — Service/business rules

### Positive

| ID | Scenario | Expected result |
|---|---|---|
| S03-P01 | A new incident is created | Status is `NEW`; assignee is absent |
| S03-P02 | Assign an existing user to a `NEW` incident | Assignment succeeds; assignee is set; status becomes `ASSIGNED` |
| S03-P03 | Reassign an incident already in `ASSIGNED` | Assignment succeeds; assignee changes; status remains `ASSIGNED` |
| S03-P04 | Assign from `ASSIGNED` to a different existing user | Assignment succeeds |
| S03-P05 | Transition `NEW -> ASSIGNED` by assignment | Status transition occurs as assignment side effect |
| S03-P06 | Transition `ASSIGNED -> IN_PROGRESS` | Status changes successfully |
| S03-P07 | Transition `IN_PROGRESS -> PENDING` | Status changes successfully |
| S03-P08 | Transition `IN_PROGRESS -> RESOLVED` | Status changes successfully |
| S03-P09 | Transition `PENDING -> IN_PROGRESS` | Status changes successfully |
| S03-P10 | Transition `RESOLVED -> CLOSED` | Status changes successfully |
| S03-P11 | Update incident data without assignment/status operation | Data fields change; assignee/status remain unchanged |
| S03-P12 | Assignment operation does not update incident content fields | Incident content remains unchanged |
| S03-P13 | Status operation does not replace incident assignee or content | Assignee and content remain unchanged |

### Negative

| ID | Scenario | Expected result |
|---|---|---|
| S03-N01 | Assign a nonexistent user | Assignment is rejected; incident unchanged |
| S03-N02 | Assign a `NEW` incident to null/blank/nonexistent ID | Assignment is rejected |
| S03-N03 | Assign an incident that does not exist | Operation is rejected; no incident changed |
| S03-N04 | Reassign an incident in `NEW` | Rejection (a NEW incident has no prior assignee); assignment is the initial assignment only |
| S03-N05 | Reassign in `IN_PROGRESS` | Rejection; reassignment is allowed only in `ASSIGNED` |
| S03-N06 | Reassign in `PENDING` | Rejection |
| S03-N07 | Reassign in `RESOLVED` | Rejection |
| S03-N08 | Reassign in `CLOSED` | Rejection |
| S03-N09 | Direct status transition `NEW -> IN_PROGRESS` | Rejected |
| S03-N10 | Direct status transition `NEW -> PENDING` | Rejected |
| S03-N11 | Direct status transition `NEW -> RESOLVED` | Rejected |
| S03-N12 | Direct status transition `NEW -> CLOSED` | Rejected |
| S03-N13 | Transition `ASSIGNED -> PENDING` | Rejected |
| S03-N14 | Transition `ASSIGNED -> RESOLVED` | Rejected |
| S03-N15 | Transition `ASSIGNED -> CLOSED` | Rejected |
| S03-N16 | Transition `IN_PROGRESS -> ASSIGNED` | Rejected |
| S03-N17 | Transition `IN_PROGRESS -> NEW` | Rejected |
| S03-N18 | Transition `IN_PROGRESS -> CLOSED` | Rejected |
| S03-N19 | Transition `PENDING -> ASSIGNED` | Rejected |
| S03-N20 | Transition `PENDING -> PENDING` or PENDING -> RESOLVED/CLOSED | Rejected |
| S03-N21 | Transition `RESOLVED -> NEW/ASSIGNED/IN_PROGRESS/PENDING/RESOLVED` | Rejected |
| S03-N22 | Transition `CLOSED -> any valid or invalid status` | Rejected because CLOSED is terminal |
| S03-N23 | Self-transition to the current status | Rejected unless assignment naturally creates NEW -> ASSIGNED |
| S03-N24 | Change status using the assignment operation | Rejected or rejected as an unsupported assignment payload |
| S03-N25 | Assign through the incident status update operation | Rejected as invalid status data |
| S03-N26 | Status transition target is null/unknown | Rejected |

## Task 04 — List API/filtering rules

### Positive

| ID | Scenario | Expected result |
|---|---|---|
| L04-P01 | List with defaults | A valid page is returned; page/size metadata and sorted deterministic default are consistent |
| L04-P02 | Request page 1 with size 10 | Second page is returned with at most 10 results |
| L04-P03 | Sort by title ascending | Results sorted ascending by title |
| L04-P04 | Sort by title descending | Results sorted descending by title |
| L04-P05 | Sort by created date ascending/descending | Results follow requested date order |
| L04-P06 | Filter status only | Only incidents matching status |
| L04-P07 | Filter priority only | Only incidents matching priority |
| L04-P08 | Filter category only | Only incidents matching category |
| L04-P09 | Keyword matches title exactly or as a substring | Matching incidents are returned |
| L04-P10 | Keyword is lower-case against mixed-case title | Matching is case-insensitive |
| L04-P11 | Keyword is upper-case against lower-case title | Matching is case-insensitive |
| L04-P12 | Start date only | Includes incidents on/after start date |
| L04-P13 | End date only | Includes incidents on/before end date |
| L04-P14 | Both date bounds, inclusive range | Includes bounds and incidents between them |
| L04-P15 | Combine status + priority + category | Every filter must match |
| L04-P16 | Combine all filters and keyword/date range | Intersection of all filters |
| L04-P17 | Filter with no matches | Empty page with valid zero/empty content and correct metadata |
| L04-P18 | Soft-deleted incident is excluded | Hidden when hide-deleted policy is in effect |
| L04-P19 | Pagination remains valid when filters narrow result set | Page data and total counts reflect filtered set |

### Negative

| ID | Scenario | Expected result |
|---|---|---|
| L04-N01 | Start date after end date | 400 and no list data |
| L04-N02 | Equal start/end date | Valid for a one-day inclusive range |
| L04-N03 | Invalid status value | 400 |
| L04-N04 | Invalid priority value | 400 |
| L04-N05 | Negative page | 400 or documented framework default/rejection; requirement only allows pagination and implies invalid pagination should be rejected |
| L04-N06 | Zero/negative size | 400 |
| L04-N06 duplicate | Oversized page size | 400 or explicit maximum-size policy; policy must be confirmed |
| L04-N07 | Invalid sort field | 400; unknown field must not silently expose unsorted/unexpected behavior |
| L04-N08 | Invalid sort direction | 400 |
| L04-N09 | Malformed start/end date | 400 |
| L04-N10 | Keyword consists only of whitespace | Treated as no keyword or rejected; policy must be confirmed |
| L04-N11 | Unknown category value | 400 if category is enum-backed; category contract must be confirmed |
| L04-N12 | Unknown pagination parameter type | 400 |

## API/integration scenarios

### Assignment endpoint

- Existing incident + existing user -> successful assignment and response status reflects `ASSIGNED`.
- Missing incident -> not found.
- Missing assignee user -> not found or bad request according to confirmed contract.
- Null/blank/malformed assignee ID -> 400.
- Assignment on `ASSIGNED` -> reassignment succeeds.
- Assignment on `NEW` after reassignment attempt -> only allowed initial assignment.
- Assignment on statuses outside `NEW`/`ASSIGNED` -> rejected and state unchanged.
- Assignment endpoint must not act as general incident update or status update endpoint.

### Status endpoint

- All 6 valid transition cases return success.
- Representative invalid transitions and every matrix state pair are rejected.
- Status request for null/blank/unknown state -> 400.
- Missing incident -> 404.
- Status endpoint must not update assignee or incident content.

### List endpoint

- Correct page/size metadata, sorted result, and filter intersection.
- Case-insensitive title keyword at mixed casing.
- Open-ended date ranges with only start or only end.
- Reversed date range -> 400.
- Soft-deleted incidents excluded under hide policy.
- Empty result set retains valid pagination metadata.

## Blockers / not encoded in HTTP tests

- Exact controller routes and HTTP methods.
- Exact request/response field names and status enums.
- Exact HTTP success/error status codes and error response envelope.
- How category and priority are represented.
- Pagination defaults and maximum size.
- Sortable fields and sort parameter format.
- Date parameter format and timezone semantics.
- Test database: PostgreSQL availability, transaction cleanup, fixture setup, and soft-delete representation.

