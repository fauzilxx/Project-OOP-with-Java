# Project OOP with Java

This project is a **JavaFX-based Hospital Management System** developed to demonstrate the implementation of **Object-Oriented Programming (OOP)** concepts using **Java**, **JavaFX**, **Maven**, and **MySQL**.

The application provides features for managing patients, doctors, queues, room bookings, drug orders, and revenue records through a graphical user interface.

---

## 🏥 Project Overview

The system simulates real-world hospital operations, including:

- User authentication (login & registration)
- Patient and admin dashboards
- Inpatient and outpatient management
- Doctor data management
- Queue handling system
- Room booking system
- Drug ordering system
- Revenue tracking
- MySQL database integration

This project emphasizes clean OOP design, separation of concerns, and modular Java architecture.

---

## 🧠 Object-Oriented Concepts Applied

- **Encapsulation** – Private fields with controlled access
- **Inheritance** – Shared behavior between related classes
- **Polymorphism** – Method overriding for flexible logic
- **Abstraction** – Separation between UI, logic, and database
- **Modular Design** – Feature-based class organization

## 🗂️ Project Structure

```text
Project-OOP-with-Java/
├── src/
│   └── main/
│       └── java/
│           └── projectpbo/
│               ├── Launcher.java
│               ├── MainApp.java
│               ├── DBConnection.java
│               ├── AccountService.java
│               ├── LoginView.java
│               ├── RegisterView.java
│               ├── ForgotPasswordView.java
│               ├── AdminDashboard.java
│               ├── PatientDashboard.java
│               ├── Doctor.java
│               ├── Inpatient.java
│               ├── Outpatient.java
│               ├── Queue.java
│               ├── QueueView.java
│               ├── RoomBooking.java
│               ├── RoomBookingView.java
│               ├── DrugOrder.java
│               ├── DrugOrderView.java
│               ├── Revenue.java
│               └── RevenueView.java
├── lib/
│   └── mysql-connector-j-9.5.0.jar
├── pom.xml
├── dependency-reduced-pom.xml
└── README.md


## ⚙️ Technologies Used

- Java
- JavaFX (GUI)
- Maven
- MySQL
- JDBC
- Git & GitHub

---

## 🚀 How to Run the Application

### Prerequisites
- Java JDK 8 or newer
- JavaFX
- MySQL Server
- Maven

---

### 1. Clone Repository

```bash
git clone https://github.com/fauzilxx/Project-OOP-with-Java.git
cd Project-OOP-with-Java

### 2. Configure Database
DBConnection.java

### 3. Run Application
mvn clean javafx:run





