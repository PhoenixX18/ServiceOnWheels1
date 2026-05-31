# 🚗 Service on Wheels

A full-stack roadside assistance platform built using **Angular, Spring Boot, MongoDB, and JWT Authentication**.

Service on Wheels allows users to request roadside assistance, share their location, manage service requests, and track assistance workflows through a modern web interface.

---

# 📌 Project Overview

Vehicle breakdowns and roadside emergencies often require fast assistance and clear communication.

Service on Wheels is designed to simplify this process by providing a centralized platform where users can:

* Register and securely log in
* Create roadside assistance requests
* Share location using interactive maps
* Track request status
* View request history
* Manage profile and account details
* Reset passwords securely

The project follows a **full-stack architecture** with Angular frontend, Spring Boot backend, and MongoDB database.

---

# ✨ Current Features

## Authentication & Security

* User Registration
* Secure Login
* JWT Authentication
* Protected Routes
* BCrypt Password Encryption
* Forgot Password Flow (SMTP-based reset)

## User Dashboard

* Personalized Dashboard
* Request Overview
* Request Statistics
* Quick Navigation

## Roadside Assistance Requests

Users can:

* Submit vehicle details
* Select vehicle type
* Describe roadside issues
* Share GPS location
* Use map-based location selection
* Create assistance requests

## Location Integration

* OpenStreetMap
* Leaflet Maps
* GPS Location Detection
* Map-based Coordinate Selection
* Reverse Geocoding Support

## Request Management

* View Request History
* Track Request Status
* Dashboard Integration
* Request Lifecycle Visibility

---

# 🛠 Tech Stack

## Frontend

* Angular
* TypeScript
* HTML
* CSS
* Leaflet

## Backend

* Java
* Spring Boot
* Spring Security
* JWT Authentication
* REST APIs
* Maven

## Database

* MongoDB

## Version Control & Tools

* Git
* GitHub
* VS Code

---

# 🏗 System Architecture

The application follows a decoupled frontend-backend architecture.

```text
Angular Frontend
        ↓
REST API + JWT Authentication
        ↓
Spring Boot Backend
        ↓
MongoDB Database
```

This architecture improves maintainability, scalability, and separation of concerns.

---

# 📸 Screenshots

Add project screenshots here.

Recommended:

* Landing Page
* Login Page
* Dashboard
* Request Form
* Location Map
* Request History
* Profile Page

Example:

```markdown
![Dashboard](screenshots/dashboard.png)
```

---

# ⚙ Installation & Setup

## Clone Repository

```bash
git clone https://github.com/PhoenixX18/ServiceOnWheels1.git
```

---

## Backend Setup

Navigate:

```bash
cd backend/auth-service/auth-service
```

Run backend:

```bash
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8081
```

---

## Frontend Setup

Navigate:

```bash
cd customer-app
```

Install dependencies:

```bash
npm install
```

Run Angular:

```bash
ng serve
```

Frontend runs on:

```text
http://localhost:4200
```

---

# 🔐 Configuration

Configure:

* MongoDB URI
* JWT Secret
* SMTP Credentials (Forgot Password)

Example:

```properties
spring.data.mongodb.uri=
jwt.secret=
spring.mail.username=
spring.mail.password=
```

Do not commit credentials or `.env` files.

---

# 🚀 Development Roadmap

Planned improvements:

* Live mechanic tracking
* Mechanic workflow system
* Role-based access
* Enhanced request lifecycle
* Testing
* Docker deployment
* CI/CD pipeline

---

# 🎯 Learning & Engineering Goals

This project was built to strengthen practical knowledge in:

* Full-stack development
* REST API design
* Authentication & authorization
* Frontend-backend integration
* MongoDB persistence
* Secure application architecture
* Real-world service platform workflows

---

# 👨‍💻 Author

Mukesh Nathan

GitHub: PhoenixX18
