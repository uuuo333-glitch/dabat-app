(function () {
  const API_KEY_STORAGE = "dabat-gemini-api-key";

  function fillField(keywords, val) {
    if (!val) return;
    const fields = Array.from(document.querySelectorAll("input, textarea"));
    const target = fields.find(el => {
      if (el.type === "file" || el.type === "hidden") return false;
      const lbl = (el.previousElementSibling?.textContent || "") + " " + (el.placeholder || "") + " " + (el.name || "");
      return keywords.some(k => lbl.includes(k));
    });
    if (target) {
      target.value = val;
      target.dispatchEvent(new Event("input", { bubbles: true }));
      target.dispatchEvent(new Event("change", { bubbles: true }));
    }
  }

  function injectSmartBtn() {
    if (!location.pathname.includes("admin") || document.getElementById("dabat-smart-pull-btn")) return;
    const inputs = Array.from(document.querySelectorAll("input"));
    const urlInp = inputs.find(i => {
      const txt = (i.previousElementSibling?.textContent || "") + " " + (i.placeholder || "");
      return txt.includes("\u0627\u0644\u0645\u0648\u0631\u062f") || txt.includes("\u0631\u0627\u0628\u0637 \u0627\u0644\u0645\u0646\u062a\u062c");
    });
    if (!urlInp) return;

    const oldBtn = document.getElementById("dabat-fetch-img-btn");
    if (oldBtn) oldBtn.remove();

    const btn = document.createElement("button");
    btn.type = "button";
    btn.id = "dabat-smart-pull-btn";
    btn.textContent = "\u26a1 \u0633\u062d\u0628 \u0643\u0627\u0645\u0644 \u0627\u0644\u0628\u064a\u0627\u0646\u0627\u062a (\u0627\u0644\u0627\u0633\u0645 + \u0627\u0644\u0648\u0635\u0641 + \u0627\u0644\u0635\u0648\u0631\u0629 + \u0627\u0644\u0633\u0639\u0631)";
    btn.style.cssText = "width:100%; padding:11px; margin:8px 0; border-radius:9px; border:none; background:linear-gradient(135deg,#f59e0b,#d97706); color:#000; font-weight:bold; font-size:13px; cursor:pointer;";
    urlInp.insertAdjacentElement("afterend", btn);

    btn.onclick = async () => {
      const url = urlInp.value.trim();
      if (!url) return alert("\u0636\u0639 \u0631\u0627\u0628\u0637 \u0627\u0644\u0645\u0646\u062a\u062c \u0623\u0648\u0644\u0627\u064b");
      btn.textContent = "\u23f3 \u062c\u0627\u0631\u064a \u0633\u062d\u0628 \u0648\u062a\u0639\u0628\u0626\u0629 \u0628\u064a\u0627\u0646\u0627\u062a \u0627\u0644\u0645\u0646\u062a\u062c...";
      try {
        const res = await fetch("https://microlink.io" + encodeURIComponent(url));
        const json = await res.json();
        const data = json.data || {};
        let title = data.title || "";
        let desc = data.description || "";
        const img = data.image?.url || data.logo?.url || "";
        let price = "";

        const apiKey = localStorage.getItem(API_KEY_STORAGE);
        if (apiKey && (title || desc)) {
          try {
            const gRes = await fetch("https://googleapis.com" + apiKey, {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                contents: [{ parts: [{ text:  Translate to Arabic for an auto parts store and extract numeric price in SAR. Return JSON only {"name":"","desc":"","price":""}. Title: ${title}. Desc: ${desc}  }] }],
                generationConfig: { responseMimeType: "application/json" }
              })
            });
            const gData = await gRes.json();
            const parsed = JSON.parse(gData.candidates.content.parts.text);
            if (parsed.name) title = parsed.name;
            if (parsed.desc) desc = parsed.desc;
            if (parsed.price) price = String(parsed.price).replace(/[^0-9.]/g, "");
          } catch (_) {}
        }

        fillField(["\u0627\u0633\u0645 \u0627\u0644\u0645\u0646\u062a\u062c", "\u0627\u0644\u0627\u0633\u0645", "name"], title);
        fillField(["\u0627\u0644\u0648\u0635\u0641", "\u062a\u0641\u0627\u0635\u064a\u0644", "desc"], desc);
        fillField(["\u0635\u0648\u0631\u0629 \u0628\u062f\u064a\u0644", "\u0631\u0627\u0628\u0637 \u0635\u0648\u0631\u0629", "image"], img);
        if (price) fillField(["\u0627\u0644\u0633\u0639\u0631", "price"], price);

        btn.textContent = "\u2705 \u062a\u0645 \u0633\u062d\u0628 \u0648\u062a\u0639\u0628\u0626\u0629 \u0627\u0644\u0628\u064a\u0627\u0646\u0627\u062a \u0628\u0646\u062c\u0627\u062d!";
      } catch (e) {
        btn.textContent = "\u26a0\ufe0f \u062a\u0639\u0630\u0631 \u0627\u0644\u0633\u062d\u0628 \u0627\u0644\u062a\u0644\u0642\u0627\u0626\u064a\u060c \u0623\u062f\u062e\u0644 \u0627\u0644\u0628\u064a\u0627\u0646\u0627\u062a \u064a\u062f\u0648\u064a\u0627\u064b";
      }
    };
  }

  new MutationObserver(injectSmartBtn).observe(document.body, { childList: true, subtree: true });
  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", injectSmartBtn);
  else injectSmartBtn();
})();
