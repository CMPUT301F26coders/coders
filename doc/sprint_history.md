# Sprint History

## Sprint 1 (Oct 21 – Oct 28)

### Planned User Stories
- US 02.03.01: View created campsites — Avery Rowley
- - Pre-user story: Set up Firebase configuration and Firestore dependency — George Mikhaiel
- Pre-user story: Create the initial backend data structure skeleton — George Mikhaiel
- US 01.07.03, US 01.08.01, US 01.09.01, US 03.11.01: Build backend profile support for profile information, profile deletion, device identification, and admin roles — George Mikhaiel
- US 02.01.01–02.01.04, US 02.03.01, US 03.01.01: Build campsite backend support for registering, detailing, updating, viewing, and removing campsites — George Mikhaiel
- User login — Member B
- Campsite creation — Member C

### Sprint Review
- Completed:
  - Pre-user story: Set up Firebase configuration and Firestore dependency — George Mikhaiel
  - US 01.07.03, US 01.08.01: Created profile models and initial `ProfileRepo` add, update, delete, and load operations — George Mikhaiel
  - US 02.01.01–02.01.04, US 02.03.01, US 03.01.01: Created campsite models and initial `CampsiteRepo` Firestore operations — George Mikhaiel
  - US 01.02.01, US 02.04.01–02.05.01, US 01.04.01–01.04.03, US 01.06.01–01.06.02, US 02.07.01–02.07.03: Created initial waitlist, lottery, and reservation data models — George Mikhaiel
- Not completed:
  - US 01.09.01: Local session ID saving and automatic profile restoration were not completed in Sprint 1.
  - US 03.11.01: Admin profiles could not yet act as campsite owners and/or entrants.
  - US 01.01.01–01.01.05, US 01.12.01, US 01.13.01, US 02.02.01: Search, QR scanning/generation, and rating behaviour were not implemented in this backend sprint.

## Sprint 2 (Oct 29 – Nov 5)

### Planned User Stories
- US 02.07.03, 02.07.02, 02.07.01, 02.03.01: Reservations screens and finish view created campsites — Avery Rowley
- US 01.01.04, US 01.06.01–01.06.02, US 02.07.01–02.07.03: Build backend data-structure support for campsite summaries and reservations — George Mikhaiel
- Complete user login — Member B
- Create reservation screen — Member C

### Sprint Review
- Completed:
  - US 01.09.01: Added stable profile IDs and DataStore session ID saving and restoration — George Mikhaiel
  - US 01.07.03: Added profile-field validation and profile creation support — George Mikhaiel
  - US 02.10.01–02.10.02: Updated notification models to distinguish owner-sent and owner-received notifications — George Mikhaiel
- Not completed:
  - US 01.01.04: A separate campsite summary backend model was not completed.
  - US 01.05.01, US 01.06.01–01.06.02, US 02.07.01–02.07.03: A reservation repository and full reservation-processing backend were not completed.
  - US 02.04.01–02.05.01: Lottery setup and automatic winner-drawing behaviour were not completed.
  
# Sprint 3 (Nov 6 - Nov 13)

###
