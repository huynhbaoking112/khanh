## ADDED Requirements

### Requirement: Staff can manage book records
The system SHALL allow authorized staff to create, update, view, and deactivate book records with the fields needed by the library workflow.

#### Scenario: Create a book
- **WHEN** an authorized user saves a new book with code, title, author, publisher, publication year, and quantities
- **THEN** the system stores the book and makes it available for lookup and circulation

#### Scenario: Update a book
- **WHEN** an authorized user edits an existing book record
- **THEN** the system persists the updated metadata and displays the new values in the catalog

### Requirement: Available quantity is tracked for circulation
The system SHALL maintain total quantity and available quantity for every book and prevent available quantity from becoming negative.

#### Scenario: Borrow decreases availability
- **WHEN** a loan is created for an available book
- **THEN** the system decreases that book's available quantity by the borrowed amount

#### Scenario: Unavailable book cannot be borrowed
- **WHEN** a book has an available quantity of zero
- **THEN** the system rejects adding that book to a new loan
