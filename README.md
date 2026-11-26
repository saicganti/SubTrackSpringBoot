# SubTrackSpringBoot — Subscription & Recurring Expense Tracker 

A full-stack subscription tracker built with Springboot, PostgreSQL, and React. Track recurring expenses, see monthly/yearly totals, and get a spending breakdown by category.

## Features
- JWT authentication with registration and login
- Subscription CRUD with category-based organization
- Dashboard: monthly/yearly totals, spending chart, upcoming renewals
- Background service scans for renewals in the next 7 days
- JUnit tests for billing-cycle math and dashboard calculations

## Tech Stack
- **Backend:** Java 17, Spring Boot, Hibernate, Spring Security, JWT, Docker, Flyway Migration
- **Frontend:** React 18, Vite, React Router, Axios, Recharts, Tailwind CSS
- **Testing:** JUnit

## Architecture

## Design Notes
- **Category normalized to a FK table** — eliminates update anomalies and group-by typos from a free-text category.
- **`BillingCycle` kept as an enum** — a fixed closed set; new values require new date-math code, not new data.
- **Composite index on `(userId, nextRenewalDate)`** — matches the two query shapes: filter by user, and filter by user + date range.
- **Check constraint `Cost > 0`** at the database level — validation protects the API; the constraint protects the data.
