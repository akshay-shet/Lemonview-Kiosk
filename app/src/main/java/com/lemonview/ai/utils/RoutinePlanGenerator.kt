package com.lemonview.ai.utils

import com.lemonview.ai.model.DailyRoutine
import com.lemonview.ai.model.RoutinePlan14Days
import com.lemonview.ai.model.SkinResult
import kotlin.math.round

/**
 * RoutinePlanGenerator - Creates TRULY PERSONALIZED 14-day skincare routines
 * Each routine is UNIQUE based on:
 * - Individual skin health percentage
 * - Specific disease severity for THIS user
 * - Progressive healing phases tailored to their conditions
 * - Day-by-day adaptations based on their exact skin metrics
 * 
 * CRITICAL FIX: Each user with different skin analysis gets a COMPLETELY DIFFERENT routine
 */
class RoutinePlanGenerator {

    /**
     * Generate a personalized 14-day routine plan based on SPECIFIC skin analysis
     * Routine changes daily based on user's unique disease severity map
     * NOW WITH ENHANCED PERSONALIZATION: Uses exact disease percentages and combinations
     */
    fun generateRoutinePlan(skinResult: SkinResult): RoutinePlan14Days {
        val dailyRoutines = mutableListOf<DailyRoutine>()

        // Analyze primary concerns based on THIS user's disease severity with enhanced logic
        val primaryConcerns = analyzePrimaryConcernsEnhanced(skinResult)
        val treatmentFocus = determineTreatmentFocusEnhanced(skinResult)
        val skinProfile = createSkinProfile(skinResult) // New: Create detailed skin profile

        // Create 14 days of ULTRA-PERSONALIZED routines
        for (day in 1..14) {
            // Each day adapts based on the severity levels AND combinations in THIS user's analysis
            val dayRoutine = generateUltraPersonalizedDayRoutine(day, skinResult, primaryConcerns, treatmentFocus, skinProfile)
            dailyRoutines.add(dayRoutine)
        }

        return RoutinePlan14Days(
            analysisSkinResult = skinResult,
            dailyRoutines = dailyRoutines
        )
    }

    /**
     * Create detailed skin profile for ultra-personalization
     */
    private fun createSkinProfile(skinResult: SkinResult): Map<String, Any> {
        val diseases = skinResult.diseasesLevel
        val health = skinResult.skinHealthPercentage

        return mapOf(
            "primary_skin_type" to determinePrimarySkinType(diseases),
            "secondary_concerns" to getSecondaryConcerns(diseases),
            "severity_combinations" to analyzeSeverityCombinations(diseases),
            "health_category" to categorizeHealthLevel(health),
            "treatment_urgency" to calculateTreatmentUrgency(diseases, health),
            "skin_age_equivalent" to estimateSkinAge(diseases)
        )
    }

    /**
     * Determine primary skin type based on dominant characteristics
     */
    private fun determinePrimarySkinType(diseases: Map<String, Int>): String {
        val oiliness = diseases["유분기 (Oiliness)"] ?: 0
        val dryness = diseases["건조함 (Dryness)"] ?: 0
        val sensitivity = diseases["민감성 (Sensitivity)"] ?: 0

        return when {
            oiliness > 60 -> "OILY"
            dryness > 60 -> "DRY"
            sensitivity > 50 -> "SENSITIVE"
            oiliness > 40 && dryness > 40 -> "COMBINATION"
            else -> "NORMAL"
        }
    }

    /**
     * Get secondary concerns (conditions that need attention but aren't primary)
     */
    private fun getSecondaryConcerns(diseases: Map<String, Int>): List<String> {
        return diseases.filter { it.value in 20..49 }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }

    /**
     * Analyze combinations of conditions for complex treatment needs
     */
    private fun analyzeSeverityCombinations(diseases: Map<String, Int>): List<String> {
        val combinations = mutableListOf<String>()

        val acne = diseases["여드름 (Acne)"] ?: 0
        val oiliness = diseases["유분기 (Oiliness)"] ?: 0
        val inflammation = diseases["염증 (Inflammation)"] ?: 0

        if (acne > 30 && oiliness > 40) combinations.add("ACNE_OILY")
        if (inflammation > 40 && acne > 20) combinations.add("INFLAMMATORY_ACNE")
        if (acne > 25 && inflammation > 25 && oiliness > 35) combinations.add("COMPLEX_ACNE")

        return combinations
    }

