package com.example.androidproject.data

import com.example.androidproject.domain.model.Exercise
import com.google.gson.Gson
// import java.util.UUID // UUID import 제거

object ExerciseCatalog {
    // 앱이 알고 있는 '모든' 운동 목록 정의 (총 75개)
    val allExercises = listOf(
        // ==========================================
        // [Part 1] 스트레칭 및 유연성 (1~25)
        // ==========================================

        // A. 경추/상부 승모근 (Neck & Upper Trapezius)
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX001_NECK_LAT_FLEX",
            name = "목 측면 굽힘",
            description = "머리를 한 쪽 옆으로 부드럽게 숙여 목 측면 근육을 이완시킵니다.",
            bodyPart = "목/어깨",
            difficulty = "초급",
            imageName = "neck_lateral_flexion",
            precautions = "통증이 없는 범위 내에서 천천히 진행하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX002_NECK_FLEX_EXT",
            name = "목 전후 스트레칭",
            description = "턱을 가슴 쪽으로 당기거나 뒤통수를 하늘로 향하게 하여 목의 앞뒤 근육을 늘립니다.",
            bodyPart = "목/어깨",
            difficulty = "초급",
            imageName = "neck_flexion_and_extension",
            precautions = "급격한 회전은 피합니다."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX003_SHOULDER_SHRUG",
            name = "어깨 올리기/내리기",
            description = "양 어깨를 귀 쪽으로 최대한 올렸다가 힘을 빼고 툭 떨어뜨려 승모근의 긴장을 풉니다.",
            bodyPart = "목/어깨",
            difficulty = "초급",
            imageName = "shoulder_shrugs"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX004_ASSISTED_NECK_SIDE",
            name = "목 측면 손으로 지지",
            description = "손으로 머리 반대쪽을 가볍게 잡고 지그시 당겨 목 측면을 더 깊게 스트레칭합니다.",
            bodyPart = "목/어깨",
            difficulty = "초급",
            imageName = "assisted_neck_side_stretch",
            precautions = "과도하게 당기지 않도록 주의합니다."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX005_CORNER_PECT_STRETCH",
            name = "가슴 앞쪽(흉근) 스트레칭",
            description = "벽 모서리에 팔꿈치를 90도로 대고 몸을 앞으로 밀어 굽은 가슴을 펴줍니다.",
            bodyPart = "목/어깨",
            difficulty = "초급",
            imageName = "corner_pectoralis_stretch"
        ),

