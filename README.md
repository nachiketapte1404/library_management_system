# Library Manager
A web-based library management system that provides automated cataloguing, book circulation tracking, and user registry administration.

## Objective

The objective of this application is to streamline core library operations through a simplified digital interface. It handles catalog searches, inventory updates for multi-copy titles, book issuance, automated return tracking, user registration, and data synchronization across flat-text storage files (`books.txt` and `users.txt`).

## Technologies Used

### Backend
* Java 17
* Spring Boot

### Frontend
* HTML5
* CSS3
* JavaScript (Vanilla)

## Features Implemented

### Public & User Features
* **Search Book Catalog:** Allows users to query the library inventory by entering a specific ISBN number to retrieve details directly from `books.txt`.
* **Books Directory Catalog:** Lists all unique book titles (one entry per ISBN). It displays complete title specifications with options to modify metadata or remove a title entirely, provided no copies are currently borrowed.
* **My Borrowed Books:** Displays all books currently issued to a specific User ID. It dynamically renders the Book ID, ISBN, Title, and Author for every active loan.
* **Issue Book Copy:** Processes loans by taking an ISBN and User ID, automatically assigning the first available individual Book ID copy to the user, and decrementing the available copy count in the main catalog.
* **Return Book Copy:** Processes returns via individual Book ID, removing the entry from the user's active loans, clearing it from the global borrowed list, and incrementing the available copy count back into inventory.

### Administrative Features
* **Add Books to Inventory:** Adds new material by ISBN. If the ISBN exists, the system prompts only for the number of additional copies. If it is a new title, it captures the Title, Author, and Classification (General, Fiction, Academic, or Magazine). 
  * *Fiction* prompts for a Genre field.
  * *Academic* prompts for a Subject field.
  * *Magazine* prompts for an Issue Number field.
  * Backend algorithms auto-generate unique individual Book IDs for each copy before persisting records.
* **Add User:** Enrolls new members using a custom User ID, Name, and National/Unique Identification Number after verifying that the User ID is available and the identification number does not already exist.
* **Users Registry:** Displays all registered library members with options to update information or delete accounts. Deletion is strictly blocked if the user has unreturned books.
* **Report Lost Book:** Allows administrators to flag an individual Book ID as lost, purging its record from `books.txt`, updating the available copy count for that title, and removing it from the borrower's profile if it was currently issued.

## API Endpoints

### Books Management (`localhost:8080/books`)
* `GET /books` - Fetch all books in the inventory.
* `POST /books` - Batch add books to the inventory.
* `GET /books/check/{isbn}` - Verify the existence or status of an ISBN.
* `GET /books/search/{isbn}` - Search for a specific book title by its ISBN.
* `GET /books/user/{userId}` - Retrieve all books currently borrowed by a specific user.
* `POST /books/issue/{isbn}/{userId}` - Issue the first available copy of an ISBN to a user.
* `POST /books/{bookId}/return` - Process the return of a specific book copy.
* `PUT /books/update/{isbn}` - Update the metadata of a specific book title.
* `DELETE /books/catalog/{isbn}` - Delete a book title and all its copies from the catalog.
* `DELETE /books/lost/{bookId}` - Remove a specific book copy flagged as lost and update records.

### Users Management (`localhost:8080/users`)
* `GET /users` - Fetch the complete registry of library users.
* `POST /users` - Register a new user into the system.
* `PUT /users/update/{userId}` - Modify details for an existing user.
* `DELETE /users/{userId}` - Remove a user profile from the registry (enforces zero active loans).

## Steps to Run the Application
Follow these procedures to launch the backend service and frontend client locally.

### Prerequisites
* Java Development Kit (JDK) 17 installed on your system path.

### 1. Start the Backend Server
Open your terminal, navigate to the root directory of the backend project, and execute the application using Maven.


```bash
./mvn spring-boot:run

or 
/path/to/your/jdk17 ./mvn spring-boot:run
```

The server will start backend on `http://localhost:8080`.

### 2. Launch the Frontend
The frontend consists of static files that do not require any deployment.

1. Navigate to your frontend source directory.
2. Locate the file named `index.html`.
3. Open `index.html` directly in any web browser.