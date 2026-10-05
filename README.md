# Online Recipe Sharing Platform

A Java Swing GUI based desktop application built for BTech Java GUI Project Review 1.

The project allows users to share, discover, rate, and manage culinary recipes with role-based access control for **Admins**, **Recipe Contributors**, and **Recipe Explorers**.

---

## 🌟 Key Features

### 👨‍💼 1. Admin Role
- **User Management:** View all registered users, add new users, edit existing user details, and delete user accounts (with database transaction safety).
- **Recipe Management:** Review recipe submissions, approve or reject pending recipes, view full details, or delete recipes.
- **Content Moderation:** Moderate and delete inappropriate reviews.
- **System Settings:** View and update global platform configuration settings.

### 👨‍🍳 2. Recipe Contributor Role
- **My Recipes:** Submit new recipes (starts in `PENDING` state), edit own recipes, delete recipes, and view details.
- **Live Statistics (Multithreaded):** View live performance metrics (Total Recipes, Approved Count, Pending Count, Average Rating, Total Views) loaded asynchronously in a background thread without freezing the GUI.
- **Messaging:** Send direct messages to Admin or Explorers and view received messages in the inbox.
- **Profile Management:** Update personal name, email, and password.

### 🍽️ 3. Recipe Explorer Role
- **Browse & Filter:** Browse approved recipes with real-time title search and category filtering ("Main Course", "Breakfast", "Dessert", etc.).
- **Recipe Details & Views:** View full ingredients, instructions, view count, and average rating.
- **Ratings & Reviews:** Rate recipes (1 to 5 stars) and write comments. Prevents duplicate reviews per user.
- **Personal Collection (`HashSet`):** Save recipes to a personal collection and remove saved items.
- **Browsing History:** Track recently viewed recipes.

---

## 🛠️ Technology Stack

- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing
- **Database:** SQLite (`database/recipe_platform.db`)
- **Database Driver:** `sqlite-jdbc`
- **Architecture:** Layered Architecture (Model, DAO, Service, GUI, Exceptions, Interfaces, Util)
- **Build Tools:** Maven (`pom.xml`) or standalone javac/java scripts

---

## 🔑 Demo Credentials for Viva / Testing

The database is automatically initialized with sample data on first launch:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@recipe.com` | `admin123` |
| **Contributor** | `contributor@recipe.com` | `contrib123` |
| **Explorer** | `explorer@recipe.com` | `explorer123` |

*(Note: The login screen also includes quick "Demo Login" buttons for rapid testing during viva defense).*

---

## 🚀 How to Run the Application

### Option A: Using Maven (Recommended)
```bash
mvn compile exec:java
```

### Option B: Using Shell Script (Linux / macOS)
```bash
chmod +x compile_and_run.sh
./compile_and_run.sh
```

### Option C: Using Windows Batch Script
Double-click `run.bat` or run in CMD:
```cmd
run.bat
```

---

## 📁 Project Structure

```
RecipeSharingPlatform/
│
├── pom.xml                     # Maven build file
├── compile_and_run.sh          # Linux/macOS run script
├── run.bat                     # Windows run script
├── README.md                   # Project documentation
├── RUBRIC_MAPPING.md           # Review 1 Rubric Mapping (33 Marks)
│
├── lib/                        # Pre-packaged JARs for direct execution
│   ├── sqlite-jdbc.jar
│   ├── slf4j-api.jar
│   └── slf4j-simple.jar
│
└── src/main/java/com/recipeplatform/
    ├── Main.java               # Main entry point
    │
    ├── model/                  # Domain Models & Inheritance hierarchy
    │   ├── User.java           # Base class (Abstract)
    │   ├── Admin.java          # Extends User
    │   ├── RecipeContributor.java # Extends User
    │   ├── RecipeExplorer.java # Extends User
    │   ├── Recipe.java
    │   ├── Review.java
    │   ├── Message.java
    │   └── SystemSetting.java
    │
    ├── interfaces/             # OOP Interfaces
    │   ├── RecipeOperations.java
    │   ├── ReviewOperations.java
    │   └── CollectionOperations.java
    │
    ├── exceptions/             # Custom Exceptions
    │   ├── InvalidLoginException.java
    │   ├── RecipeValidationException.java
    │   ├── DatabaseException.java
    │   └── DuplicateReviewException.java
    │
    ├── database/               # Database Connection & Auto-Initialization
    │   ├── DatabaseConnection.java
    │   └── DatabaseInitializer.java
    │
    ├── dao/                    # Database Operation Classes (CRUD)
    │   ├── UserDAO.java
    │   ├── RecipeDAO.java
    │   ├── ReviewDAO.java
    │   ├── CollectionDAO.java
    │   └── MessageDAO.java
    │
    ├── service/                # Business Logic, Multithreading & Synchronization
    │   ├── AuthenticationService.java
    │   ├── RecipeService.java
    │   └── ReviewService.java
    │
    ├── util/                   # Generics & Validation Utilities
    │   ├── ValidationUtil.java
    │   └── GenericRepository.java
    │
    └── gui/                    # Java Swing Desktop UI
        ├── UITheme.java
        ├── LoginFrame.java
        ├── RegisterFrame.java
        ├── AdminDashboard.java
        ├── ContributorDashboard.java
        ├── ExplorerDashboard.java
        ├── RecipeDetailsDialog.java
        ├── RecipeFormDialog.java
        └── UserFormDialog.java
```

---

## 📜 Rubric Defense & viva Preparation

Refer to [`RUBRIC_MAPPING.md`](./RUBRIC_MAPPING.md) for a line-by-line mapping of all 33 marks required for the Java GUI Project Review 1.
