document.addEventListener("DOMContentLoaded", function () {
    const toggler = document.getElementById("chatbot-toggler");
    const windowEl = document.getElementById("chatbot-window");
    const closeBtn = document.getElementById("chatbot-close");
    const sendBtn = document.getElementById("chatbot-send-btn");
    const inputField = document.getElementById("chatbot-input");
    const messagesBox = document.getElementById("chatbot-messages");
    const loadingIndicator = document.getElementById("chatbot-loading");

    if (!toggler || !windowEl || !closeBtn || !sendBtn || !inputField || !messagesBox) {
        return;
    }

    let sessionToken = localStorage.getItem("chat_session_token") || createSessionToken();

    if (!localStorage.getItem("chat_session_token")) {
        localStorage.setItem("chat_session_token", sessionToken);
    }

    function createSessionToken() {
        return "chat_" + Date.now() + "_" + Math.random().toString(36).substring(2, 12);
    }

    function getContextPath() {
        if (window.APP_CONTEXT_PATH) {
            return window.APP_CONTEXT_PATH;
        }

        const bodyContext = document.body ? document.body.getAttribute("data-context-path") : "";
        if (bodyContext) {
            return bodyContext;
        }

        const path = window.location.pathname;
        const parts = path.split("/").filter(Boolean);

        if (parts.length > 0) {
            return "/" + parts[0];
        }

        return "";
    }

    function toggleChat() {
        windowEl.classList.toggle("active");

        if (windowEl.classList.contains("active")) {
            inputField.focus();

            if (messagesBox.children.length === 0) {
                appendMessage("bot", "Hello! FreshMart AI Assistant here. How can I help you today?");
            }
        }
    }

    function appendMessage(role, text) {
        const msgDiv = document.createElement("div");
        msgDiv.className = "chatbot-msg shadow-sm " + role;
        msgDiv.textContent = text;
        messagesBox.appendChild(msgDiv);
        messagesBox.scrollTop = messagesBox.scrollHeight;
    }

    function setLoading(isLoading) {
        if (loadingIndicator) {
            loadingIndicator.style.display = isLoading ? "block" : "none";
            loadingIndicator.textContent = "AI is typing...";
        }
        sendBtn.disabled = isLoading;
        inputField.disabled = isLoading;
    }

    async function sendMessage() {
        const text = inputField.value.trim();
        if (!text) return;

        appendMessage("user", text);
        inputField.value = "";
        setLoading(true);

        try {
            const apiUrl = getContextPath() + "/api/chatbot/message";

            const response = await fetch(apiUrl, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json; charset=UTF-8",
                    "Accept": "application/json",
                    "X-CSRF-Token": window.CSRF_TOKEN || ""
                },
                body: JSON.stringify({
                    message: text,
                    sessionToken: sessionToken
                })
            });

            let data = null;
            try {
                data = await response.json();
            } catch (jsonError) {
                throw new Error("Invalid server response format.");
            }

            if (!response.ok) {
                throw new Error(data && data.message ? data.message : "HTTP " + response.status);
            }

            if (data.status === "success") {
                appendMessage("bot", data.reply || "FreshMart has received your query.");
                if (data.sessionToken) {
                    sessionToken = data.sessionToken;
                    localStorage.setItem("chat_session_token", sessionToken);
                }
            } else {
                appendMessage("bot", data.message || "Sorry, an internal error occurred.");
            }
        } catch (error) {
            console.error("Chat Error:", error);

            if (String(error.message).includes("403")) {
                appendMessage("bot", "Session invalid. Please refresh the page.");
            } else {
                appendMessage("bot", "Unable to connect. Please try again later.");
            }
        } finally {
            setLoading(false);
            inputField.focus();
        }
    }

    toggler.addEventListener("click", toggleChat);
    closeBtn.addEventListener("click", toggleChat);
    sendBtn.addEventListener("click", sendMessage);

    inputField.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
});