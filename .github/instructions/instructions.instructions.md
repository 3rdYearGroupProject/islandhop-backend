---
applyTo: '**'
---
Coding standards, domain knowledge, and preferences that AI should follow.

# General Copilot Instructions

## Core Development Principles

### 1. Code Quality Standards
- **Clean Code**: Follow SOLID principles, DRY, and KISS
- **Consistency**: Match existing patterns in the codebase
- **Documentation**: Always document new components and functions 
- **EditingFiles**: update teh existing files only when necessary, and always ensure the changes are well-justified and documented. Dont keep creating new files.

### 2. Implementation Approach
- Always:
  - Clarify requirements with follow-up questions
  - Present a solution plan before coding
  - Highlight potential edge cases
  - Suggest alternatives with pros/cons
  - Always create components for the reusable parts
  - Go through the code with a critical eye before updating anything
  - Make sure to update routes and navigation if necessary
- Never:
  - Make assumptions about unclear requirements
  - Implement without confirmation on approach
  - Break existing functionality without warning

### 3. Project Workflow
```mermaid
graph TD
    A[Receive Task] --> B[Analyze Requirements]
    B --> C{Need Clarification?}
    C -->|Yes| D[Ask Questions]
    C -->|No| E[Propose Solution]
    D --> E
    E --> F[Get Approval]
    F --> G[Implement]
    G --> H[Document]