const API_URL = "http://localhost:8080/books";
const USER_API = "http://localhost:8080/users";

let isNewIsbn = true;
let currentRole = 'ADMIN';

function setRole(role) {
    currentRole = role;
    const userBtn = document.getElementById("userRoleBtn");
    const adminBtn = document.getElementById("adminRoleBtn");

    if (role === "ADMIN") {
        adminBtn.classList.add("active");
        userBtn.classList.remove("active");
    }
    else {
        adminBtn.classList.remove("active");
        userBtn.classList.add("active");
    }

    applyRoleVisibility();
}

function applyRoleVisibility() {
    const is_admin = (currentRole === "ADMIN");
    document.getElementById("adminAddBookSection").style.display = is_admin ? "block" : "none";
    document.getElementById("adminAddUserSection").style.display = is_admin ? "block" : "none";
    document.getElementById("adminUsersTableSection").style.display = is_admin ? "block" : "none";
    document.getElementById("everyoneIssueSection").style.display = "block";

    // EVERYONE panel (Always visible to both roles)
    document.getElementById("everyoneReturnSection").style.display = "block";

    // Toggle Table Header Action column wrapper visibility
    const actionHeader = document.querySelector(".admin-action-col");
    if (actionHeader) {
        actionHeader.style.display = is_admin ? "table-cell" : "none";
    }
    loadBooks();
}

function toggleGenreField() {
    const type = document.getElementById("bookType").value;

    document.getElementById("genreGroup").style.display = "none";
    document.getElementById("subjectGroup").style.display = "none";
    document.getElementById("issueNumberGroup").style.display = "none";

    if (type === "FICTION") {
        document.getElementById("genreGroup").style.display = "block";
    } else if (type === "ACADEMIC") {
        document.getElementById("subjectGroup").style.display = "block";
    } else if (type === "MAGAZINE") {
        document.getElementById("issueNumberGroup").style.display = "block";
    }
}

async function verifyISBN() {
    const isbn = document.getElementById("isbn").value.trim();
    if (!isbn) {
        alert("Please input an ISBN value to verify.");
        return;
    }

    // 1. Declare these fields at the VERY TOP of the function so all blocks can see them
    const titleField = document.getElementById("title");
    const authorField = document.getElementById("author");
    const typeField = document.getElementById("bookType");
    const genreField = document.getElementById("genre");

    try {
        const response = await fetch(`${API_URL}/check/${isbn}`);

        if (response.status === 200) {
            // ISBN Exists
            const existingBook = await response.json();
            alert(`ISBN Found! Automatically matching: "${existingBook.title}"`);

            titleField.value = existingBook.title;
            authorField.value = existingBook.author;
            typeField.value = existingBook.type;
            document.getElementById("genre").value = "";
            document.getElementById("subject").value = "";
            document.getElementById("issueNumber").value = "";

            if (existingBook.type === "FICTION") {
                document.getElementById("genre").value = existingBook.genre;
            } else if (existingBook.type === "ACADEMIC") {
                document.getElementById("subject").value = existingBook.subject;
            } else if (existingBook.type === "MAGAZINE") {
                document.getElementById("issueNumber").value = existingBook.issueNumber;
            }

            titleField.disabled = true;
            authorField.disabled = true;
            typeField.disabled = true;
            isBrandNewIsbn = false;

        } else if (response.status === 204) {
            // ISBN is New
            alert("This ISBN is new to our network. Please fill out the profile fields manually.");

            titleField.value = "";
            authorField.value = "";
            typeField.value = "GENERAL";
            genreField.value = "";
            document.getElementById("genre").value = "";
            document.getElementById("subject").value = "";
            document.getElementById("issueNumber").value = "";

            // These lines will now execute cleanly because authorField is defined above!
            titleField.disabled = false;
            authorField.disabled = false;
            typeField.disabled = false;
            isBrandNewIsbn = true;
        }

        toggleGenreField();

    } catch (error) {
        console.error("Verification connection error:", error);
        alert("Failed to reach data registry verification endpoint.");
    }
}

