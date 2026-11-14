import { resetPassword } from "./firebase-auth.js";
async function hashNewPassword(password, salt) {
  const enc = new TextEncoder();
  const keyMaterial = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  const bits = await crypto.subtle.deriveBits(
    { name: "PBKDF2", salt: enc.encode(salt), iterations: 100000, hash: "SHA-256" },
    keyMaterial,
    256
  );
  return Array.from(new Uint8Array(bits)).map(b => b.toString(16).padStart(2, "0")).join("");
}
function attachResetButton() {
  const passInput = document.querySelector('form input[name="password"]');
  const nameInput = document.querySelector('form input[name="name"]');
  if (!passInput || nameInput || document.getElementById("dabat-reset-pw-btn")) return;
  const btn = document.createElement("button");
  btn.type = "button";
  btn.id = "dabat-reset-pw-btn";
  btn.textContent = "\u2728 \u0646\u0633\u064a\u062a \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631\u061f \u0625\u0639\u0627\u062f\u0629 \u062a\u0639\u064d\u064d\u0646";
  btn.style.cssText = "background:none; border:none; color:#38bdf8; text-decoration:underline; cursor:pointer; font-size:13px; margin-top:8px; display:block; text-align:right; padding:0;";
  passInput.parentElement.insertAdjacentElement("afterend", btn);
  btn.onclick = async () => {
    const phoneInput = document.querySelector('form input[name="phone"]');
    const identifier = prompt("\u0623\u062f\u062e\u0644 \u0631\u0642\u0645 \u062c\u0648\u0627\u0644\u0643 \u0627\u0644\u0645\u0633\u062c\u0644 (\u0623\u0648 \u0628\u0631\u064a\u062f\u0643 \u0627\u0644\u0625\u0644\u0643\u062a\u0631\u0648\u0646\u064a):", phoneInput ? phoneInput.value.trim() : "");
    if (!identifier) return;
    if (identifier.includes("@")) {
      try {
        await resetPassword(identifier.trim());
        alert("\u2705 \u062a\u0645 \u0625\u0631\u0633\u0627\u0644 \u0631\u0627\u0628\u0637 \u0625\u0639\u0627\u062f\u0629 \u0627\u0644\u062a\u0639\u064d\u064d\u0646 \u0625\u0644\u0649 \u0628\u0631\u064d\u062f\u0643\u0621!");
      } catch (err) {
        alert("\u274c \u062e\u0637\u0623: " + err.message);
      }
      return;
    }
    const usersKey = Object.keys(localStorage).find(k => k.includes("users")) || "dabat-store-users-v1";
    const users = JSON.parse(localStorage.getItem(usersKey) || "[]");
    const userIndex = users.findIndex(u => String(u.phone).trim() === identifier.trim());
    if (userIndex === -1) {
      alert("\u26a0\ufe0b \u0631\u0642\u0645 \u0627\u0644\u062c\u0648\u0627\u0644 \u063a\u064a\u0631 \u0645\u0633\u062c\u0644 \u0641\u0649 \u0627\u0644\u0645\u062a\u062c\u0631.");
      return;
    }
    const newPass = prompt("\u0623\u062f\u062e\u0644 \u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \L
���L
�
L
���L
���L
�WL
���L
��H

�L
���L
��L
��WL
�HL
��WL
�
L
�HL
���L
�
L
���L
��L
�
N��NY�
[�]�\���]�\�˛[��
�H[\�
�L��LY�L�L
�WL
���L
��L
���L
�
�L
��WL
��L
�L
�
�
�L
���L
��L
��WL
�HL
��WL
�
L
�HL
���L
�
L
���L
��L
�
��N�]\��B��H�ۜ��[Hܞ\˜�[��UURQ�ܞ\˜�[��UURQ

H���[��]K����
JN�ۜ�\�H]�Z]\��]�\���ܙ
�]�\���[
N\�\���\�\�[�^K�\���ܙH��[\�N��[�ܘY�K��]][J\�\���^K��Ӌ���[��Y�J\�\��JN[\�
�L��
HL
��WL
�
HL
��WL
��WL
�L
�L
��HL
��L
�
L
�
WL
��HL
���L
�
L
�
WL
��WL
�L
��HSc#��ScCe�Sc&5�Sc#u�Sc&B"����6F6��W'"����W'B�%�S#sF2�Sc&U�Sc3u�Sc#3�"�W'"��W76vR���ТӰ�Ц6��7B�'6W'fW"��Wr�WFF����'6W'fW"�����GF6�&W6WD'WGF�ₒ����'6W'fW"��'6W'fR�F�7V�V�B�&�G���6���DƗ7C�G'VR�7V'G&VS�G'VRғ��GF6�&W6WD'WGF�ₓ