    /**
     * Categorize overall health level
     */
    private fun categorizeHealthLevel(health: Int): String {
        return when {
            health >= 80 -> "EXCELLENT"
            health >= 65 -> "GOOD"
            health >= 50 -> "FAIR"
            health >= 35 -> "POOR"
            else -> "CRITICAL"
        }
    }

    /**
     * Calculate treatment urgency based on multiple factors
     */
    private fun calculateTreatmentUrgency(diseases: Map<String, Int>, health: Int): Int {
        val maxDisease = diseases.values.maxOrNull() ?: 0
        val activeConditions = diseases.count { it.value > 30 }
        val healthFactor = (100 - health) / 10

        return maxDisease + (activeConditions * 5) + healthFactor
    }

    /**
     * Estimate skin age equivalent based on conditions
     */
    private fun estimateSkinAge(diseases: Map<String, Int>): Int {
        val wrinkles = diseases["주름 (Wrinkles)"] ?: 0
        val elasticity = diseases["탄력저하 (Loss of Elasticity)"] ?: 0
        val dullness = diseases["칙칙함 (Dullness)"] ?: 0

        val ageFromWrinkles = wrinkles / 3
        val ageFromElasticity = elasticity / 4
        val ageFromDullness = dullness / 5

        return 25 + ageFromWrinkles + ageFromElasticity + ageFromDullness
    }

    /**
     * Enhanced primary concerns analysis with more sophisticated logic
     */
    private fun analyzePrimaryConcernsEnhanced(skinResult: SkinResult): List<Pair<String, Int>> {
        val diseases = skinResult.diseasesLevel

        // Weight diseases by their impact and user's specific situation
        val weightedDiseases = diseases.map { (disease, level) ->
            val weight = when {
                // High-impact conditions get higher priority
                disease.contains("여드름") || disease.contains("Acne") -> 1.5
                disease.contains("염증") || disease.contains("Inflammation") -> 1.4
                disease.contains("색소침착") || disease.contains("Hyperpigmentation") -> 1.3
                disease.contains("주름") || disease.contains("Wrinkles") -> 1.2
                // Lower priority for cosmetic concerns
                disease.contains("칙칙함") || disease.contains("Dullness") -> 0.8
                disease.contains("모공확대") || disease.contains("Pores") -> 0.9
                else -> 1.0
            }
            disease to (level * weight).toInt().coerceIn(0, 100)
        }.toMap()

        return weightedDiseases
            .filter { it.value > 25 }  // Only conditions above 25% weighted severity
            .toList()
            .sortedByDescending { it.second }
            .take(5) // Top 5 concerns instead of 3
    }

    /**
     * Enhanced treatment focus determination
     */
    private fun determineTreatmentFocusEnhanced(skinResult: SkinResult): String {
        val diseases = skinResult.diseasesLevel
        val health = skinResult.skinHealthPercentage
        val primaryConcerns = analyzePrimaryConcernsEnhanced(skinResult)

        // Complex decision tree based on multiple factors
        val maxDisease = diseases.values.maxOrNull() ?: 0
        val activeSevereConditions = diseases.count { it.value > 60 }
        val activeModerateConditions = diseases.count { it.value in 40..59 }

        return when {
            // Critical conditions requiring immediate intensive care
            maxDisease > 80 || activeSevereConditions >= 2 -> "EMERGENCY_INTENSIVE"
            maxDisease > 70 || activeSevereConditions >= 1 -> "INTENSIVE_REPAIR"

            // Multiple moderate conditions
            activeModerateConditions >= 3 -> "MULTI_TARGETED_TREATMENT"
            activeModerateConditions >= 2 -> "DUAL_TARGETED_TREATMENT"

            // Specific high-priority conditions
            primaryConcerns.any { it.first.contains("여드름") || it.first.contains("Acne") } &&
            diseases["염증 (Inflammation)"] ?: 0 > 50 -> "ACNE_INFLAMMATION_CONTROL"

            primaryConcerns.any { it.first.contains("색소침착") || it.first.contains("Hyperpigmentation") } &&
            health < 60 -> "PIGMENTATION_CORRECTION"

            // Age-related concerns
            primaryConcerns.any { it.first.contains("주름") || it.first.contains("Wrinkles") } &&
            diseases["탄력저하 (Loss of Elasticity)"] ?: 0 > 40 -> "ANTI_AGING_RESTORATION"

            // Skin barrier issues
            health < 45 -> "BARRIER_RESTORATION"
            diseases["건조함 (Dryness)"] ?: 0 > 60 -> "HYDRATION_RECOVERY"

            // Maintenance and prevention
            health > 75 && activeModerateConditions <= 1 -> "PREMIUM_MAINTENANCE"
            health > 60 -> "PREVENTIVE_MAINTENANCE"

            // Default balanced approach
            else -> "BALANCED_IMPROVEMENT"
        }
    }

