<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/fc7f9ea1-3396-4ee1-bd98-e5114797cfec

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Owner email verification codes

The owner portal uses the fixed owner email, Firebase Email/Password for the first factor, and a short-lived email code for each new sign-in. Codes are sent by the Firebase Cloud Function `beginOwnerOtp` through Resend; the raw code is never stored in Firestore. The function allows five sends per hour, one send per minute, and five verification attempts per code.

Before deploying:

1. Enable **Email/Password** in Firebase Authentication. Create the owner account or use the owner portal's account setup button, then confirm the email address.
2. Verify a sender domain in Resend and copy `functions/.env.example` to `functions/.env`, setting `OWNER_OTP_FROM_EMAIL` to that verified sender.
3. Set the Resend API key and a random HMAC key as Firebase secrets. Generate the HMAC key with `openssl rand -base64 32`:

   ```sh
   firebase functions:secrets:set RESEND_API_KEY
   firebase functions:secrets:set OWNER_OTP_HMAC_SECRET
   ```

4. Deploy the functions with `firebase deploy --only functions`. The project must use the same Firebase project as `app/google-services.json` and have Cloud Functions billing enabled.
5. Keep Firebase App Check enabled. Register a debug App Check token for local development; production calls are App Check enforced.
6. Merge the following rules into the project's existing Firestore rules before production. Do not replace existing rules with this snippet; apply the owner claim to every collection that contains owner-only data:

   ```text
   function isOtpVerifiedOwner() {
     return request.auth != null
       && request.auth.token.email == "uuuo333@gmail.com"
       && request.auth.token.email_verified == true
       && request.auth.token.ownerOtpVerified == true;
   }

   match /ownerWallet/{document=**} {
     allow read, write: if isOtpVerifiedOwner();
   }

   match /ownerLoginChallenges/{document=**} {
     allow read, write: if false;
   }

   match /ownerLoginRateLimits/{document=**} {
     allow read, write: if false;
   }
   ```

## Local website

The static customer storefront is `website/index.html`; the separate local owner console is `website/admin.html`. Serve the repository root over HTTP (for example, `python3 -m http.server 8000`) and open `/website/` and `/website/admin.html`. The first owner-console visit creates a PIN for that browser.

Website data is stored in that browser's `localStorage` and is not synchronized to Firebase, other browsers, or devices. The PIN only gates the interface and is not a security boundary; do not use this static console to store sensitive financial or customer data in production. Payment-provider links only redirect to configured URLs; this static site does not process payments.

The owner console also manages ad pauses and subscription visibility, shipping-company choices, preparation-time limits, coupon settings, ticker speed, theme, and imported-product dimensions. Imported products can be displayed with only their image, type, stock, and measurements, with inquiries sent through WhatsApp. The static site stores supplier URLs but cannot reliably scrape arbitrary third-party stores; that requires an authorized API or server-side integration.

Products include a share action. Advertising requests support restaurant daily/monthly/yearly plans, taxi-driver city/area and car photos, and electronic services. Taxi contact details remain hidden in customer listings until the owner manually confirms payment in the ads tab. This local confirmation is a display control only, not payment processing or secure access control.

The storefront footer links to the owner's PIN-gated console. Owners can configure the store icon and customer-facing visibility, define general e-services, and add up to three custom pages. Orders support a locally attached payment receipt, owner-managed payment/shipping status, and a tracking code shown only in that buyer's account when enabled. Because the site uses browser-local storage, receipts, settings, and order updates do not sync across devices and are not suitable for sensitive production records. Structured product data and SEO metadata are best-effort; Google indexing depends on public hosting and Google's crawl.
