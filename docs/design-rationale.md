# UI/UX Design Rationale and Accessibility Review

**Project:** P03 Library Lending System  
**Course:** Rapid Development of Java Applications Using Programming Frameworks  
**Academic year:** 2026/27  
**Institution:** Algebra Bernays University  
**Design tool:** Figma  
**Review date:** 9 November 2026

## 1. Design Objectives

The interface is designed for a public library management system with distinct public, member, librarian and administrator workflows. The main objectives are clarity, consistency, accessibility and straightforward implementation using Spring MVC and Thymeleaf.

The design separates bibliographic titles from their individual physical copies. Librarians manage inventory and physical circulation, while members can browse the catalog, manage eligible renewals, view reservations and submit purchase suggestions.

## 2. Visual Design and Reusable Components

The visual identity uses forest green (`#163A39`), warm ivory (`#F6F5F1`) and muted gold (`#C78E52`). Inter is used for typography, with consistent spacing, clear headings, white content surfaces and restrained status colors.

Eight reusable Figma component families were created: Button, Text Input, Select, Status Badge, Alert, Navigation Item, Catalog Table Row and Category Badge. Components use variants and Auto Layout to improve consistency and support future implementation.

The high-fidelity prototype includes six desktop screens covering catalog browsing, editing, title details, empty results, validation errors and AI-assisted purchase suggestion review. Two additional 390px mobile screens demonstrate responsive catalog and form layouts.

## 3. Interaction and Responsive Design

Desktop librarian screens use persistent sidebar navigation. Mobile screens replace this with a compact navigation header. The desktop catalog table becomes vertically stacked cards on mobile, while the edit form uses a single-column layout.

A clickable Figma prototype demonstrates navigation from the book catalog to title details, editing and returning to the details screen. The developer successfully tested the primary navigation flow in Figma presentation mode.

Form designs include explicit labels, required-field indicators and validation feedback. AI-assisted purchase suggestions are clearly separated from librarian decisions. AI recommendations are advisory and require human review.

## 4. WCAG Contrast Review

A read-only Figma inspection extracted text colors and their nearest solid-background colors from the six desktop and two mobile high-fidelity frames. Contrast ratios were calculated using the WCAG relative-luminance formula.

WCAG AA requires a minimum contrast ratio of 4.5:1 for normal text and 3:1 for large text. Essential non-text interface indicators generally require at least 3:1 contrast against adjacent colors.

Representative measured results:

| Color combination | Contrast ratio | Result |
|---|---:|---|
| Primary text on white | 13.20:1 | Pass |
| Secondary text on white | 5.79:1 | Pass |
| Secondary text on ivory | 5.30:1 | Pass |
| White text on forest green | 12.36:1 | Pass |
| Sidebar secondary text on forest green | 9.17:1 | Pass |
| Success text on pale green | 7.01:1 | Pass |
| Warning text on pale cream | 5.98:1 | Pass |
| Error text on pale red | 6.55:1 | Pass |
| Standard input border on white | 3.64:1 | Pass |
| Primary focus outline on white | 4.77:1 | Pass |

All 20 distinct text/background combinations extracted by this inspection met the 4.5:1 normal-text threshold.

Some lighter design colors require restrictions. The subtle border (`#DCE1DC`) has a contrast ratio of approximately 1.33:1 against white and must not serve as the only essential control boundary. The gold accent (`#C78E52`) has a ratio of approximately 2.83:1 against white and must not be the sole meaningful status or control indicator on that background.

## 5. Accessibility Limitations and Implementation Requirements

The contrast calculations provide evidence for the inspected color combinations, not proof of complete WCAG conformance. Transparent layers, dynamic states and all possible neighboring-color combinations have not been exhaustively assessed.

The implemented application must additionally be tested for keyboard navigation, focus order, screen-reader labels, error announcements, responsive reflow and browser-rendered contrast. Automated accessibility testing should be supplemented with manual verification.

The design supports accessibility through labeled fields, textual status indicators, visible validation messages, responsive layouts and reusable focus-state components.

## 6. Conclusion

The Figma prototype establishes a consistent visual and interaction foundation for the P03 Library Lending System. The inspected text contrast combinations satisfy WCAG AA requirements, while identified lighter colors have documented usage restrictions.

The prototype remains a design artifact. Application functionality, accessibility behavior and backend integration will be implemented and verified during subsequent development milestones.