    /**
     * Generate ultra-personalized routine for specific day
     * Uses enhanced skin profile and complex decision logic
     */
    private fun generateUltraPersonalizedDayRoutine(
        day: Int,
        skinResult: SkinResult,
        primaryConcerns: List<Pair<String, Int>>,
        treatmentFocus: String,
        skinProfile: Map<String, Any>
    ): DailyRoutine {
        val morning = generateUltraPersonalizedMorningRoutine(day, skinResult, primaryConcerns, treatmentFocus, skinProfile)
        val afternoon = generateUltraPersonalizedAfternoonRoutine(day, skinResult, primaryConcerns, treatmentFocus, skinProfile)
        val evening = generateUltraPersonalizedEveningRoutine(day, skinResult, primaryConcerns, treatmentFocus, skinProfile)

        return DailyRoutine(
            day = day,
            morning = morning,
            afternoon = afternoon,
            evening = evening
        )
    }

    /**
     * Ultra-personalized morning routine based on THIS user's specific conditions and profile - IN KOREAN
     */
    private fun generateUltraPersonalizedMorningRoutine(
        day: Int,
        skinResult: SkinResult,
        primaryConcerns: List<Pair<String, Int>>,
        treatmentFocus: String,
        skinProfile: Map<String, Any>
    ): List<String> {
        val routine = mutableListOf<String>()
        val diseases = skinResult.diseasesLevel
        val health = skinResult.skinHealthPercentage

        // Enhanced progress indicator with personalized health target
        val progressPercent = round((day / 14f) * 100).toInt()
        val healthTarget = (health + (14 - day) * 2 + (skinProfile["treatment_urgency"] as? Int ?: 0) / 10).coerceIn(0, 100)
        routine.add("기간: 14일 중 ${day}일차 | 진행도: ${progressPercent}% | 목표 피부 건강: ${healthTarget}%")
        routine.add("치료 전략: $treatmentFocus | 피부 타입: ${skinProfile["primary_skin_type"]}")
        routine.add("")

        // Step 1: Ultra-personalized cleansing based on skin profile
        routine.add("01. 세안 - 맞춤 클렌징 (개인 피부 프로필 기반)")
        val skinType = skinProfile["primary_skin_type"] as? String ?: "NORMAL"
        val oilLevel = diseases["유분기 (Oiliness)"] ?: 0
        val dryLevel = diseases["건조함 (Dryness)"] ?: 0

        when {
            skinType == "OILY" && oilLevel > 65 -> routine.add("    → 고강도 오일 컨트롤 클렌저 (유분기 ${oilLevel}%)")
            skinType == "DRY" && dryLevel > 65 -> routine.add("    → 크리미 수분 공급 클렌저 (건조함 ${dryLevel}%)")
            skinType == "SENSITIVE" -> routine.add("    → pH 5.5 저자극 아미노산 클렌저 (민감성 피부용)")
            skinType == "COMBINATION" -> routine.add("    → T존/U존 이중 텍스처 클렌저 (복합성 피부)")
            treatmentFocus.contains("ACNE") -> routine.add("    → 살리실산 BHA 클렌저 (여드름 집중 케어)")
            else -> routine.add("    → 밸런스 클렌저 (개인 피부 상태 최적화)")
        }
        routine.add("")

        // Step 2: Enhanced toner selection based on treatment focus and skin profile
        routine.add("02. 토너/에센스 - 맞춤 포뮬러 (치료 전략 기반)")
        val inflammationLevel = diseases["염증 (Inflammation)"] ?: 0
        val sensitivityLevel = diseases["민감성 (Sensitivity)"] ?: 0
        val rednessLevel = diseases["홍조 (Redness)"] ?: 0

        when {
            treatmentFocus == "EMERGENCY_INTENSIVE" || inflammationLevel > 70 -> {
                routine.add("    → 응급 진정 앰플 (염증 ${inflammationLevel}% - 최대 강도)")
                routine.add("    → 성분: 마데카소사이드, 아시아틱오사이드, 트라넥사믹애씨드")
            }
            treatmentFocus.contains("ACNE_INFLAMMATION") -> {
                routine.add("    → 여드름 진정 앰플 (여드름+염증 복합 케어)")
                routine.add("    → 성분: 나이아신아마이드, 판테놀, 세라마이드")
            }
            treatmentFocus.contains("PIGMENTATION") -> {
                routine.add("    → 브라이트닝 포텐셜 앰플 (색소침착 집중)")
                routine.add("    → 성분: 비타민C, 트라넥사믹애씨드, 알부틴")
            }
            sensitivityLevel > 60 -> {
                routine.add("    → 센시티브 포뮬러 (극민감성 피부용 ${sensitivityLevel}%)")
                routine.add("    → 저자극, 무첨가 포뮬러")
            }
            oilLevel > 60 -> {
                routine.add("    → 오일 컨트롤 토너 (고유분기 피부용 ${oilLevel}%)")
                routine.add("    → 나이아신아마이드 + 아연 피리치온")
            }
            else -> {
                routine.add("    → 밸런싱 토너 (개인 피부 건강 ${health}%)")
            }
        }
        routine.add("")

        // Step 3: Advanced serum selection based on day phase and treatment focus
        routine.add("03. 세럼 - 단계별 집중 케어 (14일 프로그램)")
        val phase = ((day - 1) / 3) + 1
        val acneLevel = diseases["여드름 (Acne)"] ?: 0
        val wrinkleLevel = diseases["주름 (Wrinkles)"] ?: 0
        val pigmentationLevel = diseases["색소침착 (Hyperpigmentation)"] ?: 0

        when {
            // Phase-based progression for complex treatments
            treatmentFocus == "EMERGENCY_INTENSIVE" && day <= 5 -> {
                routine.add("    → 응급 회복 세럼 (1-5일: 위기 극복 단계)")
                routine.add("    → 고농도 진정 + 회복 복합 포뮬러")
            }
            treatmentFocus == "EMERGENCY_INTENSIVE" && day in 6..10 -> {
                routine.add("    → 재건 세럼 (6-10일: 피부장벽 재건 단계)")
                routine.add("    → 세라마이드 + 펩타이드 복합")
            }
            treatmentFocus == "EMERGENCY_INTENSIVE" && day > 10 -> {
                routine.add("    → 유지 세럼 (11-14일: 안정화 단계)")
                routine.add("    → 저자극 유지 포뮬러")
            }

            // Acne-specific progression
            acneLevel > 60 && day <= 7 -> {
                routine.add("    → 여드름 공격 세럼 (1-7일: 적극 치료)")
                routine.add("    → 살리실산 + 티트리 오일 + 나이아신아마이드")
            }
            acneLevel > 60 && day > 7 -> {
                routine.add("    → 여드름 관리 세럼 (8-14일: 안정 관리)")
                routine.add("    → 티트리 + 판테놀 + 세라마이드")
            }

            // Pigmentation treatment progression
            pigmentationLevel > 65 && day <= 10 -> {
                routine.add("    → 고강도 브라이트닝 세럼 (1-10일: 집중 미백)")
                routine.add("    → 비타민C 20% + 트라넥사믹애씨드 + 알부틴")
            }
            pigmentationLevel > 65 && day > 10 -> {
                routine.add("    → 유지 브라이트닝 세럼 (11-14일: 톤 유지)")
                routine.add("    → 저농도 비타민C + 나이아신아마이드")
            }

            // Anti-aging progression
            wrinkleLevel > 50 && day > 7 -> {
                routine.add("    → 리프팅 펩타이드 세럼 (${day}일차: 집중 리프팅)")
                routine.add("    → 팔미토일 펩타이드 + 아세틸 헥사펩타이드")
            }

            // Default intelligent selection
            dryLevel > 60 -> {
                routine.add("    → 수분 장벽 세럼 (건조함 ${dryLevel}%)")
                routine.add("    → 히알루론산 + 세라마이드 + 판테놀")
            }
            oilLevel > 60 -> {
                routine.add("    → mattifying 세럼 (유분기 ${oilLevel}%)")
                routine.add("    → 나이아신아마이드 + 아연")
            }
            else -> {
                routine.add("    → 멀티-펑션 세럼 (균형 피부 건강 ${health}%)")
                routine.add("    → 비타민C + 나이아신아마이드 + 히알루론산")
            }
        }
        routine.add("")

        // Step 4: Intelligent moisturizer selection
        routine.add("04. 보습 - 개인 피부 장벽 최적화")
        when {
            health < 35 -> {
                routine.add("    → 장벽 회복 크림 (피부 건강 위기: ${health}%)")
                routine.add("    → 세라마이드 1,3,6 + 콜레스테롤 + 지방산")
            }
            health < 50 -> {
                routine.add("    → 집중 영양 크림 (피부 건강 취약: ${health}%)")
                routine.add("    → 세라마이드 + 펩타이드 + 히알루론산")
            }
            skinType == "OILY" -> {
                routine.add("    → 젤 크림 (유분기 피부용)")
                routine.add("    → 가벼운 텍스처, 논코메도제닉")
            }
            skinType == "DRY" -> {
                routine.add("    → 리치 크림 (건조 피부용)")
                routine.add("    → 고보습, 오클루시브 성분 함유")
            }
            treatmentFocus.contains("AGING") -> {
                routine.add("    → 리프팅 크림 (안티에이징 포커스)")
                routine.add("    → 펩타이드 + 레티놀 + 히알루론산")
            }
            else -> {
                routine.add("    → 데일리 크림 (균형 피부용)")
            }
        }
        routine.add("")

        // Step 5: Advanced sun protection
        routine.add("05. 자외선 차단제 - 개인 피부 타입 최적화")
        when {
            skinType == "SENSITIVE" -> {
                routine.add("    → 민감성 피부용 선스크린 (SPF 50+ PA++++)")
                routine.add("    → 무기물 필터, 저자극 포뮬러")
            }
            skinType == "OILY" -> {
                routine.add("    → 매트 선스크린 (SPF 50+ PA+++)")
                routine.add("    → 오일 컨트롤, 논코메도제닉")
            }
            pigmentationLevel > 50 -> {
                routine.add("    → 브라이트닝 선스크린 (SPF 50+ PA+++)")
                routine.add("    → 자외선 + 색소침착 이중 케어")
            }
            else -> {
                routine.add("    → 올 데이 선스크린 (SPF 50+ PA+++)")
            }
        }
        routine.add("    → 적용: 얼굴, 목, 귀, 데콜테 | 2-3시간마다 덧발라주기")

        return routine
    }

