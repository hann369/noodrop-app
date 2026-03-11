# Step 1: Visual Architecture Overview

## 🏗️ Component Hierarchy

```
NoodropRoot
├── AuthScreen (unchanged)
├── SplashScreen (unchanged)
└── MainApp
    └── Scaffold (with BottomNavigation)
        ├── DashboardScreen ⭐ ENHANCED
        │   ├── StatCards (unchanged)
        │   ├── Today's Protocol (unchanged)
        │   ├── 7-Day Wellbeing (unchanged)
        │   ├── 🎯 Personalized Insights (NEW)
        │   │   └── SuggestionCard (NEW)
        │   │       ├── Compound Name + Category
        │   │       ├── Confidence Badge
        │   │       ├── Reason Text
        │   │       └── Action Buttons
        │   │           ├── "Add to Stack" → StackViewModel
        │   │           └── "Learn More" → CompoundDetailSheet
        │   ├── 30-Day Consistency (unchanged)
        │   └── "Log Today" Button
        │
        ├── StackScreen (unchanged)
        │
        ├── TrackerScreen (unchanged)
        │
        ├── MetricsScreen (unchanged)
        │
        └── LibraryScreen ⭐ ENHANCED
            ├── Tab Navigation (NEW)
            │   ├── Protocols Tab
            │   │   ├── Status Filters
            │   │   └── ProtocolCards (unchanged)
            │   │
            │   └── Compounds Tab (NEW)
            │       ├── Search Bar
            │       ├── Category Filters
            │       └── CompoundCard List (NEW)
            │           └── Click → CompoundDetailSheet (NEW)
            │
            └── ModalBottomSheets
                ├── ProtocolDetailSheet (existing)
                └── CompoundDetailSheet (NEW)
                    ├── Description
                    ├── Benefits
                    ├── Dosage Info
                    ├── Food Interactions
                    ├── Synergies
                    ├── Safety Notes
                    ├── Research Links
                    └── "Add to Stack" Action
```

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    USER ACTIONS                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
    ┌───────────────────────┼───────────────────────┐
    ↓                       ↓                       ↓
[Log Daily]           [Browse Library]        [View Metrics]
    ↓                       ↓                       ↓
    │              ┌────────┴────────┐             │
    │              ↓                 ↓             │
    │        [Protocols]        [Compounds]        │
    │              │                 │             │
    │              │        ┌────────┴────────┐    │
    │              │        ↓                 ↓    │
    │              │    [Search]          [View]   │
    │              │        ↓                 ↓    │
    │              │    [Filter]        [Details]  │
    │              │        │                │     │
    └──────────────┼────────┴────────┬───────┘     │
                   │                 │             │
                   ↓                 ↓             ↓
            ┌──────────────────────────────────────────┐
            │   NoodropRepository & Models             │
            │   - stackFlow()                          │
            │   - logsFlow()                           │
            │   - addToStack()                         │
            │   - CompoundData.all                     │
            └──────────────────────────────────────────┘
                           ↓
            ┌──────────────────────────────────────────┐
            │     DashboardViewModel                   │
            │   - computeSuggestions()                 │
            │   - Analyzes logs vs. stack              │
            │   - Generates CompoundSuggestions        │
            └──────────────────────────────────────────┘
                           ↓
            ┌──────────────────────────────────────────┐
            │     DashboardState                       │
            │   - suggestions: List<CompoundSuggestion>│
            │   - Updated every log change             │
            └──────────────────────────────────────────┘
                           ↓
            ┌──────────────────────────────────────────┐
            │     UI Recomposition                     │
            │   - SuggestionCard updates               │
            │   - Shows top 2 suggestions              │
            │   - Displays confidence scores           │
            └──────────────────────────────────────────┘
