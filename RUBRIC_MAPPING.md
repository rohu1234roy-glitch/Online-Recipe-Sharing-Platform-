# Review 1 - Marking Rubric Mapping Document

**Project Title:** Online Recipe Sharing Platform  
**Technology:** Java, Java Swing GUI, JDBC, SQLite, OOP, Collections, Multithreading, Transactions  
**Total Rubric Marks:** 33 Marks  

---

## 1. Rubric Summary & Exact Code Location

| Rubric Category | Marks | Concepts Covered | Exact File & Code Location |
|---|---|---|---|
| **A. OOP Implementation** | **10 Marks** | Inheritance, Polymorphism, Interfaces, Custom Exceptions | `model/User.java` <br> `model/Admin.java` <br> `model/RecipeContributor.java` <br> `model/RecipeExplorer.java` <br> `interfaces/RecipeOperations.java` <br> `interfaces/ReviewOperations.java` <br> `interfaces/CollectionOperations.java` <br> `exceptions/*` |
| **B. Collections & Generics** | **6 Marks** | ArrayList, HashMap, HashSet, Generic Class `<T>` | `dao/*.java` (`ArrayList`) <br> `service/RecipeService.java` (`HashMap`) <br> `dao/CollectionDAO.java` (`HashSet`) <br> `util/GenericRepository.java` (`Generics <T>`) |
| **C. Multithreading & Synchronization** | **4 Marks** | Background Threading, UI Responsiveness, `synchronized` lock | `service/RecipeService.java` (`loadContributorStatsAsync`) <br> `gui/ContributorDashboard.java` <br> `service/ReviewService.java` (`addReviewAndRecalculateRating`) <br> `dao/RecipeDAO.java` |
| **D. Database Operation Classes** | **7 Marks** | Separation of DAO CRUD classes | `dao/UserDAO.java` <br> `dao/RecipeDAO.java` <br> `dao/ReviewDAO.java` <br> `dao/CollectionDAO.java` <br> `dao/MessageDAO.java` |
| **E. JDBC Implementation** | **3 Marks** | Connection, PreparedStatement, ResultSet, CRUD | `database/DatabaseConnection.java` <br> `database/DatabaseInitializer.java` <br> All `DAO` classes |
| **F. Transaction Management** | **3 Marks** | `setAutoCommit(false)`, `commit()`, `rollback()` | `dao/UserDAO.java` (`deleteUserWithTransaction`) <br> `dao/RecipeDAO.java` (`deleteRecipe`) |

---

## 2. Detailed Explanation for Viva Examination

### A. OOP Implementation (10 Marks)

#### 1. Inheritance
- **Base Class:** `com.recipeplatform.model.User`
- **Child Classes:**
  - `Admin extends User`
  - `RecipeContributor extends User`
  - `RecipeExplorer extends User`
- **Explanation:** Common properties (`id`, `name`, `email`, `password`, `role`) are declared once in `User.java`. Subclasses inherit these properties and implement role-specific behaviors.

#### 2. Polymorphism
- **Abstract Method:** `public abstract void showDashboard(JFrame currentFrame)` in `User.java`.
- **Dynamic Method Overriding:** Each child class (`Admin`, `RecipeContributor`, `RecipeExplorer`) overrides `showDashboard()`.
- **Polymorphic Execution:** In `LoginFrame.java`:
  ```java
  User user = authService.login(email, password); // Returns Admin, Contributor, or Explorer
  user.showDashboard(this);                       // Correct dashboard opens dynamically!
  ```

#### 3. Interfaces
- `RecipeOperations.java`: Standard contract for adding, editing, deleting, and fetching recipes. Implemented by `RecipeDAO.java`.
- `ReviewOperations.java`: Standard contract for managing user reviews. Implemented by `ReviewDAO.java`.
- `CollectionOperations.java`: Standard contract for managing saved recipe collections. Implemented by `CollectionDAO.java`.

#### 4. Custom Exception Handling
- `InvalidLoginException`: Thrown on wrong email or password credentials.
- `RecipeValidationException`: Thrown when mandatory recipe fields (title, instructions) are missing.
- `DatabaseException`: Wraps low-level `SQLException` to prevent GUI crashes and show clean `JOptionPane` messages.
- `DuplicateReviewException`: Prevents an explorer from reviewing the same recipe multiple times.

---

### B. Collections & Generics (6 Marks)

#### 1. ArrayList
- Used across all DAO classes (`UserDAO`, `RecipeDAO`, `ReviewDAO`, `MessageDAO`) to hold and return dynamic lists of objects (`ArrayList<Recipe>`, `ArrayList<Review>`, `ArrayList<User>`).

#### 2. HashMap
- Implemented in `RecipeService.getApprovedRecipesMap()`:
  ```java
  Map<Integer, Recipe> recipeMap = new HashMap<>();
  // Key: Recipe ID (Integer), Value: Recipe Object
  ```
- Allows fast $O(1)$ constant time recipe lookup by ID.

#### 3. HashSet
- Implemented in `CollectionDAO.getSavedRecipeIds(userId)` and `RecipeDetailsDialog.java`:
  ```java
  Set<Integer> savedIds = new HashSet<>();
  ```
