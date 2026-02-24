# Stats Screen Architecture

## Overview
The Stats Screen has been overhauled to provide detailed insights into user swipe behavior. It adheres to the "Midnight Palette" design system (True Black background, Cyan/Purple accents).

## Data Source
Currently, the screen pulls data from `SwipeDataStore` via `SwipeRepository`. 
- **Weekly Stats**: Simulated using real "Today" data and mocked historical data (due to Room compatibility issues).
- **App Breakdown**: Real-time breakdown between TikTok and Instagram swipes stored in DataStore.

## Components

### 1. Weekly Chart
- **Library**: Vico
- **Type**: Column Chart
- **Styling**: 
  - Cyan bars (`primary` color) with rounded top corners.
  - Light gray axis labels.
  - Dark container background (`surfaceVariant`).

### 2. App Breakdown
- **Visual**: List of apps with progress bars.
- **Data**: Shows swipe count and percentage share.
- **Colors**: 
  - TikTok: Cyan (`#00E5FF`)
  - Instagram: Purple (`#E040FB`)

### 3. Daily Breakdown (Premium)
- **Feature**: Detailed day-by-day swipe counts.
- **Restriction**: Locked for non-premium users.

## Key Files
- `StatsScreen.kt`: Main composable.
- `StatsViewModel.kt`: Logic for data aggregation and Flow combination.
- `SwipeRepository.kt`: Single source of truth for swipe data.