    /**
     * Ultra-personalized afternoon routine based on THIS user's midday needs - IN KOREAN
     */
    private fun generateUltraPersonalizedAfternoonRoutine(
        day: Int,
        skinResult: SkinResult,
        primaryConcerns: List<Pair<String, Int>>,
        treatmentFocus: String,
        skinProfile: Map<String, Any>
    ): List<String> {
        val routine = mutableListOf<String>()
        val diseases = skinResult.diseasesLevel
        val health = skinResult.skinHealthPercentage
        
        routine.add("☀️ 낮 시간 피부 관리 (오후 2-4시)")
        routine.add("")
        
        val oilLevel = diseases["유분기 (Oiliness)"] ?: 0
        val dryLevel = diseases["건조함 (Dryness)"] ?: 0
        val acneLevel = diseases["여드름 (Acne)"] ?: 0
        
        // Midday adjustment based on THIS user
        routine.add("01. 피부 상태 확인 - 현재 피부 상태 관찰하기")
        routine.add("")
        
        if (oilLevel > 55) {
            routine.add("02. 유분 조절 (피부 상태: 유분기 ${oilLevel}%)")
            routine.add("    → T존과 코 부위에 종이 팩으로 유분 제거")
            routine.add("    → 유분 많은 부위에만 매트 파우더 살짝 톡톡 두드려 사용")
        } else if (dryLevel > 55) {
            routine.add("02. 수분 공급 (피부 상태: 건조함 ${dryLevel}%)")
            routine.add("    → 수분 미스트 분사 (얼굴 전체에)")
            routine.add("    → 톡톡 두드려 흡수시키기 - 비비지 않기")
        } else {
            routine.add("02. 피부 밸런스 체크")
            routine.add("    → 필요시 가벼운 수분 미스트 사용")
        }
        routine.add("")
        
        // Targeted midday treatment
        if (acneLevel > 55 && day > 1 && day % 2 == 0) {
            routine.add("03. 부위별 집중 관리 (${day}일차: 여드름 ${acneLevel}%)")
            routine.add("    → 활성 여드름 부위에 여드름 치료제 바르기")
            routine.add("    → 완전히 말린 후 건드리기")
        } else if (day % 3 == 1 && health < 60) {
            routine.add("03. 빠른 마스크팩")
            routine.add("    → 5분 에센스 시트 마스크로 빠른 진정")
        } else {
            routine.add("03. 휴식")
            routine.add("    → 피부가 숨 쉴 수 있도록 최소한의 관리만 하기")
        }
        routine.add("")
        
        // Sunscreen reapplication
        routine.add("04. 자외선 차단제 덧바르기")
        routine.add("    → 야외 활동시: SPF 30 이상 자외선 차단제 덧바르기")
        routine.add("    → 자외선 차단제 스틱으로 편리하게 덧바르기")
        routine.add("")
        
        // Nutrition/hydration tips
        routine.add("05. 건강 관리")
        routine.add("    → 물 250ml 마시기 (하루 목표 2L)")
        routine.add("    → 항산화 식품 간식 먹기 (베리, 견과류, 차)")
        if (acneLevel > 45) {
            routine.add("    → 피하기: 유제품, 설탕, 가공식품 (여드름 ${acneLevel}%)")
        }
        
        return routine
    }

