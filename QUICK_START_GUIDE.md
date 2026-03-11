# 🚀 QUICK START GUIDE - NOODROP APP

## Installation & Setup

### 1. Prerequisites
- Android Studio 2024.1 or later
- Android SDK 31+ (target: 34)
- Gradle 8.0+
- JDK 17+

### 2. Firebase Setup
1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package name: `com.noodrop.app`
3. Download `google-services.json` and place in `app/src/` directory
4. Enable Authentication (Email/Password)
5. Create Firestore database (Start in test mode)

### 3. Clone & Build
```bash
git clone <repo-url>
cd noodrop-firebase-mvp_5
./gradlew build
```

### 4. Run on Emulator/Device
```bash
./gradlew installDebug
# or use Android Studio's Run button
```

---

## 🎯 First Steps as a User

### **Step 1: Sign Up**
- Tap "Create Account"
- Enter email & password
- Verify (check Firebase console for test accounts)

### **Step 2: Build Your Stack**
- Navigate to "Stack" tab
- Tap "+ Add" button
- Search for compounds (e.g., "Alpha-GPC")
- Set dose and timing
- Your stack is ready!

### **Step 3: Start Tracking**
- Navigate to "Track" tab
- Check off compounds you took today
- Rate your metrics (Mood, Energy, Focus, etc.)
- Tap "Log Today ✓"
- Success! Data is saved

### **Step 4: View Analytics**
- Navigate to "Metrics" tab
- See your trends over 7 days / 2 weeks / 30 days
- Discover which compounds help you most

### **Step 5: Explore Protocols**
- Navigate to "Library" tab
- Browse 6 pre-built protocols
- Click to learn more
- Free protocols load instantly
- Paid protocols require subscription (Premium/Pro)

---

## 🔧 Features Overview

### **Dashboard** 🏠
- Your daily mood & streak
- Quick protocol checklist
- 7-day wellbeing trend
- Personalized suggestions
- Pull-to-refresh

### **Stack Builder** 🧪
- Add/remove compounds
- Set doses & timings
- View last 3 changes
- 20+ compound database
- Notes & protocol management

### **Daily Tracker** 📝
- Check off taken compounds
- Rate 5 wellness metrics
- Add observations
- Auto-save on toggle
- Clear visual feedback

### **Metrics & Analytics** 📊
- 5 key health metrics
- Interactive charts
- Compound impact analysis
- Best/worst days insights
- 3 time period filters (7/14/30 days)

### **Library** 📚
- 6 pre-built protocols
- 20+ compound database
- Full scientific information
- Dosing recommendations
- Safety information

### **Profile** 👤
- Your stats (days logged, streak)
- Account info
- Sign out button
- Dark/light theme toggle

---

## 💡 Pro Tips

### **Daily Habit**
- Log every morning after taking compounds
- Rate your mood/energy each evening
- After 2 weeks, check Metrics for patterns

### **Stack Optimization**
- Start with Free protocols
- Track for 3 weeks minimum
- Use Metrics to identify best compounds
- Adjust based on your personal data

### **Exploring Data**
- Metrics show your personal data
- Compare 7-day vs 30-day trends
- Look for correlations (Caffeine + L-Theanine?)
- Export insights (coming in v1.1)

### **Subscriptions**
- Premium: €9.99/month (Advanced protocols + analytics)
- Pro: €19.99/month (All premium + priority support)
- Manage from Profile screen

---

## 🐛 Troubleshooting

### **Login Issues**
- Check internet connection
- Verify email is correct
- Reset password if needed
- Check Firebase console for user status

### **Data Not Saving**
- Check internet connection
- Verify Firebase is enabled
- Try logging out & back in
- Check device storage (should have >100MB free)

### **Compounds Not Showing**
- Make sure app is connected to Firebase
- Try pulling down to refresh
- Restart app
- Check Firebase Firestore permissions

### **Charts Not Loading**
- Need at least 2 days of data
- Check Metrics time period selector
- Try different time range (7 → 14 → 30 days)

### **App Crashes**
- Check logcat: `adb logcat | grep "noodrop"`
- Update Android SDK
- Clear app cache: Settings → Apps → noodrop → Storage → Clear Cache
- Reinstall app

---

## 📱 Keyboard Shortcuts (if implemented)

- `Double-tap` Dashboard → Pull refresh
- `Swipe left` on compound → Delete from stack
- `Long-press` metric → See details/history

---

## 🔐 Data Privacy

- Your data is stored in Firebase
- Encrypted in transit (HTTPS)
- Stored securely (Firestore rules)
- Only you can access your data
- No tracking/analytics sent to third parties

### **Permissions Required:**
- Internet (Firebase communication)
- No location, camera, or microphone needed

---

## 🎓 Learning Resources

### **Understanding Nootropics**
- Visit [noodrop.vercel.app](https://noodrop.vercel.app) - Main research platform
- Read "About Noodrop" in app Profile
- Check compound descriptions in Library

### **Using the App**
- Onboarding screens (first launch)
- In-app tooltips (hover over ?)
- This guide!

### **Getting Help**
- Contact: support@noodrop.app
- GitHub Issues: [link]
- FAQ: [link]

---

## 🚀 What's Coming in v1.1?

- Push notifications for reminders
- Export data as PDF/CSV
- Social sharing of protocols
- Wearable integration (Apple Watch)
- Advanced recommendations (ML)
- Community protocols
- API access

---

## ✨ Version History

### **v1.0.0** (Current - March 10, 2026)
- ✅ Complete MVP with all core features
- ✅ Compound tracking and analytics
- ✅ Subscription system ready
- ✅ Beautiful Material 3 UI
- ✅ Firebase integration

---

## 📞 Support

- **Email:** support@noodrop.app
- **GitHub:** [project link]
- **Website:** [noodrop.vercel.app](https://noodrop.vercel.app)

---

**Happy tracking! 🎯**

