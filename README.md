# Lemonview - Your Personal Skincare Companion

Hey there! Welcome to **Lemonview**, your intelligent skincare analysis app that brings professional skin care guidance right to your fingertips. Whether you're dealing with acne, dryness, aging signs, or just want to maintain healthy skin, Lemonview has got you covered with AI-powered analysis and personalized routines.

## What Makes Lemonview Special?

Lemonview isn't just another skincare app - it's your personal skin expert that:

- **Analyzes your skin** using advanced AI and image processing
- **Creates personalized 14-day routines** based on YOUR specific skin needs
- **Learns from your unique skin profile** to give you truly customized recommendations
- **Guides you through professional skincare** with easy-to-follow steps
- **Tracks your progress** and adapts routines as your skin improves

---

## 🚀 Quick Start Guide

### For Beginners: Getting Started in 3 Easy Steps

1. **Download & Install**
   ```bash
   # Clone the project
   git clone [your-repo-url]
   cd Lemonview-V1

   # Build the app
   .\gradlew assembleDebug

   # Install on your device
   .\gradlew installDebug
   ```

2. **Open the App**
   - Launch Lemonview on your Android device
   - Grant camera permissions when asked

3. **Start Your Skin Journey**
   - Take a clear selfie in good lighting
   - Get your personalized skin analysis
   - Follow your custom 14-day routine!

---

## 🎯 Core Features

### 1. **Smart Skin Analysis** 📸
Take a photo and let our AI analyze your skin for:
- **18 Different Skin Conditions**: Acne, wrinkles, dryness, oiliness, pigmentation, and more
- **Skin Health Score**: Overall percentage of your skin's health
- **Personalized Recommendations**: Specific advice based on your results

### 2. **AI-Powered Routine Planner** 📅
Get a **completely personalized 14-day skincare routine** that:
- Adapts to your specific skin type (oily, dry, sensitive, combination, normal)
- Considers your exact disease severity levels
- Progresses through healing phases (cleansing → treatment → maintenance)
- Updates daily based on your skin's improvement

### 3. **Makeup Advisor** 💄
Discover makeup that complements your skin:
- **Color Analysis**: Find foundation shades that match perfectly
- **Product Recommendations**: Get makeup suggestions for your skin type
- **Before/After Previews**: See how products will look on you

### 4. **Progress Tracking** 📊
- **Daily Routine Reminders**: Never miss a skincare step
- **Progress Photos**: Track your skin's improvement over time
- **Health Score Updates**: See your skin getting healthier

### 5. **Expert Guidance** 🧑‍⚕️
- **Professional Recommendations**: Based on dermatological best practices
- **Korean Beauty Secrets**: Traditional Korean skincare wisdom
- **Scientific Backing**: Evidence-based skincare advice

---

## 🛠️ Technical Setup (For Developers)

### Prerequisites
- **Android Studio** (latest version recommended)
- **Android SDK** (API 24+)
- **Java 11** or higher
- **Android device** for testing (USB debugging enabled)
- **Gradle package should be 8.2 Version only 

### Project Structure
```
Lemonview-V1/
├── app/                          # Main Android application
│   ├── src/main/java/com/lemonview/ai/
│   │   ├── MainMenuActivity.kt   # App navigation hub
│   │   ├── SkinAnalysisActivity.kt # AI skin analysis
│   │   ├── RoutinePlannerActivity.kt # Personalized routines
│   │   ├── MakeupAdvisorActivity.kt # Beauty recommendations
│   │   └── utils/                # Core utilities
│   │       ├── SkinAnalysisProcessor.kt # ML skin analysis
│   │       ├── RoutinePlanGenerator.kt  # AI routine creation
│   │       └── MakeupColorAnalyzer.kt   # Color matching
│   └── src/main/res/             # UI resources
├── ml/                           # Machine learning models
└── build.gradle.kts             # Project configuration
```

### Building from Source

#### Step 1: Clone and Setup
```bash
# Get the code
git clone [your-repo-url]
cd Lemonview-V1

# Make sure you have the right Java version
java -version  # Should be 11+
```

#### Step 2: Build the App
```bash
# Clean previous builds (recommended)
.\gradlew clean

# Build debug version
.\gradlew assembleDebug

# Build release version (for production)
.\gradlew assembleRelease
```

