# MarketLens

**MarketLens** is an Android finance insights app built to help users track the stock market through **live quotes, interactive charts, financial news, watchlists, volatility alerts, and news-driven market signals**.

Unlike trading apps that push speculation, MarketLens is designed around an educational and explainable philosophy: **inform, not advise**.

---

## Overview

MarketLens combines market data, news context, analytics, and user-defined alerts into a single mobile experience.

### Current scope includes:
- **9 app screens**
- **2 background workers**
- **4 chart timeframes**
- **4 investment horizons**
- **8 market sectors** for news-driven signals
- Historical analysis over up to **365 days** of price data

The app is built as a portfolio-grade Android project with a strong emphasis on **clean architecture, scalability, and explainable product logic**.

---

## Features

### Market Dashboard
- View live market data and stock information in a mobile-first interface
- Monitor watchlist activity and surface important changes quickly
- Access alerts and signals from a centralized dashboard experience

### Stock Detail & Analytics
- Interactive stock charts with **4 supported timeframes**:
  - **1M**
  - **3M**
  - **1Y**
- Historical analytics over up to **365 days** of price data
- Computes:
  - **Target-return probability**
  - **Median days to target**
  - **Maximum drawdown risk**
- Supports **4 investment horizons**:
  - **1 month**
  - **3 months**
  - **6 months**
  - **1 year**

### News & Signals
- Financial news feed for market context
- News-driven signals using keyword detection
- Signal engine maps events across **8 market sectors**
- Signal settings allow users to control:
  - whether signals are enabled
  - whether only high-priority signals appear
  - whether notifications are sent
  - whether signals are limited to watchlist-relevant sectors

### Alerts & Background Processing
- User-defined volatility and stock alert workflows
- **2 background workers**:
  - `AlertCheckWorker`
  - `NewsSignalWorker`
- Background jobs support price-based alerts and news-driven notifications

### Personalization
- Firebase-backed authentication
- Watchlist sync through Firestore
- Settings for signals and alert behavior
- Theme-ready Compose UI architecture

---

## Tech Stack

### Android
- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Navigation Compose**

### Architecture
- **MVVM**
- **Repository Pattern**
- **StateFlow / MutableStateFlow**

### Backend / Cloud
- **Firebase Authentication**
- **Firebase Firestore**

### Data & APIs
- **Finnhub API**
- **Yahoo Finance API**
- Financial/news API integrations
- **Retrofit**

### Background Processing
- **WorkManager**

---

## Architecture

MarketLens follows a clean **MVVM architecture**:

```text
Remote APIs / Firebase
        ↓
    Repository
        ↓
    ViewModel
        ↓
 Jetpack Compose UI
