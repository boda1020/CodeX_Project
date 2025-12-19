# 🎓 Student Results Management System

A complete **JavaFX desktop application** built using **Java**, **MySQL**, and **MVC architecture**.
This project manages student data, courses, and academic results with a modern UI and secure backend.

---

## 🧩 Overview

The **Student Results Management System** allows both **Admins** and **Students** to interact with the platform:

* Admins can manage students, courses, and grades.
* Students can log in, view their results, and update their profiles.

The application uses **JavaFX (FXML)** for the interface, **MySQL** for the database, and follows an **MVC architecture** for scalability and clean structure.

---

## 🚀 Key Features

* 🔐 **Secure Login System** (Admin & Student roles)
* 🧑‍🎓 **Student Dashboard** to view courses and results
* 🧑‍💼 **Admin Dashboard** to manage students, courses, and grades
* 🧾 **Results Management** (Add / Edit / Delete results)
* 🧘 **Profile Management** (Admin & Student profiles)
* 🧮 **Automatic Grade Calculation**
* 💾 **MySQL Integration** with DAO pattern
* 🎨 **JavaFX Interface** built with Scene Builder
* ⚡ **Responsive Design** using CSS stylesheets
* 🧠 **Modular MVC Structure**

---

## 🧠 Database Design

### 📊 Tables Overview

| Table            | Description                             |
| ---------------- | --------------------------------------- |
| **admins**       | Stores admin login data                 |
| **students**     | Holds student information               |
| **courses**      | Defines course data (name, code, hours) |
| **results**      | Records student grades                  |
| **activity_log** | Tracks admin & student actions          |

### 🔗 Relationships

* `students → results` (1 to many)
* `courses → results` (1 to many)
* `admins → activity_log` (1 to many)
* `students → activity_log` (1 to many)

---

## ⚙️ Technologies Used

| Component        | Technology                  |
| ---------------- | --------------------------- |
| **Language**     | Java (JDK 23)               |
| **Framework**    | JavaFX 25                   |
| **Database**     | MySQL                       |
| **Build Tool**   | IntelliJ IDEA / NetBeans    |
| **Architecture** | MVC (Model-View-Controller) |
| **Styling**      | CSS                         |
| **Design Tool**  | Scene Builder               |

---

## 🧾 Example Features

* Admin can add, update, and delete student data.
* Students can view their own grades.
* GPA is automatically calculated from the results.
* Activity logs record all changes for accountability.

---

## 👨‍💻 Developed By

**Abdulrahman Khamis**
Team Leader — *CodeX Team* 💻

---

> 🔥 A complete, real-world JavaFX + MySQL project built for production-level performance and modular scalability.
