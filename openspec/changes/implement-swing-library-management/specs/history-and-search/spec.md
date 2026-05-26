## ADDED Requirements

### Requirement: Users can search the catalog
The system SHALL provide a catalog search flow that returns matching books by code, title, author, or availability status.

#### Scenario: Search by keyword
- **WHEN** a user enters a title or author keyword
- **THEN** the system shows the matching books in the result table

#### Scenario: Search by availability
- **WHEN** a user filters the catalog for unavailable books
- **THEN** the system shows only books whose available quantity is zero

### Requirement: Staff can review circulation history
The system SHALL allow staff to view loan history for a reader or a loan, including due dates, returned status, and associated fine information.

#### Scenario: View reader history
- **WHEN** a staff user opens the history view for a reader
- **THEN** the system lists that reader's current and past loans with statuses and dates

#### Scenario: View fine information
- **WHEN** a selected loan has an associated fine
- **THEN** the system displays the fine amount, reason, and payment status with the loan details
