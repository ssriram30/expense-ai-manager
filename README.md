# 💰 Expense AI Manager

> **Your Smart Finance Companion** — Track expenses in MYR & INR, scan receipts with OCR, get AI-powered spending insights, and manage budgets with beautiful Material You design.

[![Build Status](https://github.com/YOUR_USERNAME/expense-ai-manager/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/expense-ai-manager/actions)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.04-blue.svg)](https://developer.android.com/jetpack/compose)

---

## 📱 Features

### 🎯 Core Expense Tracking
- **Receipt OCR Scanning** — Point camera at any receipt; ML Kit automatically extracts merchant, amount, date, tax, and category
- **Manual Entry** — Rich form with title, description, amount, currency, category, merchant, payment method, notes, tags, and date
- **Multi-Currency** — Full MYR and INR support with separate totals; no currency mixing
- **Transaction Types** — Expenses, Income, and Transfers tracked separately

### 🌐 Malaysia–India Focus
- **MYR & INR Dashboards** — View separate or combined totals
- **Money Transfer Tracking** — Record MYR→INR transfers with live exchange rate, fee, and recipient
- **Live Exchange Rates** — Auto-fetches MYR/INR rate via free API
- **Currency Comparison** — Side-by-side MYR vs INR analytics

### 📊 Analytics & Charts
- **Pie Charts** — Category breakdown (animated, donut style)
- **Line Charts** — 12-month spending trend with smooth bezier curves
- **Bar Charts** — Monthly comparison bars
- **Budget vs Actual** — Visual progress bars per budget
- **Filter by** — Date range, category, merchant, payment method, currency
- **Period Selection** — This month, last month, 3/6 months, full year, all time

### 🤖 AI-Powered Insights
- Spending spike detection (>20% vs last month)
- Budget warning & exceeded alerts
- Frequent merchant detection
- Recurring subscription detection
- Monthly spending prediction
- Savings rate analysis and goals
- Unusual expense detection (2.5σ outlier)
- Positive savings streak recognition

### 💼 Budget Planning
- Monthly and category-wise budgets
- Configurable alert thresholds (50%–95%)
- Colored status cards (green/orange/red)
- Month navigation

### 🔄 Recurring Expenses
- Daily, weekly, bi-weekly, monthly, quarterly, yearly
- Auto-creates expense on due date via WorkManager
- Reminder notifications

### 🔒 Security
- **Biometric Authentication** — Fingerprint / Face ID (androidx.biometric)
- **PIN Lock** — SHA-256 hashed 4–6 digit PIN
- **Encrypted Preferences** — DataStore for sensitive settings

### 📤 Export
- **CSV Export** — All expenses with full metadata (OpenCSV)
- **PDF Report** — Formatted PDF with charts summary (Android PdfDocument)
- Share via any Android app

### 🔁 Backup & Restore
- Local database backup to internal storage
- One-tap restore
- Timestamped backup files

### 🎨 Premium UI
- Material Design 3 + Material You dynamic colors
- Dark / Light theme
- Glassmorphism hero cards
- Smooth animations (enter/exit, counters, progress bars)
- Swipe-to-delete on transactions
- Pull-to-refresh ready

---

## 🏗️ Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── dao/         # Room DAOs (Flow-based queries)
│   │   ├── entity/      # Room entities with domain mapping
│   │   └── database/    # AppDatabase (Room)
│   ├── remote/          # Retrofit (ExchangeRateApi)
│   ├── repository/      # Repository implementations
│   └── datastore/       # UserPreferencesDataStore
├── domain/
│   ├── model/           # Pure Kotlin domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # (available for future use cases)
├── presentation/
│   ├── auth/            # Biometric / PIN screen
│   ├── dashboard/       # Home screen
│   ├── expense/         # List, Add/Edit, Detail screens
│   ├── ocr/             # Camera + ML Kit OCR screen
│   ├── analytics/       # Charts & analytics
│   ├── budget/          # Budget planner
│   ├── insights/        # AI insights
│   ├── income/          # Income tracking
│   ├── transfer/        # Money transfers
│   ├── search/          # Full-text search
│   ├── settings/        # App settings
│   ├── navigation/      # NavGraph + Screen routes
│   └── components/      # Reusable Compose components
├── di/                  # Hilt modules
├── util/                # Utilities (OCR, Export, AI Engine, etc.)
└── worker/              # WorkManager workers
```

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room 2.6 |
| Async | Coroutines + Flow |
| Camera | CameraX 1.3 |
| OCR | ML Kit Text Recognition |
| Network | Retrofit + OkHttp |
| Images | Coil |
| Storage | DataStore Preferences |
| Background | WorkManager |
| Auth | AndroidX Biometric |
| Export | OpenCSV + Android PdfDocument |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- JDK 17

### Build Locally

```bash
git clone https://github.com/YOUR_USERNAME/expense-ai-manager.git
cd expense-ai-manager
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Install on Device

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## ⬇️ Download APK from GitHub Releases

Every push to `main` and every version tag triggers GitHub Actions which:
1. Runs all unit tests
2. Builds a signed release APK
3. Uploads it as a GitHub Release artifact

**To download:**
1. Go to the [Releases page](https://github.com/YOUR_USERNAME/expense-ai-manager/releases)
2. Download `app-release.apk` (or `app-debug.apk`)
3. Transfer to your Android device
4. Enable **"Install from Unknown Sources"** in Android Settings → Security
5. Open the APK to install

---

## 🔑 GitHub Secrets Setup (for Signed APK)

Go to your GitHub repo → **Settings → Secrets and variables → Actions** and add:

| Secret | Description |
|--------|-------------|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

### Generate Keystore

```bash
keytool -genkey -v -keystore release.keystore \
  -alias expenseai -keyalg RSA -keysize 2048 -validity 10000

# Convert to Base64 for GitHub Secret
base64 -i release.keystore | pbcopy   # macOS
base64 -w 0 release.keystore          # Linux
```

---

## 🗄️ Database Schema

### expenses
| Column | Type | Description |
|--------|------|-------------|
| id | INTEGER PK | Auto-generated |
| title | TEXT | Expense title |
| description | TEXT | Detailed description |
| amount | REAL | Amount (always in original currency) |
| currency | TEXT | Currency code (MYR/INR/USD…) |
| category | TEXT | ExpenseCategory enum name |
| merchant | TEXT | Store / merchant name |
| date | INTEGER | Unix timestamp (ms) |
| paymentMethod | TEXT | PaymentMethod enum name |
| notes | TEXT | Additional notes |
| tags | TEXT | Comma-separated tags |
| receiptImagePath | TEXT | Path to scanned receipt image |
| isRecurring | INTEGER | 0/1 boolean |
| taxAmount | REAL | GST/SST amount |
| type | TEXT | EXPENSE/INCOME/TRANSFER |

### incomes, transfers, budgets, recurring_expenses
See [DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) for full schema.

---

## 📋 Categories

| Category | Emoji | Color |
|----------|-------|-------|
| Food & Dining | 🍔 | Orange |
| Transport | 🚗 | Blue |
| Shopping | 🛍️ | Pink |
| Health & Fitness | 💊 | Green |
| Entertainment | 🎬 | Purple |
| Bills & Utilities | 💡 | Red |
| Education | 📚 | Cyan |
| Travel | ✈️ | Yellow |
| Groceries | 🛒 | Teal |
| Transfer | 💸 | Teal |
| Other | 📦 | Gray |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

```
MIT License — Copyright (c) 2024 Sriram
```

---

*Built with ❤️ for Malaysians managing finances across borders*
