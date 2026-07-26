# Database Schema — Expense AI Manager

Room Database v1: `expense_ai_manager.db`

## Tables

### expenses
Primary table for all financial transactions.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | INTEGER | PK AUTO | Unique identifier |
| title | TEXT | NO | Display title |
| description | TEXT | NO | Longer description |
| amount | REAL | NO | Transaction amount in native currency |
| currency | TEXT | NO | ISO 4217 code: MYR, INR, USD, etc. |
| category | TEXT | NO | ExpenseCategory enum name |
| merchant | TEXT | NO | Merchant/store name (from OCR or manual) |
| date | INTEGER | NO | Unix epoch milliseconds |
| paymentMethod | TEXT | NO | PaymentMethod enum name |
| notes | TEXT | NO | Free-form notes |
| tags | TEXT | NO | Comma-separated tag list |
| receiptImagePath | TEXT | YES | Absolute path to receipt image |
| isRecurring | INTEGER | NO | 0=false, 1=true |
| taxAmount | REAL | NO | Tax/GST amount |
| type | TEXT | NO | EXPENSE, INCOME, or TRANSFER |

### budgets
Monthly budget allocations.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | INTEGER | PK AUTO | Unique identifier |
| name | TEXT | NO | Budget label |
| amount | REAL | NO | Budget limit |
| currency | TEXT | NO | Currency code |
| category | TEXT | YES | NULL = all categories |
| month | INTEGER | NO | 1–12 |
| year | INTEGER | NO | Full year, e.g. 2024 |
| alertThreshold | REAL | NO | 0.0–1.0, default 0.80 |
| createdAt | INTEGER | NO | Creation timestamp |

### incomes
Income records.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | INTEGER | PK AUTO | Unique identifier |
| title | TEXT | NO | Income source label |
| amount | REAL | NO | Amount |
| currency | TEXT | NO | Currency code |
| source | TEXT | NO | ExpenseCategory (SALARY, FREELANCE…) |
| date | INTEGER | NO | Unix epoch milliseconds |
| notes | TEXT | NO | Notes |
| isRecurring | INTEGER | NO | Boolean |
| tags | TEXT | NO | Comma-separated |

### transfers
Cross-currency money transfers (MYR→INR etc.).

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | INTEGER | PK AUTO | Unique identifier |
| title | TEXT | NO | Transfer label |
| amount | REAL | NO | Amount sent (in fromCurrency) |
| fromCurrency | TEXT | NO | Source currency code |
| toCurrency | TEXT | NO | Destination currency code |
| exchangeRate | REAL | NO | Rate at time of transfer |
| convertedAmount | REAL | NO | amount × exchangeRate |
| fee | REAL | NO | Transfer fee (in fromCurrency) |
| date | INTEGER | NO | Unix epoch milliseconds |
| notes | TEXT | NO | Notes |
| recipient | TEXT | NO | Recipient name |
| transferMethod | TEXT | NO | Bank Transfer, Wise, etc. |

### recurring_expenses
Templates for auto-recurring expense creation.

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| id | INTEGER | PK AUTO | Unique identifier |
| title | TEXT | NO | Expense title template |
| amount | REAL | NO | Amount |
| currency | TEXT | NO | Currency code |
| category | TEXT | NO | ExpenseCategory enum |
| frequency | TEXT | NO | RecurringFrequency enum |
| nextDueDate | INTEGER | NO | Next creation timestamp |
| reminderEnabled | INTEGER | NO | Boolean |
| paymentMethod | TEXT | NO | PaymentMethod enum |
| notes | TEXT | NO | Notes |

## Enums

### ExpenseCategory
`FOOD_DINING`, `TRANSPORT`, `SHOPPING`, `HEALTH_FITNESS`, `ENTERTAINMENT`,
`BILLS_UTILITIES`, `EDUCATION`, `TRAVEL`, `GROCERIES`, `TRANSFER`,
`SALARY`, `FREELANCE`, `INVESTMENT`, `RENTAL`, `OTHER`

### PaymentMethod
`CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `E_WALLET`, `UPI`, `CRYPTO`, `OTHER`

### RecurringFrequency
`DAILY` (1d), `WEEKLY` (7d), `BIWEEKLY` (14d), `MONTHLY` (30d), `QUARTERLY` (90d), `YEARLY` (365d)

### TransactionType
`EXPENSE`, `INCOME`, `TRANSFER`
