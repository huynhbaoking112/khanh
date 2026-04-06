## ADDED Requirements

### Requirement: Staff can manage reader profiles
The system SHALL allow authorized staff to create, update, view, and deactivate reader profiles with contact information, borrowing limit, and status.

#### Scenario: Create a reader
- **WHEN** an authorized user saves a new reader profile with required identity and contact fields
- **THEN** the system stores the reader and allows that profile to be searched in circulation workflows

#### Scenario: Disable a reader
- **WHEN** an authorized user changes a reader status to inactive
- **THEN** the system keeps the reader record but blocks new borrowing transactions for that profile

### Requirement: Reader eligibility is validated before borrowing
The system SHALL validate the reader's existence, active status, and current borrowed count before a loan is created.

#### Scenario: Eligible reader can proceed
- **WHEN** a reader exists, is active, and has borrowed fewer books than the allowed limit
- **THEN** the system allows the circulation flow to continue

#### Scenario: Borrowing limit reached
- **WHEN** a reader already has 3 active borrowed books
- **THEN** the system rejects the new loan request and shows a limit violation
