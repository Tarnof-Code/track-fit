package com.sport.gymtracker.domain

/**
 * Catalogue indicatif du matériel pour **ON AIR La Défense** (Westfield Les 4 Temps, Puteaux)
 * et aligné sur les **espaces décrits** pour ce club et le réseau ON AIR :
 * cardio, musculation guidée, charges libres, hybrid training (TRX, kettlebells, battle rope, etc.),
 * boxe/MMA, espace abdos.
 *
 * Marques fréquemment citées pour le réseau / ce type de club : **Technogym**, **Gym80**,
 * **Hammer Strength**, **Eleiko** (site onair-fitness.fr).
 *
 * La salle peut faire évoluer son parc machine : ce catalogue sert à **sélectionner** un équipement
 * typique ; tu peux compléter avec « Autre (saisie libre) » dans l’app.
 */
object OnAirLaDefenseCatalog {

    val categoriesInOrder: List<String> = listOf(
        "Cardio",
        "Musculation guidée",
        "Charges libres & haltères",
        "Hybrid & fonctionnel",
        "Abdos & accessoires",
        "Boxe & combat",
        "Autre",
    )

    val items: List<GymEquipment> = listOf(
        // —— Cardio (club : tapis, stepper, elliptique, escalier, rameur) ——
        GymEquipment("cardio_treadmill", "Tapis de course", "Cardio"),
        GymEquipment("cardio_bike_upright", "Vélo droit", "Cardio"),
        GymEquipment("cardio_bike_recumbent", "Vélo allongé", "Cardio"),
        GymEquipment("cardio_elliptical", "Elliptique", "Cardio"),
        GymEquipment("cardio_stepper", "Stepper", "Cardio"),
        GymEquipment("cardio_stair_climber", "Grimpeur / escalier (climber)", "Cardio"),
        GymEquipment("cardio_rower", "Rameur", "Cardio"),
        GymEquipment("cardio_ski_erg", "SkiErg", "Cardio"),
        GymEquipment("cardio_spinning", "Vélo RPM / biking (cours)", "Cardio"),
        // —— Musculation guidée ——
        GymEquipment("mach_leg_press", "Presse à cuisses (leg press)", "Musculation guidée"),
        GymEquipment("mach_hack_squat", "Hack squat", "Musculation guidée"),
        GymEquipment("mach_leg_extension", "Leg extension", "Musculation guidée"),
        GymEquipment("mach_leg_curl", "Leg curl (ischios)", "Musculation guidée"),
        GymEquipment("mach_adductor", "Adducteurs (machine)", "Musculation guidée"),
        GymEquipment("mach_abductor", "Abducteurs (machine)", "Musculation guidée"),
        GymEquipment("mach_calf", "Machine à mollets", "Musculation guidée"),
        GymEquipment("mach_glute", "Machine fessiers / extension hanche", "Musculation guidée"),
        GymEquipment("mach_pec_deck", "Pec deck / écartés machine", "Musculation guidée"),
        GymEquipment("mach_chest_press", "Presse à pectoraux", "Musculation guidée"),
        GymEquipment("mach_lat_pulldown", "Tirage vertical (lat pulldown)", "Musculation guidée"),
        GymEquipment("mach_seated_row", "Rowing assis (poulie basse)", "Musculation guidée"),
        GymEquipment("mach_low_row", "Tirage horizontal / low row", "Musculation guidée"),
        GymEquipment("mach_cable_crossover", "Poulies vis-à-vis / crossover", "Musculation guidée"),
        GymEquipment("mach_high_pulley", "Poulie haute simple", "Musculation guidée"),
        GymEquipment("mach_low_pulley", "Poulie basse simple", "Musculation guidée"),
        GymEquipment("mach_shoulder_press", "Presse à épaules / shoulder press", "Musculation guidée"),
        GymEquipment("mach_lateral_raise", "Élévations latérales machine", "Musculation guidée"),
        GymEquipment("mach_triceps_press", "Machine à triceps", "Musculation guidée"),
        GymEquipment("mach_biceps_curl", "Machine à biceps", "Musculation guidée"),
        GymEquipment("mach_ab_crunch", "Machine abdos crunch", "Musculation guidée"),
        GymEquipment("mach_rotary_torso", "Rotation du buste / rotary torso", "Musculation guidée"),
        GymEquipment("mach_back_extension", "Postérieur chaîne / hyperextension guidée", "Musculation guidée"),
        GymEquipment("mach_smith", "Smith machine", "Musculation guidée"),
        // —— Charges libres ——
        GymEquipment("free_squat_rack", "Cage à squat / power rack", "Charges libres & haltères"),
        GymEquipment("free_half_rack", "Demi-rack / rack multiposition", "Charges libres & haltères"),
        GymEquipment("free_bench_flat", "Banc plat", "Charges libres & haltères"),
        GymEquipment("free_bench_incline", "Banc incliné", "Charges libres & haltères"),
        GymEquipment("free_bench_decline", "Banc décliné", "Charges libres & haltères"),
        GymEquipment("free_preacher", "Banc Scott / pupitre", "Charges libres & haltères"),
        GymEquipment("free_bar_olympic", "Barre olympique", "Charges libres & haltères"),
        GymEquipment("free_bar_ez", "Barre EZ", "Charges libres & haltères"),
        GymEquipment("free_bar_trap", "Trap bar / barre hexagonale", "Charges libres & haltères"),
        GymEquipment("free_dumbbells", "Haltères (présentoir)", "Charges libres & haltères"),
        GymEquipment("free_plates", "Disques / bumper", "Charges libres & haltères"),
        GymEquipment("free_landmine", "Landmine", "Charges libres & haltères"),
        GymEquipment("free_platform", "Plateforme haltérophilie", "Charges libres & haltères"),
        // —— Hybrid (site club : TRX, kettlebells, battle rope, etc.) ——
        GymEquipment("hyb_trx", "TRX / sangles", "Hybrid & fonctionnel"),
        GymEquipment("hyb_battle_rope", "Battle rope", "Hybrid & fonctionnel"),
        GymEquipment("hyb_kettlebell", "Kettlebell", "Hybrid & fonctionnel"),
        GymEquipment("hyb_plyo_box", "Box pliométrique", "Hybrid & fonctionnel"),
        GymEquipment("hyb_wall_ball", "Wall ball / slam ball", "Hybrid & fonctionnel"),
        GymEquipment("hyb_climbing_rope", "Corde à grimper", "Hybrid & fonctionnel"),
        GymEquipment("hyb_sled", "Traîneau / sled", "Hybrid & fonctionnel"),
        GymEquipment("hyb_tire", "Pneu (flip / frappe)", "Hybrid & fonctionnel"),
        GymEquipment("hyb_agility", "Plot / agility ladder (espace fonctionnel)", "Hybrid & fonctionnel"),
        // —— Abdos & accessoires ——
        GymEquipment("acc_ab_wheel", "Roue à abdos", "Abdos & accessoires"),
        GymEquipment("acc_swiss_ball", "Swiss ball", "Abdos & accessoires"),
        GymEquipment("acc_resistance_band", "Bandes élastiques", "Abdos & accessoires"),
        GymEquipment("acc_foam_roller", "Rouleau foam / massage", "Abdos & accessoires"),
        GymEquipment("acc_jump_rope", "Corde à sauter", "Abdos & accessoires"),
        GymEquipment("acc_dip_station", "Barres à dips / station à dips", "Abdos & accessoires"),
        GymEquipment("acc_pullup_bar", "Barre de traction / monkey bar", "Abdos & accessoires"),
        // —— Boxe / MMA (club : espace boxe) ——
        GymEquipment("box_heavy_bag", "Sac de frappe lourd", "Boxe & combat"),
        GymEquipment("box_speed_bag", "Poire de vitesse", "Boxe & combat"),
        GymEquipment("box_pao", "Pattes d’ours / pao", "Boxe & combat"),
        GymEquipment("box_ring_space", "Espace ring / MMA", "Boxe & combat"),
        // —— Autre ——
        GymEquipment("other_custom", "Autre (saisie libre)", "Autre"),
    ).sortedWith(compareBy({ categoriesInOrder.indexOf(it.category).takeIf { i -> i >= 0 } ?: 99 }, { it.label }))

    fun byId(id: String): GymEquipment? = items.find { it.id == id }

    fun labelForStoredEquipment(stored: String): String = stored
}

data class GymEquipment(
    val id: String,
    val label: String,
    val category: String,
)
