(() => {
  "use strict";

  const DATABASE = "dabat-store-media-v1";
  const STORE = "media";
  const PREFIX = "idb-media:";
  const objectUrls = new Map();
  const pendingUrls = new Map();
  const legacyUrls = new Map();
  let databasePromise;

  function openDatabase() {
    if (databasePromise) return databasePromise;
    if (!("indexedDB" in window)) {
      return Promise.reject(new Error("المتصفح لا يدعم تخزين الوسائط المحلي"));
    }

    databasePromise = new Promise((resolve, reject) => {
      const request = indexedDB.open(DATABASE, 1);
      request.onupgradeneeded = () => {
        if (!request.result.objectStoreNames.contains(STORE)) {
          request.result.createObjectStore(STORE, { keyPath: "id" });
        }
      };
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error || new Error("تعذر فتح قاعدة بيانات الوسائط"));
      request.onblocked = () => reject(new Error("قاعدة بيانات الوسائط مشغولة في تبويب آخر"));
    });
    return databasePromise;
  }

  async function compressImage(file) {
    if (!file.type.startsWith("image/") || /image\/(?:gif|svg\+xml)/i.test(file.type) || typeof createImageBitmap !== "function") {
      return file;
    }

    let bitmap;
    try {
      bitmap = await createImageBitmap(file);
    } catch {
      return file;
    }

    try {
      const scale = Math.min(1, 1920 / bitmap.width, 1920 / bitmap.height);
      const width = Math.max(1, Math.round(bitmap.width * scale));
      const height = Math.max(1, Math.round(bitmap.height * scale));
      const canvas = document.createElement("canvas");
      canvas.width = width;
      canvas.height = height;
      const context = canvas.getContext("2d");
      if (!context) return file;
      context.drawImage(bitmap, 0, 0, width, height);
      const compressed = await new Promise(resolve => canvas.toBlob(resolve, "image/webp", 0.82));
      return compressed && compressed.size < file.size ? compressed : file;
    } finally {
      bitmap.close();
    }
  }

  async function save(file) {
    if (!(file instanceof Blob)) throw new TypeError("الملف المرفوع غير صالح");
    const blob = await compressImage(file);
    const id = `media-${crypto.randomUUID()}`;
    const database = await openDatabase();

    await new Promise((resolve, reject) => {
      const transaction = database.transaction(STORE, "readwrite");
      transaction.objectStore(STORE).put({
        id,
        blob,
        type: blob.type || file.type || "application/octet-stream",
        name: file.name || "media"
      });
      transaction.oncomplete = resolve;
      transaction.onerror = () => reject(transaction.error || new Error("تعذر حفظ الملف في قاعدة بيانات الوسائط"));
      transaction.onabort = () => reject(transaction.error || new Error("تم إلغاء حفظ الملف"));
    });

    return `${PREFIX}${id}`;
  }

  function readBlob(id) {
    return openDatabase().then(database => new Promise((resolve, reject) => {
      const transaction = database.transaction(STORE, "readonly");
      const request = transaction.objectStore(STORE).get(id);
      request.onsuccess = () => {
        if (!request.result?.blob) {
          reject(new Error("ملف الوسائط غير موجود في قاعدة بيانات المتصفح"));
          return;
        }
        resolve(request.result.blob);
      };
      request.onerror = () => reject(request.error || new Error("تعذر قراءة ملف الوسائط"));
    }));
  }

  async function resolve(source) {
    if (typeof source !== "string" || !source.startsWith(PREFIX)) return source || "";
    if (objectUrls.has(source)) return objectUrls.get(source);
    if (pendingUrls.has(source)) return pendingUrls.get(source);

    const pending = readBlob(source.slice(PREFIX.length)).then(blob => {
      const url = URL.createObjectURL(blob);
      objectUrls.set(source, url);
      return url;
    }).finally(() => pendingUrls.delete(source));
    pendingUrls.set(source, pending);
    return pending;
  }

  function watch(root, onError) {
    function hydrate(element, attribute) {
      const source = element.getAttribute(attribute);
      if (!source || !source.startsWith(PREFIX)) return;
      resolve(source).then(url => {
        if (element.getAttribute(attribute) !== source) return;
        element.setAttribute(attribute, url);
        if (attribute === "src" && element instanceof HTMLVideoElement && (element.classList.contains("hero-media") || element.classList.contains("footer-media"))) {
          element.muted = true;
          element.autoplay = true;
          element.loop = true;
          element.playsInline = true;
          element.setAttribute("muted", "");
          element.setAttribute("autoplay", "");
          element.setAttribute("loop", "");
          element.setAttribute("playsinline", "");
          element.load();
          element.play().catch(error => console.warn("تعذر تشغيل فيديو العرض تلقائياً", error));
        }
      }).catch(error => {
        if (onError) onError(error, element);
        else console.error("تعذر تحميل الوسائط", error);
      });
    }

    function hydrateTree(node) {
      if (!(node instanceof Element)) return;
      hydrate(node, "src");
      hydrate(node, "poster");
      node.querySelectorAll("[src], [poster]").forEach(element => {
        hydrate(element, "src");
        hydrate(element, "poster");
      });
    }

    hydrateTree(root);
    const observer = new MutationObserver(records => {
      for (const record of records) {
        if (record.type === "attributes") {
          hydrate(record.target, record.attributeName);
        } else {
          record.addedNodes.forEach(hydrateTree);
        }
      }
    });
    observer.observe(root, { childList: true, subtree: true, attributes: true, attributeFilter: ["src", "poster"] });
    return observer;
  }

  function isMediaDataUrl(value) {
    return /^data:(?:image|video|audio)\//i.test(value) || /^data:application\/pdf[;,]/i.test(value);
  }

  async function migrateValue(value) {
    if (typeof value === "string" && isMediaDataUrl(value)) {
      if (legacyUrls.has(value)) return legacyUrls.get(value);
      const response = await fetch(value);
      if (!response.ok) throw new Error("تعذر قراءة ملف وسائط قديم");
      const reference = await save(await response.blob());
      legacyUrls.set(value, reference);
      return reference;
    }
    if (Array.isArray(value)) return Promise.all(value.map(migrateValue));
    if (value && typeof value === "object") {
      const entries = await Promise.all(Object.entries(value).map(async ([key, item]) => [key, await migrateValue(item)]));
      return Object.fromEntries(entries);
    }
    return value;
  }

  async function migrateLocalStorageKeys(keys) {
    const migrated = [];
    for (const key of keys) {
      const raw = localStorage.getItem(key);
      if (raw === null) continue;
      const original = JSON.parse(raw);
      const replacement = await migrateValue(original);
      if (JSON.stringify(original) !== JSON.stringify(replacement)) {
        if (localStorage.getItem(key) !== raw) continue;
        localStorage.setItem(key, JSON.stringify(replacement));
        migrated.push(key);
      }
    }
    return migrated;
  }

  window.DabatMediaStore = Object.freeze({
    database: DATABASE,
    store: STORE,
    save,
    resolve,
    watch,
    migrateLocalStorageKeys
  });
})();
