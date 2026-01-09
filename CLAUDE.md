# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Sleep Timer is an Android application that provides sleep timer functionality with device lock capability. Built with Kotlin, Jetpack Compose, and modern Android development practices.

- **Package**: `io.github.pndhd1.sleeptimer`

## Architecture

Clean Architecture with Decompose navigation:

```
app/src/main/kotlin/io/github/pndhd1/sleeptimer/
├── domain/           # Business logic interfaces
│   ├── model/        # Data classes (TimerSettings, ActiveTimerData)
│   └── repository/   # Repository interfaces
├── data/             # Implementation layer
│   ├── repository/   # Repository implementations (DataStore + AlarmManager)
│   └── receiver/     # Broadcast receivers
├── di/               # Dependency injection (Metro framework)
├── ui/
│   ├── screens/      # Screen components using Decompose
│   └── theme/        # Material 3 theming
└── utils/            # Helpers (Formatter, Defaults)
```

### Key Patterns

- **Navigation**: Decompose library with stack (tabs) and slot (timer states) navigation
- **State Management**: Kotlin Flow + StateFlow
- **DI**: Metro framework - `@SingleIn(AppScope::class)`, `@ContributesBinding`
- **UI**: Jetpack Compose with Material 3
- **Persistence**: DataStore (preferences-based)

### Component Pattern

Each screen follows: Interface (contract) → `Default*Component` (implementation) → `Preview*Component` (previews) → `*Content` (Compose UI)

## Key Libraries

- **Decompose** - Architecture and navigation
- **Metro** - Dependency injection
- **DataStore** - Preferences persistence
- **Jetpack Compose** - UI framework
