const API_URL = "http://localhost:8080/books";
const USER_API = "http://localhost:8080/users";

let currentRole = "ADMIN";

const BOOK_TYPE_FIELDS = {
    FICTION: "genreGroup",
    ACADEMIC: "subjectGroup",
    MAGAZINE: "issueNumberGroup",
};

const EDIT_EXTRA_FIELDS = {
    FICTION: "Genre:",
    ACADEMIC: "Subject:",
    MAGAZINE: "Issue / Vol Number:",
};

function setRole(role) {
    currentRole = role;
    document.getElementById("userRoleBtn").classList.toggle("active", role === "USER");
    document.getElementById("adminRoleBtn").classList.toggle("active", role === "ADMIN");
    applyRoleVisibility();
}

function applyRoleVisibility() {
    const isAdmin = currentRole === "ADMIN";

    document.querySelectorAll(".admin-section").forEach(section => {
        section.classList.toggle("hidden", !isAdmin);
    });

    document.querySelectorAll(".admin-action-col").forEach(col => {
        col.classList.toggle("hidden", !isAdmin);
    });

    loadBooks();
    loadUsers();
}

function toggleGenreField() {
    const type = document.getElementById("bookType").value;
    const visibleGroupId = BOOK_TYPE_FIELDS[type];

    document.querySelectorAll(".book-type-field").forEach(group => {
        group.classList.toggle("is-visible", group.id === visibleGroupId);
    });
}

function clearExtraBookFields() {
    document.getElementById("genre").value = "";
    document.getElementById("subject").value = "";
    document.getElementById("issueNumber").value = "";
}

function resetBookFormAfterSubmit() {
    document.getElementById("isbn").value = "";
    document.getElementById("title").value = "";
    document.getElementById("author").value = "";
    clearExtraBookFields();
    document.getElementById("title").disabled = true;
    document.getElementById("author").disabled = true;
    document.getElementById("bookType").disabled = true;
    document.getElementById("quantity").value = "1";
    toggleGenreField();
}

async function verifyISBN() {
    const isbn = document.getElementById("isbn").value.trim();
    if (!isbn) {
        alert("Please input an ISBN value to verify.");
        return;
    }

    const titleField = document.getElementById("title");
    const authorField = document.getElementById("author");
    const typeField = document.getElementById("bookType");

    try {
        const response = await fetch(`${API_URL}/check/${isbn}`);

        if (response.status === 200) {
            const existingBook = await response.json();
            alert(`ISBN Found! Automatically matching: "${existingBook.title}"`);

            titleField.value = existingBook.title;
            authorField.value = existingBook.author;
            typeField.value = existingBook.type;
            clearExtraBookFields();

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
        } else if (response.status === 204) {
            alert("This ISBN is new to our network. Please fill out the profile fields manually.");

            titleField.value = "";
            authorField.value = "";
            typeField.value = "GENERAL";
            clearExtraBookFields();
            titleField.disabled = false;
            authorField.disabled = false;
            typeField.disabled = false;
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
    const quantity = parseInt(document.getElementById("quantity").value, 10);

    if (!isbn || !title || !author || isNaN(quantity) || quantity < 1) {
        alert("Ensure all necessary structural book metadata and quantities are set correctly.");
        return;
    }

    const payload = { isbn, title, author, type, quantity };

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
            body: JSON.stringify(payload),
        });

        const resultMessage = await response.text();
        if (response.ok) {
            alert(resultMessage);
            resetBookFormAfterSubmit();
            loadBooks();
        } else {
            alert("Error adding copies: " + resultMessage);
        }
    } catch (error) {
        console.error("Network batch payload tracking crash:", error);
    }
}

function openEditModal(isbn, title, author, type, extraField) {
    document.getElementById("editModalIsbnDisplay").innerText = isbn;
    document.getElementById("editModalIsbn").value = isbn;
    document.getElementById("editModalTitle").value = title;
    document.getElementById("editModalAuthor").value = author;

    const extraGroup = document.getElementById("editModalExtraGroup");
    const extraLabel = document.getElementById("editModalExtraLabel");
    const extraInput = document.getElementById("editModalExtraValue");
    const extraLabelText = EDIT_EXTRA_FIELDS[type];

    if (extraLabelText) {
        extraGroup.classList.remove("hidden");
        extraLabel.innerText = extraLabelText;
        extraInput.value = extraField;
    } else {
        extraGroup.classList.add("hidden");
        extraInput.value = "N/A";
    }

    document.getElementById("editBookModal").classList.add("is-open");
}

function closeEditModal() {
    document.getElementById("editBookModal").classList.remove("is-open");
}