    /**
     * Personalized evening routine based on THIS user's specific needs and day phase - IN KOREAN
     */
    private fun generateUltraPersonalizedEveningRoutine(
        day: Int,
        skinResult: SkinResult,
        primaryConcerns: List<Pair<String, Int>>,
        treatmentFocus: String,
        skinProfile: Map<String, Any>
    ): List<String> {
        val routine = mutableListOf<String>()
        val diseases = skinResult.diseasesLevel
        val health = skinResult.skinHealthPercentage
        
        routine.add("🌙 저녁 집중 관리 (오후 7-11시)")
        routine.add("")
        
        val acneLevel = diseases["여드름 (Acne)"] ?: 0
        val dryLevel = diseases["건조함 (Dryness)"] ?: 0
        val wrinkleLevel = diseases["주름 (Wrinkles)"] ?: 0
        val inflammationLevel = diseases["염증 (Inflammation)"] ?: 0
        val rednessLevel = diseases["홍조 (Redness)"] ?: 0
        val oilLevel = diseases["유분기 (Oiliness)"] ?: 0
        val pigmentationLevel = diseases["색소침착 (Hyperpigmentation)"] ?: 0
        
        // PHASE 1: Deep cleansing
        routine.add("01. 이중 세안 - 철저한 클렌징")
        routine.add("")
        routine.add("    1단계: 오일 클렌저 (60초)")
        if (oilLevel > 50) {
            routine.add("    → 지성 피부용 가벼운 오일 클렌저 사용 (${oilLevel}%)")
        } else {
            routine.add("    → 밤 타입 클렌저로 메이크업 쉽게 제거")
        }
        routine.add("    → 모든 피부 부위에 부드럽게 마사지")
        routine.add("")
        routine.add("    2단계: 수성 클렌저 (45초)")
        if (dryLevel > 50) {
            routine.add("    → 크리미/밀크 클렌저 사용 (건조함 ${dryLevel}%)")
        } else if (acneLevel > 50) {
            routine.add("    → 클래리파잉 젤 클렌저 사용 (여드름 ${acneLevel}%)")
        } else {
            routine.add("    → 순한 폼 클렌저 사용")
        }
        routine.add("    → 깨끗한 타올로 톡톡 두드려 물기 제거")
        routine.add("")
        
        // PHASE 2: Active treatment
        routine.add("02. 집중 치료 - ${day}일차 ${treatmentFocus.replace("_", " ")}")
        routine.add("")
        
        when {
            acneLevel > 60 -> {
                routine.add("    → 여드름 집중 관리 (여드름 ${acneLevel}% - 심함)")
                routine.add("    단계1: 여드름 에센스 (BHA 함유) 바르기")
                routine.add("    단계2: 10분 충분히 흡수 대기")
                routine.add("    단계3: 여드름 세럼을 문제 부위에 바르기")
                routine.add("    단계4: 여드름 부위에 스팟 마스크 (15분)")
            }
            inflammationLevel > 55 || rednessLevel > 55 -> {
                routine.add("    → 진정 집중 관리 (염증 ${inflammationLevel}%, 홍조 ${rednessLevel}%)")
                routine.add("    단계1: 진정 에센스 (센텔라 + 판테놀)")
                routine.add("    단계2: 진정 세럼을 얼굴 전체에 바르기")
                routine.add("    단계3: 시원한 찜질 또는 아이스팩 (5분)")
                routine.add("    단계4: 진정 슬리핑 마스크 밤새 사용")
            }
            dryLevel > 55 -> {
                routine.add("    → 수분 집중 관리 (건조함 ${dryLevel}% - 심함)")
                routine.add("    단계1: 수분 에센스 (2-3층으로 켜켜이)")
                routine.add("    단계2: 수분 세럼 또는 앰플 바르기")
                routine.add("    단계3: 시트 마스크팩 (15-20분)")
                routine.add("    단계4: 슬리핑 마스크 밤새 사용")
            }
            wrinkleLevel > 50 && day > 7 -> {
                routine.add("    → 안티에이징 집중 관리 (${day}일차, 주름 ${wrinkleLevel}%)")
                routine.add("    단계1: 펩타이드 에센스 바르기")
                routine.add("    단계2: 레티놀 세럼 (주 2-3회만 사용!)")
                routine.add("    단계3: 펩타이드 나이트 크림 바르기")
            }
            health < 50 -> {
                routine.add("    → 피부장벽 회복 관리 (건강도 ${health}% - 관리 필요)")
                routine.add("    단계1: 회복 에센스 바르기")
                routine.add("    단계2: 세라마이드 세럼 바르기")
                routine.add("    단계3: 풍부한 크림으로 피부장벽 보강")
            }
            else -> {
                routine.add("    → 유지 관리 (건강도 ${health}%)")
                routine.add("    단계1: 토닝 에센스 바르기")
                routine.add("    단계2: 유지 관리 세럼 바르기")
                routine.add("    단계3: 일반 나이트 크림 바르기")
            }
        }
        routine.add("")
        
        // PHASE 3: Weekly special treatment
        if (day % 3 == 0) {
            routine.add("03. 특별 주간 집중 관리 (${day}일차 - 집중 치료일)")
            routine.add("    → 피부 상태에 맞는 타겟 치료:")
            if (acneLevel > 40) routine.add("       • 여드름 필링 마스크 (10-15분)")
            if (dryLevel > 40) routine.add("       • 수분 슬리핑 마스크")
            if (wrinkleLevel > 40) routine.add("       • 안티에이징 팩")
            if (pigmentationLevel > 40) routine.add("       • 브라이트닝 마스크")
            routine.add("")
        }
        
        // PHASE 4: Final barrier protection
        routine.add("04. 최종 피부장벽 보호")
        routine.add("    → 적절한 나이트 크림/에뮬젼 바르기:")
        when {
            health < 45 -> routine.add("    🔴 풍부한 크림 (건강도 매우 낮음: ${health}%)")
            dryLevel > 50 -> routine.add("    🟡 영양 크림 (건조함 높음: ${dryLevel}%)")
            oilLevel > 50 -> routine.add("    🟢 가벼운 크림 (유분기 높음: ${oilLevel}%)")
            else -> routine.add("    ⚪ 밸런스 크림")
        }
        routine.add("")
        
        // PHASE 5: Sleep optimization
        routine.add("05. 숙면 최적화")
        routine.add("    → 7-9시간 충분한 수면 (피부는 수면 중 회복됨)")
        routine.add("    → 실내 습도: 40-60%")
        routine.add("    → 가능하면 등을 대고 누워 자기 (주름 예방)")
        routine.add("    → 실크 베갯잇 사용 (마찰 손상 감소)")
        routine.add("")
        
        // Personalized sleep advice
        if (inflammationLevel > 50) {
            routine.add("💤 피부에 염증이 있으니 오늘 밤 충분한 휴식이 중요합니다")
        }
        if (acneLevel > 50) {
            routine.add("💤 자기 전 유제품 피하기 - 물 충분히 마시기 (여드름 ${acneLevel}%)")
        }
        if (dryLevel > 50) {
            routine.add("💤 오늘 밤 가습기 사용 - 피부에 수분 공급 필요 (건조함 ${dryLevel}%)")
        }
        
        return routine
    }

