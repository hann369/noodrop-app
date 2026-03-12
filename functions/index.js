const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin  = require("firebase-admin");
const stripe = require("stripe");

admin.initializeApp();

// ── Secrets ───────────────────────────────────────────────────────────────────
// Run these once before deploying:
//   firebase functions:secrets:set STRIPE_SECRET_KEY
//   firebase functions:secrets:set STRIPE_WEBHOOK_SECRET
const stripeSecretKey     = defineSecret("STRIPE_SECRET_KEY");
const stripeWebhookSecret = defineSecret("STRIPE_WEBHOOK_SECRET");

exports.stripeWebhook = onRequest(
  { secrets: [stripeSecretKey, stripeWebhookSecret] },
  async (req, res) => {
    const stripeClient = stripe(stripeSecretKey.value());
    const sig          = req.headers["stripe-signature"];

    let event;
    try {
      event = stripeClient.webhooks.constructEvent(
        req.rawBody,
        sig,
        stripeWebhookSecret.value()
      );
    } catch (err) {
      console.error("Webhook signature verification failed:", err.message);
      return res.status(400).send(`Webhook Error: ${err.message}`);
    }

    const db = admin.firestore();

    try {
      switch (event.type) {

        case "checkout.session.completed": {
          const session = event.data.object;
          const uid     = session.client_reference_id;

          if (!uid) {
            console.warn("No client_reference_id in session:", session.id);
            break;
          }

          await db
            .collection("users").doc(uid)
            .collection("meta").doc("subscription")
            .set({
              isActive:         true,
              plan:             "PREMIUM",
              paymentMethod:    "STRIPE",
              stripeCustomerId: session.customer     || null,
              subscriptionId:   session.subscription || null,
              activatedAt:      admin.firestore.FieldValue.serverTimestamp(),
              expiresAt:        null,
            }, { merge: true });

          console.log(`PREMIUM activated for uid=${uid}`);
          break;
        }

        case "customer.subscription.deleted": {
          const customerId = event.data.object.customer;

          const snap = await db
            .collectionGroup("meta")
            .where("stripeCustomerId", "==", customerId)
            .limit(1).get();

          if (!snap.empty) {
            await snap.docs[0].ref.set({
              isActive:      false,
              plan:          "FREE",
              paymentMethod: "NONE",
              cancelledAt:   admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });
            console.log(`PREMIUM deactivated for customer=${customerId}`);
          }
          break;
        }

        default:
          console.log(`Unhandled event: ${event.type}`);
      }
    } catch (err) {
      console.error("Error:", err);
      return res.status(500).send("Internal error");
    }

    res.json({ received: true });
  }
);