async function submitCatalogUpdate() {
    const isbn = document.getElementById("editModalIsbn").value;
    const title = document.getElementById("editModalTitle").value.trim();
    const author = document.getElementById("editModalAuthor").value.trim();
    const extraValue = document.getElementById("editModalExtraValue").value.trim();

    if (!title || !author) {
        alert("Title and Author values cannot be blank.");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/update/${isbn}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ title, author, extraValue }),
        });

        const feedbackMessage = await response.text();
        alert(feedbackMessage);

        if (response.ok) {
            closeEditModal();
            loadBooks();
        }
    } catch (error) {
        console.error("Catalog update channel failure:", error);
    }
}

async function loadBooks() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const catalog = await response.json();
        const tbody = document.querySelector("#bookTable tbody");
        if (!tbody) return;

        const isAdmin = currentRole === "ADMIN";
        tbody.innerHTML = catalog.map(item => {
            const availableClass = item.availableCopies > 0 ? "available-yes" : "available-no";
            const actionCell = isAdmin ? `<td>
    <button class="btn-edit" onclick="openEditModal('${item.isbn}', '${item.title.replace(/'/g, "\\'")}', '${item.author.replace(/'/g, "\\'")}', '${item.type}', '${item.extraField.replace(/'/g, "\\'")}')">Edit</button>
    <button class="btn-delete" onclick="deleteEntireCatalogTitle('${item.isbn}')">Delete Title</button>
</td>` : "";

            return `
            <tr>
                <td><strong>${item.isbn}</strong></td>
                <td>${item.title}</td>
                <td>${item.author}</td>
                <td>${item.type}</td>
                <td>${item.extraField}</td>
                <td>${item.totalCopies}</td>
                <td class="${availableClass}">${item.availableCopies}</td>
                <td>${item.borrowedCopies}</td>
                ${actionCell}
            </tr>`;
        }).join("");
    } catch (error) {
        console.error("Error loading aggregated catalog view:", error);
    }
}

async function searchBook() {
    const isbnInput = document.getElementById("searchIsbn").value.trim();
    const resultDiv = document.getElementById("searchResult");

    if (!isbnInput) {
        alert("Please enter an ISBN number to perform a search.");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/search/${isbnInput}`);

        if (response.status === 404) {
            resultDiv.innerHTML = `<p class="search-error">Book profile not found for ISBN: ${isbnInput}</p>`;
            return;
        }

        const item = await response.json();

        resultDiv.innerHTML = `
            <div class="search-result-card">
                <h3 class="search-result-title">Catalog Match Details</h3>
                <p><strong>ISBN:</strong> ${item.isbn}</p>
                <p><strong>Title:</strong> ${item.title}</p>
                <p><strong>Author / Publisher:</strong> ${item.author}</p>
                <p><strong>Classification:</strong> ${item.type}</p>
                <p><strong>Extra Spec:</strong> ${item.extraField}</p>
                <hr class="search-result-divider" />
                <p><strong>Total Copies in Library:</strong> ${item.totalCopies}</p>
                <p><strong>Available on Shelves:</strong> <span class="available-yes">${item.availableCopies}</span></p>
                <p><strong>Currently Borrowed:</strong> <span class="text-muted">${item.totalCopies - item.availableCopies}</span></p>
            </div>
        `;
    } catch (error) {
        console.error("Catalog look up failure:", error);
        resultDiv.innerHTML = "<p class='search-connection-error'>Failed to connect to search service registry.</p>";
    }
}

async function addUser() {
    const userIdField = document.getElementById("userId");
    const userNameField = document.getElementById("userName");
    const uniqueCardField = document.getElementById("userUniqueCard");

    const userId = parseInt(userIdField.value, 10);
    const name = userNameField.value.trim();
    const uniqueIdCard = uniqueCardField.value.trim();

    if (!userId || !name || !uniqueIdCard) {
        alert("Please enter User ID, Name, and a Unique ID Card reference.");
        return;
    }

    try {
        const response = await fetch(USER_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId, name, uniqueIdCard }),
        });

        const resultText = await response.text();
        alert(resultText);

        if (response.ok && !resultText.includes("Failed")) {
            userIdField.value = "";
            userNameField.value = "";
            uniqueCardField.value = "";
            loadUsers();
        }
    } catch (error) {
        console.error("Network error during user addition:", error);
    }
}

function openEditUserModal(userId, name, uniqueIdCard) {
    document.getElementById("editUserModalIdDisplay").innerText = userId;
    document.getElementById("editUserModalId").value = userId;
    document.getElementById("editUserModalName").value = name;
    document.getElementById("editUserModalUniqueCard").value = uniqueIdCard;
    document.getElementById("editUserModal").classList.add("is-open");
}

function closeEditUserModal() {
    document.getElementById("editUserModal").classList.remove("is-open");
}

async function submitUserUpdate() {
    const userId = document.getElementById("editUserModalId").value;
    const name = document.getElementById("editUserModalName").value.trim();
    const uniqueIdCard = document.getElementById("editUserModalUniqueCard").value.trim();

    if (!name || !uniqueIdCard) {
        alert("Name and Unique ID Card cannot be blank.");
        return;
    }

    try {
        const response = await fetch(`${USER_API}/update/${userId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, uniqueIdCard }),
        });

        const feedbackMessage = await response.text();
        alert(feedbackMessage);

        if (response.ok) {
            closeEditUserModal();
            loadUsers();
        }
    } catch (error) {
        console.error("User update channel failure:", error);
    }
}

