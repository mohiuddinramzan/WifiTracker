# WiFi Time Tracker

An Android app that shows how long your phone has been connected to WiFi — in a style similar to **Digital Wellbeing**. See your usage broken down by day, week, month, year, and all-time.

## What it does

- Tracks how much time your phone spends connected to WiFi.
- Shows your stats with a clean ring progress view and bar charts.
- Lets you switch between **Daily / Weekly / Monthly / Yearly / All-time** views.
- Works fully offline — all data stays on your phone. Nothing is ever sent to the internet.

## How to install

1. Download the APK file (`app-debug.apk` or similar).
2. Open it on your Android phone. You may need to allow **"Install from unknown sources"** the first time.
3. Open the app once installed.

## Permissions it needs

- **Notifications** (Android 13 and above): used to show a status notification while the background tracking service is running.

## Getting the most accurate tracking

Android sometimes shuts down background services to save battery. To keep tracking reliable:

1. Go to **Settings → Apps → WiFi Time Tracker → Battery**.
2. Set it to **Unrestricted**.

This lets the app keep running in the background so it doesn't miss any connection time.

## Privacy

This app does not collect, transmit, or share any of your data. Everything is stored locally on your device only.

## Planned features

- Separate stats per WiFi network (SSID)
- Export data as CSV
- Language toggle (Bengali/English)
