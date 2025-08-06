function renderMarkdownToElement(element, rawText) {
    if (!element) return;

    // 변환된 텍스트로 마크다운 렌더링
    let html = marked.parse(rawText || "", {
        highlight: function (code, lang) {
            if (lang && hljs.getLanguage(lang)) {
                return hljs.highlight(code, { language: lang }).value;
            }
            return hljs.highlightAuto(code).value;
        }
    });
    element.innerHTML = html;

    // 코드 블럭 복사 버튼 추가
    element.querySelectorAll("pre code").forEach(block => {
        const parentPre = block.parentElement;
        if (!parentPre.classList.contains("code-block")) {
            const wrapperDiv = document.createElement("div");
            wrapperDiv.classList.add("code-block");
            parentPre.replaceWith(wrapperDiv);
            wrapperDiv.appendChild(parentPre);

            const btn = document.createElement("button");
            btn.className = "copy-btn";
            btn.innerText = "Copy";
            btn.onclick = () => {
                navigator.clipboard.writeText(block.innerText);
                btn.innerText = "Copied!";
                setTimeout(() => btn.innerText = "Copy", 1500);
            };
            wrapperDiv.appendChild(btn);
        }
    });
}

// 과거 대화 내역용
function renderMarkdownFromDataAttr(selector) {
    document.querySelectorAll(selector).forEach(el => {
        const raw = el.getAttribute("data-content") || "";
        renderMarkdownToElement(el, raw);
    });
}

function clearTypingIndicator() {
    document.querySelectorAll(".typing-indicator").forEach(el => el.remove());
}

window.renderMarkdownToElement = renderMarkdownToElement;
window.renderMarkdownFromDataAttr = renderMarkdownFromDataAttr;