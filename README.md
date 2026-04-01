# MarketLens

**MarketLens** is an Android finance insights app built to help users track the stock market through **live quotes, interactive charts, financial news, watchlists, volatility alerts, and news-driven market signals**.

Unlike trading apps that push speculation, MarketLens is designed around an educational and explainable philosophy: **inform, not advise**.

---

## Overview

MarketLens combines market data, news context, analytics, and user-defined alerts into a single mobile experience.

### Current scope includes:
- **9 app screens**
- **2 background workers**
- **3 chart timeframes** (1M, 3M, 1Y)
- **4 investment horizons**
- **8 market sectors** for news-driven signals
- Historical analysis over up to **2 years** of weekly price data

The app is built as a portfolio-grade Android project with a strong emphasis on **clean architecture, scalability, and explainable product logic**.

---

## Features

### Authentication
- Email/password sign-in and sign-up via Firebase Authentication
- Persistent sessions — users stay logged in between app launches
- Per-user data isolation in Firestore

### Market Dashboard
- Live index tracking (S&P 500, NASDAQ, Dow Jones via ETF proxies)
- Top gainer and top loser from a curated stock universe
- Watchlist preview with live prices
- Quick access to alerts and signals

### Stock Detail & Analytics
- Custom Canvas-drawn price charts — no third-party charting library
- **3 supported timeframes**: 1M, 3M, 1Y
- Chart data sourced from Yahoo Finance
- Real company data via Finnhub: name, industry, exchange, market cap, P/E ratio, 52W high/low, beta
- **Target Return Simulator** — computes from 2 years of weekly price history:
  - Historical probability of reaching the target price
  - Median weeks to reach the target
  - Maximum drawdown risk while waiting
- Supports **4 investment horizons**: 1 Month, 3 Months, 6 Months, 1 Year
- Per-stock financial news feed

### News & Signals
- Financial news feed sourced from Finnhub, cached in Firestore (1-hour TTL)
- News and signals combined in a single tabbed screen
- News-driven signal engine using keyword detection across **8 market sectors**:
  Technology, Energy, Financials, Healthcare, Automotive, Geopolitical, ConsumerGoods, RealEstate
- Signals cross-referenced against user's watchlist for relevance
- Signal strength classification: HIGH, MEDIUM, LOW

### Watchlist
- Add or remove stocks from any stock detail screen via bookmark icon
- Per-user Firestore-backed watchlist, synced across sessions
- Live prices fetched on load

### Alerts
- User-defined price alerts with ABOVE and BELOW direction
- Alerts stored per-user in Firestore
- `AlertCheckWorker` polls every 15 minutes via WorkManager
- Push notifications on trigger via Android notification channels

### Background Processing
- `AlertCheckWorker` — checks active alerts every 15 minutes
- `NewsSignalWorker` — scans news for signals every 30 minutes
- Both workers respect battery optimization via WorkManager scheduling

### Settings
- Toggle signals on/off
- Filter to HIGH signals only
- Toggle push notifications for signals
- Limit signals to watchlist-relevant sectors only
- Settings persisted per-user in Firestore

---

## Tech Stack

### Android
- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Navigation Compose** with animated screen transitions

### Architecture
- **MVVM**
- **Repository Pattern**
- **StateFlow / MutableStateFlow**
- In-memory quote cache with 1-minute TTL

### Backend / Cloud
- **Firebase Authentication** (email/password)
- **Firebase Firestore** (watchlist, alerts, signals, settings, news cache)

### Data & APIs
- **Finnhub API** — live quotes, symbol search, company profile, key metrics, news
- **Yahoo Finance API** — historical chart data (unofficial, browser User-Agent via OkHttp interceptor)
- **Retrofit** — HTTP client
- **OkHttp** — with custom interceptors for Finnhub token injection and Yahoo Finance User-Agent
- **Moshi** — JSON parsing with Kotlin reflection adapter

### Background Processing
- **WorkManager** — periodic background tasks

### UI
- **Compose Canvas API** — fully custom stock chart (no third-party chart library)
- **Compose Animation** — `AnimatedContent`, `fadeIn/fadeOut` transitions, slide navigation

---

## Architecture

MarketLens follows a clean **MVVM architecture**:
```text
Remote APIs (Finnhub, Yahoo Finance) / Firebase
                    ↓
              Repository Layer
          (Real + Firestore impls)
                    ↓
            ViewModel Layer
         (StateFlow-driven state)
                    ↓
       Jetpack Compose UI Layer
```

### Key architectural decisions
- **Repository interfaces** decouple ViewModels from data sources — `FakeMarketRepository` enables UI development without live API calls
- **AppContainer** provides manual dependency injection via lazy singletons — no Hilt overhead for a project of this scope
- **Firestore as cache** for news and signals — reduces API calls and enables offline reads
- **WorkManager** for all background work — respects Doze mode and battery optimization
- **Single-module structure** — deliberate for a portfolio project; would split into feature modules at production scale

---

## Screens

| Screen | Description |
|---|---|
| Auth | Email/password login and signup |
| Dashboard | Market overview, watchlist preview, index tracking |
| Markets | Browsable stock list with live prices and debounced search |
| Stock Detail | Chart, key stats, alerts, Target Return Simulator, news |
| News + Signals | Tabbed screen: financial news feed and sector signals |
| Watchlist | User's saved stocks with live prices |
| Alerts | User-defined price alerts with trigger history |
| Settings | Signal preferences and notification controls |
| Portfolio Snapshot | *(In progress)* Holdings tracker with P&L |

---

## Background Workers

| Worker | Interval | Purpose |
|---|---|---|
| `AlertCheckWorker` | 15 min | Fetches live prices, checks against active alerts, sends notification if triggered |
| `NewsSignalWorker` | 30 min | Fetches market news, runs keyword detection, saves signals, notifies on HIGH/MEDIUM |

---

## API Usage

| API | Used For | Auth |
|---|---|---|
| Finnhub | Quotes, search, profile, metrics, news | API key via query param |
| Yahoo Finance | Historical price charts | None (browser User-Agent header) |
| Firebase Auth | User authentication | Firebase project config |
| Firebase Firestore | All user data + news cache | Firebase project config |

---

## Project Links

- **GitHub**: https://github.com/bhavishyadash/MarketLens.git
- **Design Document**: Available in repository

---

## Disclaimer

MarketLens is an educational project. All data, signals, and analytics are informational only and do not constitute financial advice. Past performance does not guarantee future results.