        // B. 어깨/팔 관절 (Shoulder & Arm Joints)
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX006_SHOULDER_CIRCLES",
            name = "어깨 앞뒤 원 그리기",
            description = "팔을 내린 채 어깨관절만 이용하여 앞뒤로 천천히 원을 그리며 관절을 풀어줍니다.",
            bodyPart = "어깨/팔",
            difficulty = "초급",
            imageName = "shoulder_circles"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX007_CROSS_BODY_SHOULDER",
            name = "어깨 후방 스트레칭",
            description = "한 팔을 가슴 앞으로 가로지르게 하고 다른 팔로 당겨 어깨 뒤쪽을 늘립니다.",
            bodyPart = "어깨/팔",
            difficulty = "초급",
            imageName = "cross_body_shoulder_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX008_OVERHEAD_TRICEPS",
            name = "윗팔 뒤쪽(삼두근) 스트레칭",
            description = "팔을 머리 위로 올려 팔꿈치를 굽힌 뒤, 반대 손으로 팔꿈치를 아래로 지그시 누릅니다.",
            bodyPart = "어깨/팔",
            difficulty = "초급",
            imageName = "overhead_triceps_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX009_WRIST_EXTENSOR",
            name = "손목/전완근 스트레칭 (손등)",
            description = "팔을 앞으로 펴고 손등이 몸 쪽으로 오도록 손을 당겨 팔 바깥쪽 근육을 늘립니다.",
            bodyPart = "어깨/팔",
            difficulty = "초급",
            imageName = "wrist_extensor_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX010_WRIST_FLEXOR",
            name = "손목/전완근 스트레칭 (손바닥)",
            description = "팔을 앞으로 펴고 손바닥이 정면을 향하게 한 뒤 손가락을 몸 쪽으로 당깁니다.",
            bodyPart = "어깨/팔",
            difficulty = "초급",
            imageName = "wrist_flexor_stretch"
        ),

        // C. 척추/몸통 유연성 (Spine & Trunk)
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX011_SEATED_TRUNK_ROT",
            name = "앉아서 몸통 좌우 회전",
            description = "의자에 바르게 앉아 상체를 좌우로 천천히 돌려 척추의 회전 범위를 넓힙니다.",
            bodyPart = "허리/몸통",
            difficulty = "초급",
            imageName = "seated_trunk_rotation"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX012_SUPINE_KNEE_SWAY",
            name = "누워서 무릎 모아 좌우로 넘기기",
            description = "누운 상태에서 무릎을 세우고 양다리를 모은 채 좌우로 천천히 움직여 허리를 이완합니다.",
            bodyPart = "허리/몸통",
            difficulty = "초급",
            imageName = "supine_knee_sway"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX013_OVERHEAD_SIDE_STRETCH",
            name = "옆구리 늘리기 (만세 측면 굽힘)",
            description = "두 손을 머리 위로 뻗은 채 몸을 한쪽으로 굽혀 옆구리 근육을 길게 늘립니다.",
            bodyPart = "허리/몸통",
            difficulty = "초급",
            imageName = "overhead_side_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX014_CAT_CAMEL",
            name = "고양이/낙타 자세",
            description = "네발기기 자세에서 등을 둥글게 말았다가 펴는 동작을 반복하여 척추 유연성을 높입니다.",
            bodyPart = "허리/몸통",
            difficulty = "초급",
            imageName = "cat_camel_exercise"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX015_SEATED_FORWARD_BEND",
            name = "앉아서 상체 앞으로 숙이기",
            description = "다리를 펴고 앉아 등을 곧게 유지하며 상체를 앞으로 숙여 허벅지 뒤쪽을 늘립니다.",
            bodyPart = "허리/몸통",
            difficulty = "초급",
            imageName = "seated_forward_bend",
            precautions = "허리 디스크가 있는 경우 주의하세요."
        ),

        // D. 고관절/대퇴 관절 (Hip & Thigh Joints)
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX016_SEATED_FIGURE_4",
            name = "고관절 바깥 돌림 (앉아서)",
            description = "의자에 앉아 한쪽 발목을 반대쪽 무릎에 올리고 상체를 숙여 엉덩이 근육을 풉니다.",
            bodyPart = "고관절/허벅지",
            difficulty = "초급",
            imageName = "seated_figure_4_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX017_STANDING_QUADRICEPS",
            name = "대퇴사두근 스트레칭",
            description = "서서 발목을 잡고 엉덩이 쪽으로 당겨 허벅지 앞쪽 근육을 늘립니다.",
            bodyPart = "고관절/허벅지",
            difficulty = "초급",
            imageName = "standing_quadriceps_stretch",
            precautions = "균형 잡기 힘들면 벽을 잡고 하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX018_BUTTERFLY_STRETCH",
            name = "고관절 안쪽 스트레칭 (나비 자세)",
            description = "앉아서 발바닥을 맞대고 무릎을 바닥 쪽으로 눌러 골반과 허벅지 안쪽을 이완합니다.",
            bodyPart = "고관절/허벅지",
            difficulty = "초급",
            imageName = "butterfly_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX019_WALL_CALF_STRETCH",
            name = "종아리 스트레칭 (벽 밀기)",
            description = "벽을 짚고 한 발을 뒤로 뻗은 상태에서 뒤쪽 다리의 뒤꿈치를 바닥에 붙여 종아리를 늘립니다.",
            bodyPart = "고관절/허벅지",
            difficulty = "초급",
            imageName = "wall_calf_stretch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX020_SEATED_HAMSTRING",
            name = "허벅지 뒤쪽(햄스트링) 스트레칭",
            description = "바닥에 앉아 한 다리를 펴고 발끝을 잡으려 노력하며 허벅지 뒤쪽을 스트레칭합니다.",
            bodyPart = "고관절/허벅지",
            difficulty = "초급",
            imageName = "seated_hamstring_stretch"
        ),

        // E. 무릎/발목 관절 (Knee & Ankle Joints)
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX021_KNEE_CIRCLES",
            name = "무릎 돌리기",
            description = "양발을 모으고 무릎을 살짝 굽힌 뒤 손을 얹어 부드럽게 원을 그리며 돌립니다.",
            bodyPart = "무릎/발목",
            difficulty = "초급",
            imageName = "knee_circles"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX022_SEATED_ANKLE_CIRCLES",
            name = "앉아서 발목으로 원 그리기",
            description = "의자에 앉아 발을 들고 발목을 시계/반시계 방향으로 크게 돌립니다.",
            bodyPart = "무릎/발목",
            difficulty = "초급",
            imageName = "seated_ankle_circles"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX023_ANKLE_PUMPS",
            name = "앉아서 발끝 당기기/펴기",
            description = "의자에 앉아 발목을 몸 쪽으로 당겼다가 바닥 쪽으로 미는 동작을 반복합니다.",
            bodyPart = "무릎/발목",
            difficulty = "초급",
            imageName = "ankle_pumps"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX024_STANDING_ANKLE_INV_EVR",
            name = "발목 안팎으로 돌리기 (선 자세)",
            description = "지지물을 잡고 서서 발목을 안쪽과 바깥쪽으로 롤링하여 관절을 유연하게 합니다.",
            bodyPart = "무릎/발목",
            difficulty = "초급",
            imageName = "standing_ankle_inversion_eversion"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX025_TOE_SPREAD",
            name = "발가락 펴기 (수건 깔고)",
            description = "발가락을 넓게 벌려 수건을 펴거나 발가락 사이를 스트레칭하여 유연성을 기릅니다.",
            bodyPart = "무릎/발목",
            difficulty = "초급",
            imageName = "toe_spread"
        ),

        // ==========================================
        // [Part 2] 근력 강화 (26~50)
        // ==========================================

        // A. 대퇴부/엉덩이
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX026_CHAIR_SQUAT",
            name = "앉았다 일어서기 (의자 스쿼트)",
            description = "의자에 앉았다가 손을 쓰지 않고 하체 힘으로만 일어서는 동작을 반복합니다.",
            bodyPart = "허벅지/엉덩이",
            difficulty = "초급",
            imageName = "chair_squat_sit_to_stand",
            precautions = "무릎이 발끝보다 많이 나가지 않도록 주의하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX027_SEATED_KNEE_EXT",
            name = "무릎 펴기 (앉아서)",
            description = "의자에 앉아 한쪽 다리를 일자로 펴고 허벅지에 힘을 주어 버팁니다.",
            bodyPart = "허벅지/엉덩이",
            difficulty = "초급",
            imageName = "seated_knee_extension"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX028_GLUTE_BRIDGE",
            name = "누워서 엉덩이 들기 (브릿지)",
            description = "누워서 무릎을 세운 뒤 엉덩이를 들어 올려 엉덩이와 허리 근육을 강화합니다.",
            bodyPart = "허벅지/엉덩이",
            difficulty = "초급",
            imageName = "glute_bridge",
            precautions = "허리를 과도하게 꺾지 않도록 합니다."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX029_BANDED_HIP_ABD",
            name = "밴드 이용한 다리 옆으로 벌리기",
            description = "다리에 밴드를 걸고 옆으로 벌려 중둔근(엉덩이 옆쪽)을 강화합니다.",
            bodyPart = "허벅지/엉덩이",
            difficulty = "중급",
            imageName = "banded_hip_abduction"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX030_BANDED_HIP_EXT",
            name = "밴드 이용한 다리 뒤로 밀기",
            description = "다리에 밴드를 걸고 뒤로 차듯이 밀어 대둔근(엉덩이 뒤쪽)을 강화합니다.",
            bodyPart = "허벅지/엉덩이",
            difficulty = "중급",
            imageName = "banded_hip_extension"
        ),

        // B. 종아리/발목 안정화
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX031_STANDING_HEEL_RAISE",
            name = "선 자세 뒤꿈치 올리기",
            description = "벽을 잡고 서서 뒤꿈치를 최대한 높이 들어 올려 종아리 근육을 키웁니다.",
            bodyPart = "종아리/정강이",
            difficulty = "초급",
            imageName = "standing_heel_raise_calf_raise"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX032_STANDING_TOE_RAISE",
            name = "선 자세 발가락 올리기",
            description = "벽을 잡고 서서 발가락 앞쪽을 들어 올려 정강이 앞쪽 근육을 강화합니다.",
            bodyPart = "종아리/정강이",
            difficulty = "초급",
            imageName = "standing_toe_raise"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX033_TOWEL_CURL",
            name = "수건 발가락으로 집기",
            description = "바닥에 둔 수건을 발가락 힘으로 꽉 쥐어 발바닥 내재근을 단련합니다.",
            bodyPart = "종아리/정강이",
            difficulty = "초급",
            imageName = "towel_curl_toe_curl"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX034_SUPINE_LEG_PRESS_BAND",
            name = "누워서 수건 잡고 발 밀기",
            description = "누워서 발바닥에 수건/밴드를 걸고 밀어내며 하체 전반의 힘을 씁니다.",
            bodyPart = "종아리/정강이",
            difficulty = "초급",
            imageName = "supine_leg_press_with_band_towel"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX035_STAIR_CLIMBING",
            name = "계단 오르내리기",
            description = "계단을 오르내리며 엉덩이, 허벅지, 종아리 등 하체 근력을 종합적으로 강화합니다.",
            bodyPart = "하체 전반",
            difficulty = "중급",
            imageName = "stair_climbing",
            precautions = "낙상에 주의하며 난간을 잡고 진행하세요."
        ),

        // C. 팔/어깨
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX036_WALL_PUSH_UP",
            name = "벽 짚고 팔 굽혀 펴기",
            description = "벽에 손을 짚고 팔굽혀펴기를 하여 가슴과 팔 근육을 안전하게 단련합니다.",
            bodyPart = "팔/어깨",
            difficulty = "초급",
            imageName = "wall_push_up"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX037_DB_BICEP_CURL",
            name = "아령 들고 팔꿈치 굽히기 (이두)",
            description = "가벼운 아령을 들고 팔꿈치를 굽혀 팔 앞쪽(이두박근)을 강화합니다.",
            bodyPart = "팔/어깨",
            difficulty = "초급",
            imageName = "dumbbell_bicep_curl"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX038_DB_LATERAL_RAISE",
            name = "아령 들고 팔 옆으로 들기",
            description = "아령을 들고 양팔을 어깨 높이까지 옆으로 들어 올려 어깨 측면을 강화합니다.",
            bodyPart = "팔/어깨",
            difficulty = "중급",
            imageName = "dumbbell_lateral_raise"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX039_STANDING_ROW_TOWEL",
            name = "수건 이용한 로우 (등 당기기)",
            description = "수건이나 밴드를 기둥에 걸고 양손으로 당겨 등 근육을 수축시킵니다.",
            bodyPart = "팔/어깨",
            difficulty = "초급",
            imageName = "standing_row_towel_band"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX040_OVERHEAD_PRESS",
            name = "물건 머리 위로 들어 올리기",
            description = "가벼운 물건을 머리 위로 들어 올리며 어깨와 삼두근을 강화합니다.",
            bodyPart = "팔/어깨",
            difficulty = "중급",
            imageName = "overhead_press",
            precautions = "어깨 통증 시 중단하세요."
        ),

        // D. 코어/복부 안정화
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX041_SUPINE_LEG_HOLD",
            name = "누워서 다리 살짝 들고 버티기",
            description = "누운 상태에서 다리를 지면에서 살짝 띄워 유지하며 하복부 코어를 자극합니다.",
            bodyPart = "복부/코어",
            difficulty = "중급",
            imageName = "supine_leg_hold",
            precautions = "허리가 바닥에서 뜨지 않도록 주의하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX042_KNEELING_PLANK",
            name = "플랭크 (무릎 대고)",
            description = "엎드려 팔꿈치와 무릎으로 몸을 지탱하며 머리부터 무릎까지 일직선을 유지합니다.",
            bodyPart = "복부/코어",
            difficulty = "중급",
            imageName = "kneeling_plank"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX043_POSTERIOR_PELVIC_TILT",
            name = "누워서 허리 지면 누르기",
            description = "누워서 허리 뒤쪽 공간이 없도록 바닥을 꾹 눌러 코어 심부 근육을 활성화합니다.",
            bodyPart = "복부/코어",
            difficulty = "초급",
            imageName = "posterior_pelvic_tilt_abdominal_bracing"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX044_SEATED_RUSSIAN_TWIST",
            name = "앉아서 몸통 돌리기 (저항)",
            description = "밴드나 물건을 들고 앉은 채로 몸통을 회전시켜 복부 옆쪽 근육을 단련합니다.",
            bodyPart = "복부/코어",
            difficulty = "중급",
            imageName = "seated_russian_twist"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX045_DEAD_BUG",
            name = "데드 버그 (Dead Bug)",
            description = "누워서 팔과 다리를 서로 교차하며 뻗어 코어 안정성을 높입니다.",
            bodyPart = "복부/코어",
            difficulty = "중급",
            imageName = "dead_bug"
        ),

        // E. 허리/등 안정화
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX046_PRONE_HIP_EXT",
            name = "엎드려 한다리 들기",
            description = "엎드린 자세에서 다리를 교대로 들어 올려 엉덩이와 허리 근육을 강화합니다.",
            bodyPart = "허리/등",
            difficulty = "초급",
            imageName = "prone_hip_extension"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX047_PRONE_PRESS_UP_COBRA",
            name = "엎드려 손 짚고 상체 일으키기",
            description = "엎드려 손으로 바닥을 짚고 상체를 살짝 들어 등 근육(기립근)을 강화합니다.",
            bodyPart = "허리/등",
            difficulty = "초급",
            imageName = "prone_press_up_cobra",
            precautions = "허리에 통증이 오지 않는 범위까지만 수행합니다."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX048_BIRD_DOG",
            name = "버드독 (Bird Dog)",
            description = "네발기기 자세에서 대각선 방향의 팔과 다리를 뻗어 신체 균형과 코어를 잡습니다.",
            bodyPart = "허리/등",
            difficulty = "중급",
            imageName = "bird_dog"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX049_SCAPULAR_PROTRACTION",
            name = "팔 앞으로 뻗고 어깨 뽑기",
            description = "팔을 앞으로 뻗은 상태에서 견갑골(날개뼈)을 앞뒤로 움직여 전거근을 강화합니다.",
            bodyPart = "허리/등",
            difficulty = "초급",
            imageName = "scapular_protraction_serratus_punch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX050_SUPERMAN",
            name = "슈퍼맨 (Superman)",
            description = "엎드려 팔과 다리를 동시에 들어 올려 등 전체와 엉덩이를 강하게 수축합니다.",
            bodyPart = "허리/등",
            difficulty = "중급",
            imageName = "superman_exercise"
        ),

        // ==========================================
        // [Part 3] 균형 및 협응력 (51~75)
        // ==========================================

        // A. 정적 균형
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX051_SINGLE_LEG_SUPPORTED",
            name = "한 발 서기 (지지)",
            description = "의자나 벽을 잡고 한 발로 서서 버티며 정적 균형 감각을 익힙니다.",
            bodyPart = "하체/코어",
            difficulty = "초급",
            imageName = "single_leg_stance_supported"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX052_SINGLE_LEG_UNSUPPORTED",
            name = "한 발 서기 (비지지)",
            description = "지지물 없이 스스로 균형을 잡으며 한 발로 서는 시간을 늘려갑니다.",
            bodyPart = "하체/코어",
            difficulty = "중급",
            imageName = "single_leg_stance_unsupported",
            precautions = "넘어지지 않도록 주의하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX053_TANDEM_STANCE",
            name = "탠덤 스탠스 (일자 서기)",
            description = "발뒤꿈치와 앞발 끝을 일직선으로 붙여 서서 좌우 균형을 잡습니다.",
            bodyPart = "하체/코어",
            difficulty = "중급",
            imageName = "tandem_stance"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX054_HEEL_TOE_HOLD",
            name = "뒤꿈치/발가락 들고 버티기",
            description = "제자리에서 뒤꿈치나 앞발을 들고 멈춘 상태로 균형을 유지합니다.",
            bodyPart = "하체/코어",
            difficulty = "중급",
            imageName = "heel_toe_stance_hold"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX055_SEMI_TANDEM",
            name = "발 앞뒤로 놓기",
            description = "보폭을 좁게 하여 발을 앞뒤로 두고 서서 지지 면적을 줄인 채 버팁니다.",
            bodyPart = "하체/코어",
            difficulty = "초급",
            imageName = "semi_tandem_stance"
        ),

        // B. 동적 균형
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX056_MARCHING_IN_PLACE",
            name = "제자리 무릎 들고 걷기",
            description = "제자리에서 무릎을 가슴 높이까지 힘차게 들어 올리며 걷습니다.",
            bodyPart = "하체/전신",
            difficulty = "초급",
            imageName = "marching_in_place"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX057_HEEL_WALKING",
            name = "발뒤꿈치로 걷기",
            description = "발 앞쪽을 들고 뒤꿈치만 닿게 하여 걸으며 정강이 근력과 균형을 훈련합니다.",
            bodyPart = "하체/전신",
            difficulty = "중급",
            imageName = "heel_walking"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX058_TOE_WALKING",
            name = "발끝으로 걷기",
            description = "까치발을 든 상태로 걸으며 종아리 근력과 발목 안정성을 높입니다.",
            bodyPart = "하체/전신",
            difficulty = "중급",
            imageName = "toe_walking"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX059_SIDE_STEPPING",
            name = "옆으로 걷기",
            description = "게처럼 옆으로 이동하며 체중 이동 능력과 하체 측면 균형을 잡습니다.",
            bodyPart = "하체/전신",
            difficulty = "초급",
            imageName = "side_stepping_lateral_walk"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX060_BACKWARD_WALKING",
            name = "뒤로 걷기",
            description = "시야가 확보된 곳에서 뒤로 걸으며 평소 쓰지 않는 감각과 근육을 자극합니다.",
            bodyPart = "하체/전신",
            difficulty = "중급",
            imageName = "backward_walking",
            precautions = "장애물이 없는지 확인하세요."
        ),

        // C. 기능적 균형/보행
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX061_SIT_TO_STAND_UNSUPPORTED",
            name = "앉았다 일어서기 (비지지)",
            description = "손을 쓰지 않고 의자에서 일어나며 동적인 균형 이동을 연습합니다.",
            bodyPart = "기능/인지",
            difficulty = "중급",
            imageName = "sit_to_stand_unsupported"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX062_TANDEM_WALKING",
            name = "발뒤꿈치에 발끝 붙여 걷기 (탠덤 워킹)",
            description = "일직선상에서 앞발의 뒤꿈치에 뒷발의 발가락을 붙이며 걷습니다.",
            bodyPart = "기능/인지",
            difficulty = "고급",
            imageName = "tandem_walking"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX063_SINGLE_LEG_CATCH",
            name = "한 발 서서 물건 주고받기",
            description = "한 발로 선 채로 공이나 물건을 주고받으며 동적 평형성을 기릅니다.",
            bodyPart = "기능/인지",
            difficulty = "고급",
            imageName = "single_leg_stance_with_catch"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX064_FIGURE_8_WALKING",
            name = "8자 모양으로 걷기",
            description = "바닥에 8자를 그리며 걸어 방향 전환 시의 균형 능력을 향상합니다.",
            bodyPart = "기능/인지",
            difficulty = "중급",
            imageName = "figure_8_walking"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX065_TURNING_IN_PLACE",
            name = "제자리에서 돌기",
            description = "제자리에서 360도 회전하며 어지러움을 극복하고 평형 감각을 유지합니다.",
            bodyPart = "기능/인지",
            difficulty = "중급",
            imageName = "turning_in_place",
            precautions = "어지러움을 느끼면 중단하세요."
        ),

        // D. 인지/이중 과제 균형
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX066_COGNITIVE_WALKING",
            name = "말하거나 계산하며 걷기",
            description = "걸으면서 계산이나 대화를 하여 인지 과제와 보행을 동시에 수행합니다.",
            bodyPart = "인지/협응력",
            difficulty = "중급",
            imageName = "walking_with_cognitive_task"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX067_MOTOR_TASK_WALKING",
            name = "걸으면서 손동작 하기",
            description = "걷는 동시에 박수치기 등의 손동작을 수행하여 이중 과제 능력을 높입니다.",
            bodyPart = "인지/협응력",
            difficulty = "중급",
            imageName = "walking_with_motor_task"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX068_TARGET_WALKING",
            name = "숫자 밟으며 걷기",
            description = "바닥의 특정 숫자나 마크를 밟으며 걸어 시각 인지와 보행을 연결합니다.",
            bodyPart = "인지/협응력",
            difficulty = "중급",
            imageName = "target_walking"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX069_STOP_AND_GO_WALKING",
            name = "걷다가 멈추기",
            description = "걷다가 신호에 맞춰 즉시 멈추는 연습을 통해 반응 속도와 제동 능력을 기릅니다.",
            bodyPart = "인지/협응력",
            difficulty = "중급",
            imageName = "stop_and_go_walking"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX070_OBSTACLE_WALKING",
            name = "물건 장애물 넘어 걷기",
            description = "바닥의 장애물을 피하거나 넘어가며 보행 중 균형 조절 능력을 키웁니다.",
            bodyPart = "인지/협응력",
            difficulty = "중급",
            imageName = "obstacle_walking",
            precautions = "낙상에 주의하세요."
        ),

        // E. 감각통합 균형
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX071_SINGLE_LEG_EYES_CLOSED",
            name = "눈 감고 한 발 서기 (지지)",
            description = "지지물을 잡은 상태에서 눈을 감고 한 발로 서서 체성 감각에 집중합니다.",
            bodyPart = "감각/코어",
            difficulty = "고급",
            imageName = "single_leg_stance_eyes_closed",
            precautions = "안전을 위해 반드시 지지물을 확보하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX072_FOAM_STANDING_DOUBLE",
            name = "쿠션 위에서 두 발 서기 (지지)",
            description = "푹신한 쿠션 위에서 서서 불안정한 지면에서의 적응력을 높입니다.",
            bodyPart = "감각/코어",
            difficulty = "중급",
            imageName = "foam_standing_double_leg"
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX073_FOAM_STANDING_SINGLE",
            name = "쿠션 위에서 한 발 서기 (지지)",
            description = "쿠션 위에서 한 발로 서서 고난도의 균형 감각을 훈련합니다.",
            bodyPart = "감각/코어",
            difficulty = "고급",
            imageName = "foam_standing_single_leg",
            precautions = "난이도가 높으므로 주의가 필요합니다."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX074_BLINDFOLDED_MARCHING",
            name = "눈 감고 제자리 걷기",
            description = "눈을 감고 제자리걸음을 하여 몸의 위치 감각과 평형성을 테스트합니다.",
            bodyPart = "감각/코어",
            difficulty = "고급",
            imageName = "blindfolded_marching",
            precautions = "주변에 부딪힐 물건이 없는지 확인하세요."
        ),
        Exercise(
            // ★ 수정: 고정 ID 적용 ★
            id = "EX075_SINGLE_LEG_OBJ",
            name = "한 발로 발등에 물건 올리기",
            description = "한 발로 선 채 들어 올린 발등에 물건을 올려 떨어뜨리지 않게 버팁니다.",
            bodyPart = "감각/코어",
            difficulty = "고급",
            imageName = "single_leg_balance_with_object"
        )
    )

    // AI에게 전달할 최소한의 정보를 JSON 형태로 제공
    fun getExercisesJson(): String {
        val gson = Gson()
        val compactList = allExercises.map { mapOf(
            "name" to it.name,
            "bodyPart" to it.bodyPart,
            "difficulty" to it.difficulty,
            "description_brief" to it.description
        ) }
        return gson.toJson(compactList)
    }
}