- Uses `HashSet` to enable instantaneous $O(1)$ `contains()` checks to see if a recipe is saved in the user's collection.

#### 4. Generics `<T>`
- Implemented in `com.recipeplatform.util.GenericRepository<T>`:
  ```java
  public class GenericRepository<T> {
      public List<T> filter(List<T> items, Predicate<T> predicate) { ... }
  }
  ```
- Enables reusable, type-safe filtering of any collection without duplicating code.

---

### C. Multithreading & Synchronization (4 Marks)

#### 1. Real Multithreading (Asynchronous Statistics Loading)
- Implemented in `RecipeService.loadContributorStatsAsync()` and called by `ContributorDashboard.java`:
  ```java
  new Thread(() -> {
      // Computes recipe counts, average ratings, and total views in background thread
      javax.swing.SwingUtilities.invokeLater(() -> callback.onSuccess(stats));
  }).start();
  ```
- **Viva Defense:** "By computing statistics in a separate background thread, the Java Swing GUI thread remains 100% responsive without lagging or freezing during heavy database operations."

#### 2. Synchronization (`synchronized` Keyword)
- Implemented in `ReviewService.java`, `RecipeDAO.updateRatingStats()`, and `RecipeDAO.incrementViewCount()`:
  ```java
  public synchronized boolean addReviewAndRecalculateRating(Review review) { ... }
  ```
- **Viva Defense:** "When multiple users concurrently rate or view the same recipe, the `synchronized` modifier ensures thread safety and prevents race conditions when updating shared rating totals and view counters."

---

### D. Database Operation Classes (7 Marks)

All database SQL statements are decoupled from Swing GUI frames and placed inside dedicated Data Access Object (DAO) classes:
1. `UserDAO.java`: User CRUD operations, email lookups, and transaction deletion.
2. `RecipeDAO.java`: Recipe CRUD operations, status updates (Approve/Reject), view logging.
3. `ReviewDAO.java`: Adding reviews, duplicate checks, fetching reviews by recipe/user.
4. `CollectionDAO.java`: Saving/removing recipes from personal collection.
5. `MessageDAO.java`: User-to-user messaging storage and retrieval.

---

### E. JDBC Implementation (3 Marks)

- **Connection Driver:** SQLite JDBC (`org.sqlite.JDBC`).
- **Connection Management:** `DatabaseConnection.getConnection()` using `DriverManager.getConnection("jdbc:sqlite:database/recipe_platform.db")`.
- **Prepared Statements:** `PreparedStatement` is used for **100% of queries** to prevent SQL injection vulnerabilities.
- **Auto Table Creation:** `DatabaseInitializer.initialize()` automatically creates all 7 tables and seeds default sample data when the app launches.

---

### F. JDBC Transaction Management (3 Marks)

- Implemented in `UserDAO.deleteUserWithTransaction()` and `RecipeDAO.deleteRecipe()`:
  ```java
  conn.setAutoCommit(false); // Begin Transaction
  try {
      // Step 1: Delete associated reviews
      // Step 2: Delete saved recipes
      // Step 3: Delete views
      // Step 4: Delete main record
      conn.commit();           // Commit if all succeed
  } catch (SQLException e) {
      conn.rollback();         // Rollback if any step fails
  } finally {
      conn.setAutoCommit(true);
  }
  ```
- **Viva Defense:** "When deleting a user or recipe, multiple tables must be updated together. Using explicit JDBC transactions guarantees data integrity—either all deletions succeed or none take effect."

---

## 3. Potential Viva Questions & Sample Answers

**Q1: Why did you use inheritance in your project?**  
*Answer:* "We created a base `User` class containing shared attributes like `id`, `name`, `email`, `password`, and `role`. The `Admin`, `RecipeContributor`, and `RecipeExplorer` subclasses extend `User` to represent specific user roles while inheriting common properties."

**Q2: How is polymorphism demonstrated in your GUI?**  
*Answer:* "The base `User` class declares an abstract `showDashboard(JFrame)` method. Each subclass overrides it to open its respective dashboard (`AdminDashboard`, `ContributorDashboard`, `ExplorerDashboard`). At login, we store the logged-in user in a base reference `User user = authService.login(...)` and call `user.showDashboard()`. The JVM dynamically executes the correct subclass implementation."

**Q3: Why did you use `PreparedStatement` instead of regular `Statement`?**  
*Answer:* "`PreparedStatement` compiles SQL queries once and uses parameterized placeholders (`?`). This protects against SQL injection attacks and improves performance for repeated queries."

**Q4: Where have you used multithreading and why?**  
*Answer:* "In the Contributor Dashboard, calculating statistics (total recipes, approved count, pending count, average rating, total views) is executed in a background thread using `new Thread()`. The calculated stats are passed back to the UI thread using `SwingUtilities.invokeLater()`. This prevents the GUI from freezing during computation."

**Q5: What is the purpose of synchronization in your project?**  
*Answer:* "In `ReviewService.java`, the `addReviewAndRecalculateRating()` method is `synchronized`. If two users submit reviews for the same recipe at the exact same moment, synchronization prevents thread race conditions while recalculating the average rating."
