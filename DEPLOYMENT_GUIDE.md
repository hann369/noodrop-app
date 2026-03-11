# 📦 DEPLOYMENT GUIDE - NOODROP APP

## Pre-Deployment Checklist

### Code Quality
- [x] No compilation errors
- [x] No runtime crashes (tested on Android 12+)
- [x] No console errors/warnings
- [x] All features working
- [x] Proper error handling

### Testing
- [x] Manual testing complete
- [x] Authentication working
- [x] Data persistence verified
- [x] Charts rendering correctly
- [x] Animations smooth

### Security
- [x] API keys not hardcoded
- [x] Passwords encrypted
- [x] Firebase rules configured
- [x] HTTPS enforced
- [x] No sensitive data in logs

### Performance
- [x] App load < 3 seconds
- [x] List scrolling smooth (60fps)
- [x] No memory leaks
- [x] Image optimization complete

---

## Firebase Configuration

### 1. Firestore Database Setup

```javascript
// Enable these collections:

// Users collection
users/{userId}/
├── email: string
├── createdAt: timestamp
├── theme: string (dark/light)
└── subscription: reference

// Stacks collection (personal)
stacks/{userId}/
├── entries: List<StackEntry>
├── notes: string
└── lastUpdated: timestamp

// Logs collection (daily tracking)
logs/{userId}/
├── date: date
├── mood: int (0-10)
├── fog: int (0-10)
├── energy: int (0-10)
├── health: int (0-10)
├── focus: int (0-10)
├── checkedCompounds: List<string>
└── notes: string

// Subscriptions
subscriptions/{userId}/
├── isActive: boolean
├── plan: string (FREE/PREMIUM/PRO)
├── expiresAt: timestamp
├── paymentMethod: string
└── stripeCustomerId: string

// Products
products/
├── premium_monthly: {name, price, stripePriceId}
└── pro_monthly: {name, price, stripePriceId}

// Timeline
timeline/{userId}/
├── entries: List<TimelineEntry>
└── lastUpdated: timestamp
```

### 2. Firebase Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Stacks - user personal
    match /stacks/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Logs - user personal
    match /logs/{userId}/{logId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Subscriptions - user personal
    match /subscriptions/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if false; // Write only from Cloud Function
    }
    
    // Products - public read
    match /products/{document=**} {
      allow read: if request.auth != null;
    }
    
    // Timeline - user personal
    match /timeline/{userId}/{entry} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. Authentication Setup

```bash
# In Firebase Console:
1. Go to Authentication
2. Enable Email/Password provider
3. Optional: Enable Google Sign-In
4. Set up email templates (welcome, reset, etc.)
```

---

## Build & Sign APK

### 1. Generate Signing Key
```bash
# Generate keystore (one-time)
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias noodrop_key

# Enter: CN, OU, O, L, ST, C when prompted
```

### 2. Configure Signing in Gradle

Create `app/keystore.properties`:
```properties
storeFile=../release.keystore
storePassword=YOUR_PASSWORD
keyAlias=noodrop_key
keyPassword=YOUR_PASSWORD
```

Update `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "noodrop_key"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 3. Build Release APK
```bash
# Build signed APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk

# Or build AAB for Play Store
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

---

## Google Play Store Submission

### 1. Developer Account Setup
- Create Google Play Developer account ($25 one-time)
- Verify phone number
- Set up payment method
- Review Google Play policies

### 2. App Listing

**Store Listing Info:**
- Title: "noodrop - Nootropics Tracker"
- Subtitle: "Track, Optimize & Research Nootropics"
- Description:
  ```
  noodrop is your personal nootropics research and optimization platform.
  
  Features:
  • Track compound stacks and daily metrics
  • Analyze mood, energy, focus improvements
  • Access 20+ researched compounds
  • Explore 6 science-backed protocols
  • Premium analytics and insights
  
  Perfect for biohackers, researchers, and anyone optimizing their cognition.
  ```
- Category: Health & Fitness / Medical
- Content Rating: Unrated (submit for rating)
- Target: Android 12+ (API 31+)

**Screenshots (5 required, up to 8 recommended):**
1. Dashboard overview
2. Stack builder
3. Daily tracker
4. Metrics charts
5. Library protocols
6. Profile stats

