package com.example.predicte_plant_diseases.util;

import java.util.HashMap;
import java.util.Map;

public class DiseaseTreatmentUtil {

    private static final Map<String, String> TREATMENTS = new HashMap<>();

    static {
        // Apple
        TREATMENTS.put("apple - apple scab", "Apply protective fungicides (such as captan or copper-based sprays) in early spring. Rake and destroy all fallen leaves to prevent the spores from overwintering.");
        TREATMENTS.put("apple - black rot", "Prune out dead wood, cankers, and remove infected/mummified fruit from the tree. Apply protective fungicides from the green tip stage through harvest.");
        TREATMENTS.put("apple - cedar apple rust", "Avoid planting apple trees near eastern red cedars. Apply preventative fungicides (like myclobutanil) during early leaf development and bud bloom.");
        TREATMENTS.put("apple - healthy", "Plant is healthy. Maintain regular watering, proper pruning, and routine checkups.");

        // Blueberry
        TREATMENTS.put("blueberry - healthy", "Plant is healthy. Keep soil acidic (pH 4.5-5.5), maintain mulch, and water regularly.");

        // Cherry
        TREATMENTS.put("cherry (including sour) - powdery mildew", "Apply fungicides such as sulfur, horticultural oils, or potassium bicarbonate. Prune the canopy to improve air circulation and sunlight penetration.");
        TREATMENTS.put("cherry (including sour) - healthy", "Plant is healthy. Prune annually in late winter, keep area free of weeds, and water consistently.");

        // Corn
        TREATMENTS.put("corn (maize) - cercospora leaf spot gray leaf spot", "Plant resistant hybrids, rotate crops to non-hosts (like soybeans), and manage crop residue. Apply foliar fungicides if disease pressure is high.");
        TREATMENTS.put("corn (maize) - common rust", "Use rust-resistant corn hybrids. Apply foliar fungicides if infection starts early in the season and weather is warm and humid.");
        TREATMENTS.put("corn (maize) - northern leaf blight", "Select resistant hybrids, practice crop rotation, and clear crop residue to reduce inoculum. Apply foliar fungicides if blight appears early.");
        TREATMENTS.put("corn (maize) - healthy", "Plant is healthy. Keep soil fertile, irrigate during dry periods, and monitor for pests.");

        // Grape
        TREATMENTS.put("grape - black rot", "Remove and destroy all mummified fruit and infected canes. Apply copper-based or chemical fungicides starting at bud break until after bloom.");
        TREATMENTS.put("grape - esca (black measles)", "Protect pruning wounds with wound sealants, prune during dry winter weather, and remove severely infected wood or vines.");
        TREATMENTS.put("grape - leaf blight (isariopsis leaf spot)", "Clean up and destroy fallen leaves in autumn. Apply copper-based fungicides or other protective sprays in late summer.");
        TREATMENTS.put("grape - healthy", "Plant is healthy. Prune vines annually, ensure good support/trellising, and monitor leaves.");

        // Orange
        TREATMENTS.put("orange - haunglongbing (citrus greening)", "Control the Asian citrus psyllid vector using systemic insecticides or horticultural oils. Remove and destroy infected trees to prevent spread. Keep remaining trees well-fertilized.");

        // Peach
        TREATMENTS.put("peach - bacterial spot", "Plant resistant cultivars. Apply copper sprays during dormancy and early bud development. Avoid excessive nitrogen fertilization.");
        TREATMENTS.put("peach - healthy", "Plant is healthy. Thin fruits in spring, prune annually, and monitor for borers.");

        // Pepper
        TREATMENTS.put("pepper, bell - bacterial spot", "Use pathogen-free seeds and transplants. Apply copper-based bactericides early. Rotate crops and avoid overhead sprinkler irrigation.");
        TREATMENTS.put("pepper, bell - healthy", "Plant is healthy. Ensure well-draining soil, adequate sunlight, and consistent watering at the base.");

        // Potato
        TREATMENTS.put("potato - early blight", "Plant certified disease-free seeds. Practice crop rotation and avoid overhead watering. Apply protective fungicides (like chlorothalonil or copper).");
        TREATMENTS.put("potato - late blight", "Apply preventative fungicides (like chlorothalonil). Pull and destroy all infected plants and tubers immediately to avoid spread. Ensure good air circulation.");
        TREATMENTS.put("potato - healthy", "Plant is healthy. Hill soil around stems, water deeply at the base, and store harvested tubers in a cool, dry place.");

        // Raspberry
        TREATMENTS.put("raspberry - healthy", "Plant is healthy. Prune fruited canes after harvest, maintain trellis support, and keep mulched.");

        // Soybean
        TREATMENTS.put("soybean - healthy", "Plant is healthy. Rotate crops, control weeds, and maintain proper soil fertility.");

        // Squash
        TREATMENTS.put("squash - powdery mildew", "Plant in full sun and ensure wide spacing for airflow. Apply organic fungicides like neem oil, sulfur, or potassium bicarbonate at first sign of spots.");

        // Strawberry
        TREATMENTS.put("strawberry - leaf scorch", "Remove and destroy infected leaves. Avoid overhead watering. Apply protective fungicides during wet spring weather.");
        TREATMENTS.put("strawberry - healthy", "Plant is healthy. Keep runners managed, mulch with straw, and keep soil well-drained.");

        // Tomato
        TREATMENTS.put("tomato - bacterial spot", "Avoid overhead watering to limit wet leaves. Apply copper-based fungicides combined with mancozeb. Practice crop rotation.");
        TREATMENTS.put("tomato - early blight", "Prune lower leaves to prevent contact with the soil. Avoid overhead irrigation and apply preventative fungicides (copper or chlorothalonil) regularly.");
        TREATMENTS.put("tomato - late blight", "Immediately pull and destroy infected plants. Apply copper fungicides to surrounding healthy plants. Keep foliage dry.");
        TREATMENTS.put("tomato - leaf mold", "Improve greenhouse or garden ventilation. Keep humidity levels below 85% and apply preventative fungicides.");
        TREATMENTS.put("tomato - septoria leaf spot", "Remove infected lower leaves immediately. Avoid overhead watering, apply mulch around the base, and use copper-based fungicides.");
        TREATMENTS.put("tomato - spider mites two-spotted spider mite", "Spray the undersides of leaves with a strong stream of water to dislodge mites. Release predatory mites or use insecticidal soaps or neem oil.");
        TREATMENTS.put("tomato - target spot", "Improve airflow with proper spacing and staking. Avoid overhead irrigation. Apply preventative fungicides such as chlorothalonil.");
        TREATMENTS.put("tomato - tomato yellow leaf curl virus", "Control the whitefly vector using insecticidal soaps, neem oil, or yellow sticky traps. Use protective row covers and destroy infected plants.");
        TREATMENTS.put("tomato - tomato mosaic virus", "Pull and burn infected plants immediately. Clean and sanitize all gardening tools. Wash hands thoroughly before handling healthy plants.");
        TREATMENTS.put("tomato - healthy", "Plant is healthy. Stake plants for support, water at the soil level, and prune suckers to encourage airflow.");
    }

    public static String getTreatment(String plantName, String diseaseName) {
        if (plantName == null || diseaseName == null) {
            return "No recommendation available.";
        }

        String key = (plantName.trim() + " - " + diseaseName.trim()).toLowerCase();

        // Try exact match
        if (TREATMENTS.containsKey(key)) {
            return TREATMENTS.get(key);
        }

        // Try fallback based on disease name containing healthy
        if (diseaseName.toLowerCase().contains("healthy")) {
            return "Plant is healthy. Maintain regular watering, proper fertilization, and monitor for changes.";
        }

        // Try matching by disease name alone
        for (Map.Entry<String, String> entry : TREATMENTS.entrySet()) {
            if (entry.getKey().contains(diseaseName.toLowerCase().trim())) {
                return entry.getValue();
            }
        }

        return "No specific treatment found. Please consult a local agricultural extension office or a plant pathologist.";
    }
}