#### Step 3: Run on Device
```bash
# Install debug version on connected device
.\gradlew installDebug

# Or install release version
.\gradlew installRelease
```

#### Step 4: Run Tests
```bash
# Run unit tests
.\gradlew test

# Run instrumented tests on device
.\gradlew connectedAndroidTest
```

---

## 📱 How to Use Lemonview (Step-by-Step)

### First Time Setup
1. **Install the app** on your Android device
2. **Open Lemonview** and grant camera permissions
3. **Create your profile** (optional but recommended)

### Daily Usage Flow

#### Step 1: Skin Analysis 🧴
```
1. Tap "AI Skin Analysis" on the main menu
2. Position your face in the camera frame
3. Take a clear, well-lit selfie
4. Wait for AI analysis (takes about 10-15 seconds)
5. Review your personalized skin report
```

#### Step 2: Get Your Routine 📝
```
1. After analysis, tap "Routine Planner"
2. View your custom 14-day skincare plan
3. Start with Day 1 morning routine
4. Follow the step-by-step instructions
```

#### Step 3: Daily Care Routine 🌅
```
Morning Routine (Every Day):
1. Cleanse with your recommended cleanser
2. Apply toner/essence
3. Use targeted serum for your concerns
4. Moisturize with appropriate cream
5. Apply sunscreen (SPF 50+)

Evening Routine (Every Day):
1. Double cleanse if wearing makeup
2. Apply treatment products
3. Use moisturizer
4. Apply overnight treatments if needed
```

#### Step 4: Track Progress 📈
```
1. Take progress photos every 7 days
2. Note how your skin feels and looks
3. The app adapts routines based on improvements
4. Celebrate your skin health improvements!
```

### Advanced Features

#### Makeup Advisor Mode 💄
```
1. Go to main menu → Tap "Makeup Advisor"
2. Take a fresh selfie
3. Get foundation shade recommendations
4. Browse makeup product suggestions
5. See virtual try-on previews
```

#### Routine Customization 🎨
```
1. View your 14-day plan in Routine Planner
2. Each day adapts to your skin's response
3. Products change based on your progress
4. Get reminders for routine steps
```

---

## 🔧 Troubleshooting

### Common Issues & Solutions

#### App Won't Install
```bash
# Check device connection
adb devices

# If no devices found, enable USB debugging:
# Settings → Developer Options → USB Debugging

# Try reinstalling
.\gradlew uninstallDebug
.\gradlew installDebug
```

#### Camera Not Working
- **Solution**: Grant camera permissions in app settings
- **Alternative**: Restart the app and try again
- **Lighting Tip**: Use natural daylight for best results

#### Analysis Takes Too Long
- **Cause**: Heavy processing on device
- **Solution**: Ensure good lighting and clear photo
- **Tip**: Close other apps to free up memory

#### Routine Not Personalized Enough
- **Solution**: Retake skin analysis with better lighting
- **Tip**: Clean face and use natural light for accurate results

---

## 🎨 Understanding Your Skin Analysis

### What the AI Detects
Lemonview analyzes **18 different skin conditions**:

| Condition | What It Means | Treatment Focus |
|-----------|---------------|-----------------|
| **Acne** | Pimples, blackheads, whiteheads | Oil control, antibacterial |
| **Dryness** | Flaky, tight skin | Hydration, barrier repair |
| **Oiliness** | Shiny, greasy skin | Sebum control, mattifying |
| **Wrinkles** | Fine lines, crow's feet | Collagen boost, anti-aging |
| **Pigmentation** | Dark spots, uneven tone | Brightening, exfoliation |
| **Redness** | Flushed, irritated skin | Soothing, anti-inflammatory |

### Skin Health Score Explained
- **80-100%**: Excellent skin health
- **65-79%**: Good overall condition
- **50-64%**: Fair, needs improvement
- **35-49%**: Poor, requires attention
- **0-34%**: Critical, seek professional help

### Routine Phases
1. **Days 1-3**: Deep cleansing and detoxification
2. **Days 4-7**: Targeted treatment for your specific concerns
3. **Days 8-14**: Maintenance and prevention

---