**Graphics:**
- Feature graphic (1024x500)
- App icon (512x512)
- Promo video (optional, 30 sec)

### 3. Pricing & Distribution

- **Price:** Free with in-app subscriptions
- **Premium:** €9.99/month
- **Pro:** €19.99/month
- **Countries:** Configure available regions
- **Devices:** Phone (800x480 minimum)

### 4. Content Rating Questionnaire
- Violence: None
- Advertising: No
- Financial info: Yes (subscriptions)
- Location: No
- Health topics: Yes

### 5. Privacy Policy
- Describe data collection
- Explain Firestore use
- Detail subscription info
- Link to privacy policy URL

### 6. Testing & Rollout

**Closed Testing (Beta):**
1. Create test group (add 50+ testers)
2. Release to 1% for 1 week
3. Monitor crashes/ratings
4. Gather feedback

**Production Release:**
1. Increase rollout to 100%
2. Monitor metrics daily
3. Watch for crashes
4. Respond to user reviews

---

## Stripe Payment Integration

### 1. Create Stripe Account
- Sign up at [stripe.com](https://stripe.com)
- Verify business info
- Enable payments

### 2. Create Products & Prices

**Premium Subscription:**
- Product ID: "premium_monthly"
- Price: €9.99
- Billing cycle: Monthly
- Lookup code: "premium_monthly_eur"

**Pro Subscription:**
- Product ID: "pro_monthly"
- Price: €19.99
- Billing cycle: Monthly
- Lookup code: "pro_monthly_eur"

### 3. Configure Webhooks
```
Endpoint URL: your-backend.com/webhooks/stripe
Events: 
- invoice.payment_succeeded
- invoice.payment_failed
- customer.subscription.updated
- customer.subscription.deleted
```

### 4. Testing
- Use Stripe test keys during development
- Switch to live keys for production
- Test with card: 4242 4242 4242 4242

---

## Post-Deployment Monitoring

### Firebase Analytics Dashboard
```
Track:
- Daily active users
- Retention (D1, D7, D30)
- Crash-free users
- Average session duration
```

### Performance Monitoring
```
Monitor:
- App load time
- Screen transitions
- Network latency
- Memory usage
```

### User Feedback
```
Channels:
- In-app crash reports
- Play Store reviews
- Email: support@noodrop.app
- GitHub issues
```

---

## Rollback Plan

If critical issues arise:

```bash
# 1. Identify issue from Firebase logs
# 2. Fix in code
# 3. Increment versionCode (build.gradle.kts)
# 4. Build new release APK
# 5. Submit to Play Store

# Rollback to previous version:
# In Play Store console → Release management → Promote previous release
```

---

## Version Upgrade Process

1. **Increment versionCode** (each release)
   ```kotlin
   versionCode = 2  // was 1
   versionName = "1.0.1"
   ```

2. **Update changelog**
   ```
   Version 1.0.1 (Bugfixes)
   - Fixed crash on logout
   - Improved error messages
   - Performance optimizations
   ```

3. **Build & submit**
   ```bash
   ./gradlew bundleRelease
   # Upload to Play Store
   ```

---

## Maintenance Schedule

**Daily:**
- Monitor Firebase dashboard
- Check error logs
- Respond to critical bugs

**Weekly:**
- Review user feedback
- Check analytics trends
- Plan next release

**Monthly:**
- Security updates
- Dependency updates
- Feature planning

**Quarterly:**
- Major version release
- New feature rollout
- Infrastructure review

---

## Legal & Compliance

### Privacy Policy
Required elements:
- What data you collect
- How data is used
- Who data is shared with
- GDPR compliance (EU users)
- Cookie/tracking policy
- Data retention period

### Terms of Service
- Account responsibilities
- Content ownership
- Limitation of liability
- Dispute resolution

### Medical Disclaimer
```
DISCLAIMER: This app is for research and educational purposes only. 
It is not medical advice. Consult healthcare providers before 
using any nootropics.
```

---

## Success Metrics

Target KPIs:
- 10k+ downloads in first month
- 4.5+ star rating
- <1% crash rate
- 30% 7-day retention
- 500+ subscribers (Premium + Pro)

---

## Contact & Support

**Deployment Support:**
- Email: deploy@noodrop.app
- Firebase console: [link]
- Play Store console: [link]

---

**Ready to deploy! 🚀**

