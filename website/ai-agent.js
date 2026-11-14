(function() {
  const PRODUCTS_KEY = "dabat-store-products-v1";
  const API_KEY_STORAGE = "dabat-gemini-api-key";

  function initAiAgent() {
    const empTab = document.querySelector(".tab[data-tab=\"employees\"]");
    if (!empTab) return;

    const aiTabBtn = document.createElement("button");
    aiTabBtn.className = "tab";
    aiTabBtn.setAttribute("data-tab", "ai-agent");
    aiTabBtn.innerHTML = "\uD83E\uDD16 \u0648\u0643\u064A\u0644 \u0627\u0644\u0630\u0643\u0627\u0621 \u0627\u0644\u0627\u0635\u0637\u0646\u0627\u0639\u064A";
    empTab.insertAdjacentElement("afterend", aiTabBtn);

    const container = empTab.closest(".tabs")?.parentElement || document.querySelector("main") || document.body;
    const aiPanel = document.createElement("section");
    aiPanel.id = "ai-agent-panel";
    aiPanel.style.cssText = "display:none; background:#141824; border:1px solid #2a3147; border-radius:14px; padding:20px; margin-top:16px; color:#fff; direction:rtl;";
    
    const savedKey = localStorage.getItem(API_KEY_STORAGE) || "";

    aiPanel.innerHTML = `
      <h2 style="margin-top:0; color:#38bdf8;">\uD83E\uDD16 \u0648\u0643\u064A\u0644 \u0625\u062F\u0627\u0631\u0629 \u0627\u0644\u0645\u062A\u062C\u0631 (Gemini)</h2>
      
      <div style="background:#1e2436; padding:14px; border-radius:10px; margin-bottom:18px; border:1px solid #334155;">
        <label style="display:block; margin-bottom:6px; font-weight:bold;">\uD83D\uDD11 \u0645\u0641\u062A\u0627\u062D Gemini API:</label>
        <div style="display:flex; gap:8px;">
          <input type="password" id="ai-gemini-key" value="${savedKey}" placeholder="AIzaSy..." style="flex:1; padding:10px; border-radius:8px; border:1px solid #475569; background:#0f172a; color:#fff;">
          <button id="ai-save-key-btn" style="padding:10px 18px; border-radius:8px; border:none; background:#10b981; color:#fff; font-weight:bold; cursor:pointer;">\u062D\u0641\u0638</button>
        </div>
      </div>

      <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap:18px;">
        <div style="background:#1e2436; padding:16px; border-radius:10px; border:1px solid #334155;">
          <h3 style="margin-top:0; color:#a78bfa;">\uD83D\uDED2 \u0633\u062D\u0628 \u0645\u0646\u062A\u062C \u0645\u0646 \u0631\u0627\u0628\u0637</h3>
          <input type="url" id="ai-scrape-url" placeholder="https://..." style="width:100%; box-sizing:border-box; padding:10px; border-radius:8px; border:1px solid #475569; background:#0f172a; color:#fff; margin-bottom:10px;">
          <input type="number" id="ai-profit-margin" value="25" placeholder="25" style="width:100%; box-sizing:border-box; padding:10px; border-radius:8px; border:1px solid #475569; background:#0f172a; color:#fff; margin-bottom:10px;">
          <input type="text" id="ai-scrape-notes" placeholder="\u062A\u0639\u0644\u064A\u0645\u0627\u062A \u0625\u0636\u0627\u0641\u064A\u0629..." style="width:100%; box-sizing:border-box; padding:10px; border-radius:8px; border:1px solid #475569; background:#0f172a; color:#fff; margin-bottom:12px;">
          <button id="ai-scrape-btn" style="width:100%; padding:12px; border-radius:8px; border:none; background:#6366f1; color:#fff; font-weight:bold; cursor:pointer;">\u2728 \u0633\u062D\u0628 \u0648\u0625\u0636\u0627\u0641\u0629 \u0644\u0644\u0645\u062A\u062C\u0631</button>
        </div>

        <div style="background:#1e2436; padding:16px; border-radius:10px; border:1px solid #334155;">
          <h3 style="margin-top:0; color:#fbbf24;">\u26A1 \u0623\u0648\u0627\u0645\u0631 \u062A\u0639\u062F\u064A\u0644 \u0627\u0644\u0645\u062A\u062C\u0631</h3>
          <textarea id="ai-store-command" rows="4" placeholder="\u0645\u062B\u0627\u0644: \u0627\u0631\u0641\u0639 \u0627\u0644\u0623\u0633\u0639\u0627\u0631 10% \u0648\u062D\u0633\u0646 \u0627\u0644\u0623\u0648\u0635\u0627\u0641..." style="width:100%; box-sizing:border-box; padding:10px; border-radius:8px; border:1px solid #475569; background:#0f172a; color:#fff; margin-bottom:12px;"></textarea>
          <button id="ai-command-btn" style="width:100%; padding:12px; border-radius:8px; border:none; background:#f59e0b; color:#000; font-weight:bold; cursor:pointer;">\uD83D\uDE80 \u062A\u0646\u0641\u064A\u0630 \u0627\u0644\u0623\u0645\u0631</button>
        </div>
      </div>

      <div id="ai-agent-status" style="margin-top:16px; padding:12px; border-radius:8px; background:#0f172a; color:#38bdf8; display:none; white-space:pre-wrap;"></div>
    `;

    container.appendChild(aiPanel);

    document.querySelectorAll(".tab").forEach(btn => {
      btn.addEventListener("click", () => {
        if (btn === aiTabBtn) {
          document.querySelectorAll(".tab").forEach(b => b.classList.remove("active"));
          aiTabBtn.classList.add("active");
          aiPanel.style.display = "block";
        } else {
          aiPanel.style.display = "none";
        }
      });
    });

    const showStatus = (msg, isError = false) => {
      const box = document.getElementById("ai-agent-status");
      box.style.display = "block";
      box.style.color = isError ? "#f87171" : "#34d399";
      box.textContent = msg;
    };

    document.getElementById("ai-save-key-btn").onclick = () => {
      const k = document.getElementById("ai-gemini-key").value.trim();
      localStorage.setItem(API_KEY_STORAGE, k);
      showStatus("\u2705 \u062A\u0645 \u062D\u0641\u0638 \u0627\u0644\u0645\u0641\u062A\u0627\u062D!");
    };

    async function callGemini(promptText) {
      const apiKey = localStorage.getItem(API_KEY_STORAGE);
      if (!apiKey) throw new Error("\u0623\u062F\u062E\u0644 \u0645\u0641\u062A\u0627\u062D API \u0623\u0648\u0644\u0627\u064B");
      const res = await fetch( https://googleapis.com{apiKey} , {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ parts: [{ text: promptText }] }],
          generationConfig: { responseMimeType: "application/json" }
        })
      });
      const data = await res.json();
      if (data.error) throw new Error(data.error.message);
      return JSON.parse(data.candidates.content.parts.text);
    }

    document.getElementById("ai-scrape-btn").onclick = async () => {
      const url = document.getElementById("ai-scrape-url").value.trim();
      const margin = Number(document.getElementById("ai-profit-margin").value) || 0;
      const notes = document.getElementById("ai-scrape-notes").value.trim();
      if (!url) return showStatus("\u26A0\uFE0F \u0623\u062F\u062E\u0644 \u0627\u0644\u0631\u0627\u0628\u0637", true);

      try {
        showStatus("\u23F3 \u062C\u0627\u0631\u064A \u0633\u062D\u0628 \u0627\u0644\u0645\u0646\u062A\u062C...");
        const proxyRes = await fetch( https://allorigins.win{encodeURIComponent(url)} );
        const proxyData = await proxyRes.json();
        const doc = new DOMParser().parseFromString(proxyData.contents, "text/html");
        const ogImg = doc.querySelector("meta[property=\"og:image\"]")?.content || doc.querySelector("img")?.src || "";
        doc.querySelectorAll("script, style, nav, footer").forEach(el => el.remove());
        const pageText = doc.body.innerText.replace(/\s+/g, " ").slice(0, 12000);

        const prompt =  Extract product info and translate/rewrite to Arabic marketing style for a car parts store. Add ${margin}% profit margin and convert price to SAR number. Notes: ${notes}. Return ONLY JSON: {"name":"","description":"","price":0,"productType":"","location":""}. Text: ${pageText} ;
        const aiProduct = await callGemini(prompt);
        const currentProducts = JSON.parse(localStorage.getItem(PRODUCTS_KEY) || "[]");
        const newProduct = {
          id: "prod_" + Date.now(),
          name: aiProduct.name || "Product",
          description: aiProduct.description || "",
          price: Number(aiProduct.price) || 0,
          image: ogImg,
          productType: aiProduct.productType || "General",
          location: aiProduct.location || "Saudi Arabia",
          imported: true
        };
        currentProducts.unshift(newProduct);
        localStorage.setItem(PRODUCTS_KEY, JSON.stringify(currentProducts));
        showStatus("\u2705 \u062A\u0645\u062A \u0627\u0644\u0625\u0636\u0627\u0641\u0629! \u062C\u0627\u0631\u064A \u0627\u0644\u062A\u062D\u062F\u064A\u062B...");
        setTimeout(() => location.reload(), 1500);
      } catch (err) {
        showStatus("\u274C " + err.message, true);
      }
    };

    document.getElementById("ai-command-btn").onclick = async () => {
      const cmd = document.getElementById("ai-store-command").value.trim();
      if (!cmd) return showStatus("\u26A0\uFE0F \u0627\u0643\u062A\u0628 \u0627\u0644\u0623\u0645\u0631", true);

      try {
        showStatus("\u23F3 \u062C\u0627\u0631\u064A \u0627\u0644\u062A\u0646\u0641\u064A\u0630...");
        const currentProducts = JSON.parse(localStorage.getItem(PRODUCTS_KEY) || "[]");
        const prompt =  Execute this store owner command on the products array and return ONLY JSON {"reply":"Arabic summary","updatedProducts":[...]}. Command: ${cmd}. Products: ${JSON.stringify(currentProducts)} ;
        const result = await callGemini(prompt);
        if (Array.isArray(result.updatedProducts)) {
          localStorage.setItem(PRODUCTS_KEY, JSON.stringify(result.updatedProducts));
          showStatus("\u2705 " + result.reply);
          setTimeout(() => location.reload(), 1500);
        }
      } catch (err) {
        showStatus("\u274C " + err.message, true);
      }
    };
  }
cd .. && npx firebase deploy --only hosting
