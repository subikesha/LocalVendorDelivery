package com.example.localvendordelivery;

public class ProductVisualHelper {

    public static String getEmojiForImageCode(String imageCode) {
        if (imageCode == null) return "📦";
        switch (imageCode.toLowerCase()) {
            case "pizza":
                return "🍕";
            case "burger":
                return "🍔";
            case "garlic_bread":
            case "bread":
                return "🍞";
            case "milk":
                return "🥛";
            case "eggs":
                return "🥚";
            case "rice":
                return "🍚";
            case "croissant":
                return "🥐";
            case "cake":
            case "pastry":
                return "🍰";
            case "vegetables":
            case "tomato":
            case "potato":
                return "🥦";
            case "fruits":
            case "apple":
            case "banana":
                return "🍎";
            case "medicine":
            case "pharmacy":
                return "💊";
            case "drinks":
            case "beverage":
                return "🧃";
            case "sweets":
            case "chocolate":
                return "🍫";
            case "coffee":
            case "tea":
                return "☕";
            default:
                return "📦";
        }
    }

    public static String[] getAvailableImageOptions() {
        return new String[]{
                "🍕 Pizza",
                "🍔 Burger & Fast Food",
                "🍞 Bread & Bakery",
                "🥛 Milk & Dairy",
                "🥚 Fresh Eggs",
                "🍚 Rice & Grains",
                "🥐 Croissant",
                "🍰 Cake & Pastries",
                "🥦 Fresh Vegetables",
                "🍎 Fresh Fruits",
                "💊 Medicine & Healthcare",
                "🧃 Juices & Beverages",
                "🍫 Sweets & Chocolates",
                "☕ Coffee & Tea",
                "📦 General Product"
        };
    }

    public static String getImageCodeFromOption(String option) {
        if (option == null) return "general";
        if (option.contains("Pizza")) return "pizza";
        if (option.contains("Burger")) return "burger";
        if (option.contains("Bread")) return "bread";
        if (option.contains("Milk")) return "milk";
        if (option.contains("Eggs")) return "eggs";
        if (option.contains("Rice")) return "rice";
        if (option.contains("Croissant")) return "croissant";
        if (option.contains("Cake")) return "cake";
        if (option.contains("Vegetables")) return "vegetables";
        if (option.contains("Fruits")) return "fruits";
        if (option.contains("Medicine")) return "medicine";
        if (option.contains("Juices")) return "drinks";
        if (option.contains("Sweets")) return "sweets";
        if (option.contains("Coffee")) return "coffee";
        return "general";
    }
}
