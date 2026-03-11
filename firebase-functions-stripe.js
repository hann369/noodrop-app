// Firebase Functions for Stripe Checkout Integration
// Deploy this to Firebase Functions

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const stripe = require('stripe')(functions.config().stripe.secret_key); // Set via: firebase functions:config:set stripe.secret_key="sk_test_..."

admin.initializeApp();

// Create Stripe Checkout Session
exports.createStripeCheckoutSession = functions.https.onCall(async (data, context) => {
  // Check authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { productId, productName, amount, currency = 'eur', userId, userEmail } = data;

  try {
    // Get product from Firestore
    const productDoc = await admin.firestore()
      .collection('products')
      .doc(productId)
      .get();

    if (!productDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Product not found');
    }

    const product = productDoc.data();

    // Create Stripe checkout session
    const session = await stripe.checkout.sessions.create({
      payment_method_types: ['card'],
      line_items: [{
        price_data: {
          currency: currency,
          product_data: {
            name: productName,
            description: product.description,
            images: product.image ? [product.image] : [],
          },
          unit_amount: amount, // Amount in cents
        },
        quantity: 1,
      }],
      mode: 'payment',
      success_url: `https://noodrop.vercel.app/success?session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `https://noodrop.vercel.app/cancel`,
      customer_email: userEmail,
      metadata: {
        productId: productId,
        userId: userId,
        firebaseUID: context.auth.uid,
      },
    });

    return {
      sessionId: session.id,
      url: session.url,
    };

  } catch (error) {
    console.error('Error creating checkout session:', error);
    throw new functions.https.HttpsError('internal', 'Unable to create checkout session');
  }
});

// Handle Stripe Webhooks
exports.handleStripeWebhook = functions.https.onRequest(async (req, res) => {
  const sig = req.headers['stripe-signature'];
  const endpointSecret = functions.config().stripe.webhook_secret; // Set via: firebase functions:config:set stripe.webhook_secret="whsec_..."

  let event;

  try {
    event = stripe.webhooks.constructEvent(req.rawBody, sig, endpointSecret);
  } catch (err) {
    console.error('Webhook signature verification failed:', err.message);
    return res.status(400).send(`Webhook Error: ${err.message}`);
  }

  try {
    switch (event.type) {
      case 'checkout.session.completed':
        const session = event.data.object;

        // Update user subscription in Firestore
        const userId = session.metadata.firebaseUID;
        const productId = session.metadata.productId;

        // Grant access to the purchased product
        await admin.firestore()
          .collection('users')
          .doc(userId)
          .collection('purchasedProducts')
          .doc(productId)
          .set({
            productId: productId,
            purchasedAt: admin.firestore.FieldValue.serverTimestamp(),
            stripeSessionId: session.id,
            amount: session.amount_total,
            currency: session.currency,
          });

        // Update subscription status if it's a subscription product
        if (productId.includes('subscription') || productId.includes('premium')) {
          await admin.firestore()
            .collection('users')
            .doc(userId)
            .collection('meta')
            .doc('subscription')
            .set({
              isActive: true,
              plan: productId.includes('pro') ? 'PRO' : 'PREMIUM',
              paymentMethod: 'STRIPE',
              stripeCustomerId: session.customer,
              purchasedAt: admin.firestore.FieldValue.serverTimestamp(),
              expiresAt: null, // Set expiration logic as needed
            }, { merge: true });
        }

        console.log(`Purchase completed for user ${userId}, product ${productId}`);
        break;

      case 'invoice.payment_succeeded':
        // Handle recurring payments if you have subscriptions
        break;

      default:
        console.log(`Unhandled event type: ${event.type}`);
    }

    res.json({ received: true });
  } catch (error) {
    console.error('Error handling webhook:', error);
    res.status(500).json({ error: 'Webhook handler failed' });
  }
});

// Optional: Get user's purchased products
exports.getUserPurchases = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  try {
    const purchases = await admin.firestore()
      .collection('users')
      .doc(context.auth.uid)
      .collection('purchasedProducts')
      .get();

    const purchasedProductIds = purchases.docs.map(doc => doc.id);

    return {
      purchasedProducts: purchasedProductIds,
    };
  } catch (error) {
    console.error('Error getting user purchases:', error);
    throw new functions.https.HttpsError('internal', 'Unable to get purchases');
  }
});
