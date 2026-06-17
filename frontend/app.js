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
    document.getElementById("adminLostSection").style.display = is_admin ? "block" : "none";
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
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const catalog = await response.json(); // Defined safely inside the try block
        const tbody = document.querySelector("#bookTable tbody");
        if (!tbody) return; // Guard clause in case table isn't rendered yet

        tbody.innerHTML = "";
        const is_admin = (currentRole === 'ADMIN');

        catalog.forEach(item => {
            // N/A button placeholder for aggregated rows
            let actionCell = is_admin ? `<td><button onclick="deleteEntireCatalogTitle('${item.isbn}')" style="background-color: #dc3545; color: white; border: none; padding: 4px 8px; cursor: pointer;">Delete Title</button></td>` : "";

            tbody.innerHTML += `
            <tr>
                <td><strong>${item.isbn}</strong></td>
                <td>${item.title}</td>
                <td>${item.author}</td>
                <td>${item.type}</td>
                <td>${item.extraField}</td>
                <td>${item.totalCopies}</td>
                <td style="color: ${item.availableCopies > 0 ? 'green' : 'red'}; font-weight: bold;">
                    ${item.availableCopies}
                </td>
                <td>${item.borrowedCopies}</td>
                ${actionCell}
            </tr>`;
        });
    } catch (error) {
        console.error("Error loading aggregated catalog view:", error);
    }
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
    const isbn = document.getElementById("issueIsbn").value.trim();
    const userId = document.getElementById("issueUserId").value.trim();
    if (!isbn || !userId) {
        alert("Please enter both the Book ISBN and the target User ID.");
        return;
    }
    try {
        const response = await fetch(`${API_URL}/issue/${isbn}/${userId}`, {
            method: "POST"
        });
        const textReponse = await response.text();
        alert(textReponse);
        if (response.ok) {
            document.getElementById("issueIsbn").value = "";
            document.getElementById("issueUserId").value = "";
            loadBooks(); // Dynamic catalog reload
        }
    } catch (error) {
        console.error("Error issuing book asset:", error);
    }
}

async function returnBook() {
    const bookId = document.getElementById("returnBookId").value;
    if (!bookId)
        return;
    const response = await fetch(`${API_URL}/${bookId}/return`, { method: "POST" });
    const text = await response.text();
    alert(text);
    loadBooks();
    if (document.getElementById("viewUserBooksId").value.trim())
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
        // 🚀 Hit the new dedicated user tracking endpoint!
        const response = await fetch(`${API_URL}/user/${userId}`);
        if (!response.ok) {
            throw new Error("Failed to pull user specific data records");
        }

        const myBorrowedCopies = await response.json(); // Gets raw Book instances

        // Clear out old rows
        tbody.innerHTML = "";

        if (myBorrowedCopies.length === 0) {
            table.style.display = "none";
            noBooksMsg.style.display = "block";
        } else {
            noBooksMsg.style.display = "none";
            table.style.display = "table";

            myBorrowedCopies.forEach(copy => {
                tbody.innerHTML += `
                <tr>
                    <td><strong>${copy.bookId}</strong></td> <!-- Shows actual returnable ID -->
                    <td>${copy.isbn ?? "N/A"}</td>
                    <td>${copy.title}</td>
                    <td>${copy.author}</td>
                </tr>`;
            });
        }
    } catch (error) {
        console.error("User list aggregation error:", error);
        alert("Failed to retrieve your borrowed tracking list from the server.");
    }
}

async function deleteEntireCatalogTitle(isbn) {
    if (!confirm(`Warning: This will delete ALL physical copies under ISBN ${isbn}. Proceed only if all items are on the shelves.`)) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/catalog/${isbn}`, {
            method: "DELETE"
        });

        const textMessage = await response.text();
        alert(textMessage);

        if (response.ok) {
            loadBooks(); // Fresh catalog refresh
        }
    } catch (error) {
        console.error("Catalog drop communication failure:", error);
    }
}

async function reportLostCopy() {
    const bookIdInput = document.getElementById("lostBookId").value.trim();
    if (!bookIdInput) {
        alert("Please input a valid physical Book ID to report as lost.");
        return;
    }

    if (!confirm(`Are you certain Book ID ${bookIdInput} is lost? This permanently updates inventory records regardless of borrow states.`)) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/lost/${bookIdInput}`, {
            method: "DELETE"
        });

        const feedback = await response.text();
        alert(feedback);

        if (response.ok) {
            document.getElementById("lostBookId").value = "";
            loadBooks(); // Re-calculate summary catalog totals
        }
    } catch (error) {
        console.error("Lost reporting link error:", error);
    }
}

loadBooks();
loadUsers();
applyRoleVisibility();