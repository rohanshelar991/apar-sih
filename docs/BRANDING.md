# APAAR GARUDA NETRA branding

**AICTE Student Data Intelligence & Verification Platform**

*A Vision for Academic Trust.*

The product continues to support Smart India Hackathon 2026, Problem Statement
26263, AICTE Regulation Bureau, and the Smart Automation theme. Team AuthenIQ and
existing prototype/synthetic-data disclosures retain their meaning.

## Identity and presentation

The original SVG mark combines an abstract Garuda wing, an eye, and a shield.
It is a product illustration, not an official government emblem. The source is
`frontend/public/brand/garuda-mark.svg`; the decorative hero artwork is
`frontend/public/brand/garuda-wing.svg`.

| Use | Colour |
| --- | --- |
| Navigation and headings | `#101D35` |
| Primary actions | `#244B83` |
| Brand accent | `#C49A45` |
| Page background | `#F8F6F0` |
| Cards | `#FFFFFF` |
| Borders | `#E8E9EC` |
| Success | `#23865B` |
| Warning | `#D99A32` |
| Error | `#C64C4C` |
| Muted details | `#718096` |

Shared Tailwind tokens apply the identity to dashboards, tables, forms, charts,
status chips, and modals. Status labels and meanings remain unchanged. Gold is
an accent; darker gold supports readable text on light backgrounds. Keyboard
focus has a visible outline, login inputs have associated labels, controls have
larger touch targets, and reduced-motion preferences are respected.

## Compatibility

Product names in the UI, browser metadata, API documentation, verification
issuer labels, generated reports, startup banners, README, and demo narration
sources use the new identity. Routes, API shapes, login handlers, role gates,
validation rules, and database definitions are preserved.

Internal compatibility identifiers remain unchanged: Java package/class names,
Maven artifact ID and JAR filename, npm package name, database names, storage keys,
repository URLs, and existing media filenames. These are technical references,
not display branding.

Existing recorded video, audio, and thumbnails are retained. Their embedded
screens and narration may show the previous identity; updating a source script
does not regenerate a recording. A new recording is needed to refresh that media.

## Verification of this refresh

- Frontend production build and TypeScript compilation pass.
- Backend tests: 38 discovered, 21 passed, 17 skipped, no failures or errors.
  Skips cover Docker-dependent and opt-in external-database tests.
- Backend packaging passes after releasing the running JAR's Windows file lock.
- Browser checks confirm desktop, tablet, and mobile login layouts; mobile has
  no horizontal overflow and both login inputs have associated labels.
- The video-demo link opens the existing walkthrough with the new page heading.
- Source comparison confirms unchanged event handlers, route/form bindings,
  React hooks, API client, authentication context, and database/rule definitions.
- Authenticated end-to-end persona, upload, correction, report, QR, and audit
  checks were not completed: automatic approval review blocked backend launches.
  These checks must be run against a disposable H2 demo, not by seeding production.