```

---

## 🔄 Suggestion Engine Flow

```
┌─────────────────────────────────────┐
│  Recent Logs (last 10 with data)   │
└──────────────────┬──────────────────┘
                   │
        ┌──────────┼──────────┐
        ↓          ↓          ↓
    [Mood]     [Fog]      [Focus]
    (avg)      (avg)      (avg)
        │          │          │
        └──────────┼──────────┘
                   ↓
    ┌──────────────────────────────┐
    │  Check Against Thresholds    │
    ├──────────────────────────────┤
    │ Mood < 5?     → Ashwagandha   │
    │ Fog > 5?      → Alpha-GPC     │
    │ Focus < 5?    → L-Theanine    │
    └──────────────────────────────┘
                   ↓
    ┌──────────────────────────────┐
    │  Already in Stack?           │
    │  (Skip if yes)               │
    └──────────────────────────────┘
                   ↓
    ┌──────────────────────────────┐
    │  Create CompoundSuggestion   │
    ├──────────────────────────────┤
    │ - compound: Compound         │
    │ - reason: String             │
    │ - confidence: Float (0-1)    │
    │ - priority: Int (1-3)        │
    └──────────────────────────────┘
                   ↓
    ┌──────────────────────────────┐
    │  Sort by Priority            │
    │  Take Top 2                  │
    │  Display on Dashboard        │
    └──────────────────────────────┘
```

---

## 📱 Screen Layouts

### DashboardScreen - NEW Section

```
┌─────────────────────────────┐
│  Good morning. ← Greeting   │
│  Friday, March 10           │
├─────────────────────────────┤
│ [Streak: 5] [Mood: 7]       │ ← Stat Cards
│ [Fog: 3]    [Compounds: 4]  │
├─────────────────────────────┤
│ Today's Protocol            │ ← Existing
│ ✓ Alpha-GPC 300mg Morning   │
│ □ Lion's Mane 500mg Morning │
├─────────────────────────────┤
│ 7-Day Wellbeing             │ ← Existing
│ 7 → 6 → 5 → 6 → 7 → 8 → 8  │
├─────────────────────────────┤
│ 🎯 Personalized Insights    │ ← NEW SECTION
│ ┌─────────────────────────┐ │
│ │ 💡 Smart Suggestion     │ │
│ │ Ashwagandha    [80%]    │ │
│ │ May improve your mood   │ │
│ │ Adaptogen • 300mg       │ │
│ │ [Add to Stack] [Learn+] │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │ 💡 Smart Suggestion     │ │
│ │ Alpha-GPC      [85%]    │ │
│ │ Reduce brain fog        │ │
│ │ Cholinergic • 300mg     │ │
│ │ [Add to Stack] [Learn+] │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│ 30-Day Consistency          │ ← Existing
│ ● ● ○ ● ● ● ○              │
│ ● ● ● ● ● ● ●              │
│ (Heatmap view)              │
├─────────────────────────────┤
│     [Log Today →]           │ ← Button
└─────────────────────────────┘
```

### LibraryScreen - Enhanced with Tabs

```
┌─────────────────────────────┐
│  Knowledge Hub              │
├─────────────────────────────┤
│ [Protocols] | [Compounds]   │ ← NEW Tabs
├─────────────────────────────┤
│                             │
│  PROTOCOLS TAB (Active)     │
│  ┌───┬───┬────┬──────┐      │
│  │All│Free│Paid│Soon │      │
│  └───┴───┴────┴──────┘      │
│                             │
│  [🧠] Clarity Protocol      │
│  Eliminate brain fog...     │
│  Alpha-GPC, Lion's Mane...  │
│  Goal: Clarity • 8 weeks    │
│                             │
│  [🎯] Focus Protocol        │
│  Deep work mode...          │
│  Modafinil, Noopept...      │
│  Goal: Focus • 4 weeks      │
│                             │
└─────────────────────────────┘
       OR (Tab switch)
       ↓
