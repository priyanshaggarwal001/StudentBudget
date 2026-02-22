# StudentBudget

StudentBudget is an Android expense tracking app focused on simple budgeting and day-to-day spending awareness. Built with modern Android tooling using Kotlin, Jetpack Compose, and Room.

## Features

- Track expenses with a clean, Compose-based UI
- Local persistence with Room database
- Material 3 theming

## Feature Details

### Expense and Income Tracking

- Add, edit, and delete transactions from the floating action button and history list.
- Each entry supports amount, category, optional note, date, type (expense or income), and payment method.
- Balance updates automatically as you add, edit, or remove entries.

### Categories

- Built-in categories for common student expenses and income sources.
- Custom categories with your own label and icon, used across entry, analytics, and history.

### Budgeting

- Set a monthly budget limit and track progress against it.
- Remaining amount and progress visualization update in real time.

### Dashboard Insights

- Current balance, month-to-date spend, transaction count, and average daily spend.
- Spending streak based on staying under the daily target.
- Smart insights such as budget usage and top category highlights.

### Analytics

- Month-by-month analytics with a month picker.
- Category distribution donut chart and category bar chart.

### Calendar View

- Month grid with daily spend highlights.
- Daily totals list for quick review.

### History and Search

- Month-scoped history grouped by date.
- Search by note or category, plus quick category filters.
- Tap to edit or delete transactions.

### Profile and Personalization

- Store profile details (name, phone, email, college, course).
- Set a profile photo from device storage.
- Quick stats: balance, all-time spend, average per day, total transactions.

### Settings and Data Management

- Currency symbol customization.
- Optional 4-digit PIN lock.
- Backup to JSON and restore from JSON.
- Factory reset to clear transactions, categories, and settings.

### Storage and Offline Use

- Local-only data storage using Room and SharedPreferences.
- Fully offline experience.

## Tech Stack

- Kotlin 17
- Jetpack Compose + Material 3
- Room (KSP)
- Gradle (Kotlin DSL)

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17
- Android SDK 34

### Build

Windows:

```bash
gradlew.bat assembleDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
```

### Run

1. Open the project in Android Studio.
2. Select an emulator or connected device.
3. Click Run.

## Project Structure

```
app/
  src/main/
    java/            App source
    res/             Resources (layouts, strings, themes)
  build.gradle.kts   App module configuration
```

## License

MIT. See LICENSE.
