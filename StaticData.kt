package com.noodrop.app.data.model

object CompoundData {
    val all = listOf(
        Compound(
            name = "Alpha-GPC", category = "Cholinergic", defaultDose = "300 mg",
            description = "Cholinergic that increases acetylcholine, supporting memory and cognitive clarity.",
            benefits = listOf("Memory", "Focus", "Clarity"),
            bioavailability = "90%",
            halfLife = "4-6 hours",
            optimalTiming = "Morning",
            foodInteraction = "Better with food",
            safetyNotes = "Generally well-tolerated. May cause headaches at high doses.",
            synergies = listOf("CDP-Choline", "L-Theanine", "Lion's Mane"),
            researchLinks = listOf(
                ResearchLink("Alpha-GPC and cognitive function", "https://pubmed.ncbi.nlm.nih.gov/", "Winblad B", 2005),
            )
        ),
        Compound(
            name = "Lion's Mane", category = "Mushroom", defaultDose = "500 mg",
            description = "Medicinal mushroom that promotes neurogenesis and nerve growth factor (NGF) production.",
            benefits = listOf("Neurogenesis", "Neuroprotection", "Memory"),
            bioavailability = "Moderate - requires extraction",
            halfLife = "Unknown",
            optimalTiming = "Morning",
            foodInteraction = "Take with food for better absorption",
            safetyNotes = "Very safe. May take 4-8 weeks to notice effects.",
            synergies = listOf("Alpha-GPC", "Bacopa Monnieri"),
            researchLinks = listOf(
                ResearchLink("Hericium erinaceus and nerve growth", "https://pubmed.ncbi.nlm.nih.gov/", "Zhang et al.", 2018),
            )
        ),
        Compound(
            name = "L-Theanine", category = "Amino Acid", defaultDose = "200 mg",
            description = "Amino acid that increases GABA and promotes relaxed alertness without sedation.",
            benefits = listOf("Relaxation", "Focus", "Stress Relief"),
            bioavailability = "95%",
            halfLife = "1-2 hours",
            optimalTiming = "Morning (with Caffeine) or Evening",
            foodInteraction = "Works great with Caffeine",
            safetyNotes = "Very safe, non-toxic.",
            synergies = listOf("Caffeine", "Ashwagandha"),
            researchLinks = listOf(
                ResearchLink("L-Theanine and alpha wave production", "https://pubmed.ncbi.nlm.nih.gov/", "Juneja et al.", 1999),
            )
        ),
        Compound(
            name = "Caffeine", category = "Stimulant", defaultDose = "100 mg",
            description = "Adenosine antagonist that increases alertness and dopamine. Best paired with L-Theanine.",
            benefits = listOf("Alertness", "Focus", "Energy"),
            bioavailability = "95-99%",
            halfLife = "3-5 hours",
            optimalTiming = "Morning, before 2 PM",
            foodInteraction = "Empty stomach or with food, both work",
            safetyNotes = "Can increase anxiety if not paired with L-Theanine. Avoid after 2 PM.",
            synergies = listOf("L-Theanine"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Noopept", category = "Peptide", defaultDose = "10 mg",
            description = "Cycloprolyl dipeptide that enhances memory consolidation and synaptic plasticity.",
            benefits = listOf("Memory", "Focus", "Learning"),
            bioavailability = "High",
            halfLife = "30-60 minutes",
            optimalTiming = "Morning",
            foodInteraction = "Take on empty stomach or with light meal",
            safetyNotes = "Takes 2-3 weeks of regular use to notice effects. Some users experience headaches.",
            synergies = listOf("CDP-Choline", "Alpha-GPC"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Piracetam", category = "Racetam", defaultDose = "1600 mg",
            benefits = listOf("Memory", "Neuroprotection", "Cognition"),
            bioavailability = "100% (not metabolized)",
            halfLife = "4-5 hours",
            optimalTiming = "Morning & Evening",
            foodInteraction = "Take with meals",
            safetyNotes = "Very safe, well-studied, rarely produces side effects.",
            synergies = listOf("CDP-Choline", "Alpha-GPC"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Aniracetam", category = "Racetam", defaultDose = "750 mg",
            benefits = listOf("Mood", "Focus", "Memory"),
            bioavailability = "High",
            halfLife = "1-2.5 hours",
            optimalTiming = "Morning & Afternoon",
            foodInteraction = "Better with fat-containing meal",
            safetyNotes = "Generally safe. May enhance anxiety relief.",
            synergies = listOf("Piracetam", "Alpha-GPC"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Ashwagandha", category = "Adaptogen", defaultDose = "300 mg",
            description = "Adaptogenic herb that reduces cortisol and stress while supporting mood and sleep.",
            benefits = listOf("Stress Relief", "Mood", "Sleep"),
            bioavailability = "Moderate",
            halfLife = "Unknown",
            optimalTiming = "Evening",
            foodInteraction = "Take with food",
            safetyNotes = "Very safe. May cause drowsiness.",
            synergies = listOf("Rhodiola Rosea", "L-Theanine"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Rhodiola Rosea", category = "Adaptogen", defaultDose = "200 mg",
            benefits = listOf("Stress Relief", "Energy", "Mood"),
            bioavailability = "Moderate",
            halfLife = "Unknown",
            optimalTiming = "Morning",
            foodInteraction = "With or without food",
            safetyNotes = "Generally safe. May cause mild stimulation.",
            synergies = listOf("Ashwagandha", "L-Theanine"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Bacopa Monnieri", category = "Herb", defaultDose = "300 mg",
            description = "Ayurvedic herb known as 'Brahmi' that enhances memory and reduces anxiety.",
            benefits = listOf("Memory", "Focus", "Stress Relief"),
            bioavailability = "Enhanced with fat",
            halfLife = "Unknown",
            optimalTiming = "Evening",
            foodInteraction = "Take with fat-containing food",
            safetyNotes = "Very safe. Takes 4-12 weeks to show effects.",
            synergies = listOf("Lion's Mane", "Alpha-GPC"),
            researchLinks = listOf()
        ),
        Compound(
            name = "CDP-Choline", category = "Cholinergic", defaultDose = "250 mg",
            description = "Precursor to acetylcholine that supports cognitive function and neuroplasticity.",
            benefits = listOf("Focus", "Memory", "Clarity"),
            bioavailability = "90%",
            halfLife = "60 hours",
            optimalTiming = "Morning",
            foodInteraction = "With or without food",
            safetyNotes = "Very safe. Long half-life means once-daily dosing works.",
            synergies = listOf("Alpha-GPC", "Noopept"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Modafinil", category = "Wakefulness", defaultDose = "100 mg",
            description = "Eugeroic (promotes wakefulness) used for focus and mental clarity without jitteriness.",
            benefits = listOf("Focus", "Alertness", "Energy"),
            bioavailability = "Very high",
            halfLife = "12-15 hours",
            optimalTiming = "Morning (ONLY)",
            foodInteraction = "Works better on empty stomach",
            safetyNotes = "PRESCRIPTION in many countries. Dosing after 2 PM disrupts sleep. Not for beginners.",
            synergies = listOf("L-Theanine", "Noopept"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Magnesium L-Threonate", category = "Mineral", defaultDose = "144 mg",
            description = "Magnesium bound to L-threonic acid for enhanced BBB penetration and neuroprotection.",
            benefits = listOf("Sleep", "Memory", "Neuroprotection"),
            bioavailability = "High (crosses BBB)",
            halfLife = "Unknown",
            optimalTiming = "Evening",
            foodInteraction = "Take with food or before bed",
            safetyNotes = "Very safe. May promote sleep quality.",
            synergies = listOf("Ashwagandha", "L-Theanine"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Phosphatidylserine", category = "Phospholipid", defaultDose = "100 mg",
            description = "Structural component of cell membranes that supports cognitive function and mood.",
            benefits = listOf("Mood", "Memory", "Neuroprotection"),
            bioavailability = "Moderate",
            halfLife = "Unknown",
            optimalTiming = "Morning",
            foodInteraction = "Take with fat",
            safetyNotes = "Very safe.",
            synergies = listOf("Omega-3", "PQQ"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Creatine", category = "Performance", defaultDose = "5 g",
            description = "Energy substrate that supports ATP production for muscles and brain.",
            benefits = listOf("Energy", "Focus", "Physical Performance"),
            bioavailability = "Very high",
            halfLife = "33 days (load and maintain in tissue)",
            optimalTiming = "Anytime (consistent daily)",
            foodInteraction = "With carbs and protein",
            safetyNotes = "Very safe and well-studied. Requires consistent daily dosing.",
            synergies = listOf("Caffeine", "L-Theanine"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Omega-3 (DHA/EPA)", category = "Essential Fatty Acid", defaultDose = "1 g",
            description = "Marine lipids essential for neuronal function, mood, and long-term neuroprotection.",
            benefits = listOf("Neuroprotection", "Mood", "Inflammation"),
            bioavailability = "Moderate to High (with fat)",
            halfLife = "Unknown",
            optimalTiming = "With meals",
            foodInteraction = "MUST take with food for absorption",
            safetyNotes = "Very safe. Choose quality sources (molecularly distilled).",
            synergies = listOf("Phosphatidylserine", "PQQ"),
            researchLinks = listOf()
        ),
        Compound(
            name = "NAC", category = "Antioxidant", defaultDose = "600 mg",
            description = "N-acetyl-cysteine precursor to glutathione, supporting antioxidant defense.",
            benefits = listOf("Antioxidant", "Neuroprotection", "Mood Support"),
            bioavailability = "Moderate",
            halfLife = "5.7 hours",
            optimalTiming = "Morning or Evening",
            foodInteraction = "On empty stomach preferred",
            safetyNotes = "Very safe. May have mild detoxification effects.",
            synergies = listOf("Pterostilbene", "Omega-3"),
            researchLinks = listOf()
        ),
        Compound(
            name = "ALCAR", category = "Nootropic", defaultDose = "500 mg",
            description = "Acetyl-L-carnitine supports mitochondrial function and energy production.",
            benefits = listOf("Energy", "Focus", "Neuroprotection"),
            bioavailability = "High",
            halfLife = "Unknown",
            optimalTiming = "Morning",
            foodInteraction = "With or without food",
            safetyNotes = "Very safe.",
            synergies = listOf("PQQ", "Creatine"),
            researchLinks = listOf()
        ),
        Compound(
            name = "PQQ", category = "Mitochondrial", defaultDose = "20 mg",
            description = "Pyrroloquinoline quinone supports mitochondrial biogenesis and neuroprotection.",
            benefits = listOf("Energy", "Neuroprotection", "Mitochondrial Health"),
            bioavailability = "Moderate",
            halfLife = "Unknown",
            optimalTiming = "Morning",
            foodInteraction = "With fat",
            safetyNotes = "Very safe. Expensive but potent.",
            synergies = listOf("ALCAR", "Omega-3", "CoQ10"),
            researchLinks = listOf()
        ),
        Compound(
            name = "Pterostilbene", category = "Antioxidant", defaultDose = "50 mg",
            description = "Polyphenol similar to resveratrol with potent antioxidant and neuroprotective properties.",
            benefits = listOf("Antioxidant", "Neuroprotection", "Longevity"),
            bioavailability = "High",
            halfLife = "Unknown",
            optimalTiming = "Evening",
            foodInteraction = "With fat for absorption",
            safetyNotes = "Very safe.",
            synergies = listOf("NAC", "Omega-3"),
            researchLinks = listOf()
        ),
    )
}

object ProtocolData {
    val all = listOf(

        Protocol(
            id = "clarity", name = "Clarity Protocol", icon = "[Brain]",
            accent = ProtocolAccent.ORANGE, status = ProtocolStatus.FREE,
            description = "Eliminate brain fog, sharpen focus, and enter a clear mental state within 60 minutes. Great starting protocol for beginners.",
            compounds = listOf("Alpha-GPC", "Lion's Mane", "L-Theanine", "Bacopa Monnieri"),
            goal = "Mental Clarity", duration = "8 weeks", price = "Free",
            detailText = """The Clarity Protocol systematically reduces brain fog and improves baseline cognitive clarity through acetylcholine support and neurogenesis.

- Morning: Alpha-GPC 300mg + L-Theanine 200mg
- Morning: Lion's Mane 500mg
- Evening: Bacopa Monnieri 300mg (with food)

Expect gradual improvements over 4-8 weeks.""",
            presetEntries = listOf(
                StackEntry(compoundName = "Alpha-GPC",       dose = "300 mg", timing = Timing.MORNING,  sortOrder = 0),
                StackEntry(compoundName = "Lion's Mane",     dose = "500 mg", timing = Timing.MORNING,  sortOrder = 1),
                StackEntry(compoundName = "L-Theanine",      dose = "200 mg", timing = Timing.MORNING,  sortOrder = 2),
                StackEntry(compoundName = "Bacopa Monnieri", dose = "300 mg", timing = Timing.EVENING,  sortOrder = 3),
            )
        ),

        Protocol(
            id = "focus", name = "Focus Protocol", icon = "[Target]",
            accent = ProtocolAccent.BLUE, status = ProtocolStatus.PAID,
            description = "Deep work mode. Engineered for extended periods of intense concentration and flow state entry.",
            compounds = listOf("Modafinil", "Noopept", "Caffeine", "L-Theanine", "CDP-Choline"),
            goal = "Deep Focus", duration = "4 weeks on / 1 week off", price = "?9/mo",
            detailText = """Advanced focus protocol for experienced users. Cycle 4 weeks on, 1 week off.

- Pre-work: Modafinil 100mg
- +30 min: Noopept 10mg + CDP-Choline 250mg
- Optional: Caffeine 100mg + L-Theanine 200mg

Not for beginners. Consult a physician before use.""",
            presetEntries = listOf(
                StackEntry(compoundName = "Modafinil",   dose = "100 mg", timing = Timing.MORNING, sortOrder = 0),
                StackEntry(compoundName = "Noopept",     dose = "10 mg",  timing = Timing.MORNING, sortOrder = 1),
                StackEntry(compoundName = "Caffeine",    dose = "100 mg", timing = Timing.MORNING, sortOrder = 2),
                StackEntry(compoundName = "L-Theanine",  dose = "200 mg", timing = Timing.MORNING, sortOrder = 3),
                StackEntry(compoundName = "CDP-Choline", dose = "250 mg", timing = Timing.MORNING, sortOrder = 4),
            )
        ),

        Protocol(
            id = "mood", name = "Mood Lift Protocol", icon = "[Sun]",
            accent = ProtocolAccent.GREEN, status = ProtocolStatus.FREE,
            description = "Naturally elevate mood, reduce anxiety, and build emotional resilience through targeted adaptogens.",
            compounds = listOf("Ashwagandha", "Rhodiola Rosea", "L-Theanine", "Omega-3 (DHA/EPA)"),
            goal = "Mood & Stress", duration = "6 weeks", price = "Free",
            detailText = """Uses adaptogens and anxiolytics to naturally modulate stress response and lift baseline mood.

- Morning: Rhodiola Rosea 200mg + L-Theanine 200mg
- Evening: Ashwagandha 300mg + Omega-3 1g

Most users report noticeable shifts by week 3.""",
            presetEntries = listOf(
                StackEntry(compoundName = "Rhodiola Rosea",    dose = "200 mg", timing = Timing.MORNING, sortOrder = 0),
                StackEntry(compoundName = "L-Theanine",        dose = "200 mg", timing = Timing.MORNING, sortOrder = 1),
                StackEntry(compoundName = "Ashwagandha",       dose = "300 mg", timing = Timing.EVENING, sortOrder = 2),
                StackEntry(compoundName = "Omega-3 (DHA/EPA)", dose = "1 g",    timing = Timing.EVENING, sortOrder = 3),
            )
        ),

        Protocol(
            id = "longevity", name = "Longevity Stack", icon = "[Clock]",
            accent = ProtocolAccent.ORANGE, status = ProtocolStatus.PAID,
            description = "Slow cognitive aging and protect neurological health long-term.",
            compounds = listOf("Omega-3 (DHA/EPA)", "Phosphatidylserine", "PQQ", "Pterostilbene", "NAC"),
            goal = "Neuroprotection", duration = "Ongoing", price = "?12/mo",
            detailText = """Focuses on neuroprotection, mitochondrial health, and reducing oxidative stress.

- Morning: Omega-3 1g + PQQ 20mg + PS 100mg
- Evening: NAC 600mg + Pterostilbene 50mg

Best results seen over 3-6 months.""",
            presetEntries = listOf(
                StackEntry(compoundName = "Omega-3 (DHA/EPA)",  dose = "1 g",    timing = Timing.MORNING, sortOrder = 0),
                StackEntry(compoundName = "PQQ",                dose = "20 mg",  timing = Timing.MORNING, sortOrder = 1),
                StackEntry(compoundName = "Phosphatidylserine", dose = "100 mg", timing = Timing.MORNING, sortOrder = 2),
                StackEntry(compoundName = "NAC",                dose = "600 mg", timing = Timing.EVENING, sortOrder = 3),
                StackEntry(compoundName = "Pterostilbene",      dose = "50 mg",  timing = Timing.EVENING, sortOrder = 4),
            )
        ),

        Protocol(
            id = "fitness", name = "Fitness Protocol", icon = "[Flex]",
            accent = ProtocolAccent.GREEN, status = ProtocolStatus.COMING_SOON,
            description = "Physical performance meets cognitive edge. Built for athletes who want the mental advantage.",
            compounds = listOf("Alpha-GPC", "Creatine", "Ashwagandha", "Rhodiola Rosea", "ALCAR"),
            goal = "Performance + Cognition", duration = "12 weeks", price = "Coming soon",
            detailText = "Coming soon."
        ),

        Protocol(
            id = "sleep", name = "Recovery & Sleep", icon = "[Moon]",
            accent = ProtocolAccent.PURPLE, status = ProtocolStatus.COMING_SOON,
            description = "Optimize sleep architecture and overnight recovery. Wake up actually rested.",
            compounds = listOf("Magnesium L-Threonate", "Ashwagandha", "L-Theanine"),
            goal = "Sleep Quality", duration = "4 weeks", price = "Coming soon",
            detailText = "Coming soon."
        ),
    )
}
