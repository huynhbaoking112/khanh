## ADDED Requirements

### Requirement: System can create a loan
The system SHALL create a loan only after validating the reader, validating each selected book, and applying the default due date of 7 days from the loan date.

#### Scenario: Successful loan creation
- **WHEN** a librarian submits a loan request for an active reader and all selected books are available within the borrowing limit
- **THEN** the system creates a `LOAN` record, creates `LOAN_DETAIL` records, and sets the due date to 7 days after the loan date

#### Scenario: Loan creation fails on invalid input
- **WHEN** the request contains an inactive reader, a missing reader, or a book with no available quantity
- **THEN** the system rejects the loan and does not persist any circulation changes

### Requirement: System can process book returns and fines
The system SHALL update return status, restore available quantity, and create a fine when the return date is later than the due date.

#### Scenario: On-time return
- **WHEN** a librarian confirms the return of borrowed books on or before the due date
- **THEN** the system marks the relevant loan details as returned and increases available quantity for those books

#### Scenario: Late return
- **WHEN** a librarian confirms the return after the due date
- **THEN** the system creates or updates a fine for that loan using `late_days * 5000`

### Requirement: System can renew an active loan
The system SHALL allow renewal only for a loan that is still active and not fully returned.

#### Scenario: Successful renewal
- **WHEN** a librarian renews an active loan that is still eligible for renewal
- **THEN** the system updates the loan with a new due date and reports success

#### Scenario: Renewal is rejected for closed loan
- **WHEN** a librarian attempts to renew a loan that has already been fully returned or completed
- **THEN** the system rejects the renewal request and preserves the original dates
