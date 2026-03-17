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
                appendMessage("bot",
                    "Xin chào! Mình là **FreshBot** 🤖 — trợ lý AI của FreshMart.\n" +
                    "Mình có thể hỗ trợ bạn về:\n" +
                    "• Tra cứu sản phẩm & giá cả\n" +
                    "• Kiểm tra đơn hàng\n" +
                    "• Chính sách giao hàng & đổi trả\n\n" +
                    "Bạn muốn hỏi gì nhé? 😊"
                );

                // Show initial quick replies
                showQuickReplies(["Bạn bán gì?", "Tra đơn hàng", "Chính sách giao hàng"]);
            }
        }
    }

    /**
     * Parse simple markdown-like formatting to HTML.
     */
    function formatBotMessage(text) {
        if (!text) return "";

        let html = text;

        // Escape HTML
        html = html.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

        // Bold: **text** or __text__
        html = html.replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>");
        html = html.replace(/__(.+?)__/g, "<strong>$1</strong>");

        // Italic: *text* or _text_ (not inside **)
        html = html.replace(/(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*)/g, "<em>$1</em>");

        // Bullet lists: - item or * item or • item
        html = html.replace(/^[\-\*•]\s+(.+)$/gm, "<li>$1</li>");
        html = html.replace(/(<li>.*<\/li>\n?)+/gs, function (match) {
            return "<ul>" + match + "</ul>";
        });

        // Numbered lists: 1. item
        html = html.replace(/^\d+\.\s+(.+)$/gm, "<li>$1</li>");

        // Line breaks
        html = html.replace(/\n/g, "<br>");

        // Clean up extra <br> around <ul>
        html = html.replace(/<br><ul>/g, "<ul>");
        html = html.replace(/<\/ul><br>/g, "</ul>");
        html = html.replace(/<br><li>/g, "<li>");

        return html;
    }

    function appendMessage(role, text) {
        const msgDiv = document.createElement("div");
        msgDiv.className = "chatbot-msg shadow-sm " + role;

        if (role === "bot") {
            msgDiv.innerHTML = formatBotMessage(text);
        } else {
            msgDiv.textContent = text;
        }

        messagesBox.appendChild(msgDiv);
        messagesBox.scrollTop = messagesBox.scrollHeight;
    }

    /**
     * Show quick reply suggestion buttons.
     */
    function showQuickReplies(replies) {
        if (!replies || replies.length === 0) return;

        // Remove existing quick replies
        removeQuickReplies();

        const wrapper = document.createElement("div");
        wrapper.className = "chatbot-quick-replies";

        replies.forEach(function (text) {
            const btn = document.createElement("button");
            btn.className = "chatbot-quick-btn";
            btn.textContent = text;
            btn.addEventListener("click", function () {
                removeQuickReplies();
                inputField.value = text;
                sendMessage();
            });
            wrapper.appendChild(btn);
        });

        messagesBox.appendChild(wrapper);
        messagesBox.scrollTop = messagesBox.scrollHeight;
    }

    function removeQuickReplies() {
        const existing = messagesBox.querySelectorAll(".chatbot-quick-replies");
        existing.forEach(function (el) { el.remove(); });
    }

    function setLoading(isLoading) {
        if (loadingIndicator) {
            loadingIndicator.style.display = isLoading ? "flex" : "none";
        }
        sendBtn.disabled = isLoading;
        inputField.disabled = isLoading;
    }

    async function sendMessage() {
        const text = inputField.value.trim();
        if (!text) return;

        removeQuickReplies();
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
                throw new Error("Phản hồi từ server không hợp lệ.");
            }

            if (!response.ok) {
                throw new Error(data && data.message ? data.message : "HTTP " + response.status);
            }

            if (data.status === "success") {
                appendMessage("bot", data.reply || "FreshMart đã nhận câu hỏi của bạn.");
                if (data.sessionToken) {
                    sessionToken = data.sessionToken;
                    localStorage.setItem("chat_session_token", sessionToken);
                }

                // Show quick reply suggestions
                if (data.suggestedReplies && data.suggestedReplies.length > 0) {
                    showQuickReplies(data.suggestedReplies);
                }
            } else {
                appendMessage("bot", data.message || "Xin lỗi, đã xảy ra lỗi nội bộ.");
            }
        } catch (error) {
            console.error("Chat Error:", error);

            if (String(error.message).includes("403")) {
                appendMessage("bot", "Phiên đăng nhập hết hạn. Vui lòng tải lại trang. 🔄");
            } else {
                appendMessage("bot", "Không thể kết nối. Vui lòng thử lại sau nhé! 😊");
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