async function addBookBatch() {
    const isbn = document.getElementById("isbn").value.trim();
    const title = document.getElementById("title").value.trim();
    const author = document.getElementById("author").value.trim();
    const type = document.getElementById("bookType").value;
    const quantity = parseInt(document.getElementById("quantity").value);

    if (!isbn || !title || !author || isNaN(quantity) || quantity < 1) {
        alert("Ensure all necessary structural book metadata and quantities are set correctly.");
        return;
    }

    const payload = { isbn: isbn, title: title, author: author, type: type, quantity: quantity };

    if (type === "FICTION") {
        payload.genre = document.getElementById("genre").value.trim() || "General";
    } else if (type === "ACADEMIC") {
        payload.subject = document.getElementById("subject").value.trim() || "General";
    } else if (type === "MAGAZINE") {
        payload.issueNumber = document.getElementById("issueNumber").value.trim() || "N/A";
    }

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const resultMessage = await response.text();
        if (response.ok) {
            alert(resultMessage);
            document.getElementById("isbn").value = "";
            document.getElementById("title").value = "";
            document.getElementById("author").value = "";
            document.getElementById("genre").value = "";
            document.getElementById("subject").value = "";
            document.getElementById("issueNumber").value = "";

            document.getElementById("title").disabled = true;
            document.getElementById("author").disabled = true;
            document.getElementById("bookType").disabled = true;
            document.getElementById("quantity").value = "1";
            document.getElementById("genreGroup").style.display = "none";
            document.getElementById("subjectGroup").style.display = "none";
            document.getElementById("issueNumberGroup").style.display = "none";

            loadBooks();
        }
        else {
            alert("Error adding copies: " + resultMessage);
        }
    }
    catch (error) {
        console.log("Network batch payload tracking crash: ", error);
    }
}

// async function addBook() {
//     const title = document.getElementById("title").value;
//     const author = document.getElementById("author").value;
//     if (!bookId || !title || !author) {
//         alert("Please fill out all fields.");
//         return;
//     }
//     const book = {
//         bookId: bookId,
//         title: title,
//         author: author,
//         available: true,
//         issuedToUserId: null
//     };

//     try {
//         const response = await fetch(API_URL, {
//             method: "POST",
//             headers: { "Content-Type": "application/json" },
//             body: JSON.stringify(book)
//         });
//         const resultText = await response.text();
//         if (response.ok) {
//             alert(resultText);
//             document.getElementById("title").value = "";
//             document.getElementById("author").value = "";
//             loadBooks();
//         } else {
//             alert(resultText);
//         }
//     } catch (error) {
//         console.error("Network error:", error);
//     }
// }

async function loadBooks() {
    const response = await fetch(API_URL);
    const books = await response.json();
    console.log("Loading books...");
    console.log(books);
    const tbody = document.querySelector("#bookTable tbody");
    tbody.innerHTML = "";

    const is_admin = (currentRole === "ADMIN");
    books.forEach(book => {
        let actionCell = is_admin ? `<td><button onclick="deleteBook(${book.bookId})">Delete</button></td>` : "";

        tbody.innerHTML += `
        <tr>
            <td>${book.bookId}</td>
            <td>${book.isbn ?? "N/A"}</td>
            <td>${book.title}</td>
            <td>${book.author}</td>
            <td>${book.available}</td>
            <td>${book.issuedToUserId ?? "None"}</td>
            ${actionCell}
        </tr>`;
    });
}

async function searchBook() {
    const bookId = document.getElementById("searchBookId").value;
    const response = await fetch(`${API_URL}/${bookId}`);
    const book = await response.json();
    const result = document.getElementById("searchResult");
    if (!book) {
        result.innerHTML =
            "<p>Book not found</p>";
        return;
    }
    result.innerHTML = `
        <h3>Book Details</h3>

        <p>ID: ${book.bookId}</p>
        <p>Title: ${book.title}</p>
        <p>Author: ${book.author}</p>
        <p>Available: ${book.available}</p>
        <p>Issued Status: ${book.issuedToUserId}</p>
    `;
}

