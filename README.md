# Cifra

**A calculator with a private vault for notes.** Cifra looks and works like an ordinary calculator. Type your PIN and press `=`, and instead of a result you get a hidden space with notes and extra tools.

Cifra is a demo project built in Kotlin with Jetpack Compose. It is meant to be built in a few hours and to show a range of Android skills in one small, finishable app: a correct calculation engine, local persistence, MVVM, navigation, and unit tests.

> **Status:** in development. The screens are designed (see [Screens](#screens)) and the implementation follows the plan in [Roadmap](#roadmap).

---

## Table of contents

- [Concept](#concept)
- [Goals and non-goals](#goals-and-non-goals)
- [Features](#features)
- [Screens](#screens)
- [Architecture](#architecture)
- [Calculation engine](#calculation-engine)
- [Data model](#data-model)
- [Security](#security)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [MVP acceptance criteria](#mvp-acceptance-criteria)
- [Demo script](#demo-script)
- [Open decisions](#open-decisions)
- [Future ideas](#future-ideas)
- [License](#license)

---

## Concept

The app opens straight on a fully working calculator. There is no home screen, menu, or welcome screen that could give the vault away. If the user types their PIN and presses `=`, the app opens the **vault** instead of calculating.

A calculator and a notes app are each a crowded category on their own. Putting them together gives the app a clear identity and a short story to tell in a demo.

### Problems it addresses

- Keeping quick notes that are not visible at a glance on the phone.
- Having a reliable calculator, free of floating-point errors, with a history.

### Intended audience

Anyone who wants light privacy. For this project, the audience is mainly reviewers, interviewers, and teachers evaluating the work.

---

## Goals and non-goals

**Goals**

- A correct calculator with operator precedence and controlled error handling.
- Full CRUD for notes with local persistence.
- Vault access through a PIN typed on the calculator keypad.
- Clean code with separated layers and unit tests on the calculation logic.

**Non-goals**

- Cloud sync, user accounts, or any backend.
- A full scientific calculator.
- Production-grade encryption (see [Security](#security)).

---

## Features

### Calculator (priority 1)

- Digits, decimal point, `+ − × ÷`, `%`, `±`, `C`, and delete last digit.
- Parentheses (optional for the MVP).
- Evaluation with `BigDecimal`, so `0.1 + 0.2` gives `0.3` and not `0.30000000000000004`.
- Correct precedence: `2 + 3 × 4 = 14`.
- Controlled errors: division by zero, incomplete expression, input length overflow.
- History of the last N operations, with the option to reuse a result.
- Readable output: thousands separator and trimmed trailing zeros.

### Notes vault (priority 2)

- List notes sorted by last modified date.
- Create, edit, and delete (with confirmation or undo).
- Text search (optional).
- Fields: title, content, created date, modified date.

### PIN access (priority 2)

- First use: the app asks the user to set a PIN (4 to 6 digits).
- Normal use: type the PIN on the calculator and press `=` to open the vault.
- Change the PIN from inside the vault.
- If the PIN does not match, `=` behaves as a normal calculation, so the vault never gives itself away.

### Extra tools (priority 3, only if time allows)

- Tip and bill-splitting calculator (reuses the calculation engine).
- Discount calculator.
- Basic unit converter (length, weight, temperature).
- Biometric lock to enter the vault.

---

## Screens

| Screen                   | Content                                                    |
| ------------------------ | ---------------------------------------------------------- |
| `CalculatorScreen`       | Display (expression and result), keypad, access to history |
| `HistorySheet`           | List of previous operations (bottom sheet)                 |
| `VaultHomeScreen`        | Note list, button to create a note, access to tools        |
| `NoteEditorScreen`       | Edit title and content                                     |
| `SetPinScreen`           | Set or change the PIN                                      |
| `ToolsScreen` (optional) | Tips, discounts, unit converter                            |

**Main flow**

```
CalculatorScreen
   ├─ (PIN + "=") ──► VaultHomeScreen ──► NoteEditorScreen
   │                        └──► ToolsScreen
   └─ (history) ──► HistorySheet
```

Navigation uses Navigation Compose with typed destinations. The start destination is decided at launch: if no PIN hash is stored yet, the app goes to `SetPinScreen`, and after that it always opens on `CalculatorScreen`.

The UI design follows a dark, minimal style with a single accent color reserved for the main action of each screen. A visual mockup of every screen is available in [`docs/cifra-screens-en.html`](docs/cifra-screens-en.html).

---

## Architecture

**MVVM** with unidirectional data flow.

```
UI (Compose)
   │  events
   ▼
ViewModel (StateFlow<UiState>)
   │
   ▼
Domain (use cases + calculation engine, pure Kotlin)
   │
   ▼
Data (Room, DataStore)
```

- The **UI** only renders state and sends events.
- **ViewModels** expose a `StateFlow<UiState>` and handle user events.
- The **domain** layer is plain Kotlin with no Android dependencies, which keeps it easy to test.
- The **data** layer wraps Room (notes) and DataStore (PIN hash and preferences) behind repositories.

**Dependency injection:** to keep setup short, the project uses manual injection through an `AppContainer` class instead of Hilt. Hilt can be added later.

---

## Calculation engine

The engine is the part with the most technical value. It is written in pure Kotlin so it can be covered by unit tests.

**Pipeline**

1. **Tokenizer:** turns the input string into tokens (number, operator, parenthesis).
2. **Parser:** shunting-yard (to postfix notation) or recursive descent.
3. **Evaluator:** evaluates with `BigDecimal` and a defined `MathContext`, with explicit scale and rounding mode for division.

**Cases the engine must pass**

| Input                            | Expected                                                    |
| -------------------------------- | ----------------------------------------------------------- |
| `2 + 3 × 4`                      | `14`                                                        |
| `0.1 + 0.2`                      | `0.3`                                                       |
| `10 ÷ 0`                         | controlled error                                            |
| `5 %`                            | `0.05` (behavior to be defined and documented)              |
| `-3 + 5`, `2 × -3`               | unary operator handled correctly                            |
| Expression ending in an operator | one consistent policy: ignore the operator or show an error |

---

## Data model

**Room**

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
```

**History:** it can live in memory (faster) or in a Room table `history(id, expression, result, createdAt)`. For the demo, an in-memory list limited to the last 50 entries is enough.

**DataStore:** stores the PIN hash, the salt, and simple preferences.

---

## Security

Cifra provides **light privacy, not strong protection**, and this README says so on purpose.

- **PIN:** never stored in plain text. Only a salted hash is stored (for example with PBKDF2 or similar).
- **Notes:** by default, Room stores data in plain text in the app's private storage. That is enough for a demo.
- **Optional improvement:** encrypt the database with SQLCipher, or encrypt the content with a key held in the Android Keystore.
- **Biometrics:** `BiometricPrompt` as an optional second factor.
- **Useful details:** `FLAG_SECURE` on the vault screens to block screenshots and the recent-apps preview, and disabling automatic backup if you want to keep notes out of backups.

---

## Tech stack

| Area        | Technology                                                       |
| ----------- | ---------------------------------------------------------------- |
| Language    | Kotlin                                                           |
| UI          | Jetpack Compose + Material 3                                     |
| State       | ViewModel + StateFlow                                            |
| Navigation  | Navigation Compose                                               |
| Persistence | Room (notes), DataStore (PIN and preferences)                    |
| Async       | Coroutines + Flow                                                |
| Tests       | JUnit (calculation engine), ViewModel tests with coroutines test |
| Injection   | Manual (fast) or Hilt                                            |

---

## Project structure

```
com.yourapp.cifra
├── ui
│   ├── calculator      // CalculatorScreen, CalculatorViewModel, KeypadButton
│   ├── vault           // VaultHomeScreen, NoteEditorScreen, ViewModels
│   ├── pin             // SetPinScreen, PinViewModel
│   ├── tools           // utility screens (optional)
│   ├── navigation      // NavHost and routes
│   └── theme
├── domain
│   ├── calculator      // ExpressionTokenizer, ExpressionParser, Evaluator
│   ├── model           // Note, HistoryEntry
│   └── usecase         // EvaluateExpression, SaveNote, VerifyPin, etc.
├── data
│   ├── local           // Room: NoteEntity, NoteDao, AppDatabase
│   ├── prefs           // DataStore: PIN hash, preferences
│   └── repository      // NoteRepository, PinRepository, HistoryRepository
└── di                  // AppContainer (or Hilt)
```

---

## Getting started

### Requirements

- Android Studio (a recent stable version)
- JDK 17
- An Android device or emulator

### Run the app

```bash
git clone https://github.com/<your-user>/cifra.git
cd cifra
```

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Select a device or emulator and press **Run**.

On first launch the app asks you to set a PIN. After that it opens directly on the calculator. To open the vault, type your PIN and press `=`.

---

## Testing

- **Unit tests (required):** the calculation engine, using the table of cases in [Calculation engine](#calculation-engine).
- **ViewModel tests:** the state changes correctly when keys are pressed, and `=` with the correct PIN emits the navigation event.
- **Manual checks:** screen rotation, process death and restore, wrong PIN, deleting a note, empty list.

Run the unit tests with:

```bash
./gradlew test
```

---

## Roadmap

The project is planned as a 4 to 6 hour build.

| Phase                 | Time             | Deliverable                                 |
| --------------------- | ---------------- | ------------------------------------------- |
| 1. Setup              | 20 min           | Compose project, theme, empty navigation    |
| 2. Calculation engine | 60–90 min        | Tokenizer, parser, and evaluator with tests |
| 3. Calculator UI      | 60 min           | Display, keypad, history                    |
| 4. Notes with Room    | 60–90 min        | CRUD and list                               |
| 5. PIN and access     | 45 min           | Set PIN, verify on `=`                      |
| 6. Polish             | 30–60 min        | Empty states, errors, icons, dark mode      |
| 7. Extras             | Whatever is left | Utilities or biometrics                     |

**Rule of thumb:** do not move on to phase 4 until the calculator is solid. A flawless calculator with basic notes is worth more than the opposite.

---

## MVP acceptance criteria

- [ ] All basic operations give the correct result, including precedence.
- [ ] The app does not crash on invalid input.
- [ ] History shows past operations and lets you reuse results.
- [ ] Notes can be created, edited, and deleted, and they persist after closing the app.
- [ ] The PIN is stored as a hash and opens the vault only when it matches.
- [ ] Rotating the screen does not lose the calculator state.

---

## Demo script

A 2 to 3 minute walkthrough:

1. Show the calculator: one calculation with precedence and the `0.1 + 0.2` case.
2. Trigger an error (`÷ 0`) and show that it is handled well.
3. Open the history and reuse a result.
4. Type the PIN and press `=`: the vault opens.
5. Create a note, close it, then close and reopen the app to show persistence.
6. If available, open a tool such as bill splitting.

---

## Open decisions

| Decision                  | Options                                                                                          |
| ------------------------- | ------------------------------------------------------------------------------------------------ |
| Behavior of `%`           | Simple-calculator style (percentage of the first operand) or `x / 100`. Pick one and document it |
| Hilt vs manual injection  | Manual if time is tight                                                                          |
| History in memory vs Room | Memory for the demo, Room if persistence is wanted                                               |
| Note encryption           | Plain text for the demo, SQLCipher or Keystore for more seriousness                              |
| Scope of tools            | One or two at most, no more fronts                                                               |

---

## Future ideas

- Scientific calculator (trigonometry, powers, roots).
- Tags and pinned notes.
- Export and import of encrypted notes.
- Calculator widget.
- Compose Multiplatform version to share the calculation engine.

---

## License

Choose a license for the project (for example MIT) and add a `LICENSE` file.
