---
description: How to run the ResolveIQ application
---

### 1. Start the Backend
Navigate to the backend directory and start the server.
```powershell
cd "C:\Users\DELL\OneDrive\Desktop\resolveiq_backend"
python -m uvicorn app.main:app --reload
```

### 2. Run the Android Frontend
1. Open `c:\Users\DELL\AndroidStudioProjects\resolveiq_frontend` in Android Studio.
2. Select an emulator.
3. Click **Run** (Shift + F10).