┌─────────────────────────────┐
│  Knowledge Hub              │
├─────────────────────────────┤
│ [Protocols] | [Compounds]   │
├─────────────────────────────┤
│                             │
│  COMPOUNDS TAB (Active)     │
│  ┌──────────────────────┐   │
│  │ Search compounds...  │   │ ← NEW Search
│  └──────────────────────┘   │
│  ┌───┬──────┬────┬───┐      │
│  │All│Amino │Herb│All│      │ ← Filters
│  └───┴──────┴────┴───┘      │
│                             │
│  ┌──────────────────────┐   │
│  │ Alpha-GPC   [300mg]  │ ← NEW Card
│  │ Cholinergic          │
│  │ Increases acetyl...  │
│  │ ✓ Focus ✓ Memory ... │
│  │ ⏰ Morning           │
│  │ Tap to learn more →  │
│  └──────────────────────┘   │
│                             │
│  ┌──────────────────────┐   │
│  │ L-Theanine  [200mg]  │   │
│  │ Amino Acid           │   │
│  │ Promotes relaxed...  │   │
│  │ ✓ Focus ✓ Relax...  │   │
│  │ ⏰ Morning / Evening │   │
│  │ Tap to learn more →  │   │
│  └──────────────────────┘   │
│                             │
└─────────────────────────────┘
```

### CompoundDetailSheet - Modal Bottom Sheet

```
        ┌──────────────────────────────┐
        │ [X] Close                    │
        ├──────────────────────────────┤
        │ Alpha-GPC                    │ ← Name
        │ Cholinergic                  │ ← Category
        ├──────────────────────────────┤
        │ Cholinergic that increases   │ ← Description
        │ acetylcholine, supporting    │
        │ memory and cognitive clarity │
        ├──────────────────────────────┤
        │ ✓ Memory  ✓ Focus  ✓ Clarity │ ← Benefits Chips
        ├──────────────────────────────┤
        │ Standard Protocol            │ ← Info Section
        │ Default Dose: 300 mg         │
        │ Optimal Timing: Morning      │
        │ Half-Life: 4-6 hours         │
        │ Bioavailability: 90%         │
        ├──────────────────────────────┤
        │ Absorption Tips              │ ← Tip
        │ 💡 Better with food          │
        ├──────────────────────────────┤
        │ Works Well With              │ ← Synergies
        │ ⚗️ CDP-Choline               │
        │ ⚗️ L-Theanine                │
        │ ⚗️ Lion's Mane               │
        ├──────────────────────────────┤
        │ Safety Notes                 │ ← Warnings
        │ ⚠️ May cause headaches at    │
        │    high doses. Generally...  │
        ├──────────────────────────────┤
        │ Research                     │ ← Links
        │ • Alpha-GPC and cognition    │
        │   Winblad B (2005)           │
        │   View on PubMed →           │
        ├──────────────────────────────┤
        │    [Add to Stack →]          │ ← Action
        └──────────────────────────────┘
            (Bottom Sheet scrolls)
```

---

## 🎨 Color & Design System

### Colors Used:
- **NdOrange** - Primary actions, suggestions, confidence badges
- **NdGreen** - Success states, free protocols
- **NdBlue** - Secondary information
- **NdPurple** - Accent color
- **Surface/Secondary Container** - Card backgrounds

### Typography:
- **Display Small** - Screen titles
- **Title Large** - Card titles
- **Body Small/Medium** - Descriptions, details
- **Label Small/Medium** - Buttons, badges

### Components:
- **NdCard** - Rounded surface with shadow
- **NdButton** - Orange primary button
- **NdOutlineButton** - Secondary button
- **OrangeChip** - Orange badge
- **NdFilterChip** - Toggleable filter chip

---

## 🔗 Data Model Relationships

```
User
├── DayLog (per date)
│   ├── mood: Int
│   ├── fog: Int
│   ├── energy: Int
│   ├── focus: Int
│   └── checkedCompounds: List<String>
│
└── StackEntry (per compound in stack)
    ├── compoundName: String → Links to Compound
    ├── dose: String
    └── timing: Timing
        
