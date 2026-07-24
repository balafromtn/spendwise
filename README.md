# Spendwise

A personal expense and income tracker Android app with Google Sheets cloud sync.

## Features

- **Dashboard** — current balance, income/expense totals, monthly savings, spending breakdown pie chart, recent transactions
- **Add/Edit/Delete Transactions** — Income or Expense, amount, category, date/time, notes, payment method (Cash/UPI/Card/Bank)
- **Categories** — default set (Food, Transport, Shopping, Bills, Entertainment, Healthcare, Education, Salary, Freelance, Investments) plus user-created custom categories
- **Monthly Reports** — month-over-month totals, category-wise breakdown with pie chart, income vs expense comparison
- **Budget Tracking** — monthly and per-category budgets with color-coded progress bars (green/yellow/red)
- **Dark Mode** — system-default aware, manual override in settings
- **Daily Reminders** — configurable time, on-device notifications
- **Cloud Sync** — auto-sync to Google Sheets via Sheets API v4, auto-creates sheets if missing
- **Filters** — filter transaction list by date range, category, payment method

## Tech Stack

- Kotlin, Jetpack Compose, Material 3
- Min SDK 26, Target SDK 35
- Google Sign-In (Credential Manager)
- Google Sheets API v4 + Drive API
- Room (SQLite) local database
- WorkManager background sync
- AlarmManager/WorkManager notifications
- StateFlow/ViewModel, Compose Navigation

## Project Structure

```
app/src/main/java/com/expensetracker/
├── data/
│   ├── local/          # Room entities, DAOs, database
│   └── remote/         # Auth, Sheets API, token management
├── domain/
│   ├── model/          # Domain models
│   └── usecase/        # Date utils, aggregation logic
├── ui/
│   ├── auth/           # Sign-in screen
│   ├── dashboard/      # Dashboard screen
│   ├── transaction/    # Add/Edit/Delete + list + filters
│   ├── categories/     # Category management
│   ├── reports/        # Monthly reports
│   ├── budget/         # Budget tracking
│   ├── settings/       # Settings
│   ├── components/     # Shared composables (pie chart, cards)
│   └── navigation/     # Nav host, bottom bar, routes
├── sync/               # SyncWorker, sync orchestration
├── notifications/      # Daily reminder scheduling
└── di/                 # AppContainer (manual DI)
```

## Setup

### 1. Open in Android Studio

Open `C:\Users\Balaji\Documents\vscode-codes\spendwise` as the project root.

### 2. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select a project
3. Enable **Google Sheets API** and **Google Drive API**
4. Configure **OAuth consent screen** (External, add scopes: `spreadsheets`, `drive.file`)
5. Add your Google email as a **test user**
6. Create credentials:
   - **Android** client ID (package: `com.expensetracker`, SHA-1 from debug keystore)
   - **Web application** client ID

Get your SHA-1 fingerprint:
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
```

### 3. Configure local.properties

Replace the placeholders in `local.properties`:
```
SPREADSHEET_ID=your_spreadsheet_id_here
WEB_CLIENT_ID=your_web_client_id_here
```

The spreadsheet ID is the long string in your Google Sheets URL:
`https://docs.google.com/spreadsheets/d/THIS_IS_THE_ID/edit`

### 4. Build & Run

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## How It Works

- Transactions are stored locally in Room database
- On add/edit/delete, changes sync to Google Sheets automatically (when online)
- Periodic background sync runs every 15 minutes as fallback
- App creates Transactions, Budgets, and Summary sheets if they don't exist
- Computed values (month, week number, summaries) are written as plain values, never formulas

## License

Personal project.
