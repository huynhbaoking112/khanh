## ADDED Requirements

### Requirement: Staff login is required
The system SHALL require an operator to authenticate before any management workspace is displayed.

#### Scenario: Successful login
- **WHEN** the operator submits a valid username and password
- **THEN** the system opens the main desktop workspace for that account

#### Scenario: Failed login
- **WHEN** the operator submits invalid credentials
- **THEN** the system keeps the login screen visible and displays an authentication error

### Requirement: Authorized actions are role-aware
The system SHALL show and enforce only the actions that are available to the authenticated account role.

#### Scenario: Librarian access
- **WHEN** a librarian account logs in successfully
- **THEN** the system shows circulation, book lookup, and reader lookup actions without administrative maintenance actions

#### Scenario: Administrative access
- **WHEN** an administrator account logs in successfully
- **THEN** the system shows catalog management and reader management actions in addition to circulation actions