Compound (Static Database: CompoundData.all)
├── name: String (Primary Key)
├── category: String
├── defaultDose: String
├── description: String
├── benefits: List<String>
├── bioavailability: String
├── halfLife: String
├── optimalTiming: String
├── foodInteraction: String
├── researchLinks: List<ResearchLink>
├── safetyNotes: String
└── synergies: List<String> (References to other Compounds)

ResearchLink
├── title: String
├── url: String
├── authors: String
└── year: Int

CompoundSuggestion (Computed, not stored)
├── compound: Compound
├── reason: String
├── confidence: Float
└── priority: Int
```

---

## 🔄 State Management Flow

```
Repository (SingletonScope)
├── authState: Flow
├── stackFlow(): Flow<List<StackEntry>>
├── logsFlow(days): Flow<List<DayLog>>
└── Methods:
    ├── addToStack(entry)
    ├── removeFromStack(id)
    ├── upsertLog(log)
    └── loadPreset(protocol)
    
ViewModels (ScopedToViewModel)
├── DashboardViewModel
│   ├── state: StateFlow<DashboardState>
│   └── computeSuggestions() [Private]
│
├── LibraryViewModel
│   ├── state: StateFlow<LibraryState>
│   ├── setTab(tab)
│   ├── searchCompounds(query)
│   ├── selectCompound(compound)
│   └── addCompoundToStack(compound)
│
└── Other ViewModels...
    
UI Layer (Composables)
├── DashboardScreen
│   └── Uses: DashboardViewModel.state
│   └── Displays: SuggestionCard
│
└── LibraryScreen
    ├── Uses: LibraryViewModel.state
    ├── Displays: ProtocolCard, CompoundCard
    └── Shows: CompoundDetailSheet
```

---

## 📊 Suggestion Algorithm Pseudocode

```kotlin
fun computeSuggestions(
    stack: List<StackEntry>,
    logs: List<DayLog>
): List<CompoundSuggestion> {
    
    val stackedNames = stack.map { it.compoundName }.toSet()
    val recent = logs.filter { hasData }.takeLast(10)
    
    if (recent.isEmpty()) return empty
    
    val avgMood = recent.avgOf { it.mood }
    val avgFog = recent.avgOf { it.fog }
    val avgFocus = recent.avgOf { it.focus }
    
    val suggestions = mutableList<CompoundSuggestion>()
    
    // Logic 1: Low mood pattern
    if (avgMood < 5 && "Ashwagandha" !in stackedNames) {
        suggestions.add(
            CompoundSuggestion(
                compound = Ashwagandha,
                reason = "May improve low mood (avg: $avgMood/10)",
                confidence = 0.8f,
                priority = 1
            )
        )
    }
    
    // Logic 2: High brain fog
    if (avgFog > 5 && "Alpha-GPC" !in stackedNames) {
        suggestions.add(
            CompoundSuggestion(
                compound = AlphaGPC,
                reason = "May reduce brain fog (avg: $avgFog/10)",
                confidence = 0.85f,
                priority = 1
            )
        )
    }
    
    // Logic 3: Low focus
    if (avgFocus < 5 && !stackedNames.containsAny(["L-Theanine", "Caffeine"])) {
        suggestions.add(
            CompoundSuggestion(
                compound = LTheanine,
                reason = "May enhance focus (avg: $avgFocus/10)",
                confidence = 0.75f,
                priority = 2
            )
        )
    }
    
    return suggestions.sortBy { it.priority }
}
```

---

## ✅ Implementation Complete

Alle Komponenten sind implementiert und bereit für Integration!

**Total Files Created**: 3 neue Dateien
**Total Files Modified**: 5 bestehende Dateien
**Lines of Code Added**: ~600+ Zeilen
**Features Added**: 8 Major Features

🚀 **Ready for Testing & Integration!**