async function deleteBook(bookId) {
    await fetch(
        `${API_URL}/${bookId}`,
        {
            method: "DELETE"
        }
    );
    loadBooks();
}

async function addUser() {
    const userIdField = document.getElementById("userId");
    const userNameField = document.getElementById("userName");
    const userId = parseInt(userIdField.value);
    const name = userNameField.value;
    if (!userId || !name) {
        alert("Please enter both User ID and Name.");
        return;
    }
    const user = { userId: userId, name: name };
    try {
        const response = await fetch(USER_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(user)
        });
        const resultText = await response.text();
        if (response.ok) {
            alert(resultText);
            userIdField.value = "";
            userNameField.value = "";
            loadUsers();
        } else {
            alert(resultText);
        }
    } catch (error) {
        console.error("Network error:", error);
    }
}

async function loadUsers() {
    const response = await fetch(USER_API);
    const users = await response.json();
    const tbody = document.querySelector("#userTable tbody");
    tbody.innerHTML = "";
    users.forEach(user => {
        tbody.innerHTML += `
        <tr>
            <td>${user.userId}</td>
            <td>${user.name}</td>
        </tr>
        `;
    });
}

async function issueBook() {
    const bookId = document.getElementById("issueBookId").value;
    const userId = document.getElementById("issueUserId").value;
    if (!bookId || !userId) {
        alert("Please enter both a Book ID and a User ID.");
        return;
    }
    try {
        const response = await fetch(`${API_URL}/${bookId}/issue/${userId}`, {
            method: "POST"
        });
        const resultText = await response.text();
        if (response.ok && resultText === "Book Issued") {
            alert("Success: " + resultText);
            loadBooks();
        } else {
            alert("Error: " + resultText);
        }
    }
    catch (error) {
        console.error("Network error: ", error);
    }
}

async function returnBook() {
    const bookId = document.getElementById("returnBookId").value;
    if(!bookId)
        return;
    const response = await fetch(`${API_URL}/${bookId}/return`,{method: "POST"});
    const text = await response.text();
    alert(text);
    loadBooks();
    if(document.getElementById("viewUserBooksId").value.trim())
        viewMyBorrowedBooks();
}

async function viewMyBorrowedBooks() {
    const userIdInput = document.getElementById("viewUserBooksId").value.trim();
    const table = document.getElementById("myBorrowedTable");
    const tbody = document.querySelector("#myBorrowedTable tbody");
    const noBooksMsg = document.getElementById("noBooksMessage");

    if (!userIdInput) {
        alert("Please enter a valid User ID to check your borrowed items.");
        return;
    }

    const userId = parseInt(userIdInput);

    try {
        // Fetch the fresh master list from your backend API
        const response = await fetch(API_URL);
        const allBooks = await response.json();

        // Filter out books where issuedToUserId matches the entered ID
        const myBorrowedBooks = allBooks.filter(book => book.issuedToUserId === userId);

        // Clear previous rows
        tbody.innerHTML = "";

        if (myBorrowedBooks.length === 0) {
            // Show "no books" message and hide the table
            table.style.display = "none";
            noBooksMsg.style.display = "block";
        } else {
            // Hide message and construct table rows
            noBooksMsg.style.display = "none";
            table.style.display = "table";

            myBorrowedBooks.forEach(book => {
                tbody.innerHTML += `
                <tr>
                    <td>${book.bookId}</td>
                    <td>${book.isbn ?? "N/A"}</td>
                    <td>${book.title}</td>
                    <td>${book.author}</td>
                </tr>`;
            });
        }
    } catch (error) {
        console.error("Error fetching personal inventory tracking:", error);
        alert("Failed to pull your personal account list from the server.");
    }
}

loadBooks();
loadUsers();
applyRoleVisibility();