async function deleteUser(userId) {
    if (!confirm(`Are you sure you want to delete User ID ${userId}? This cannot be undone.`)) {
        return;
    }

    try {
        const response = await fetch(`${USER_API}/${userId}`, { method: "DELETE" });
        const textMessage = await response.text();
        alert(textMessage);

        if (response.ok) {
            loadUsers();
        }
    } catch (error) {
        console.error("User deletion communication failure:", error);
    }
}

async function loadUsers() {
    const response = await fetch(USER_API);
    const users = await response.json();
    const tbody = document.querySelector("#userTable tbody");
    if (!tbody) return;

    const isAdmin = currentRole === "ADMIN";
    tbody.innerHTML = users.map(user => {
        const actionCell = isAdmin ? `<td>
    <button class="btn-edit" onclick="openEditUserModal(${user.userId}, '${user.name.replace(/'/g, "\\'")}', '${user.uniqueIdCard.replace(/'/g, "\\'")}')">Edit</button>
    <button class="btn-delete" onclick="deleteUser(${user.userId})">Delete</button>
</td>` : "";

        return `
        <tr>
            <td>${user.userId}</td>
            <td>${user.name}</td>
            <td>${user.uniqueIdCard}</td>
            ${actionCell}
        </tr>`;
    }).join("");
}

async function issueBook() {
    const isbn = document.getElementById("issueIsbn").value.trim();
    const userId = document.getElementById("issueUserId").value.trim();
    if (!isbn || !userId) {
        alert("Please enter both the Book ISBN and the target User ID.");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/issue/${isbn}/${userId}`, { method: "POST" });
        const textResponse = await response.text();
        alert(textResponse);

        if (response.ok) {
            document.getElementById("issueIsbn").value = "";
            document.getElementById("issueUserId").value = "";
            loadBooks();
        }
    } catch (error) {
        console.error("Error issuing book asset:", error);
    }
}

async function returnBook() {
    const bookId = document.getElementById("returnBookId").value;
    if (!bookId) return;

    const response = await fetch(`${API_URL}/${bookId}/return`, { method: "POST" });
    alert(await response.text());
    loadBooks();

    if (document.getElementById("viewUserBooksId").value.trim()) {
        viewMyBorrowedBooks();
    }
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

    try {
        const response = await fetch(`${API_URL}/user/${parseInt(userIdInput, 10)}`);
        if (!response.ok) {
            throw new Error("Failed to pull user specific data records");
        }

        const myBorrowedCopies = await response.json();
        tbody.innerHTML = "";

        if (myBorrowedCopies.length === 0) {
            table.classList.add("hidden");
            noBooksMsg.classList.remove("hidden");
            return;
        }

        noBooksMsg.classList.add("hidden");
        table.classList.remove("hidden");
        tbody.innerHTML = myBorrowedCopies.map(copy => `
            <tr>
                <td><strong>${copy.bookId}</strong></td>
                <td>${copy.isbn ?? "N/A"}</td>
                <td>${copy.title}</td>
                <td>${copy.author}</td>
            </tr>
        `).join("");
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
        const response = await fetch(`${API_URL}/catalog/${isbn}`, { method: "DELETE" });
        const textMessage = await response.text();
        alert(textMessage);

        if (response.ok) {
            loadBooks();
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
        const response = await fetch(`${API_URL}/lost/${bookIdInput}`, { method: "DELETE" });
        const feedback = await response.text();
        alert(feedback);

        if (response.ok) {
            document.getElementById("lostBookId").value = "";
            loadBooks();
        }
    } catch (error) {
        console.error("Lost reporting link error:", error);
    }
}

loadBooks();
loadUsers();
applyRoleVisibility();