    /**
     * Get personalized skincare phase description in Korean
     */
    fun getPhaseSummary(day: Int, skinResult: SkinResult): String {
        val phase = (day - 1) / 4 + 1
        val primaryConcerns = analyzePrimaryConcernsEnhanced(skinResult)
        val concernText = primaryConcerns.joinToString(", ") { it.first.split(" ").first() }
        
        return when (phase) {
            1 -> """
            【 1단계: 피부 정화 & 진정 】
            PHASE 1: Cleansing & Calming (Days 1-3)
            
            목표: 피부 표면 정화 및 자극 진정
            목표: Clear surface debris and calm skin inflammation
            
            주요 증상 개선:
            • 클렌징으로 피부 표면 독소 제거
            • 진정 성분으로 염증 완화
            • 주요 피부 문제: $concernText
            
            예상 변화:
            • 피부톤 개선 (20-30%)
            • 홍조 및 민감성 감소
            • 피부 결 정리 시작
            """.trimIndent()
            2 -> """
            【 2단계: 수분 & 밸런싱 】
            PHASE 2: Hydration & Balancing (Days 4-7)
            
            목표: 피부 수분 보충 및 pH 밸런스 복구
            Goal: Restore hydration and pH balance
            
            주요 증상 개선:
            • 고보습 에센스로 건조함 완화
            • 밸런싱 토너로 유분/건조 조절
            • 피부 조직 강화 시작
            
            예상 변화:
            • 수분도 증가 (15-25%)
            • 피부 탄력 회복
            • 건조한 부위 진정
            """.trimIndent()
            3 -> """
            【 3단계: 집중 치료 & 타겟팅 】
            PHASE 3: Intensive Treatment & Targeting (Days 8-11)
            
            목표: 특정 피부 문제에 집중 치료
            Goal: Address specific skin concerns with targeted treatments
            
            주요 증상 개선:
            • 여드름: 항균/진정 에센스 적용
            • 주름: 펩타이드/레티놀 치료
            • 색소침착: 비타민 C 집중 에센스
            
            예상 변화:
            • 여드름 감소 (30-40%)
            • 피부결 명백한 개선
            • 톤 균등화 시작
            """.trimIndent()
            else -> """
            【 4단계: 유지 & 강화 】
            PHASE 4: Maintenance & Enhancement (Days 12-14)
            
            목표: 개선된 피부 상태 유지 및 강화
            Goal: Maintain improvements and strengthen skin barrier
            
            주요 증상 개선:
            • 개선된 피부 상태 안정화
            • 피부 방어막 강화
            • 장기적 건강한 피부 기초 확립
            
            예상 변화:
            • 전체적인 피부 건강도 증가
            • 장기 개선 효과 지속
            • 새로운 루틴 적응 완성
            
            다음 단계: 최소 월 1회 반복 진행 권장
            Next: Repeat routine monthly for sustained results
            """.trimIndent()
        }
    }
}
