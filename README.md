#  Smart Service Book – Digital Maintenance Log for Android

**Smart Service Book** is a modern, clean, and intuitive Android application written in Java. It allows vehicle owners to manage their personal garage, track detailed service and maintenance histories, calculate repair costs, and store attached receipt/invoice photos for every service entry.

---

## Features

- **Garage Overview (Main Dashboard):**
  - Card-based UI showing all saved vehicles (Make, Model, Year, License Plate, and Current Mileage).
  - Quick actions to add a new vehicle or jump straight into its service book.

- **Vehicle Service Book (History):**
  - Dedicated history page for each vehicle.
  - Timeline of performed service records sorted by date and mileage.
  - Categorized maintenance tags (*Oil Change, Brakes, Suspension, Timing Belt, Drive Belt, Wheel Bearings, etc.*).
  - Detailed descriptions of work performed and spare parts used.
  - Total repair cost tracking.

- **Receipt / Invoice Attachment:**
  - Capture or attach receipt photos via Camera or Device Gallery for any service entry.
  - Fullscreen zoomable preview of attached invoices on service records.

- **Add New Service Record:**
  - Fast, form-based data entry with automatic vehicle context.
  - Input fields for Date, Mileage, Categories, Description, Parts List, Cost, and Image attachment.

- **Multi-Vehicle Management:**
  - Add, edit, or remove multiple cars or motorcycles.

---

## Architecture & UI/UX Design

The application follows a **Single-Activity Architecture** with a **Fragment-based** navigation flow utilizing modern Android Jetpack components.

[ Garage (MainFragment) ]
│
├──► Click on Vehicle Card
│         │
│         ▼
│   [ Vehicle Service History (ServiceListFragment) ]
│   (Top Bar: Vehicle Details | Left: + Add Service | Right: Home)
│         │
│         └──► Click "+ Add Service"
│                   │
│                   ▼
│             [ Add Service Entry (AddServiceFragment) ]
│             (Form: Date, Mileage, Description, Invoice Photo, Cost)
│
└──► Click "+ Add Vehicle"
│
▼
[ Add Vehicle (AddVehicleFragment) ]

## 🛠️ Tech Stack & Libraries

- **Language:** Java
- **Min SDK:** Android 8.0 (API Level 26)
- **UI & Layouts:** Material Design 3 Components, XML, ConstraintLayout, RecyclerView, CardView.
- **Architecture:** MVVM / Repository Pattern with Android Jetpack.
- **Navigation:** Jetpack Navigation Component (Single Activity + Fragments).
- **Database (Local Storage):** Room Persistence Library (SQLite).
- **Camera & File Selection:** Android `ActivityResultLauncher` & Storage Access Framework.

---

## 🗄️ Database Schema Overview

The app uses two primary relational tables via Room DB:

1. **`vehicles`**
   - `id` (Primary Key, Auto)
   - `brand` (String)
   - `model` (String)
   - `licensePlate` (String)
   - `year` (Integer)
   - `currentMileage` (Integer)

2. **`service_records`**
   - `id` (Primary Key, Auto)
   - `vehicleId` (Foreign Key -> `vehicles.id`, Cascade Delete)
   - `dateTimestamp` (Long)
   - `mileageAtService` (Integer)
   - `category` (String)
   - `description` (String)
   - `cost` (Double)
   - `invoiceImagePath` (String / Local File URI)

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone [gh repo clone vJanos95/digit_service]

