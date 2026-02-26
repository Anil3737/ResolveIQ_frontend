
📱 ResolveIQ Android App

Enterprise AI-Based Helpdesk Application

ResolveIQ Android App is the client interface for interacting with the ResolveIQ backend. It provides role-based dashboards, real-time ticket tracking, AI risk visualization, and SLA monitoring.

🏗 Architecture

MVVM Pattern

Repository Layer

Retrofit Networking

Coroutines

Material 3 UI

⚙️ Tech Stack

Kotlin

Android SDK

Retrofit

Gson

ViewBinding

RecyclerView

Material 3

Dark Mode Support

👥 Role-Based Dashboards
👨‍💼 Admin

Dashboard Analytics

Staff Creation

System Activity

Risk Monitoring

🛡 Team Lead

Approval Pool

Team Members Workload

Assign Tickets

Department Oversight

🧑‍💻 Agent

Ticket Pool

Accept Ticket

Resolve / Decline

SLA Monitoring

👨‍💻 Employee

Create Ticket

View Progress

Track Risk Score

View SLA Timer

🎫 Ticket Progress Timeline

6-stage tracking:

Created

AI Verified

Approved

Assigned

In Progress

Resolved

🧠 AI Risk Display

Risk Score (0–100)

Color-coded indicator

Escalation badge

SLA countdown

🔄 Workflow Handling

Auto refresh tickets

401 interceptor redirects to login

Proper error parsing

Coroutine cancellation safe

📂 Project Structure
com.simats.resolveiq_frontend
│
├── data/
│   ├── model/
│   ├── repository/
│
├── ui/
│   ├── auth/
│   ├── admin/
│   ├── teamlead/
│   ├── agent/
│   └── employee/
│
├── network/
└── utils/
🔐 Authentication

JWT token stored securely

Auto logout on expiry

Role-based redirection after login

🚀 Installation

Open in Android Studio

Sync Gradle

Set BASE_URL in Retrofit client

Run on emulator/device

🌙 UI Features

Material 3 Components

Light & Dark Mode

Enterprise layout

Clean card-based design

Status color mapping

📡 API Configuration

In RetrofitClient.kt:

BASE_URL = "http://10.0.2.2:5000/api/"

(Use local IP for physical device)

🛠 Error Handling

JSON error parsing

Network failure handling

Token expiry redirect

Empty state UI

📈 Future Enhancements

Push Notifications

Offline caching

Charts & analytics

Chat system

Voice ticket input

🎯 Project Purpose

ResolveIQ demonstrates:

Full Stack Development

Secure API Design

Enterprise Workflow Modeling

AI Risk-Based Automation

Role-Based Systems

Transaction Safety

👨‍💻 Developer

J Chiranjevi Anil
Computer Science & Engineering