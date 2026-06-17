const API_URL = "http://localhost:8080/books";
const USER_API = "http://localhost:8080/users";

let isNewIsbn = true;

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
            headers: {w "Content-Type": "application/json" },
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
    books.forEach(book => {
        tbody.innerHTML += `
            <tr>
                <td>${book.bookId}</td>
                <td>${book.isbn ?? "N/A"}</td>
                <td>${book.title}</td>
                <td>${book.author}</td>
                <td>${book.available}</td>
                <td>${book.issuedToUserId ?? "None"}</td>
                <td>
                    <button
                        onclick="deleteBook(${book.bookId})">
                        Delete
                    </button>
                </td>
            </tr>
            `;
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
    await fetch(
        `${API_URL}/${bookId}/return`,
        {
            method: "POST"
        }
    );
    loadBooks();
}

loadBooks();
loadUsers();