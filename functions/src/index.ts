import { createHmac, randomBytes, randomInt, timingSafeEqual } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { defineSecret, defineString } from "firebase-functions/params";
import { HttpsError, onCall } from "firebase-functions/v2/https";

initializeApp();

const OWNER_EMAIL = "uuuo333@gmail.com";
const REGION = "us-central1";
const OTP_LIFETIME_MS = 5 * 60 * 1000;
const OTP_RESEND_INTERVAL_MS = 60 * 1000;
const OTP_SEND_WINDOW_MS = 60 * 60 * 1000;
const MAX_OTP_SENDS_PER_HOUR = 5;
const MAX_OTP_ATTEMPTS = 5;

const resendApiKey = defineSecret("RESEND_API_KEY");
const otpHashSecret = defineSecret("OWNER_OTP_HMAC_SECRET");
const otpFromEmail = defineString("OWNER_OTP_FROM_EMAIL");

function hashOtp(uid: string, code: string): string {
  const secret = otpHashSecret.value();
  if (Buffer.byteLength(secret) < 32) {
    throw new HttpsError("failed-precondition", "OTP secret must contain at least 32 bytes.");
  }
  return createHmac("sha256", secret).update(`${uid}:${code}`).digest("hex");
}

function matchesHash(actual: string, expected: string): boolean {
  const actualBytes = Buffer.from(actual, "hex");
  const expectedBytes = Buffer.from(expected, "hex");
  return actualBytes.length === expectedBytes.length && timingSafeEqual(actualBytes, expectedBytes);
}

export const beginOwnerOtp = onCall(
  {
    region: REGION,
    enforceAppCheck: true,
    maxInstances: 2,
    secrets: [resendApiKey, otpHashSecret],
  },
  async (request) => {
    if (!request.auth) {
      throw new HttpsError("unauthenticated", "Sign in with the owner email and password first.");
    }
    if (
      request.auth.token.email?.toLowerCase() !== OWNER_EMAIL ||
      request.auth.token.email_verified !== true
    ) {
      throw new HttpsError("permission-denied", "Owner account is not verified.");
    }

    const owner = await getAuth().getUser(request.auth.uid);
    if (
      owner.email?.toLowerCase() !== OWNER_EMAIL ||
      owner.emailVerified !== true
    ) {
      throw new HttpsError("permission-denied", "Owner account is not verified.");
    }

    const code = randomInt(0, 1_000_000).toString().padStart(6, "0");
    const challengeId = randomBytes(24).toString("base64url");
    const now = Date.now();
    const database = getFirestore();
    const challengeRef = database.collection("ownerLoginChallenges").doc(owner.uid);
    const rateLimitRef = database.collection("ownerLoginRateLimits").doc(owner.uid);
    const codeHash = hashOtp(owner.uid, code);

    await database.runTransaction(async (transaction) => {
      const current = await transaction.get(rateLimitRef);
      const lastSentAt = current.get("sentAt") as number | undefined;
      if (lastSentAt && now - lastSentAt < OTP_RESEND_INTERVAL_MS) {
        throw new HttpsError("resource-exhausted", "Wait before requesting another code.");
      }
      const currentWindowStart = current.get("windowStart") as number | undefined;
      const currentWindowCount = current.get("windowCount") as number | undefined;
      const inCurrentWindow =
        currentWindowStart !== undefined && now - currentWindowStart < OTP_SEND_WINDOW_MS;
      if (inCurrentWindow && (currentWindowCount ?? 0) >= MAX_OTP_SENDS_PER_HOUR) {
        throw new HttpsError("resource-exhausted", "Too many codes requested. Try again later.");
      }
      transaction.set(rateLimitRef, {
        sentAt: now,
        windowStart: inCurrentWindow ? currentWindowStart : now,
        windowCount: inCurrentWindow ? (currentWindowCount ?? 0) + 1 : 1,
      });
      transaction.set(challengeRef, {
        challengeId,
        codeHash,
        expiresAt: now + OTP_LIFETIME_MS,
        attempts: 0,
      });
    });

    const from = otpFromEmail.value().trim();
    if (!from) {
      throw new HttpsError("failed-precondition", "Configure a verified sender email.");
    }
    const emailResponse = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${resendApiKey.value()}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        from,
        to: [OWNER_EMAIL],
        subject: "رمز الدخول إلى لوحة مالك المتجر",
        text: `رمز التحقق الخاص بك هو: ${code}\n\nتنتهي صلاحيته خلال 5 دقائق. لا تشارك هذا الرمز مع أحد.`,
      }),
    });

    if (!emailResponse.ok) {
      await database.runTransaction(async (transaction) => {
        const current = await transaction.get(challengeRef);
        if (current.get("challengeId") === challengeId) transaction.delete(challengeRef);
      });
      throw new HttpsError("unavailable", "Could not send the owner verification email.");
    }

    return { challengeId };
  }
);

export const verifyOwnerOtp = onCall(
  {
    region: REGION,
    enforceAppCheck: true,
    maxInstances: 2,
    secrets: [otpHashSecret],
  },
  async (request) => {
    const challengeId = request.data?.challengeId;
    const code = request.data?.code;
    if (
      typeof challengeId !== "string" ||
      typeof code !== "string" ||
      !/^\d{6}$/.test(code)
    ) {
      throw new HttpsError("invalid-argument", "Enter a valid six-digit code.");
    }

    const owner = await getAuth().getUserByEmail(OWNER_EMAIL);
    if (!owner.emailVerified) {
      throw new HttpsError("permission-denied", "Owner email is not verified.");
    }
    const challengeRef = getFirestore()
      .collection("ownerLoginChallenges")
      .doc(owner.uid);

    const verification = await getFirestore().runTransaction(async (transaction) => {
      const snapshot = await transaction.get(challengeRef);
      if (!snapshot.exists || snapshot.get("challengeId") !== challengeId) return "missing";
      if ((snapshot.get("expiresAt") as number) <= Date.now()) {
        transaction.delete(challengeRef);
        return "expired";
      }
      const attempts = (snapshot.get("attempts") as number) ?? 0;
      if (attempts >= MAX_OTP_ATTEMPTS) {
        transaction.delete(challengeRef);
        return "locked";
      }

      const expectedHash = snapshot.get("codeHash") as string;
      if (!matchesHash(hashOtp(owner.uid, code), expectedHash)) {
        const nextAttempts = attempts + 1;
        if (nextAttempts >= MAX_OTP_ATTEMPTS) transaction.delete(challengeRef);
        else transaction.update(challengeRef, { attempts: nextAttempts });
        return "invalid";
      }

      transaction.delete(challengeRef);
      return "valid";
    });

    if (verification !== "valid") {
      throw new HttpsError(
        verification === "locked" ? "resource-exhausted" : "permission-denied",
        "The code is invalid, expired, or has reached the attempt limit."
      );
    }

    const customToken = await getAuth().createCustomToken(owner.uid, {
      ownerOtpVerified: true,
    });
    return { customToken };
  }
);
